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

	// The configuration.
	protected ConfigParser config;

	/** The hotfolder. */
	protected Hotfolder hotfolder;

	/** single instance of AbbyyServerEngine. */
	private static AbbyyServerEngine _instance;

	// State variables
	// The total file count.
	protected static Long totalFileCount = 0l;

	// The total file size.
	protected static Long totalFileSize = 0l;

	// internal tweaking variables
	// Variables used for process management
	// The max size, default is currently 50 MB
	protected static Long maxSize = 1024l * 1024 * 50l;

	// The max files, default is currently 5000 files
	protected static Long maxFiles = 5000l;

	// The check server state.
	protected static Boolean checkServerState = true;

	// The directories as process
	protected List<OCRProcess> ocrProcess = new ArrayList<OCRProcess>();

	protected Boolean started = false;

	// OCR Processes
	Queue<AbbyyProcess> processes = new ConcurrentLinkedQueue<AbbyyProcess>();

	/**
	 * Instantiates a new abbyy server engine.
	 * 
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	public AbbyyServerEngine() throws FileSystemException, ConfigurationException {
		config = new ConfigParser().loadConfig();
		hotfolder = new Hotfolder(config);
		
		maxSize = config.getMaxSize();
		maxFiles = config.getMaxFiles();
		maxThreads = config.getMaxThreads();
		checkServerState = config.getCheckServerState();

		//TODO: remove this
		hotfolder.setErrorFolder(config.errorFolder);
		hotfolder.setInputFolder(config.inputFolder);
		hotfolder.setOutputFolder(config.outputFolder);
		hotfolder.setServerURL(config.getServerURL());

	}

	/**
	 * Start the threadPooling
	 * 
	 */
	public void start () {
		if (checkServerState) {
			try {
				checkServerState();
			} catch (IOException e) {
				logger.error("got IOException during processing", e);
				throw new OCRException(e);
			}
		}

		ExecutorService pool = new OCRExecuter(maxThreads);

		//TODO: Check if this can be just one loop
		for (OCRProcess process : getOcrProcess()) {
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
	//TODO: this should be part of the Hotfolder.
	//TODO: There is a problem in her somewhere in here, a Malformed URL Exception is thrown
	@SuppressWarnings("serial")
	public void checkServerState () throws IOException {
		if (maxSize != 0 && maxFiles != 0) {

			// check if a slash is already appended
			final URL serverUrl = new URL(config.getServerURL());
			Map<URL, Long> sizeMap = new LinkedHashMap<URL, Long>() {
				{
					//TODO: There is an error in here
					put(new URL(serverUrl.toString() + config.getInputFolder() + "/"), 0l);
					put(new URL(serverUrl.toString() + config.getOutoutFolder() + "/"), 0l);
					put(new URL(serverUrl.toString() + config.getErrorFolder() + "/"), 0l);
				}
			};

			for (URL url : sizeMap.keySet()) {
				sizeMap.put(url, hotfolder.getTotalSize(url));
			}
			totalFileCount = Integer.valueOf(sizeMap.size()).longValue();
			for (Long size : sizeMap.values()) {
				if (size != null) {
					totalFileSize += size;
				}
			}
			logger.debug("TotalFileSize = " + totalFileSize);

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
		//TODO: Check if this instanceof works as expected	
		if (process instanceof AbbyyProcess) {
			processes.add((AbbyyProcess) process);
		}
		if (!started) {
			start();
		}
		//TODO: Get an Observer from somewhere, probably use a Future
		return null;
	}

}
