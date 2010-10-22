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
import java.net.MalformedURLException;
import java.util.List;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.vfs.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

/**
 * The Class AbbyyServerOCREngine.
 */
public class AbbyyServerOCREngine extends AbstractOCREngine implements OCREngine {

	// The max threads.
	protected static Integer maxThreads;
	// protected ExecutorService pool = new OCRExecuter(maxThreads);
	/** The Constant logger. */
	final static Logger logger = LoggerFactory.getLogger(AbbyyServerOCREngine.class);

	// The configuration.
	protected static ConfigParser config;

	// The hotfolder.
	protected Hotfolder hotfolder;

	/** single instance of AbbyyServerOCREngine. */
	private static AbbyyServerOCREngine _instance;

	// The check server state.
	protected static Boolean checkServerState = true;

	// OCR Processes
	protected Queue<AbbyyOCRProcess> processes = new ConcurrentLinkedQueue<AbbyyOCRProcess>();

	/**
	 * Instantiates a new abbyy server engine.
	 * 
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	private AbbyyServerOCREngine() throws FileSystemException, ConfigurationException {
		hotfolder = new Hotfolder(config);
		config = new ConfigParser().parse();

		maxThreads = config.getMaxThreads();
		checkServerState = config.getCheckServerState();
	}

	/**
	 * Start the threadPooling
	 * 
	 */
	public void start () {
		/*
		if (checkServerState) {
			try {
				checkServerState();
			} catch (IOException e) {
				logger.error("got IOException during processing", e);
				throw new OCRException(e);
			}
		}
		*/
		ExecutorService pool = new OCRExecuter(maxThreads, hotfolder);

		//TODO: Check if this can be just one loop
		for (OCRProcess process : getOcrProcess()) {
			processes.add((AbbyyOCRProcess) process);
		}

		for (OCRProcess process : processes) {
			pool.execute((Runnable) process);
		}

		pool.shutdown();
		try {
			pool.awaitTermination(3600, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Got a problem with thread pool: ", e);
		}

	}

	/**
	 * Gets the single instance of AbbyyServerOCREngine.
	 * 
	 * @return single instance of AbbyyServerOCREngine
	 * 
	 */

	public static AbbyyServerOCREngine getInstance () {

		if (_instance == null) {
			try {
				_instance = new AbbyyServerOCREngine();
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

	@Override
	public OCRImage newOCRImage () {
		return new AbbyyOCRImage();
	}

	@Override
	public OCRProcess newOCRProcess () {
		return new AbbyyOCRProcess(config, hotfolder);
	}
	
	@Override
	public OCROutput newOCROutput () {
		return new AbbyyOCROutput();
	}

	@Override
	public Observer recognize (OCRProcess process) {
		//TODO: Check if this instanceof works as expected	
		if (process instanceof AbbyyOCRProcess) {
			processes.add((AbbyyOCRProcess) process);
		}
		if (!started) {
			start();
		}
		//TODO: Get an Observer from somewhere, probably use a Future
		return null;
	}

	public static AbbyyOCRProcess createProcessFromDir (File directory, String extension) throws MalformedURLException {
		AbbyyOCRProcess ap = new AbbyyOCRProcess(config);
		List<File> imageDirs = AbstractOCRProcess.getImageDirectories(directory, extension);

		for (File id : imageDirs) {
			if (imageDirs.size() > 1) {
				logger.error("Directory " + directory.getAbsolutePath() + " contains more then one image directories");
				throw new OCRException("createProcessFromDir can currently create only one AbbyyOCRProcess!");
			}
			String jobName = id.getName();
			for (File imageFile : AbstractOCRProcess.makeFileList(id, extension)) {
				ap.setName(jobName);
				//Remote URL isn't set here because we don't know it yet. 
				AbbyyOCRImage aoi = new AbbyyOCRImage(imageFile.toURI().toURL());
				aoi.setSize(imageFile.length());
				ap.addImage(aoi);
			}
		}

		return ap;
	}
}
