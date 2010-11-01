package de.uni_goettingen.sub.commons.ocr.abbyy.sever;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.apache.xmlbeans.XmlException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRImage;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRProcess;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.unigoettingen.sub.commons.util.stream.StreamUtils;

@SuppressWarnings("serial")
public class AbbyyOCRProcessTest {
	final static Logger logger = LoggerFactory.getLogger(AbbyyOCRProcessTest.class);
	public static File BASEFOLDER_FILE = AbbyyTicketTest.BASEFOLDER_FILE;
	public static List<String> TEST_FOLDERS;

	static {
		TEST_FOLDERS = new ArrayList<String>() {
			{
				add("PPN129323640_0010");
				add("PPN31311157X_0102");
				add("PPN514401303_1890");
				add("PPN514854804_0001");
			}
		};
	}

	@BeforeClass
	public static void init () {
		logger.debug("Starting AbbyyOCRProcessTest");
		//Nothing to do here
	}

	@Test
	public void createAbbyyProcess () throws IOException {
		for (String book : TEST_FOLDERS) {
			File testDir = new File(BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book);
			logger.debug("Creating AbbyyOCRProcess for " + testDir.getAbsolutePath());
			AbbyyOCRProcess aop = AbbyyServerOCREngine.createProcessFromDir(testDir, AbbyyTicketTest.EXTENSION);
			assertNotNull(aop);
			aop.setOcrOutputs(AbbyyTicketTest.OUTPUT_DEFINITIONS);
			File testTicket = new File(BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book + ".xml");
			aop.write(new FileOutputStream(testTicket), testDir.getName());
			logger.debug("Wrote AbbyyTicket:\n" + StreamUtils.dumpInputStream(new FileInputStream(testTicket)));
		}
	}

	@Test
	public void checkTicketCount () throws IOException, XmlException {
		for (String book : TEST_FOLDERS) {
			File testDir = new File(BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book);
			logger.debug("Creating AbbyyOCRProcess for " + testDir.getAbsolutePath());
			AbbyyOCRProcess aop = AbbyyServerOCREngine.createProcessFromDir(testDir, AbbyyTicketTest.EXTENSION);
			assertNotNull(aop);
			aop.setOcrOutputs(AbbyyTicketTest.OUTPUT_DEFINITIONS);
			File testTicket = new File(BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book + ".xml");
			aop.write(new FileOutputStream(testTicket), testDir.getName());
			logger.debug("Wrote AbbyyTicket:\n" + StreamUtils.dumpInputStream(new FileInputStream(testTicket)));
			assertTrue("This fails if the number of files between ticket and file system differs.", AbbyyTicketTest.parseFilesFromTicket(testTicket).size() == aop.getOcrImages().size());
		}
	}

	@Test
	public void createProcessViaAPI () throws MalformedURLException, URISyntaxException {
		AbbyyServerOCREngine ase = AbbyyServerOCREngine.getInstance();
		assertNotNull(ase);
		OCRProcess op = ase.newOcrProcess();
		List<OCRImage> imgList = new ArrayList<OCRImage>();
		for (int i = 0; i < 10; i++) {
			OCRImage ocri = mock(OCRImage.class);
			String imageUrl = BASEFOLDER_FILE.toURI().toURL().toString() + i;
			when(ocri.getUri()).thenReturn(new URI(imageUrl));
			logger.debug("Added url to list: " + imageUrl);
			AbbyyOCRImage aoi = new AbbyyOCRImage(ocri);
			assertTrue(imageUrl.equals(aoi.getUri().toString()));
			aoi.setRemoteFileName("remoteName" + i);
			imgList.add(aoi);
		}
		op.setOcrImages(imgList);
		//AbbyyOCRProcess aop = (AbbyyOCRProcess) op;
		//aop.write(out, identifier)

	}

	@Test
	public void createUrlBasedProcess () throws MalformedURLException, URISyntaxException {
		logger.info("This test uses http Urls, this should break wrong usageg of java.io.File.");
		AbbyyServerOCREngine ase = AbbyyServerOCREngine.getInstance();
		assertNotNull(ase);
		OCRProcess op = ase.newOcrProcess();
		List<OCRImage> imgList = new ArrayList<OCRImage>();
		for (int i = 0; i < 10; i++) {
			OCRImage ocri = mock(OCRImage.class);
			String imageUrl = "http://127.0.0.1:8080/image-" + i;
			when(ocri.getUri()).thenReturn(new URI(imageUrl));
			logger.debug("Added url to list: " + imageUrl);
			imgList.add(ocri);
		}
		op.setOcrImages(imgList);
	}

	@Ignore
	@Test
	public void testUrlSchemaResolver () throws URISyntaxException, FileSystemException {
		URI webdavUrl = new URI("webdav://localhost/file");
		//This should fail
		logger.debug("Set URI to " + webdavUrl.toString());
		Boolean mue = false;
		try {
			new URL(webdavUrl.toString());
		} catch (MalformedURLException e) {
			mue = true;
			logger.trace("Got MalformedURLException as expected", e);
		}
		assertTrue(mue);
		mue = false;

		//This shouldn't fail
		//DefaultFileSystemManager fsm = new DefaultFileSystemManager();
		//fsm.addProvider("webdav", new WebdavFileProvider());
		//VFS.getManager().resolveURI(webdavUrl.toString());
		URL.setURLStreamHandlerFactory(VFS.getManager().getURLStreamHandlerFactory());
		try {
			new URL(webdavUrl.toString());
		} catch (MalformedURLException e) {
			mue = true;
			logger.trace("Got MalformedURLException, this shouldn't happen here.", e);
		}
		assertFalse(mue);
	}

	@AfterClass
	public static void cleanup () {
		logger.debug("Cleaning up");
		for (String book : TEST_FOLDERS) {
			File testTicket = new File(BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book + ".xml");
			logger.equals("Deleting file " + testTicket.getAbsolutePath());
			testTicket.delete();
			assertTrue(!testTicket.exists());
		}
	}

}
