package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import org.apache.log4j.helpers.Loader;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.Ticket;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;


public class TicketTest {

	

	private Ticket ticket;
	private static File basefolderFile;
	public static URL basefolder;
	
	static {
		/* base folder with tests */
		basefolderFile = getBaseFolderAsFile();
		try {
			basefolder = basefolderFile.toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
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
		//String strDir = "C:/Test";
		//File ticketFile = new File(strDir);
		
		//assertEquals ("Result", 50, tester.multiply (10, 5));
		
		OCRProcess ocrp = new OCRProcess();
		ocrp.addLanguage(Locale.ENGLISH);
		ocrp.addOCRFormat(OCRFormat.PDF);
		List<OCRProcess> inputFiles = new ArrayList<OCRProcess>();
		//ocrp.getFile();
		assertNotNull("base path is null", basefolder);
		URL ticketUrl = new URL(basefolder.toString() + "ticket.xml");
		File file = new File(ticketUrl.toString());
		//OCRImage ocri = new OCRImage(new File("C:/Test/TestB.tif/").toURI().toURL());
		OCRImage ocri = new OCRImage(new File("/tmp").toURI().toURL());
		
		ocrp.addImage(ocri);
		
		ticket = new Ticket(ocrp);
		
		ticket.write(file);
		
		
		assertTrue(file.exists());
		

	}
	
	public static File getBaseFolderAsFile() {
		File basefolder;
		// TODO: GDZ: Do wee really need to depend on Log4J here? I don't think so...
		URL url = Loader.getResource("");
		try {
			basefolder = new File(url.toURI());
		} catch (URISyntaxException ue) {
			basefolder = new File(url.getPath());
		}
		return basefolder;
	}
}
