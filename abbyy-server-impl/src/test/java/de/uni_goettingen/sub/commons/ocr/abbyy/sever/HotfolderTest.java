package de.uni_goettingen.sub.commons.ocr.abbyy.sever;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.vfs.FileSystemException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder;

public class HotfolderTest {
	final static Logger logger = LoggerFactory.getLogger(HotfolderTest.class);
	public static File BASEFOLDER_FILE = TicketTest.BASEFOLDER_FILE;
	public static File TEST_INPUT_FILE, TEST_HOTFOLDER_FILE, TEST_EXPECTATIONS_FILE;
	public static URL TEST_INPUT_URL, TEST_HOTFOLDER_URL;
	public static String INPUT = "input";
	public static String HOTFOLDER = "hotfolder";
	public static String IMAGE_NAME = "00000001.tif";
	public static Long IMAGE_SIZE = 10069l;

	protected static File testDirFile, testImageFile, testImageTargetFile;
	protected static URL testDirUrl, testImageUrl, testImageTargetUrl;
	protected static String dirName = "testDir";
	protected static String target;

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
		testDirFile = new File(TEST_HOTFOLDER_FILE.getAbsolutePath() + File.separator + dirName + File.separator);
		testDirUrl = testDirFile.toURI().toURL();
		logger.info("testDirUrl is " + testDirUrl);

		testImageFile = new File(TEST_INPUT_FILE.getAbsolutePath() + File.separator + AbbyyProcessTest.TEST_FOLDERS.get(0) + File.separator + IMAGE_NAME);
		assertTrue(testImageFile.exists());
		testImageUrl = testImageFile.toURI().toURL();
		logger.info("testImageUrl is " + testImageUrl);

		testImageTargetFile = new File(TEST_HOTFOLDER_FILE.getAbsolutePath() + File.separator + dirName + File.separator + IMAGE_NAME);
		assertTrue("File " + testImageTargetFile.getAbsolutePath() + " already exists", !testImageTargetFile.exists());
		testImageTargetUrl = testImageTargetFile.toURI().toURL();
		logger.info("testImageTargetUrl is " + testImageTargetUrl);

		target = testDirUrl.toString() + "/" + getFileName(testImageUrl);
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

	@Test
	public void testCopy () throws FileSystemException, MalformedURLException, URISyntaxException {
		logger.debug("Copy " + testImageUrl.toString() + " to " + target);
		Hotfolder h = new Hotfolder();
		h.copyFile(testImageUrl.toString(), target);
		assertTrue("File can't be found.", new File(new URL(target).toURI()).exists());
	}

	public static String getFileName (URL u) {
		String[] urlParts = u.toString().split("/");
		return urlParts[urlParts.length - 1];
	}

	@Test
	public void testExists () throws FileSystemException {
		logger.debug("Checking if " + target + " exists.");
		Hotfolder h = new Hotfolder();
		assertTrue(h.exists(target));
	}

	@Test
	public void testDelete () throws FileSystemException, MalformedURLException {
		Hotfolder h = new Hotfolder();
		h.delete(new URL(target));
		assertTrue(!new File(target).exists());
	}

	@AfterClass
	public static void cleanup () {
		logger.debug("Cleaning up");

		testDirFile.delete();
		assertTrue("Directory wasn't deleted", !testDirFile.exists());

		//This shouldn't be nessecary
		testImageTargetFile.delete();
		assertTrue("File wasn't deleted", !testImageTargetFile.exists());
	}

}
