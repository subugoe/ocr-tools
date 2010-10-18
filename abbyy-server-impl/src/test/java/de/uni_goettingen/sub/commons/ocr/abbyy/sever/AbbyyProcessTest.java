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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRImage;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyProcess;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerEngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

@SuppressWarnings("serial")
public class AbbyyProcessTest {
	final static Logger logger = LoggerFactory.getLogger(AbbyyProcessTest.class);
	public static File BASEFOLDER_FILE = TicketTest.BASEFOLDER_FILE;
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
		logger.debug("Starting AbbyyProcessTest");
		//Nothing to do here
	}

	@Test
	public void createAbbyyProcess () throws IOException {
		for (String book : TEST_FOLDERS) {
			File testDir = new File(BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book);
			logger.debug("Creating AbbyyProcess for " + testDir.getAbsolutePath());
			AbbyyProcess aop = AbbyyProcess.createProcessFromDir(testDir, TicketTest.EXTENSION);
			assertNotNull(aop);
			aop.setOcrOutput(TicketTest.OUTPUT_DEFINITIONS);
			File testTicket = new File(BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book + ".xml");
			aop.write(testTicket, testDir.getName());
			logger.debug("Wrote Ticket:\n" + TicketTest.dumpTicket(new FileInputStream(testTicket)));
		}

	}

	@Test
	public void checkTicketCount () throws IOException, XmlException {
		for (String book : TEST_FOLDERS) {
			File testDir = new File(BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book);
			logger.debug("Creating AbbyyProcess for " + testDir.getAbsolutePath());
			AbbyyProcess aop = AbbyyProcess.createProcessFromDir(testDir, TicketTest.EXTENSION);
			assertNotNull(aop);
			aop.setOcrOutput(TicketTest.OUTPUT_DEFINITIONS);
			File testTicket = new File(BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book + ".xml");
			aop.write(testTicket, testDir.getName());
			logger.debug("Wrote Ticket:\n" + TicketTest.dumpTicket(new FileInputStream(testTicket)));
			assertTrue("This fails if the number of files between ticket and file system differs.", TicketTest.parseFilesFromTicket(testTicket).size() == aop.getOcrImages().size());
		}
	}

	@Test
	public void createProcessViaAPI () throws MalformedURLException {
		AbbyyServerEngine ase = AbbyyServerEngine.getInstance();
		assertNotNull(ase);
		OCRProcess op = ase.newProcess();
		List<OCRImage> imgList = new ArrayList<OCRImage>();
		for (int i = 0; i < 10; i++) {
			OCRImage ocri = mock(OCRImage.class);
			String imageUrl = BASEFOLDER_FILE.toURI().toURL().toString() + i;
			when(ocri.getUrl()).thenReturn(new URL(imageUrl));
			logger.debug("Added url to list: " + imageUrl);
			AbbyyOCRImage aoi = new AbbyyOCRImage(ocri);
			assertTrue(imageUrl.equals(aoi.getUrl().toString()));
			aoi.setRemoteFileName("remoteName" + i);
			imgList.add(aoi);
		}
		op.setOcrImages(imgList);
		//AbbyyProcess aop = (AbbyyProcess) op;
		//aop.write(out, identifier)

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
