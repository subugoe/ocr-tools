package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRImage;
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
		List<AbbyyOCRImage> files = new ArrayList<AbbyyOCRImage>();
		//AbbyyOCRImage abbyy = new AbbyyOCRImage(new URL("http://localhost/webdav/Test/TestB.tif"));					

		System.out.println(TicketTest.getBaseFolderAsFile().getAbsolutePath().toString());
		URL local = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() + "local/testfile");
		URL input = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/input/testfile");
		URL hotfol = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() + "local/");
		AbbyyOCRImage abbyy = new AbbyyOCRImage(local, input, "");
		files.add(abbyy);

		Hotfolder hot = new Hotfolder();

		String remotefile = TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/input/testfile";
		String localfile = TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/error/testfile1";
		//assertTrue(new File(TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/input/testfile").exists());
		///		hot.copyAllFiles(remotefile, localfile);
		System.out.println(localfile);
		hot.delete(input);

		//zahl = hot.getTotalSize(hotfol);
		//assertTrue(zahl == 38l);

	}

}
