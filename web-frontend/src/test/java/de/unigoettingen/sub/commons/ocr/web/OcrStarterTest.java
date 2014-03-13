package de.unigoettingen.sub.commons.ocr.web;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class OcrStarterTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws InterruptedException {
		OcrStarter ocrStarter = new OcrStarter();
		OcrStarter ocrStarter2 = new OcrStarter();
		OcrParameters par = new OcrParameters();
		par.email = "mail1";
		ocrStarter.setParameters(par);
		OcrParameters par2 = new OcrParameters();
		par2.email = "mail2";
		
		new Thread(ocrStarter).start();
		Thread.sleep(7000);
		ocrStarter.setParameters(par2);
		new Thread(ocrStarter).start();
		
		Thread.sleep(1000000);
		
	}

}
