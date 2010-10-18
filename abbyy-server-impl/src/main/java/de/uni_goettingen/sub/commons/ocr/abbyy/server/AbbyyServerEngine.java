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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.vfs.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

/**
 * The Class AbbyyServerEngine.
 */
public class AbbyyServerEngine implements OCREngine {

	/** The max threads. */
	protected static Integer maxThreads = 5;
	// protected ExecutorService pool = new OCRExecuter(maxThreads);
	/** The Constant logger. */
	final static Logger logger = LoggerFactory.getLogger(AbbyyServerEngine.class);

	/** The config. */
	//Configuration config ;
	ConfigParser config;

	/** The process. */
	//protected AbbyyProcess process;

	/** The hotfolder. */
	protected Hotfolder hotfolder;

	/** single instance of AbbyyServerEngine. */
	private static AbbyyServerEngine _instance;

	/** The EXTENSION. */
	protected static String extension = "tif";
	// Server information
	/** The webdav url. */
	protected static String webdavURL = null;

	/** The webdav username. */
	protected static String webdavUsername = null;

	/** The webdav password. */
	protected static String webdavPassword = null;
	// public String defaultConfig = "config-properties";
	// State variables
	/** The total file count. */
	protected static Long totalFileCount = 0l;

	/** The total file size. */
	protected static Long totalFileSize = 0l;
	// Folders
	/** The input folder. */
	protected static String inputFolder = null;

	/** The output folder. */
	protected static String outputFolder = null;

	/** The error folder. */
	protected static String errorFolder = null;

	// internal tweaking variables
	// Variables used for process management
	/** The max size. */
	protected static Long maxSize = 5368709120l;

	/** The max files. */
	protected static Long maxFiles = 5000l;

	/** The check server state. */
	protected static Boolean checkServerState = true;

	/** The local output dir. */
	protected static String localOutputDir = null;

	/** The directories as process */
	protected List<OCRProcess> ocrProcess = new ArrayList<OCRProcess>();

	//AbbyyProcess ocrp ;

	// OCR Processes
	/** The processes. */
	Queue<AbbyyProcess> processes = new ConcurrentLinkedQueue<AbbyyProcess>();

	/**
	 * Instantiates a new abbyy server engine.
	 * 
	 * @throws FileSystemException
	 *             the file system exception
	 * 
	 */
	public AbbyyServerEngine() throws FileSystemException, ConfigurationException {
		hotfolder = new Hotfolder();
		config = new ConfigParser().loadConfig();
		
		//TODO: remove this
		webdavURL = ConfigParser.serverURL;
		webdavUsername = config.username;
		webdavPassword = config.password;
		inputFolder = config.inputFolder;
		outputFolder = config.outputFolder;
		errorFolder = config.errorFolder;

		maxSize = config.maxSize;
		maxFiles = config.maxFiles;
		maxThreads = config.maxThreads;
		checkServerState = config.checkServerState;

		hotfolder.setErrorFolder(errorFolder);
		hotfolder.setInputFolder(inputFolder);
		hotfolder.setOutputFolder(outputFolder);
		hotfolder.setWebdavURL(webdavURL);

	}

	/**
	 * API Start
	 */
	public void recognize () {
		try {
			start();
		} catch (FileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Start the threadPooling
	 * 
	 * @throws RuntimeException
	 *             the runtime exception
	 * @throws FileSystemException
	 *             the file system exception
	 */
	public void start () throws RuntimeException, FileSystemException {
		if (checkServerState) {
			try {
				checkServerState();
			} catch (Exception e) {
				logger.error(e.toString());
			}
		}

		ExecutorService pool = new OCRExecuter(maxThreads);

		for (OCRProcess process : getOcrProcess()) {
			//	AbbyyProcess process = new AbbyyProcess(dir);
			processes.add((AbbyyProcess) process);
		}

		for (OCRProcess proces : processes) {
			pool.execute((Runnable) proces);
		}

		pool.shutdown();
		try {

			pool.awaitTermination(3600, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Got a problem with thread pool: ", e);
		}

	}

	/**
	 * Gets the single instance of AbbyyServerEngine.
	 * 
	 * @return single instance of AbbyyServerEngine
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws ConfigurationException
	 *             the configuration exception
	 * 
	 */

	public static AbbyyServerEngine getInstance () {

		if (_instance == null) {
			try {
				_instance = new AbbyyServerEngine();
			} catch (FileSystemException e) {
				logger.error("Can't get file system", e);
				throw new OCRException(e);
			} catch (ConfigurationException e) {
				logger.error("Can't read configuration", e);
				throw new OCRException(e);
			}
		}
		return _instance;
	}

	/**
	 * Check server state. check all three folders since the limits are for the
	 * whole system.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void checkServerState () throws IOException {
		if (maxSize != 0 && maxFiles != 0) {
			List<URL> urls = new ArrayList<URL>();
			// check if a slash is already appended
			String input_uri = webdavURL + inputFolder + "/";
			String output_uri = webdavURL + outputFolder + "/";
			String error_uri = webdavURL + errorFolder + "/";

			File inputfile = new File(input_uri);
			input_uri = inputfile.getAbsolutePath();

			File outputfile = new File(output_uri);
			output_uri = outputfile.getAbsolutePath();

			File errorfile = new File(error_uri);
			error_uri = errorfile.getAbsolutePath();

			urls.add(new URL(input_uri));
			urls.add(new URL(output_uri));
			urls.add(new URL(error_uri));

			Map<URL, Long> infoMap = new LinkedHashMap<URL, Long>();
			for (URL uri : urls) {
				infoMap.put(uri, hotfolder.getTotalSize(uri));
			}
			totalFileCount = new Integer(infoMap.size()).longValue();
			for (Long size : infoMap.values()) {
				if (size != null) {
					totalFileSize += size;
				}
			}
			System.out.println("TotalFileSize = " + totalFileSize);
			logger.trace("TotalFileSize = " + totalFileSize);
			if (maxFiles != 0 && totalFileCount > maxFiles) {
				logger.error("Too much files. Max number of files is " + maxFiles + ". Number of files on server: " + totalFileCount + ".\nExit program.");
				throw new IllegalStateException("Max number of files exeded");
			}
			if (maxSize != 0 && totalFileSize > maxSize) {
				logger.error("Size of files is too much files. Max size of all files is " + maxSize
						+ ". Size of files on server: "
						+ totalFileSize
						+ ".\nExit program.");
				throw new IllegalStateException("Max size of files exeded");
			}
		} else {
			logger.warn("Server state checking is disabled.");
		}
	}

	public List<OCRProcess> getOcrProcess () {
		return ocrProcess;
	}

	public void addOcrProcess (OCRProcess ocrp) {
		this.ocrProcess.add(ocrp);
	}

	@Override
	public OCRImage newImage () {
		return new AbbyyOCRImage();
	}

	@Override
	public OCRProcess newProcess () {
		return new AbbyyProcess();
	}

	@Override
	public Observer recognize (OCRProcess process) {
		// TODO Auto-generated method stub
		return null;
	}

}
