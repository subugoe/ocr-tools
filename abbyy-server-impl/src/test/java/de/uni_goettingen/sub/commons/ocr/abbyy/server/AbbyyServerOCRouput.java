package de.uni_goettingen.sub.commons.ocr.abbyy.server;



import static org.junit.Assert.assertTrue;

import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.JackrabbitHotfolderImpl;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;

public class AbbyyServerOCRouput implements Runnable  {
	final static Logger logger = LoggerFactory.getLogger(AbbyyServerOCRouput.class);
	static Hotfolder imp;
	File filefrom = new File(RESOURCES.getAbsoluteFile()+ "/results");
	//AbbyyServerOCRouput abbyyServerOCRouput= new AbbyyServerOCRouput();
	
	@Override
	public void run() {
		ConfigParser config = new ConfigParser().parse();
		imp = JackrabbitHotfolderImpl.getInstance(config);
			
		try {
			mkDir();
			logger.debug("Wait for copy to out");
			Thread.currentThread();
			Thread.sleep(8000);
			logger.debug("Starting to copy HOTFOLDER");
				
				copyToHotfolder();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
	public void mkDir() throws IOException, URISyntaxException{
		File HOTFOLDER_TEST = new File(RESOURCES.getAbsoluteFile()+ "/HOTFOLDER_TEST");
		if(!new File(HOTFOLDER_TEST.toString().replace("file:/", "")+ "/"+ "input").exists()){
			imp.mkDir(new URI("http://localhost:8090/input"));
			assertTrue(new File(HOTFOLDER_TEST.toString().replace("file:/", "")+ "/"+ "input").exists());
		}
		if(!new File(HOTFOLDER_TEST.toString().replace("file:/", "")+ "/"+ "output").exists()){
			imp.mkDir(new URI("http://localhost:8090/output"));
			assertTrue(new File(HOTFOLDER_TEST.toString().replace("file:/", "")+ "/"+ "output").exists());
		}
		if(!new File(HOTFOLDER_TEST.toString().replace("file:/", "")+ "/"+ "error").exists()){
			imp.mkDir(new URI("http://localhost:8090/error"));
			assertTrue(new File(HOTFOLDER_TEST.toString().replace("file:/", "")+ "/"+ "error").exists());
		}
	
	}
	
	public void  copyToHotfolder() throws IOException, URISyntaxException{

		for (String book : AbbyyOCRProcessTest.testFolders){
			for (OCRFormat f: AbbyyTicketTest.OUTPUT_DEFINITIONS.keySet()){
				filefrom = new File(RESOURCES.getAbsoluteFile()+ "/results"+ "/" + book + "." + f.toString().toLowerCase());
				URI from = filefrom.toURI();
				imp.copyFile(from, new URI("http://localhost:8090/output"+ "/" + book + "." + f.toString().toLowerCase()));
			}
			filefrom = new File(RESOURCES.getAbsoluteFile()+ "/results"+ "/" + book + ".xml.result.xml" );
			imp.copyFile(filefrom.toURI(), new URI("http://localhost:8090/output"+ "/" + book + ".xml.result.xml"));
		}
		
	}
}
