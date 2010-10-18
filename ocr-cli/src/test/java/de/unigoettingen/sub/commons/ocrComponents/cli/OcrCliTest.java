package de.unigoettingen.sub.commons.ocrComponents.cli;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.MalformedURLException;
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
	public void testCli () throws IOException {	
		OCRProcess process = mock(OCRProcess.class);
		OCREngine engine = mock(OCREngine.class);
		OCRCli ocr = MyOCRCli.getInstance(engine, process);
		
		String[] args = new String[5];
		//TODO: This is just wrong
		args[0] = "-l Deutsch,English";
		args[1] = "-o ./target";
		args[2] = "-fTXT,PDF";
		args[3] = "./src/test/resources/hotfolder/input";		
		args[4] = "./src/test/resources/hotfolder/output";
		ocr.configureFromArgs(args);
				
	}

	@Test
	public void testLanguageParser () {
		List<Locale> langs = OCRCli.parseLangs("de,en");
		assertTrue(langs.contains(Locale.GERMAN));
		assertTrue(langs.contains(Locale.ENGLISH));
		langs = OCRCli.parseLangs("ru");
		assertTrue(langs.contains(new Locale("ru")));
	}
	
	@Test
	public void testFormatParser () {
		List<OCRFormat> formats = OCRCli.parseOCRFormat("PDF,HTML");
		assertTrue(formats.contains(OCRFormat.HTML));
		assertTrue(formats.contains(OCRFormat.PDF));
		
	}

	
}
