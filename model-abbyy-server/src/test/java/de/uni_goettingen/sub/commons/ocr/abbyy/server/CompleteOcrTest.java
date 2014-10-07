package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrImage;
import de.uni_goettingen.sub.commons.ocr.api.OcrOutput;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;

public class CompleteOcrTest {

	private OcrEngine engine = new AbbyyEngine();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MyServers.startDavServer();
	}

	@Before
	public void setUp() throws Exception {
		MyServers.startAbbyySimulator();
	}

	@After
	public void tearDown() throws Exception {
		MyServers.stopAbbyySimulator();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		MyServers.stopDavServer();
	}

	// The simplest use case: ocr one image to a text file
//	@Test
//	public void oneImage() {
//		
//		OcrProcess process = engine.newOcrProcess();
//		File inputBook = new File(LOCAL_INPUT, "oneImageBook");
//		String jobName = inputBook.getName();
//		process.setName(jobName);
//		
//		URI imageUri = new File(inputBook, "00000001.tif").toURI();
//		OcrImage image = engine.newOcrImage(imageUri);
//		
//		List<OcrImage> images = new ArrayList<OcrImage>();
//		images.add(image);
//		process.setOcrImages(images);
//		
//		OcrFormat format = OcrFormat.TXT;
//		OcrOutput output = engine.newOcrOutput();
//		File outputFile = new File(LOCAL_OUTPUT, jobName + ".txt");
//		output.setUri(outputFile.toURI());
//		process.addOutput(format, output);
//		
//		engine.addOcrProcess(process);
//		engine.recognize();
//		
//		assertTrue(outputFile.exists());
//	}

}
