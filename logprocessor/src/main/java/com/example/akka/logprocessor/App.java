package com.example.akka.logprocessor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.example.akka.logprocessor.actor.FileScanner;
import com.example.akka.logprocessor.model.Scan;

/**
 * Main program to run Akka Log Processing
 * 
 * @author MohamedAli
 */
public class App {

	private ActorSystem system = ActorSystem.create("logProcessorApp");
	private LoggingAdapter log = Logging.getLogger(system, this);

	public static void main(String[] args) {
		new App().processLogApp();
	}

	private void processLogApp() {
		String directoryPath = readInputAndValidate();
		ActorRef fileScannerActor = system.actorOf(Props.create(FileScanner.class));
		fileScannerActor.tell(new Scan(directoryPath), null);
	}

	private String readInputAndValidate() {
		log.info("Enter the directory path: ");
		Scanner scanner = new Scanner(System.in);
		while (true) {
			String directoryPath = scanner.next();
			boolean error = false;
			if (StringUtils.isBlank(directoryPath)) {
				log.error("Directiry path cannot be null or empty");
				error = true;
			} else {
				if (Files.notExists(Paths.get(directoryPath))) {
					log.error("Enter valid directory path");
					error = true;
				}
			}
			if (!error) {
				scanner.close();
				return directoryPath;
			}

		}
	}
}
