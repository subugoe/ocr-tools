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
import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyImage;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyProcess;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrImage;
import de.uni_goettingen.sub.commons.ocr.api.OcrOutput;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;

public class AbbyyServerOCREngineTest {
	final static Logger logger = LoggerFactory.getLogger(AbbyyServerOCREngineTest.class);

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
		MyServers.startAbbyySimulator();
	}
	
	@After
	public void tearDown() {
		MyServers.stopAbbyySimulator();
	}
	
	@Test
	public void getInstance() {
		AbbyyEngine engine = new AbbyyEngine();
		assertNotNull(engine);
	}
	
//	@Test
//	public void newImage() {
//		AbbyyEngine engine = new AbbyyEngine(new Properties());
//		OcrImage image = engine.newOcrImage(null);
//		assertTrue(image instanceof AbbyyImage);
//	}
	
//	@Test
//	public void newProcess() {
//		AbbyyEngine engine = new AbbyyEngine(new Properties());
//		OcrProcess process = engine.newOcrProcess();
//		assertTrue(process instanceof AbbyyProcess);
//	}
//	
//	@Test
//	public void newOutput() {
//		AbbyyEngine engine = new AbbyyEngine(new Properties());
//		OcrOutput output = engine.newOcrOutput();
//		assertTrue(output instanceof AbbyyOutput);
//	}
		
//	@Test(expected=IllegalStateException.class)
//	public void recognizeNoServer() throws Exception {
//		MyServers.stopDavServer();
//		try {
//			AbbyyEngine engine = new AbbyyEngine(new Properties());
//			OcrProcess process = engine.newOcrProcess();
//			engine.recognize(process);
//		} finally {
//			MyServers.startDavServer();
//		}
//	}
//	
//	@Test(expected=IllegalStateException.class)
//	public void recognizeEmptyProcess() {
//			AbbyyEngine engine = new AbbyyEngine(new Properties());
//			OcrProcess process = engine.newOcrProcess();
//			engine.recognize(process);
//	}
	
//	@Test(expected=ConcurrentModificationException.class)
//	public void lockExists() throws IOException {
//		File lock = new File(DAV_FOLDER, ConfigParser.SERVER_LOCK_FILE_NAME);
//		lock.createNewFile();
//		
//		try {
//			AbbyyEngine engine = new AbbyyEngine(new Properties());
//			recognizeOneImage(engine);
//		} finally {
//			lock.delete();
//		}
//	}
//	
//	@Test
//	public void overwriteLock() throws IOException {
//		File lock = new File(DAV_FOLDER, ConfigParser.SERVER_LOCK_FILE_NAME);
//		lock.createNewFile();
//		
//		Properties props = new Properties();
//		props.setProperty("lock.overwrite", "true");
//		AbbyyEngine engine = new AbbyyEngine(props);
//
//		recognizeOneImage(engine);
//		assertFalse(lock.exists());
//	}
//	
//	private void recognizeOneImage(OcrEngine engine) {		
//		OcrProcess process = engine.newOcrProcess();
//		File inputBook = new File(LOCAL_INPUT, "oneImageBook");
//		String jobName = inputBook.getName();
//		process.setName(jobName);
//		
//		URI imageUri = new File(inputBook, "00000001.tif").toURI();
//		OcrImage image = engine.newOcrImage(imageUri);
//		List<OcrImage> images = new ArrayList<OcrImage>();
//		images.add(image);
//		process.setOcrImages(images);
//
//		OcrFormat format = OcrFormat.TXT;
//		OcrOutput output = engine.newOcrOutput();
//		File outputFile = new File(LOCAL_OUTPUT, "oneImageBook.txt");
//		URI outputUri = outputFile.toURI();
//		output.setUri(outputUri);
//		process.addOutput(format, output);
//
//		engine.addOcrProcess(process);
//		engine.recognize();
//	}
	

}
