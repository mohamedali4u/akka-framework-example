package com.example.akka.logprocessor.actor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.example.akka.logprocessor.model.Parse;
import com.example.akka.logprocessor.model.Scan;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * File Scanner Actor
 * 
 * @author MohamedAli
 */

public class FileScanner extends AbstractActor {

	protected final LoggingAdapter log = Logging.getLogger(context().system(), this);
	protected ActorRef fileParserRef;
	
	private FileScanner() {
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Scan.class, scan -> this.proccessFileList(scan))
				.matchAny(o -> log.info("FileScanner Error", o)).build();
	}

	private void proccessFileList(Scan scan) throws IOException {
		// log.info("proccessFileList: ", scan.name);
		scan.setNoOfFiles(Files.list(Paths.get(scan.directoryPath).toAbsolutePath()).count());
		Files.list(Paths.get(scan.directoryPath).toAbsolutePath()).filter(Files::isRegularFile)
				.forEach(filePath -> this.initiateFileParserRef(filePath));
	}

	private void initiateFileParserRef(Path filePath) {
		// log.info("initiateFileParserRef: ", filePath);
		fileParserRef = getContext().actorOf(Props.create(FileParser.class, filePath));
		this.fileParserRef.tell(new Parse(), null);
	}

}