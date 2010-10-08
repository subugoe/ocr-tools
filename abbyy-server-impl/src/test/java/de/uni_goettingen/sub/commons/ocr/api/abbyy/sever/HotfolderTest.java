package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRFile;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder;

public class HotfolderTest {

	static URL testfile;

	@BeforeClass
	public static void init () throws MalformedURLException {
		
		testfile = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() + "local/testfile");
		//hot = mock(Hotfolder.class);
	}

	@Test
	public void testHotfolder () throws IOException, InterruptedException {
		Long zahl = (long) 284.551;
		List<AbbyyOCRFile> files = new ArrayList<AbbyyOCRFile>();
		//AbbyyOCRFile abbyy = new AbbyyOCRFile(new URL("http://localhost/webdav/Test/TestB.tif"));					

		System.out.println(TicketTest.getBaseFolderAsFile().getAbsolutePath().toString());
		URL local = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() + "local/testfile");
		URL input = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/input/testfile");
		URL hotfol = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() + "local/");
		AbbyyOCRFile abbyy = new AbbyyOCRFile(local, input, "");
		files.add(abbyy);

		Hotfolder hot = new Hotfolder();
/*

		//Credentials defaultcreds = new UsernamePasswordCredentials("gdz", "***REMOVED***");
		
	  */
		hot.copyFilesToServer(files);

		String remotefile = TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/input/testfile";
		String localfile = TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/error/testfile1";
		//assertTrue(new File(TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/input/testfile").exists());
		hot.copyAllFiles(remotefile, localfile);
		System.out.println(localfile);
		hot.delete(input);
		hot.deleteIfExists(localfile);
		//hot.deleteIfExists(url)
		
		zahl = hot.getTotalSize(hotfol);
		//assertTrue(zahl == 38l);
		
	}

	

}
