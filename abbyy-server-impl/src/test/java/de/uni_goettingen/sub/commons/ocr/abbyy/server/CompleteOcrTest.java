package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

public class CompleteOcrTest {

	private OCREngine engine = AbbyyServerOCREngine.getInstance();
	
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
	@Test
	public void oneImage() {
		
		OCRProcess process = engine.newOcrProcess();
		File inputBook = new File(LOCAL_INPUT, "oneImageBook");
		String jobName = inputBook.getName();
		process.setName(jobName);
		
		URI imageUri = new File(inputBook, "00000001.tif").toURI();
		OCRImage image = engine.newOcrImage(imageUri);
		
		List<OCRImage> images = new ArrayList<OCRImage>();
		images.add(image);
		process.setOcrImages(images);
		
		OCRFormat format = OCRFormat.TXT;
		OCROutput output = engine.newOcrOutput();
		File outputFile = new File(LOCAL_OUTPUT, jobName + ".txt");
		output.setUri(outputFile.toURI());
		process.addOutput(format, output);
		
		engine.addOcrProcess(process);
		engine.recognize();
		
		assertTrue(outputFile.exists());
	}

}
