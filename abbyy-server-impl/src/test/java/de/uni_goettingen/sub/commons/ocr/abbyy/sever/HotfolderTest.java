package de.uni_goettingen.sub.commons.ocr.abbyy.sever;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileSystemException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRImage;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder;

public class HotfolderTest {
	final static Logger logger = LoggerFactory.getLogger(HotfolderTest.class);
	public static File BASEFOLDER_FILE = TicketTest.BASEFOLDER_FILE;
	public static File TEST_INPUT_FILE, TEST_HOTFOLDER_FILE;
	public static URL TEST_INPUT_URL, TEST_HOTFOLDER_URL;
	public static String INPUT = "input";
	public static String HOTFOLDER = "hotfolder";
	public static String IMAGE_NAME = "00000001.tif";
	public static Long IMAGE_SIZE = 10069l;
	
	protected static File testDirFile, testImageFile, testImageTargetFile;
	protected static URL testDirUrl, testImageUrl, testImageTargetUrl;
	protected static String dirName = "testDir";

	static {
		TEST_INPUT_FILE = new File(BASEFOLDER_FILE.getAbsolutePath() + File.separator + INPUT);
		TEST_HOTFOLDER_FILE = new File(BASEFOLDER_FILE.getAbsolutePath() + File.separator + HOTFOLDER);
		try {
			TEST_INPUT_URL = TEST_INPUT_FILE.toURI().toURL();
			TEST_HOTFOLDER_URL = TEST_HOTFOLDER_FILE.toURI().toURL();
		} catch (MalformedURLException e) {
			logger.error("This should never happen!");
		}
		
	}
	
	@BeforeClass
	public static void init () throws MalformedURLException {
		testDirFile = new File(TEST_HOTFOLDER_FILE.getAbsolutePath() + File.separator + dirName);
		testDirUrl = testDirFile.toURI().toURL();
		logger.info("testDirUrl is " + testDirUrl);
		
		testImageFile = new File(TEST_INPUT_FILE.getAbsolutePath() + File.separator + AbbyyProcessTest.TEST_FOLDERS.get(0) + File.separator + IMAGE_NAME);
		assertTrue(testImageFile.exists());
		testImageUrl = testImageFile.toURI().toURL();
		logger.info("testImageUrl is " + testImageUrl);
		
		testImageTargetFile = new File(TEST_HOTFOLDER_FILE.getAbsolutePath() + File.separator + dirName + File.separator + IMAGE_NAME);
		assertTrue(!testImageTargetFile.exists());
		testImageTargetUrl = testImageTargetFile.toURI().toURL();
		logger.info("testImageTargetUrl is " + testImageTargetUrl);
	}

	@Ignore
	@Test
	public void testHotfolder () throws IOException, InterruptedException {
		List<AbbyyOCRImage> files = new ArrayList<AbbyyOCRImage>();
		//AbbyyOCRImage abbyy = new AbbyyOCRImage(new URL("http://localhost/webdav/Test/TestB.tif"));					

		System.out.println(TicketTest.getBaseFolderAsFile().getAbsolutePath().toString());
		URL local = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() + "local/testfile");
		URL input = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/input/testfile");
		//URL hotfol = new URL(TicketTest.getBaseFolderAsFile().toURI().toURL() + "local/");
		AbbyyOCRImage abbyy = new AbbyyOCRImage(local, input, "");
		files.add(abbyy);

		Hotfolder hot = new Hotfolder();

		//String remotefile = TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/input/testfile";
		//String localfile = TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/error/testfile1";
		//assertTrue(new File(TicketTest.getBaseFolderAsFile().toURI().toURL() + "hotfolder/input/testfile").exists());
		///		hot.copyAllFiles(remotefile, localfile);
		hot.delete(input);


	}
	
	@Test
	public void testMkDir () throws MalformedURLException, FileSystemException {
		logger.debug("Checking if " + testDirUrl.toString() + " can be created.");
		Hotfolder h = new Hotfolder();
		h.mkDir(testDirUrl);
		assertTrue(testDirFile.exists());
	}
	
	@Test
	public void checkSize () throws FileSystemException {
		logger.debug("Checking size of " + testImageUrl.toString());
		Hotfolder h = new Hotfolder();
		Long size = h.getTotalSize(testImageUrl);
		logger.debug("Size is " + size.toString());
		assertTrue(IMAGE_SIZE.equals(size));
	}
	
	@Ignore
	@Test
	public void testCopy () throws FileSystemException {
		logger.debug("Copy " + testImageUrl.toString() + " to " + testDirUrl.toString());
		Hotfolder h = new Hotfolder();
		h.copyAllFiles(testDirUrl.toString(), testImageUrl.toString());
		assertTrue(testImageTargetFile.exists());
	}
	
	@Test
	public void testExists() throws FileSystemException {
		logger.debug("Checking if " + testImageTargetUrl.toString() + " exists.");
		Hotfolder h = new Hotfolder();
		assertTrue(h.exists(testImageTargetUrl.toString()));
	}
	
	@AfterClass
	public static void cleanup () {
		logger.debug("Cleaning up");

		testDirFile.delete();
		assertTrue("Directory wasn't deleted", !testDirFile.exists());
		
		testImageTargetFile.delete();
		assertTrue("File wasn't deleted", !testImageTargetFile.exists());
	}

}
