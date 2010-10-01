package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;

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
	}

	@Test
	public void testHotfolder () throws IOException, InterruptedException {
		List<AbbyyOCRFile> files = new ArrayList<AbbyyOCRFile>();
		//AbbyyOCRFile abbyy = new AbbyyOCRFile(new URL("http://localhost/webdav/Test/TestB.tif"));					

		//System.out.print(getBaseFolderAsFile().getAbsolutePath().toString());
		URL local = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() + "local/testfile");
		URL input = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/input/testfile");

		AbbyyOCRFile abbyy = new AbbyyOCRFile(local, input, "");
		files.add(abbyy);

		Hotfolder hot = new Hotfolder();

		hot.copyFilesToServer(files);

		//assertTrue(new File(TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/input/testfile").exists());

	}

}
