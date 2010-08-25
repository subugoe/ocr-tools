package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.Ticket;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;


public class TicketTest {

	

	private Ticket ticket;
	File file = new File("C:/Test/ticket.xml");
	
	/*@Test
	public void testWrite () {
		System.out.println("test");
	}	
		*/
	/*
	@Before
	public void prepare() { 
		ticket = new Ticket();
	
	}
	*/
	@Test
	public void testWrite() throws Exception {
		
		//Integer millisPerFile = 1200;
		String strDir = "C:/Test";
		//File ticketFile = new File(strDir);
		
		//assertEquals ("Result", 50, tester.multiply (10, 5));

		OCRProcess ocrp = new OCRProcess();
		ocrp.addLanguage(Locale.ENGLISH);
		ocrp.addOCRFormat(OCRFormat.PDF);
		
		OCRImage ocri = new OCRImage(new File("/tmp").toURI().toURL());
		
		ocrp.addImage(ocri);
		
		ticket = new Ticket(ocrp);
		
		ticket.write(file);
		
		
		assertTrue(file.exists());
		

	}
}
