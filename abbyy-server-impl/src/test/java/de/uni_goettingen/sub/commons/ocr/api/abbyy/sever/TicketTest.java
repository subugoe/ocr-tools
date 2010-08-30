package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
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
	private static List<File> inputFiles = new ArrayList<File>();
	/*private static String imageFolder1 = "broken/";
	private static String imageFolder2 = "hallhist/";
	private static String imageFolder3 = "orig_mulllibe_PPN609992414_tif/";*/
	
	
	static {
		/* base folder with tests */
		basefolderFile = getBaseFolderAsFile();
		
	}
	
	@Test
	public void testWrite() throws Exception {
		
		OCRProcess ocrp = new OCRProcess();
		ocrp.addLanguage(Locale.GERMAN);
		ocrp.addOCRFormat(OCRFormat.PDF);
		inputFiles.add(new File("C:/Test/515-00000001.tif/"));
		inputFiles.add(new File("C:/Test/515-00000002.tif/"));
		inputFiles.add(new File("C:/Test/515-00000003.tif/"));
		inputFiles.add(new File("C:/Test/515-00000004.tif/"));
		inputFiles.add(new File("C:/Test/515-00000005.tif/"));
		inputFiles.add(new File("C:/Test/515-00000006.tif/"));
		inputFiles.add(new File("C:/Test/515-00000007.tif/"));
		//List<OCRProcess> inputFiles = new ArrayList<OCRProcess>();
		//ocrp.getFile();
		assertNotNull("base path is null", basefolderFile);
		//URL ticketUrl = new URL(basefolder.toString() + );
		File file = new File(basefolderFile.getAbsolutePath() + "ticket.xml");
		/*File file = new File("C:/Test/ticket.xml");*/
		//OCRImage ocri = new OCRImage(new File("C:/Test/TestB.tif/").toURI().toURL());
		OCRImage ocri = new OCRImage(new File("/tmp").toURI().toURL());
		
		ocrp.addImage(ocri);
		
		ticket = new Ticket(ocrp);
		ticket.setOutPutLocation("D:/Recognition/GDZ/output");
		Ticket.setInputFiles(inputFiles);
		//ticket.
		ticket.write(file);  //
		
		
		assertTrue(file.exists()); ///
		

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
