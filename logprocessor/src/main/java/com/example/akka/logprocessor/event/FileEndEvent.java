package com.example.akka.logprocessor.event;

import java.nio.file.Path;

/**
 * File end of line event
 * 
 * @author MohamedAli
 */

public class FileEndEvent {
	public final Path filePath;
	public final long lastLineNum;

	public FileEndEvent(Path filePath, long lastLineNum) {

		this.filePath = filePath;
		this.lastLineNum = lastLineNum;

	}
}
