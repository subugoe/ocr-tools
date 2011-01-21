package de.uni_goettingen.sub.commons.ocr.abbyy.server;

/*

 Copyright 2010 SUB Goettingen. All rights reserved.
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.

 */

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.ApacheVFSHotfolderImpl;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.JackrabbitHotfolderImpl;

public class HotfolderTest {
	final static Logger logger = LoggerFactory.getLogger(HotfolderTest.class);
	public static File BASEFOLDER_FILE = AbbyyTicketTest.BASEFOLDER_FILE;
	public static File TEST_INPUT_FILE, TEST_OUTPUT_FILE, TEST_HOTFOLDER_FILE,
			TEST_EXPECTED_FILE;
	public static URI TEST_INPUT_URI, TEST_HOTFOLDER_URI, TEST_EXPECTED_URI;
	public static String INPUT = "input";
	public static String OUTPUT = "output";
	public static String HOTFOLDER = "apacheVFSHotfolderImpl";
	public static String EXPECTED = "expected";

	public static String IMAGE_NAME = "00000001.tif";
	public static Long IMAGE_SIZE = 10069l;

	protected static File testDirFile, testImageFile, testImageTargetFile;
	protected static URI testDirUri, testImageUri, testImageTargetUri;
	protected static String dirName = "Band001test";
	protected static String target;
	protected static ApacheVFSHotfolderImpl apacheVFSHotfolderImpl;

	static {
		TEST_INPUT_FILE = new File(BASEFOLDER_FILE.getAbsoluteFile()
				+ File.separator + INPUT);
		TEST_OUTPUT_FILE = new File(BASEFOLDER_FILE.getAbsoluteFile()
				+ File.separator + OUTPUT);
		TEST_HOTFOLDER_FILE = new File(BASEFOLDER_FILE.getAbsoluteFile()
				+ File.separator + HOTFOLDER);
		TEST_EXPECTED_FILE = new File(BASEFOLDER_FILE.getAbsoluteFile()
				+ File.separator + EXPECTED);
		TEST_INPUT_URI = TEST_INPUT_FILE.toURI();
		TEST_HOTFOLDER_URI = TEST_HOTFOLDER_FILE.toURI();
	}

	@BeforeClass
	public static void init() throws MalformedURLException {
		testDirFile = new File(BASEFOLDER_FILE.getAbsoluteFile()
				+ File.separator + "newFolder");
		testDirUri = testDirFile.toURI();
		logger.info("testDirUri is " + testDirUri);

		testImageFile = new File(BASEFOLDER_FILE.getAbsoluteFile()
				+ File.separator + "testDir" + File.separator + dirName
				+ File.separator + IMAGE_NAME);
		assertTrue(testImageFile.exists());
		testImageUri = testImageFile.toURI();
		logger.info("testImageUri is " + testImageUri);

		testImageTargetFile = new File(BASEFOLDER_FILE.getAbsoluteFile()
				+ File.separator + "testDir" + File.separator + dirName
				+ File.separator + IMAGE_NAME);
		assertTrue("File " + testImageTargetFile.getAbsolutePath()
				+ " already exists", testImageTargetFile.exists());
		testImageTargetUri = testImageTargetFile.toURI();
		logger.info("testImageTargetUri is " + testImageTargetUri);

		target = testDirUri.toString() + "/" + getFileName(testImageUri);
		apacheVFSHotfolderImpl = (ApacheVFSHotfolderImpl) ApacheVFSHotfolderImpl
				.getInstance(new ConfigParser());
	}

	@Test
	public void testMkDir() throws MalformedURLException, IOException {
		logger.debug("Checking if " + testDirUri.toString()
				+ " can be created.");
		apacheVFSHotfolderImpl.mkDir(testDirUri);
		assertTrue(testDirFile.exists());
	}

	@Test
	public void checkSize() throws IOException, URISyntaxException {
		logger.debug("Checking size of " + testImageUri.toString());
		Long size = apacheVFSHotfolderImpl.getTotalSize(testImageUri);
		logger.debug("Size is " + size.toString());
		assertTrue(IMAGE_SIZE.equals(size));
	}

	// TODO bug not fixed in copy by apacheVFS 
	@Ignore
	@Test
	public void testCopy() throws IOException, URISyntaxException {
		logger.debug("Copy " + testImageUri.toString() + " to " + target);
		apacheVFSHotfolderImpl.copyFile(testImageUri, new URI(target));
		assertTrue("File can't be found.",
				new File(new URL(target).toURI()).exists());
	}

	public static String getFileName(URI testImageUri2) {
		String[] urlParts = testImageUri2.toString().split("/");
		return urlParts[urlParts.length - 1];
	}

	@Test
	public void testExists() throws IOException, URISyntaxException {
		logger.debug("Checking if " + testImageUri + " exists.");
		assertTrue(apacheVFSHotfolderImpl.exists(testImageUri));
	}

	@Test
	public void testDelete() throws IOException, URISyntaxException {
		apacheVFSHotfolderImpl.delete(new URI(target));
		assertTrue(!apacheVFSHotfolderImpl.exists(new URI(target)));
	}


	@AfterClass
	public static void cleanup() {
		logger.debug("Cleaning up");

		testDirFile.delete();
		assertTrue("Directory wasn't deleted", !testDirFile.exists());

	
	}

}
