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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRImage;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRProcess;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.unigoettingen.sub.commons.ocr.util.OCRUtil;
import de.unigoettingen.sub.commons.util.stream.StreamUtils;

public class AbbyyOCRProcessTest {
	final static Logger logger = LoggerFactory
			.getLogger(AbbyyOCRProcessTest.class);

	public static List<String> testFolders;
	protected static String extension = "tif";
	private static HashMap<OCRFormat, OCROutput> outputs;
	private static ConfigParser config;

	static {
		testFolders = new ArrayList<String>();
		testFolders.add("PPN129323640_0010");
		testFolders.add("PPN31311157X_0102");
		testFolders.add("PPN514401303_1890");
		testFolders.add("PPN514854804_0001");

		URI resultUri = null;
		try {
			resultUri = new URI(RESOURCES.toURI() + "/target/results/"
					+ "result");
		} catch (URISyntaxException e) {
			throw new ExceptionInInitializerError(e);
		}

		final AbbyyOCROutput aoo = new AbbyyOCROutput(resultUri);
		aoo.setRemoteFilename("result");

		outputs = new HashMap<OCRFormat, OCROutput>();
		outputs.put(OCRFormat.XML, aoo);

	}
	
	@BeforeClass
	public static void initBeforeClass() throws Exception {
		config = new ConfigParser().parse();
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
	
	@Test
	public void newAbbyyProcess() {
		AbbyyOCRProcess aop = (AbbyyOCRProcess) AbbyyServerOCREngine
				.getInstance().newOcrProcess();
		assertNotNull(aop);
	}

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

	public void runProcessInThread(String jobName, boolean split) throws IOException, InterruptedException {
		AbbyyOCRProcess process = new AbbyyOCRProcess(config);
		process.setName(jobName);
		process.setSplitProcess(split);
		
		File bookDir = new File(LOCAL_INPUT, jobName);

		File[] imageFiles = bookDir.listFiles();
		for (File imageFile : imageFiles) {
			AbbyyOCRImage image = new AbbyyOCRImage(imageFile.toURI());
			process.addImage(image);
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

	@Test
	public void createProcessViaAPI() throws MalformedURLException,
			URISyntaxException {
		AbbyyServerOCREngine ase = AbbyyServerOCREngine.getInstance();
		assertNotNull(ase);
		OCRProcess op = ase.newOcrProcess();
		List<OCRImage> imgList = new ArrayList<OCRImage>();
		for (int i = 0; i < 10; i++) {
			OCRImage ocri = mock(OCRImage.class);
			String imageUrl = RESOURCES.toURI().toURL().toString() + i;
			when(ocri.getUri()).thenReturn(new URI(imageUrl));
			logger.debug("Added url to list: " + imageUrl);
			AbbyyOCRImage aoi = new AbbyyOCRImage(ocri);
			assertTrue(imageUrl.equals(aoi.getUri().toString()));
			aoi.setRemoteFileName("remoteName" + i);
			imgList.add(aoi);
		}
		op.setOcrImages(imgList);

	}

	@Test
	public void createUrlBasedProcess() throws MalformedURLException,
			URISyntaxException {
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
}
