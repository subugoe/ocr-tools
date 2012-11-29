package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.*;

import it.could.webdav.DAVServlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRImage;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCROutput;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRProcess;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerOCREngine;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.JackrabbitHotfolderImpl;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextType;
import de.unigoettingen.sub.commons.ocr.util.OCRUtil;
import de.unigoettingen.sub.commons.util.file.FileExtensionsFilter;
import de.unigoettingen.sub.commons.util.stream.StreamUtils;

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

public class AbbyyServerOCRTests {

	static Hotfolder imp;
	public static String OUTPUT_LOCATION = "D:\\Recognition\\GDZ\\output";
	private Context rootContext;
	static Server server;
	File filefrom;
	protected List<File> directories = new ArrayList<File>();
	public static HashMap<OCRFormat, OCROutput> OUTPUT_DEFINITIONS;
	final static Logger logger = LoggerFactory.getLogger(AbbyyServerOCRTests.class);
	static File HOTFOLDER_TEST;
//	protected static AbbyyServerSimulator ass = null;

	@Before
	public void init () throws Exception {
		logger.debug("Starting Test");
		filefrom = new File(RESOURCES.getAbsoluteFile()+ "/results");
		HOTFOLDER_TEST = new File(RESOURCES.getAbsoluteFile()+ "/HOTFOLDER_TEST");
		HOTFOLDER_TEST.mkdir();
	    server = new Server(8090);
		ServletHolder davServletHolder = new ServletHolder(new DAVServlet());
		davServletHolder.setInitParameter("rootPath", HOTFOLDER_TEST.toString()
				.replace("file:/", ""));
		rootContext = new Context(server, "/", Context.SESSIONS);
		rootContext.addServlet(davServletHolder, "/*");
		ConfigParser config = new ConfigParser().parse();
		imp = JackrabbitHotfolderImpl.getInstance(config);
		server.start();

//		ass = new AbbyyServerSimulator(HotfolderTest.TEST_HOTFOLDER_FILE, HotfolderTest.TEST_EXPECTED_FILE);
//		ass.start();
	}

	@Test
	public void mkDir() throws IOException, URISyntaxException{
		if(!new File(HOTFOLDER_TEST.toString().replace("file:/", "")+ "/"+ "input").exists()){
			imp.mkDir(new URI("http://localhost:8090/input"));
			assertTrue(new File(HOTFOLDER_TEST.toString().replace("file:/", "")+ "/"+ "input").exists());
		}
		if(!new File(HOTFOLDER_TEST.toString().replace("file:/", "")+ "/"+ "output").exists()){
			imp.mkDir(new URI("http://localhost:8090/output"));
			assertTrue(new File(HOTFOLDER_TEST.toString().replace("file:/", "")+ "/"+ "output").exists());
		}
		if(!new File(HOTFOLDER_TEST.toString().replace("file:/", "")+ "/"+ "error").exists()){
			imp.mkDir(new URI("http://localhost:8090/error"));
			assertTrue(new File(HOTFOLDER_TEST.toString().replace("file:/", "")+ "/"+ "error").exists());
		}
	
	}
	@Ignore	
	@Test
	public void TestServer() throws InterruptedException{
		
		Thread t1 =   new Thread(new AbbyyServerOCR());
		t1.start();
		Thread t2 = new Thread(new AbbyyServerOCRouput());
		t2.start();
		
		
		
		
	}
	

	@Test
	public void copyToHotfolder() throws IOException, URISyntaxException{
		for (String book : AbbyyOCRProcessTest.testFolders){
			for (OCRFormat f: AbbyyTicketTest.OUTPUT_DEFINITIONS.keySet()){
				filefrom = new File(RESOURCES.getAbsoluteFile()+ "/results"+ "/" + book + "." + f.toString().toLowerCase());
				URI from = filefrom.toURI();
				imp.copyFile(from, new URI("http://localhost:8090/output"+ "/" + book + "." + f.toString().toLowerCase()));
			}
			filefrom = new File(RESOURCES.getAbsoluteFile()+ "/results"+ "/" + book + ".xml.result.xml" );
			imp.copyFile(filefrom.toURI(), new URI("http://localhost:8090/output"+ "/" + book + ".xml.result.xml"));
		}
		
	}
	@Ignore
	@Test
	public void copyTestFilesToServer () throws Exception {
		AbbyyServerOCREngine ase = AbbyyServerOCREngine.getInstance();
		//ase.setHotfolder(hotfolder);
		for (String book : AbbyyOCRProcessTest.testFolders) {
			File testDir = new File(RESOURCES.getAbsoluteFile() + "/hotfolder/" + "input" + "/" + book);
			logger.debug("Creating AbbyyOCRProcess for " + testDir.getAbsolutePath());
		//	List<File> imageDirs = OCRUtil.getTargetDirectories(testDir,extension);
			AbbyyOCRProcess aop = AbbyyServerOCREngine.createProcessFromDir(testDir, "tif");
			aop.addLanguage(Locale.GERMAN);
			aop.setTextType(OCRTextType.NORMAL);
			//aop.setTest(false);
			assertNotNull(aop);
			for (OCRFormat f: AbbyyTicketTest.OUTPUT_DEFINITIONS.keySet()) {
				//Call the copy contructor to get rid of mock objects
				AbbyyOCROutput aoo = new AbbyyOCROutput((AbbyyOCROutput) AbbyyTicketTest.OUTPUT_DEFINITIONS.get(f));
					//(AbbyyOCROutput) AbbyyTicketTest.OUTPUT_DEFINITIONS.get(f);
				URI uri = new URI(RESOURCES.toURI() + "LOCAL" + "/" + book + "." + f.toString().toLowerCase());
				aoo.setUri(uri);
				aop.addOutput(f, aoo);
				
			}
			//aop.setOcrOutputs();
			//TODO: set the inout folder to new File(apacheVFSHotfolderImpl.getAbsolutePath() + "/" + INPUT_NAME);
			File testTicket = new File(RESOURCES.getAbsoluteFile() + "/"
					+ "input"
					+ "/"
					+ book
					+ ".xml");
			aop.write(new FileOutputStream(testTicket), testDir.getName());
			logger.debug("Wrote AbbyyTicket:\n" + StreamUtils.dumpInputStream(new FileInputStream(testTicket)));
			logger.debug("Starting Engine");
			ase.addOcrProcess(aop);
		}
		ase.recognize();
		
	}
	
	@After
	public void destroy() throws Exception {
		server.stop();
		logger.debug("Server Stopped");
	}
}
