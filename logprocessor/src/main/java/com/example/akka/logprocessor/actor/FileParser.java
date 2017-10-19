package com.example.akka.logprocessor.actor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import scala.concurrent.duration.FiniteDuration;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.example.akka.logprocessor.event.FileEndEvent;
import com.example.akka.logprocessor.event.FileLineEvent;
import com.example.akka.logprocessor.event.FileStartEvent;
import com.example.akka.logprocessor.model.Parse;

/**
 * Asynchronous File Parser Actor
 * 
 * @author MohamedAli
 */

public class FileParser extends AbstractActor {

	protected final byte DELIMETER = (byte) '\n';
	protected final LoggingAdapter log = Logging.getLogger(context().system(), this);
	protected final CompletionHandler<Integer, ByteBuffer> readComplete;
	protected Path parseFilePath;
	protected ActorRef wordAggregator;
	protected long pos;
	protected long lineNum;
	protected ByteArrayOutputStream outStream;
	protected AsynchronousFileChannel asyncFileChannel;
	protected FileLock lock;
	protected ByteBuffer octet = ByteBuffer.allocate(1);

	private FileParser(Path parseFilePath) {
		this.parseFilePath = parseFilePath;
		this.readComplete = processCompleteHandler();
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Parse.class, message -> this.processParsing())
				.matchAny(o -> log.info("FileScanner does not understand this message ", o)).build();
	}

	public void publishFileStart() {
		context().system().eventStream().publish(new FileStartEvent(this.parseFilePath));
	}

	public void publishFileEnd() {
		context().system().eventStream().publish(new FileEndEvent(this.parseFilePath, this.lineNum));
	}

	public void publishFileLine() {
		String line = this.outStream.toString();
		this.outStream.reset();
		this.lineNum += 1;
		context().system().eventStream().publish(new FileLineEvent(this.parseFilePath, this.lineNum, line));
	}

	public void scheduleNextByteRead() throws IOException {
		this.lock = this.asyncFileChannel.tryLock(this.pos, 1, true);

		if (this.lock == null)
			this.lockBusy();
		else {
			this.octet = ByteBuffer.allocate(1);
			this.asyncFileChannel.read(this.octet, this.pos, null, this.readComplete);
		}

	}

	public void openAsyncChannel(Path parseFilePath) throws IOException {
		this.asyncFileChannel = AsynchronousFileChannel.open(parseFilePath, StandardOpenOption.READ);
	}

	private void processParsing() {
		// log.info("processParsing ", parse.name +" - "+ parse.parseFilePath);
		this.wordAggregator = getContext().actorOf(Props.create(FileAggregator.class, this.parseFilePath),
				"fileAggregator");
		this.pos = 0;
		this.lineNum = 0;
		this.outStream = new ByteArrayOutputStream();
		this.subscribeFileEvent(this.wordAggregator);

		try {
			this.openAsyncChannel(this.parseFilePath);
			this.publishFileStart();
			this.scheduleNextByteRead();
		} catch (IOException e) {
			log.info("Message result : Parsing file caused error ", this.parseFilePath, e);
		}

	}

	private void subscribeFileEvent(ActorRef subscriber) {
		//context().system().eventStream().subscribe(subscriber, FileStartEvent.class);
		context().system().eventStream().subscribe(subscriber, FileLineEvent.class);
		context().system().eventStream().subscribe(subscriber, FileEndEvent.class);
	}

	private CompletionHandler<Integer, ByteBuffer> processCompleteHandler() {
		FileParser self = this;
		return new CompletionHandler<Integer, ByteBuffer>() {
			public void completed(Integer result, ByteBuffer target) {
				try {
					self.lock.release();
					self.processNextByte(result);
				} catch (IOException e) {
					self.log.info("Complete Error: ", e);
				}

			}

			public void failed(Throwable exception, ByteBuffer target) {

				try {
					self.lock.release();
					self.log.info("Failed to Read", exception);
					self.publishFileEnd();
				} catch (IOException e) {
					self.log.info("Complete Error", e);
				}

			}

		};

	}

	private Runnable processNextReadScheduler() {
		FileParser self = this;
		return new Runnable() {
			public void run() {
				try {
					self.scheduleNextByteRead();
				} catch (IOException e) {
					self.log.info("Error on run", e);
				}
			}
		};
	}

	private void lockBusy() {
		context()
				.system()
				.scheduler()
				.scheduleOnce(FiniteDuration.create(5, "milliseconds"), this.processNextReadScheduler(),
						context().system().dispatcher());
	}

	private void processNextByte(Integer numRead) throws IOException {
		if (numRead <= 0) {
			this.publishFileEnd();
			return;
		}
		this.outStream.write(this.octet.get(0));
		if (this.octet.get(0) == this.DELIMETER) {
			this.publishFileLine();
		}
		this.pos += 1;
		this.scheduleNextByteRead();
	}
}
