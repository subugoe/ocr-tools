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
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.HotfolderProvider;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;

/**
 * The Class AbbyyServerOCREngine. The Engine is also the entry point for
 * different Hotfolder implementations, You can change the implementation
 * indirectly by changing the given configuration. Just construct an empty
 * configuration or create one from a configuration file and call the method
 * {@link ConfigParser.setHotfolderClass()}.
 */

public class AbbyyServerOCREngine extends AbstractOCREngine implements
		OCREngine {
	protected Long startTimeForProcess = null;
	protected AbbyySerializerTextMD abbyySerializerTextMD;
	
	protected Long endTimeForProcess = null;
	
	final static Logger logger = LoggerFactory
			.getLogger(AbbyyServerOCREngine.class);

	// The configuration.
	protected ConfigParser config;

	protected Hotfolder hotfolder;
	protected OCRProcessMetadata ocrProcessMetadata;

	protected static Boolean rest = false;

	// OCR Processes
	protected Queue<AbbyyOCRProcess> processes = new ConcurrentLinkedQueue<AbbyyOCRProcess>();

	protected URI lockURI;
	
	private static Object monitor = new Object();
	
	private OCRExecuter pool;
	
	protected Properties userProperties = new Properties();
	private HotfolderProvider hotfolderProvider = new HotfolderProvider();

	// for unit tests
	void setHotfolderProvider(HotfolderProvider newProvider) {
		hotfolderProvider = newProvider;
	}
	
	public AbbyyServerOCREngine(Properties initUserProperties) {
		userProperties = initUserProperties;
		String configFile = userProperties.getProperty("abbyy.config");
		if (configFile != null) {
			config = new ConfigParser("/" + configFile).parse();
		} else {
			config = new ConfigParser().parse();
		}
		String user = userProperties.getProperty("user");
		String password = userProperties.getProperty("password");
		if (user != null) {
			config.setUsername(user);
		}
		if (password != null) {
			config.setPassword(password);
		}
		hotfolder = hotfolderProvider.createHotfolder(config.getServerURL(), config.getUsername(), config.getPassword());
	}

	/* start JMX methods */
	public String getWaitingProcesses() {
		String names = "";
		for (Runnable r : pool.getQueue()) {
			OCRProcess p = (OCRProcess) r;
			names += p.getName() + " ";
		}
		return names;
	}
	
	public int getWaitingProcessesCount() {
		return pool.getQueue().size();
	}
	
	public int getRunningProcessesCount() {
		return pool.getActiveCount();
	}
	
	public void removeWaitingProcesses() {
		pool.getQueue().clear();
	}
	
	public void removeAllProcesses() {
		pool.shutdownNow();
	}
	/* end JMX methods */
	
	/**
	 * Start the threadPooling
	 * 
	 */
	protected void start() {
		started = true;
		
		try {
			String overwrite = userProperties.getProperty("lock.overwrite");
			boolean overwriteLock = (overwrite != null && overwrite.equals("true"));

			String serverLockFile = ConfigParser.SERVER_LOCK_FILE_NAME;
			lockURI = new URI(config.getServerURL() + serverLockFile);
			
			// need to synchronize because of the Web Service
			synchronized(monitor) {
				if (overwriteLock) {
					// the lock is deleted here, but a new one is created later
					hotfolder.deleteIfExists(lockURI);
				}
				handleLock();
			}
			
		} catch (IOException e1) {
			logger.error("Error with server lock file " + lockURI, e1);
		} catch (URISyntaxException e) {
			logger.error("Error with server lock file " + lockURI, e);
		}
		
		pool = createPool();

		// TODO: need this?
//		String charCoords = extraOptions.get("output.xml.charcoordinates");
//		boolean skipCharCoords = "false".equals(charCoords);
		boolean skipCharCoords = false;
		
		while (!processes.isEmpty()) {
			AbbyyOCRProcess process = processes.poll();
			process.setTime(new Date().getTime());
			if (!skipCharCoords) {
				Map<String, String> params = new HashMap<String, String>();
				params.put("charCoordinates", "true");
				OCROutput xmlOutput = process.getOcrOutputs().get(OCRFormat.XML);
				if (xmlOutput != null) {
					xmlOutput.setParams(params);
				}
			}
			boolean processSplitting = process.getSplitProcess(); 
			pool.execute(process, processSplitting);
		}

		pool.shutdown();
		try {
			pool.awaitTermination(100, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			logger.error("Got a problem with thread pool: ", e);
		}
		
		// need to synchronize because of the Web Service
		synchronized(monitor) {
			cleanUp();
		}
		started = false;
	}
	
	/**
	 * Controls the program flow depending on the state of the server lock.
	 * Can be overwritten by subclasses to implement a different state management.
	 * 
	 * @throws IOException
	 */
	protected void handleLock() throws IOException {
		boolean lockExists = hotfolder.exists(lockURI);
		
		if (lockExists) {
			throw new ConcurrentModificationException("Another client instance is running! See the lock file at " + lockURI);
		}
		writeLockFile();

	}
	
	/**
	 * Creates a lock file containing the IP address and an ID of the current JVM process.
	 * 
	 * @throws IOException
	 */
	protected void writeLockFile() throws IOException {
		String thisIp = InetAddress.getLocalHost().getHostAddress();
		String thisId = ManagementFactory.getRuntimeMXBean().getName();
		
		OutputStream tempLock = hotfolder.createTmpFile("lock");
		IOUtils.write("IP: " + thisIp + "\nID: " + thisId, tempLock);
		hotfolder.copyTmpFile("lock", lockURI);
		hotfolder.deleteTmpFile("lock");
		
	}
	
	/**
	 * Factory method for an executor. Subclasses can overwrite this method to
	 * return their own implementation.
	 * 
	 * @return an instance of a pool/executor
	 */
	protected OCRExecuter createPool() {
		return new OCRExecuter(config.getMaxThreads(), hotfolder);
	}
	
	protected void cleanUp() {
		try {
			if (hotfolder.exists(lockURI)) {
				hotfolder.delete(lockURI);
			}
		} catch (IOException e) {
			logger.error("Error while deleting lock file: " + lockURI, e);
		}
	}
	
	/**
	 * Gets the single instance of AbbyyServerOCREngine.
	 * 
	 * @return single instance of AbbyyServerOCREngine
	 * 
	 */

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
		try {
			if (!started && !processes.isEmpty()) {
				start();
			} else if (processes.isEmpty()) {
				throw new IllegalStateException("Queue is empty!");
			}
		} finally {
			started = false;
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
		boolean processIsOk = checkProcessState(process);

		if (processIsOk) {
			if (process instanceof AbbyyOCRProcess) {
	     		processes.add((AbbyyOCRProcess) process);
			} else {
				throw new RuntimeException("wrong class");
			}
		}
		return null;
	}

	private boolean checkProcessState(OCRProcess process) {
		if (process.getOcrOutputs() == null || process.getOcrOutputs().isEmpty()) {
			logger.warn("The OCR process has no outputs: " + process.getName());
			return false;
		}

		if (process.getOcrImages() == null || process.getOcrImages().isEmpty()) {
			logger.warn("The OCR process has no input images: " + process.getName());
			return false;
		}
		return true;
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

	@Override
	public Boolean init() {
		// TODO: check server connection here
		return true;
	}

	@Override
	public Boolean stop() {
		return false;
	}
	
	@Override
	public int getEstimatedDurationInSeconds() {
		long durationInMillis = 0;
		
		for (OCRProcess process : processes) {
			long imagesInProcess = process.getOcrImages().size();
			durationInMillis += imagesInProcess * config.minMillisPerFile;
		}
		return (int) (durationInMillis / 1000);
	}

}
