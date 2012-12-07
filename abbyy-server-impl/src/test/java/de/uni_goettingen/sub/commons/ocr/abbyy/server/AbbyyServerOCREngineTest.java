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
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

public class AbbyyServerOCREngineTest {
	public static OCREngine abbyy;
	public Hotfolder hotfolder;
	protected List<File> directories = new ArrayList<File>();
	final static Logger logger = LoggerFactory.getLogger(AbbyyServerOCREngineTest.class);
	protected static AbbyyServerSimulator ass = null;

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
		AbbyyServerOCREngine engine = AbbyyServerOCREngine.newOCREngine();
		assertNotNull(engine);
	}
	
	@Test
	public void newImage() {
		AbbyyServerOCREngine engine = AbbyyServerOCREngine.newOCREngine();
		OCRImage image = engine.newOcrImage(null);
		assertTrue(image instanceof AbbyyOCRImage);
	}
	
	@Test
	public void newProcess() {
		AbbyyServerOCREngine engine = AbbyyServerOCREngine.newOCREngine();
		OCRProcess process = engine.newOcrProcess();
		assertTrue(process instanceof AbbyyOCRProcess);
	}
	
	@Test
	public void newOutput() {
		AbbyyServerOCREngine engine = AbbyyServerOCREngine.newOCREngine();
		OCROutput output = engine.newOcrOutput();
		assertTrue(output instanceof AbbyyOCROutput);
	}
	
	@Test(expected=IllegalStateException.class)
	public void recognizeNoProcesses() {
		AbbyyServerOCREngine engine = AbbyyServerOCREngine.newOCREngine();
		engine.recognize();
	}
	
	@Test(expected=IllegalStateException.class)
	public void recognizeNoServer() throws Exception {
		MyServers.stopDavServer();
		try {
			AbbyyServerOCREngine engine = AbbyyServerOCREngine.newOCREngine();
			OCRProcess process = engine.newOcrProcess();
			engine.recognize(process);
		} finally {
			MyServers.startDavServer();
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void recognizeEmptyProcess() {
			AbbyyServerOCREngine engine = AbbyyServerOCREngine.newOCREngine();
			OCRProcess process = engine.newOcrProcess();
			engine.recognize(process);
	}
	
	@Test(expected=ConcurrentModificationException.class)
	public void lockExists() throws IOException {
		File lock = new File(DAV_FOLDER, ConfigParser.SERVER_LOCK_FILE_NAME);
		lock.createNewFile();
		
		try {
			AbbyyServerOCREngine engine = AbbyyServerOCREngine.newOCREngine();
			recognizeOneImage(engine);
		} finally {
			lock.delete();
		}
	}
	
	@Test
	public void overwriteLock() throws IOException {
		File lock = new File(DAV_FOLDER, ConfigParser.SERVER_LOCK_FILE_NAME);
		lock.createNewFile();
		
		AbbyyServerOCREngine engine = AbbyyServerOCREngine.newOCREngine();

		Map<String, String> opts = new HashMap<String, String>();
		opts.put("lock.overwrite", "true");
		engine.setOptions(opts);
		recognizeOneImage(engine);
		assertFalse(lock.exists());
	}
	
	private void recognizeOneImage(OCREngine engine) {		
		OCRProcess process = AbbyyServerOCREngine.createProcessFromDir(
				new File(LOCAL_INPUT, "oneImageBook"), "tif");
		OCRFormat format = OCRFormat.TXT;
		OCROutput output = engine.newOcrOutput();
		File outputFile = new File(LOCAL_OUTPUT, "oneImageBook.txt");
		URI outputUri = outputFile.toURI();
		output.setUri(outputUri);
		process.addOutput(format, output);

		engine.addOcrProcess(process);
		engine.recognize();
	}
	

}
