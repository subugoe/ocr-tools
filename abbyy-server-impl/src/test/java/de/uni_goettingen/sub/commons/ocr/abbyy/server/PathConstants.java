package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;

public class PathConstants {

	public final static File RESOURCES = new File(
			System.getProperty("user.dir") + "/src/test/resources");
	public final static File LOCAL_INPUT = new File(RESOURCES, "input");
	
	public final static File EXPECTED_ROOT = new File(
			System.getProperty("user.dir") + "/src/test/resources/expected");
	public final static File EXPECTED_OUTPUT = new File(EXPECTED_ROOT, "output");
	public final static File EXPECTED_ERROR = new File(EXPECTED_ROOT, "error");
	public final static File EXPECTED_SAMPLES = new File(EXPECTED_ROOT,
			"samples");
	
	public final static File TARGET = new File(
			System.getProperty("user.dir") + "/target");
	public final static File LOCAL_OUTPUT = new File(TARGET, "localOutput");
	public final static File MISC = new File(TARGET, "misc");

	// directories for the local webdav server, should be inside 'target'
	public final static File DAV_FOLDER = new File(System.getProperty("user.dir")
			+ "/target/dav");
	public final static File DAV_INPUT = new File(DAV_FOLDER, "input");
	public final static File DAV_OUTPUT = new File(DAV_FOLDER, "output");
	public final static File DAV_ERROR = new File(DAV_FOLDER, "error");
	
	public final static Integer DAV_PORT = 9001;
	public final static String DAV_ADDRESS = "http://localhost:9001/";
	
	// make sure the directories are present
	static {
		DAV_FOLDER.mkdirs();
		DAV_INPUT.mkdirs();
		DAV_OUTPUT.mkdirs();
		DAV_ERROR.mkdirs();
		LOCAL_OUTPUT.mkdirs();
		MISC.mkdirs();
	}
	
	
}
