package de.uni_goettingen.sub.commons.ocr.abbyy.sever;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.vfs.FileSystemException;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRImage;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerEngine;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.unigoettingen.sub.commons.util.file.FileExtensionsFilter;

public class AbbyyServerEngineTest {
	public static OCREngine abbyy;
	public Hotfolder hotfolder;
	protected static String extension = "tif";
	protected List<File> directories = new ArrayList<File>();
	private static List<File> inputFiles = new ArrayList<File>();
	final static Logger logger = LoggerFactory.getLogger(AbbyyServerEngineTest.class);

	@Before
	public void init () throws FileSystemException, ConfigurationException, URISyntaxException {
		logger.debug("Starting Test");
		ConfigParser config = new ConfigParser();
		logger.debug(config.getWebdavURL());
		URI uri = new URI(config.getWebdavURL());

		assertNotNull(uri);
		abbyy = AbbyyServerEngine.getInstance();
		assertNotNull(abbyy);

	}

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
		hotfolder = new Hotfolder();

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
		hotfolder.mkCol(hotfolder.fileToURL(new File(hotfolderError)));
		for (File currentFile : filess) {
			String currentFileString = currentFile.getName();
			if (!currentFileString.startsWith(".")) {
				hotfolder.copyAllFiles(currentFile.getAbsolutePath(), hotfolderError + "/" + currentFile.getName());
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
				hotfolder.copyAllFiles(currentFiles.getAbsolutePath(), hotfolderOutput + "/" + currentFiles.getName());
			} else {
				System.out.println("meine liste file start with " + currentFiles.getName());
			}
		}

		//Look for folders containing tif files in ./src/test/resources/local/ as listFolders
		//Add a static method for this.
		inputfile = parseString(inputfile);
		File inputfilepath = new File(inputfile);
		listFolders = getImageDirectories(new File(inputfilepath.getAbsolutePath()));
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
			fileListimage = makeFileList(files, extension);
			System.out.println(fileListimage);
			OCRProcess p = abbyy.newProcess();
			p.setName(files.getName());
			for (File fileImage : fileListimage) {
				OCRImage image = abbyy.newImage();
				//	System.out.println("fehler "+ fileImage.getAbsolutePath());

				image.setUrl(hotfolder.fileToURL(fileImage));
				p.addImage(image);
			}
			//p.setImageDirectory(files.getAbsolutePath());
			abbyy.addOcrProcess(p);

			fileListimage = null;
		}
		logger.info("Strating recognize method");
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

	public static List<File> getImageDirectories (File dir) {
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
					for (File f : files) {
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

	public static List<File> makeFileList (File dir, String filter) {
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
	
	@Test
	public void testMultipleTickets () throws IOException, ConfigurationException, XmlException {
		List<String> inputFile = new ArrayList<String>();
		List<String> imageInput = new ArrayList<String>();
		String inputfile = "file://./src/test/resources/input";
		String inputhotfolder = "file://./src/test/resources/hotfolder/input";
		String reportSuffix = ".xml";

		List<File> listFolders = new ArrayList<File>();
		hotfolder = new Hotfolder();

		inputfile = AbbyyServerEngineTest.parseString(inputfile);
		File inputfilepath = new File(inputfile);
		listFolders = AbbyyServerEngineTest.getImageDirectories(new File(inputfilepath.getAbsolutePath()));
		//Loop over listFolder to get the files, create OCR Images and add them to the process
		List<File> directories = new ArrayList<File>();

		for (File file : listFolders) {
			if (file.isDirectory()) {
				directories.add(file);
			} else {
				logger.trace(file.getAbsolutePath() + " is not a directory!");
			}
		}

		List<File> fileListimage = null;
		for (File files : directories) {
			fileListimage = AbbyyServerEngineTest.makeFileList(files, extension);
			OCRProcess p = abbyy.newProcess();
			p.setName(files.getName());
			for (File fileImage : fileListimage) {
				AbbyyOCRImage image = (AbbyyOCRImage) abbyy.newImage();
				logger.debug("File list contains " + fileImage.getAbsolutePath());
				image.setUrl(hotfolder.fileToURL(fileImage));
				p.addImage(image);
			}
			//p.setImageDirectory(files.getAbsolutePath());
			abbyy.addOcrProcess(p);

		}

		abbyy.recognize();
		for (File filelist : fileListimage) {
			String folderName = filelist.getName();
			folderName = inputhotfolder + "/" + folderName + "/" + folderName + reportSuffix;
			inputFile = TicketTest.parseFilesFromTicket(new File(folderName));
			File folder = new File(inputfile);
			File[] inputfiles = folder.listFiles();
			for (File currentFile : inputfiles) {
				imageInput.add(currentFile.getName());
			}

			assertTrue(inputFile == imageInput);
		}
		//check for results
		assertNotNull(abbyy);

	}
}
