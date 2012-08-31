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
import java.net.URI;

import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.AbstractHotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;

import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;
import de.unigoettingen.sub.commons.ocr.util.OCRUtil;

/**
 * The Class AbbyyServerOCREngine. The Engine is also the entry point for
 * different Hotfolder implementations, You can change the implementation
 * indirectly by changing the given configuration. Just construct an empty
 * configuration or create one from a configuration file and call the method
 * {@link ConfigParser.setHotfolderClass()}.
 */

public class AbbyyServerOCREngine extends AbstractOCREngine implements
		OCREngine {
	public static final String version = "0.5";
	public static final String name = AbbyyServerOCREngine.class
			.getSimpleName();
	
	// max running ocr processes in thread pool
	protected static Integer maxThreads;

	protected Long startTimeForProcess = null;
	protected AbbyySerializerTextMD abbyySerializerTextMD;
	
	protected Long endTimeForProcess = null;
	
	final static Logger logger = LoggerFactory
			.getLogger(AbbyyServerOCREngine.class);

	// The configuration.
	protected static ConfigParser config;

	protected Hotfolder hotfolder;
	protected OCRProcessMetadata ocrProcessMetadata;
	/** single instance of AbbyyServerOCREngine. */
	private static AbbyyServerOCREngine _instance, newInstance;

	protected static Boolean checkServerState = true;
	protected static Boolean rest = false;

	// OCR Processes
	protected Queue<AbbyyOCRProcess> processes = new ConcurrentLinkedQueue<AbbyyOCRProcess>();

	protected HazelcastInstance hazel;
	
	/**
	 * Instantiates a new abbyy server engine.
	 * 
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	private AbbyyServerOCREngine() throws ConfigurationException {
		config = new ConfigParser().parse();
		hotfolder = AbstractHotfolder.getHotfolder(config.hotfolderClass,
				config);
		maxThreads = config.getMaxThreads();
		checkServerState = config.getCheckServerState();
	}

	/**
	 * Start the threadPooling
	 * 
	 */
	protected void start() {
		started = true;
		hazel = Hazelcast.newHazelcastInstance(null);
		ExecutorService pool = new OCRExecuter(maxThreads, hotfolder, hazel, config);

		for (AbbyyOCRProcess process : processes) {
			process.setTime(new Date().getTime());
			if(!process.getSplitProcess()){
				((OCRExecuter) pool).noSplitProcess(process);
			}else {
				pool.execute(process);
			}
		}

		pool.shutdown();
		try {
			pool.awaitTermination(3600, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Got a problem with thread pool: ", e);
		}
		hazel.getLifecycleService().shutdown();
	}
	
	
	/**
	 * Gets the single instance of AbbyyServerOCREngine.
	 * 
	 * @return single instance of AbbyyServerOCREngine
	 * 
	 */

	public static AbbyyServerOCREngine getInstance() {

		if (_instance == null) {
			try {
				_instance = new AbbyyServerOCREngine();
			} catch (ConfigurationException e) {
				logger.error("Can't read configuration", e);
				throw new OCRException(e);
			}
		}
		return _instance;
	}

	public static AbbyyServerOCREngine newOCREngine() {	
			try {
				newInstance = new AbbyyServerOCREngine();
			} catch (ConfigurationException e) {
				logger.error("Can't read configuration", e);
				throw new OCRException(e);
			}

		return newInstance;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine#newOcrImage(java
	 * .net.URI)
	 */
	@Override
	public OCRImage newOcrImage(URI imageUri) {
		return new AbbyyOCRImage(imageUri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine#newOcrProcess()
	 */
	@Override
	public OCRProcess newOcrProcess() {
		return new AbbyyOCRProcess(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine#newOcrOutput()
	 */
	@Override
	public OCROutput newOcrOutput() {
		return new AbbyyOCROutput();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCREngine#recognize(de.uni_goettingen
	 * .sub.commons.ocr.api.OCRProcess)
	 */
	@Override
	public Observable recognize(OCRProcess process) {
		Observable o = addOcrProcess(process);
		// TODO: Get an Observer from somewhere, probably use a Future
		recognize();
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCREngine#recognize()
	 */
	@Override
	public Observable recognize() {
		if (!started && !processes.isEmpty()) {
			start();
		} else if (processes.isEmpty()) {
			throw new IllegalStateException("Queue is empty!");
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCREngine#addOcrProcess(de.
	 * uni_goettingen.sub.commons.ocr.api.OCRProcess)
	 */
	@Override
	public Observable addOcrProcess(OCRProcess process) {
		// TODO: Check if this instanceof works as expected
		if (process instanceof AbbyyOCRProcess) {
     		processes.add((AbbyyOCRProcess) process);
		} else {
			processes.add(new AbbyyOCRProcess(process, config));
		}
		return null;
	}

	/**
	 * Creates the process from directory.
	 * 
	 * @param directory
	 *            the directory
	 * @param extension
	 *            only directories containing files of this type will be processed
	 * @return the abbyy ocr process
	 */
	public static AbbyyOCRProcess createProcessFromDir(File directory,
			String extension) {
		AbbyyOCRProcess process = new AbbyyOCRProcess(config);
		List<File> imageDirs = OCRUtil.getTargetDirectories(directory,
				extension);

		for (File dir : imageDirs) {
			if (imageDirs.size() > 1) {
				logger.error("Directory " + directory.getAbsolutePath()
						+ " contains more then one image directories");
				throw new OCRException(
						"createProcessFromDir can currently create only one AbbyyOCRProcess!");
			}
			String jobName = dir.getName();
			for (File imageFile : OCRUtil.makeFileList(dir, extension)) {
				process.setName(jobName);
				// Remote URL isn't set here because we don't know it yet.
				AbbyyOCRImage image = new AbbyyOCRImage(imageFile.toURI());
				image.setSize(imageFile.length());
				process.addImage(image);
			}
			process.processTimeout = (long) process.getOcrImages().size()
					* process.config.maxMillisPerFile;
		}

		return process;
	}

	@Override
	public Boolean init() {
		// TODO: check server connection here
		return true;
	}

	@Override
	public Boolean stop() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

}
