package com.example.akka.logprocessor.event;

import java.nio.file.Path;


/**
 * File start of line event
 * 
 * @author MohamedAli
 */

public class FileStartEvent {

	public final Path parseFilePath;
	public FileStartEvent(Path parseFilePath) {
		this.parseFilePath = parseFilePath;
	}
}
