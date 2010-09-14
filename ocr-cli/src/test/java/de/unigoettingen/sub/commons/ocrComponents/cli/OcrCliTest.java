package de.unigoettingen.sub.commons.ocrComponents.cli;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.unigoettingen.sub.commons.ocrComponents.cli.OCRCli;


public class OcrCliTest {

	
	@Before
	public  void init () {
	}
	
	@Test
	public void testCli () {	
		OCRProcess process = mock(OCRProcess.class);
		OCREngine engine = mock(OCREngine.class);
		OCRCli ocr = MyOCRCli.getInstance(engine, process);
		
		String[] args = new String[4];
		args[0] = "-l Deutsch,English";
		args[1] = "-o D:/Recognition/GDZ/output";
		args[2] = "-fTXT,PDF";
		args[3] = "./src/test/resources";		
		ocr.configureFromArgs(args);
				
	}

	
	
}
