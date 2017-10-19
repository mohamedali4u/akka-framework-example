package com.example.akka.logprocessor.actor;

import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import com.example.akka.logprocessor.event.FileEndEvent;
import com.example.akka.logprocessor.event.FileLineEvent;
import com.example.akka.logprocessor.event.FileStartEvent;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * File Aggregator Actor
 * 
 * @author MohamedAli
 */

public class FileAggregator extends AbstractActor {

	public final Path filePath;
	protected long wordCount;
	private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	
	public FileAggregator(Path filePath) {
		this.filePath = filePath;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(FileStartEvent.class, event -> fileStart())
				.match(FileLineEvent.class, event -> aggregateWordCount(event))
				.match(FileEndEvent.class, event -> processEvent(event)).matchAny(o -> log.info("Event not exist", o))
				.build();
	}

	private void fileStart() {
		this.wordCount = 0;
	}

	private void aggregateWordCount(FileLineEvent event) {
		if (this.filePath.equals(event.filePath)) {
			this.wordCount += StringUtils.split(event.line).length;
		}
	}

	private void processEvent(FileEndEvent event) {
		if (this.filePath.equals(event.filePath)) {
			// log.info("LineAggregator received FileEnd event: {}", event);
			printWordCount(event);
		}
	}

	private void printWordCount(FileEndEvent event) {
		log.info("File " + event.filePath + " has " + event.lastLineNum + " lines and " + this.wordCount + " words");
	}
}
