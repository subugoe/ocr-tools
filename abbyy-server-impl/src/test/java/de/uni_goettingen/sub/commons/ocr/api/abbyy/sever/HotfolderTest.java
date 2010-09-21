package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import org.apache.log4j.helpers.Loader;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRFile;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder;

public class HotfolderTest {
	
	static URL testfile;
	
	@BeforeClass
	public static void init () throws MalformedURLException {
		testfile = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() + "local/testfile");
	}
	
	@Test
	public void testHotfolder () throws IOException, InterruptedException {
		List<AbbyyOCRFile> files = new ArrayList<AbbyyOCRFile>();
		//AbbyyOCRFile abbyy = new AbbyyOCRFile(new URL("http://localhost/webdav/Test/TestB.tif"));					
		
//		URL local = new URL("file:///C:/Dokumente%20und%20Einstellungen/mabergn.UG-SUB/workspace/ocr-tools/abbyy-server-impl/src/test/resources/local/test.xml");
//		URL input = new URL("file:///C:/Dokumente%20und%20Einstellungen/mabergn.UG-SUB/workspace/ocr-tools/abbyy-server-impl/src/test/resources/hotfolder/input/test.xml");	
		//System.out.print(getBaseFolderAsFile().getAbsolutePath().toString());
		URL local = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() +"local/testfile");
		URL input = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/input/testfile");
		
		AbbyyOCRFile abbyy = new AbbyyOCRFile(local,input , "");
		files.add(abbyy);
				
		Hotfolder hot = new Hotfolder();

		//Credentials defaultcreds = new UsernamePasswordCredentials("gdz", "Hfwveqo)");
		
	    hot.copyFilesToServer(files);
	   
	    //assertTrue(new File(TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/input/testfile").exists());
	   
	    
	}
	
}
