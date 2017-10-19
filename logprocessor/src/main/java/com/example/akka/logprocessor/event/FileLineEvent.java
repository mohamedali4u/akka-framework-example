package com.example.akka.logprocessor.event;

import java.nio.file.Path;

/**
 * File line read event
 * 
 * @author MohamedAli
 */

public class FileLineEvent {
	public final Path filePath;
	public final long lineNum;
	public final String line;

	public FileLineEvent(Path filePath, long lineNum, String line) {
		this.filePath = filePath;
		this.lineNum = lineNum;
		this.line = line;
	}
}
