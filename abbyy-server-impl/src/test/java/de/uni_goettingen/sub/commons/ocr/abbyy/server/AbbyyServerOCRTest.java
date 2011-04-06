package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import org.apache.commons.vfs.FileSystemException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRImage;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCROutput;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRProcess;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerOCREngine;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextTyp;
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

public class AbbyyServerOCRTest {
	protected static String extension = "tif";
	public static OCREngine abbyy;
	public static String OUTPUT_LOCATION = "D:\\Recognition\\GDZ\\output";
	public Hotfolder hotfolder;
	protected List<File> directories = new ArrayList<File>();
	public static HashMap<OCRFormat, OCROutput> OUTPUT_DEFINITIONS;
	final static Logger logger = LoggerFactory.getLogger(AbbyyServerOCRTest.class);
	
//	protected static AbbyyServerSimulator ass = null;

	@Before
	public void init () throws FileSystemException, ConfigurationException, URISyntaxException {
		logger.debug("Starting Test");
		ConfigParser config = new ConfigParser().parse();
//		config.setConfigUrl(new URL());
		logger.debug("Server URL is " + config.getServerURL());
		URI uri = new URI(config.getServerURL());
		
		assertNotNull(uri);

//		ass = new AbbyyServerSimulator(HotfolderTest.TEST_HOTFOLDER_FILE, HotfolderTest.TEST_EXPECTED_FILE);
//		ass.start();
	}
	
	@Ignore  
	@Test
	public void testCli () throws IOException, ConfigurationException, URISyntaxException {
		AbbyyServerOCREngine abbyy = AbbyyServerOCREngine.getInstance();
		assertNotNull(abbyy);

		//	List <String> inputFile = new ArrayList<String>();
		String inputfile = "file://./src/test/resources/input";

		List<File> listFolders = new ArrayList<File>();
	//	hotfolder = ApacheVFSHotfolderImpl.getInstance(new ConfigParser());

		//Look for folders containing tif files in ./src/test/resources/local/ as listFolders
		//Add a static method for this.
		inputfile = parseString(inputfile);
		File inputfilepath = new File(inputfile);
		listFolders = getImageDirectories(new File(inputfilepath.getAbsolutePath()));
		//Loop over listFolder to get the files, create OCR Images and add them to the process
		System.out.println(listFolders);
		for (File file : listFolders) {
			if (file.isDirectory()) {
				directories.add(file);
			} else {
				logger.trace(file.getAbsolutePath() + " is not a directory!");
			}
		}
		OCROutput oo = abbyy.newOcrOutput();
			//new AbbyyOCROutput();
		AbbyyOCROutput aoo = (AbbyyOCROutput) oo;
		//aoo.setUri(new URI(AbbyyTicketTest.BASEFOLDER_FILE.getAbsolutePath()+"/"+))
		aoo.setRemoteLocation(OUTPUT_LOCATION);
		Map<OCRFormat, OCROutput> results = new HashMap<OCRFormat, OCROutput>();
		results.put(OCRFormat.PDF, aoo);
		results.put(OCRFormat.XML, aoo);
//		results.put(OCRFormat.TXT, aoo);
	
		List<File> fileListimage;
		
		for (File files : directories) {
			Set<Locale> langs = new HashSet<Locale>();
			List<OCRImage> imgs = new ArrayList<OCRImage>(); 
			fileListimage =  makeFileList(files, extension);
			OCRProcess p = abbyy.newOcrProcess();
			p.setName(files.getName());
			for (File fileImage : fileListimage) {
				OCRImage image = abbyy.newOcrImage(fileImage.toURI());
				//	System.out.println("fehler "+ fileImage.getAbsolutePath());

				image.setUri(fileImage.toURI());
				imgs.add(image);
			}
		//	langs.add(new Locale("en"));
			langs.add(new Locale("de"));
			p.setLanguages(langs);
			p.setOcrImages(imgs);
			p.setOcrOutputs(results);
			//p.setImageDirectory(files.getAbsolutePath());
			abbyy.addOcrProcess(p);

			fileListimage = null;
		}
		logger.info("Starting recognize method");
		abbyy.recognize();

		//check for results
		assertNotNull(abbyy);

	}

	public static String parseString (String str) {
		String remoteFile = null;
		if (str.contains("/./")) {
			int i = 0;
			for (String lang : Arrays.asList(str.split("/./"))) {
				if (i == 0) {
					i++;
				} else {
					remoteFile = lang;
				}
			}
		}

		return remoteFile;
	}

	
	public static List<File> getImageDirectories(File dir) {
		List<File> dirs = new ArrayList<File>();

		if (makeFileList(dir, extension).size() > 0) {
			dirs.add(dir);
		}

		List<File> fileList;
		if (dir.isDirectory()) {
			fileList = Arrays.asList(dir.listFiles());
			for (File file : fileList) {
				if (file.isDirectory()) {
					List<File> files = makeFileList(dir, extension);
					for (File f: files) {
						logger.debug("File: " + f.getAbsolutePath());
					}
					if (files.size() > 0) {
						dirs.addAll(files);
					} else {
						dirs.addAll(getImageDirectories(file));
					}
				}
			}
		} else {
			throw new IllegalStateException(dir.getAbsolutePath() + " is not a directory");
		}
		return dirs;
	}
	
	public static List<File> makeFileList(File dir, String filter) {
		List<File> fileList;
		if (dir.isDirectory()) {
			// OCR.logger.trace(inputFile + " is a directory");

			File files[] = dir.listFiles(new FileExtensionsFilter(filter));
			fileList = Arrays.asList(files);
			Collections.sort(fileList);

		} else {
			fileList = new ArrayList<File>();
			fileList.add(dir);
			// OCR.logger.trace("Input file: " + inputFile);
		}
		return fileList;
	}
	/*@Ignore
	@Test
	public void checkDirectories () {
		String inputfile = "file://./src/test/resources/input";
		File inputDir = new File(inputfile);
		for (File f : Arrays.asList(inputDir.listFiles())) {
			if (f.isDirectory()) {
				//There shouldn't be any directories inside the apacheVFSHotfolderImpl
				assertTrue(false);
			}
		}

	}*/
	@Ignore 
	@Test
	public void copyTestFilesToServer () throws IOException, URISyntaxException {
		AbbyyServerOCREngine ase = AbbyyServerOCREngine.getInstance();
		//ase.setHotfolder(hotfolder);
		

		for (String book : AbbyyOCRProcessTest.TEST_FOLDERS) {
			File testDir = new File(AbbyyOCRProcessTest.BASEFOLDER_FILE.getAbsoluteFile() + "/" + HotfolderTest.INPUT + "/" + book);
			logger.debug("Creating AbbyyOCRProcess for " + testDir.getAbsolutePath());
			AbbyyOCRProcess aop = AbbyyServerOCREngine.createProcessFromDir(testDir, AbbyyTicketTest.EXTENSION);
			aop.addLanguage(Locale.GERMAN);
			aop.setTextTyp(OCRTextTyp.Normal);
			assertNotNull(aop);
			for (OCRFormat f: AbbyyTicketTest.OUTPUT_DEFINITIONS.keySet()) {
				//Call the copy contructor to get rid of mock objects
				AbbyyOCROutput aoo = new AbbyyOCROutput((AbbyyOCROutput) AbbyyTicketTest.OUTPUT_DEFINITIONS.get(f));
					//(AbbyyOCROutput) AbbyyTicketTest.OUTPUT_DEFINITIONS.get(f);
				URI uri = new URI(AbbyyTicketTest.BASEFOLDER_FILE.toURI() + AbbyyTicketTest. RESULTS + "/" + book + "." + f.toString().toLowerCase());
				aoo.setUri(uri);
				aop.addOutput(f, aoo);
				
			}
			//aop.setOcrOutputs();
			//TODO: set the inout folder to new File(apacheVFSHotfolderImpl.getAbsolutePath() + "/" + INPUT_NAME);
			File testTicket = new File(AbbyyOCRProcessTest.BASEFOLDER_FILE.getAbsoluteFile() + "/"
					+ HotfolderTest.INPUT
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

}
