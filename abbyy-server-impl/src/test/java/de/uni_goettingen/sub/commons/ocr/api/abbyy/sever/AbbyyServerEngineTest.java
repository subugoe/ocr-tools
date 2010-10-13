package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;

import static org.junit.Assert.assertNotNull;




import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import org.apache.commons.configuration.ConfigurationException;

import org.apache.commons.vfs.FileSystemException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	//	List <String> inputFile = new ArrayList<String>();
		String inputfile= "file://./src/test/resources/local";
		List<File> listFolders = new ArrayList<File>();
		hotfolder = new Hotfolder();
		//TODO: This is just an example, it doesn't work!
		
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
		System.out.println(directories);
		List<File> fileListimage;
		for (File files : directories){
			fileListimage = makeFileList(files, extension);
			System.out.println(fileListimage);
			OCRProcess p = abbyy.newProcess();
			p.setName(files.getName());
			for (File fileImage : fileListimage){
				OCRImage image = abbyy.newImage();
				System.out.println("fehler "+ fileImage.getAbsolutePath());
				
				image.setUrl(hotfolder.fileToURL(fileImage));
				p.addImage(image);
			}
			abbyy.addOcrProcess(p);
			fileListimage = null;
		}
		
		abbyy.recognize();
		
		//check for results
		assertNotNull(abbyy);

	}

	public static String parseString(String str){
		String remoteFile = null;
		if (str.contains("/./")) {
			int i = 0;
			for (String lang : Arrays.asList(str.split("/./"))) {
				if (i == 0){
					i++;
				}else{
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
}
