package com.example.akka.logprocessor.model;

/**
 * Scan Model
 * 
 * @author MohamedAli
 */

public class Scan {
	public final String name;
	public final String directoryPath;
	private long noOfFiles;
	public Scan(String directoryPath) {
		this.directoryPath = directoryPath;
		this.name = "scan";
	}
	public long getNoOfFiles() {
		return noOfFiles;
	}
	public void setNoOfFiles(long noOfFiles) {
		this.noOfFiles = noOfFiles;
	}
	
}
