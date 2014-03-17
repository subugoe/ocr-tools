package de.unigoettingen.sub.commons.ocr.web;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class OcrStarterTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws InterruptedException {
		OcrStarter ocrStarter = new OcrStarter();
		OcrParameters par = new OcrParameters();
		par.inputFolder = new File("src/test/resources/testInputs").getAbsolutePath();
		ocrStarter.setParameters(par);
		
		ocrStarter.run();
	}

}
