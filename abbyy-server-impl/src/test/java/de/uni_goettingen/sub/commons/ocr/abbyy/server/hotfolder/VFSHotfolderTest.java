package de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder;

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

import static org.junit.Assert.*;

import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.ApacheVFSHotfolderImpl;

public class VFSHotfolderTest {
	private final static Logger logger = LoggerFactory.getLogger(VFSHotfolderTest.class);
	
	public static Long IMAGE_SIZE = 10069l;

	protected static File testImageFile;
	protected static URI testImageUri;
	protected static ApacheVFSHotfolderImpl apacheVFSHotfolderImpl;

	@BeforeClass
	public static void init() throws MalformedURLException {

		testImageFile = new File(LOCAL_INPUT, "00000001.tif");
		
		testImageUri = testImageFile.toURI();

		apacheVFSHotfolderImpl = (ApacheVFSHotfolderImpl) ApacheVFSHotfolderImpl
				.getInstance(new ConfigParser().parse());
	}

	@Test
	public void testMkDir() throws MalformedURLException, IOException {
		File testDir = new File(MISC, "newFolder");
		URI testDirUri = testDir.toURI();
		logger.debug("Checking if " + testDirUri
				+ " can be created.");
		apacheVFSHotfolderImpl.mkDir(testDirUri);
		assertTrue(testDir.exists());
	}

	@Test
	public void checkSize() throws IOException, URISyntaxException {
		logger.debug("Checking size of " + testImageUri.toString());
		Long size = apacheVFSHotfolderImpl.getTotalSize(testImageUri);
		logger.debug("Size is " + size.toString());
		assertTrue(IMAGE_SIZE.equals(size));
	}

	// apacheVFS seems to have a bug in the WebDAV copy implementation
	@Test
	public void testCopy() throws IOException, URISyntaxException {
		File targetImage = new File(MISC, "target.tif");
		logger.debug("Copy " + testImageUri + " to " + targetImage);
		apacheVFSHotfolderImpl.copyFile(testImageUri, targetImage.toURI());
		assertTrue("File can't be found.", targetImage.exists());
		targetImage.delete();
	}
	@Test
	public void testExists() throws IOException, URISyntaxException {
		logger.debug("Checking if " + testImageUri + " exists.");
		assertTrue(apacheVFSHotfolderImpl.exists(testImageUri));
	}

	@Test
	public void testDelete() throws IOException, URISyntaxException {
		File testFile = new File(MISC, "testFile");
		testFile.createNewFile();
		assertTrue(testFile.exists());
		apacheVFSHotfolderImpl.delete(testFile.toURI());
		assertFalse(testFile.exists());
	}
	
	@Test
	public void listURIs() throws IOException {
		URI folder = LOCAL_INPUT.toURI();
		List<URI> children = apacheVFSHotfolderImpl.listURIs(folder);
		assertTrue(children.toString().contains("oneImageBook"));
	}
	
	@Test
	public void tempFile() throws IOException {
		OutputStream os = apacheVFSHotfolderImpl.createTmpFile("tempfile");
		assertNotNull(os);
		File destFile = new File(MISC, "temp.file");
		URI dest = destFile.toURI();
		apacheVFSHotfolderImpl.copyTmpFile("tempfile", dest);
		assertTrue(destFile.exists());
		
		destFile.delete();
	}
	
	@Test
	public void openInputStream() throws IOException {
		URI uri = new File(LOCAL_INPUT, "xmlExport.xml").toURI();
		InputStream is = apacheVFSHotfolderImpl.openInputStream(uri);
		assertNotNull(is);
	}
}
