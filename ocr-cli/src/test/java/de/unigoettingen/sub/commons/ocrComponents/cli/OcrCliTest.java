package de.unigoettingen.sub.commons.ocrComponents.cli;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
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
		
		String[] args = new String[5];
		args[0] = "-l de,en";
		args[1] = "-o ./target";
		args[2] = "-fTXT,PDF";
		args[3] = "./target/test-classes";		
		args[4] = "./target/test-classes";
		ocr.configureFromArgs(args);
				
	}

	@Test
	public void testFormatParser () {
		List<OCRFormat> formats = OCRCli.parseOCRFormat("PDF,HTML");
		assertTrue(formats.contains(OCRFormat.HTML));
		assertTrue(formats.contains(OCRFormat.PDF));
		
	}

	
}
