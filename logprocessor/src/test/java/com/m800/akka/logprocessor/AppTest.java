package com.m800.akka.logprocessor;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;

import com.example.akka.logprocessor.actor.FileScanner;
import com.example.akka.logprocessor.model.Scan;

/**
 * Unit test for logProcessor
 */
public class AppTest {

	static ActorSystem system;
	final String directoryPath = "E:\\log";

	@BeforeClass
	public static void setup() {
		system = ActorSystem.create();
	}

	@AfterClass
	public static void teardown() {
		TestKit.shutdownActorSystem(system);
		system = null;
	}

	@Test
	public void testFileScannerActor() {
		final TestKit testProbe = new TestKit(system);
		Props props = Props.create(FileScanner.class);
		ActorRef fileScannerActor = system.actorOf(props, "fileScanner");
		testProbe.getRef().tell(new Scan(directoryPath), fileScannerActor);
		testProbe.expectMsgClass(Scan.class);
	}
}
