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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.vfs.FileSystemException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRProcess;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerOCREngine;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.unigoettingen.sub.commons.util.stream.StreamUtils;

public class AbbyyServerOCREngineTest {
	public static OCREngine abbyy;
	public Hotfolder hotfolder;
	protected List<File> directories = new ArrayList<File>();
	final static Logger logger = LoggerFactory.getLogger(AbbyyServerOCREngineTest.class);
	protected static AbbyyServerSimulator ass = null;

	@Before
	public void init () throws FileSystemException, ConfigurationException, URISyntaxException {
		logger.debug("Starting Test");
		ConfigParser config = new ConfigParser().loadConfig();

		logger.debug("Server URL is " + config.getServerURL());
		URI uri = new URI(config.getServerURL());

		assertNotNull(uri);

		ass = new AbbyyServerSimulator(HotfolderTest.TEST_HOTFOLDER_FILE, HotfolderTest.TEST_EXPECTED_FILE);
		ass.start();
	}

	@Test
	public void checkThread () throws InterruptedException {
		Thread.sleep(1000);
		logger.debug("Checking for Thread");
		assertTrue("Thread is dead", ass.isAlive());
	}

	@Test
	public void testRecognize () throws IOException {
		AbbyyServerOCREngine ase = AbbyyServerOCREngine.getInstance();
		assertNotNull(ase);
		

		for (String book : AbbyyOCRProcessTest.TEST_FOLDERS) {
			File testDir = new File(AbbyyOCRProcessTest.BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book);
			logger.debug("Creating AbbyyOCRProcess for " + testDir.getAbsolutePath());
			AbbyyOCRProcess aop = AbbyyServerOCREngine.createProcessFromDir(testDir, AbbyyTicketTest.EXTENSION);
			assertNotNull(aop);
			aop.setOcrOutput(AbbyyTicketTest.OUTPUT_DEFINITIONS);
			//TODO: set the inout folder to new File(hotfolder.getAbsolutePath() + File.separator + INPUT_NAME);
			File testTicket = new File(AbbyyOCRProcessTest.BASEFOLDER_FILE.getAbsoluteFile() + File.separator
					+ HotfolderTest.INPUT
					+ File.separator
					+ book
					+ ".xml");
			aop.write(testTicket, testDir.getName());
			logger.debug("Wrote AbbyyTicket:\n" + StreamUtils.dumpInputStream(new FileInputStream(testTicket)));
			logger.debug("Starting Engine");
			ase.recognize(aop);
		}
		//TODO: Test if process failed

	}

	@Ignore
	@Test
	public void testCli () throws IOException, ConfigurationException {
		//TODO: Move this to a @Before class, start a thread for the hotfolder
		//and just use recognize as test

		//TODO: Extract the variables to be reused in other tests as well.
		//	List <String> inputFile = new ArrayList<String>();
		String inputfile = "file://./src/test/resources/input";

		String errorfolderResult = "file://./src/test/resources/error/PPN129323640_0010";
		String hotfolderError = "file://./src/test/resources/hotfolder/error/";

		String resultFolder = "file://./src/test/resources/result";
		String hotfolderOutput = "file://./src/test/resources/hotfolder/output";

		List<File> listFolders = new ArrayList<File>();
		hotfolder = new Hotfolder(new ConfigParser());

		// copy all files from  errorfolderResult to hotfolderError 
		errorfolderResult = parseString(errorfolderResult);
		hotfolderError = parseString(hotfolderError);
		File errorfolderResultpath = new File(errorfolderResult);
		File hotfolderErrorpath = new File(hotfolderError);
		errorfolderResult = errorfolderResultpath.getAbsolutePath();

		errorfolderResultpath = new File(errorfolderResult);
		hotfolderError = hotfolderErrorpath.getAbsolutePath() + "/" + errorfolderResultpath.getName();
		File[] filess = errorfolderResultpath.listFiles();
		assertNotNull(filess);
		hotfolder.mkDir(new File(hotfolderError).toURI().toURL());
		for (File currentFile : filess) {
			String currentFileString = currentFile.getName();
			if (!currentFileString.startsWith(".")) {
				hotfolder.copyFile(currentFile.getAbsolutePath(), hotfolderError + "/" + currentFile.getName());
			} else {
				System.out.println("meine liste file start with " + currentFile.getName());
			}
		}

		// copy all files from  folder move to hotfolder output 	
		resultFolder = parseString(resultFolder);
		hotfolderOutput = parseString(hotfolderOutput);
		File moveFolderpath = new File(resultFolder);
		File hotfolderOutputpath = new File(hotfolderOutput);
		resultFolder = moveFolderpath.getAbsolutePath();

		moveFolderpath = new File(resultFolder);
		hotfolderOutput = hotfolderOutputpath.getAbsolutePath() + "/";
		File[] folder = moveFolderpath.listFiles();
		for (File currentFiles : folder) {
			String currentFilesString = currentFiles.getName();
			if (!currentFilesString.startsWith(".")) {
				hotfolder.copyFile(currentFiles.getAbsolutePath(), hotfolderOutput + "/" + currentFiles.getName());
			} else {
				System.out.println("meine liste file start with " + currentFiles.getName());
			}
		}

		//Look for folders containing tif files in ./src/test/resources/local/ as listFolders
		//Add a static method for this.
		inputfile = parseString(inputfile);
		//File inputfilepath = new File(inputfile);
		listFolders = null; // getImageDirectories(new File(inputfilepath.getAbsolutePath()));
		//Loop over listFolder to get the files, create OCR Images and add them to the process

		for (File file : listFolders) {
			if (file.isDirectory()) {
				directories.add(file);
			} else {
				logger.trace(file.getAbsolutePath() + " is not a directory!");
			}
		}

		List<File> fileListimage;
		for (File files : directories) {
			fileListimage = null; // makeFileList(files, extension);
			System.out.println(fileListimage);
			OCRProcess p = abbyy.newProcess();
			p.setName(files.getName());
			for (File fileImage : fileListimage) {
				OCRImage image = abbyy.newImage();
				//	System.out.println("fehler "+ fileImage.getAbsolutePath());

				image.setUrl(fileImage.toURI().toURL());
				p.addImage(image);
			}
			//p.setImageDirectory(files.getAbsolutePath());
			abbyy.addOcrProcess(p);

			fileListimage = null;
		}
		logger.info("Starting recognize method");
		//abbyy.recognize();

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

	@Ignore
	@Test
	public void checkDirectories () {
		String inputfile = "file://./src/test/resources/input";
		File inputDir = new File(inputfile);
		for (File f : Arrays.asList(inputDir.listFiles())) {
			if (f.isDirectory()) {
				//There shouldn't be any directories inside the hotfolder
				assertTrue(false);
			}
		}

	}

	@After
	public void stop () throws InterruptedException {
		ass.interrupt();
		ass.join();
	}

}
