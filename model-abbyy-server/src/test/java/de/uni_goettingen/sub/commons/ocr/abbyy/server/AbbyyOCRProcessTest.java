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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRImage;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRProcess;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

public class AbbyyOCRProcessTest {
	final static Logger logger = LoggerFactory
			.getLogger(AbbyyOCRProcessTest.class);

	@BeforeClass
	public static void initBeforeClass() throws Exception {
		MyServers.startDavServer();
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		MyServers.stopDavServer();
	}

	@Before
	public void init() {
		for (File file : LOCAL_OUTPUT.listFiles()) {
			file.delete();
		}
		MyServers.startAbbyySimulator();
	}
	
	@After
	public void tearDown() {
		MyServers.stopAbbyySimulator();
	}
	
//	@Test
//	public void newAbbyyProcess() {
//		AbbyyOCRProcess aop = (AbbyyOCRProcess) new AbbyyServerOCREngine(new Properties()).newOcrProcess();
//		assertNotNull(aop);
//	}

	@Test
	public void executeWithOneImage() throws InterruptedException, IOException {
		String jobName = "oneImageBook";
		runProcessInThread(jobName, false);

		File outputFile = new File(LOCAL_OUTPUT, jobName + ".txt");
		assertTrue(outputFile.exists());
	}

	@Test
	public void executeWithManyImages() throws InterruptedException, IOException {
		String jobName = "threeImagesBook";
		runProcessInThread(jobName, false);

		File outputFile = new File(LOCAL_OUTPUT, jobName + ".txt");
		assertTrue(outputFile.exists());
	}

	@Test
	public void executeWithSplitting() throws InterruptedException, IOException {
		String jobName = "threeImagesBook";
		runProcessInThread(jobName, true);

		File outputFile = new File(LOCAL_OUTPUT, jobName + ".txt");
		assertTrue(outputFile.exists());
	}

	@Test
	public void equality() {
		AbbyyOCRProcess a1 = new AbbyyOCRProcess();
		a1.initialize(new Properties());
		AbbyyOCRProcess a2 = new AbbyyOCRProcess();
		a2.initialize(new Properties());
		assertFalse(a1.equals(a2));
		assertFalse(a1.hashCode() == a2.hashCode());
	}
	
	@Test
	public void calculateSize() {
		AbbyyOCRProcess process = new AbbyyOCRProcess();
		process.initialize(new Properties());

		AbbyyOCRImage im1 = new AbbyyOCRImage(new File("/test1").toURI());
		im1.setSize(1L);
		AbbyyOCRImage im2 = new AbbyyOCRImage(new File("/test2").toURI());
		im2.setSize(2L);
		process.addOcrImage(im1);
		process.addOcrImage(im2);
		long totalSize = process.calculateSize();
		assertEquals(3L, totalSize);
	}
	
	public void runProcessInThread(String jobName, boolean split) throws IOException, InterruptedException {
		AbbyyOCRProcess process = new AbbyyOCRProcess();
		Properties props = new Properties();
		props.load(new FileInputStream(System.getProperty("user.dir") + "/src/test/resources/gbv-antiqua.properties"));
		process.initialize(new Properties());

		process.setName(jobName);
		
		File bookDir = new File(LOCAL_INPUT, jobName);

		File[] imageFiles = bookDir.listFiles();
		for (File imageFile : imageFiles) {
			// could be the .svn directory
			if(imageFile.isFile()) {
				AbbyyOCRImage image = new AbbyyOCRImage(imageFile.toURI());
				process.addOcrImage(image);
			}
		}

		OCRFormat format = OCRFormat.TXT;
		File outputFile = new File(LOCAL_OUTPUT, jobName + ".txt");
		AbbyyOCROutput output = new AbbyyOCROutput(outputFile.toURI());
		output.setlocalOutput(LOCAL_OUTPUT.getAbsolutePath());

		process.addOutput(format, output);
		
		if (split) {
			List<AbbyyOCRProcess> processes = process.split();
			List<Thread> runningThreads = new ArrayList<Thread>();
			for (AbbyyOCRProcess sub : processes) {
				Thread thread = new Thread(sub);
				thread.start();
				runningThreads.add(thread);
			}
			for (Thread t : runningThreads) {
				t.join();
			}
		} else {
			Thread thread = new Thread(process);
			thread.start();
			thread.join();
		}
	}

//	@Test
//	public void createProcessViaAPI() throws MalformedURLException,
//			URISyntaxException {
//		AbbyyServerOCREngine ase = new AbbyyServerOCREngine(new Properties());
//		assertNotNull(ase);
//		OCRProcess op = ase.newOcrProcess();
//		List<OCRImage> imgList = new ArrayList<OCRImage>();
//		for (int i = 0; i < 10; i++) {
//			OCRImage ocri = mock(AbstractOCRImage.class);
//			String imageUrl = RESOURCES.toURI().toURL().toString() + i;
//			when(ocri.getUri()).thenReturn(new URI(imageUrl));
//			logger.debug("Added url to list: " + imageUrl);
//			AbbyyOCRImage aoi = new AbbyyOCRImage(ocri);
//			assertTrue(imageUrl.equals(aoi.getUri().toString()));
//			aoi.setRemoteFileName("remoteName" + i);
//			imgList.add(aoi);
//		}
//		op.setOcrImages(imgList);
//
//	}

//	@Test
//	public void createUrlBasedProcess() throws MalformedURLException,
//			URISyntaxException {
//		logger.info("This test uses http Urls, this should break wrong usageg of java.io.File.");
//		AbbyyServerOCREngine ase = new AbbyyServerOCREngine(new Properties());
//		assertNotNull(ase);
//		OCRProcess op = ase.newOcrProcess();
//		List<OCRImage> imgList = new ArrayList<OCRImage>();
//		for (int i = 0; i < 10; i++) {
//			OCRImage ocri = mock(OCRImage.class);
//			String imageUrl = "http://127.0.0.1:8080/image-" + i;
//			when(ocri.getUri()).thenReturn(new URI(imageUrl));
//			logger.debug("Added url to list: " + imageUrl);
//			imgList.add(ocri);
//		}
//		op.setOcrImages(imgList);
//	}
}
