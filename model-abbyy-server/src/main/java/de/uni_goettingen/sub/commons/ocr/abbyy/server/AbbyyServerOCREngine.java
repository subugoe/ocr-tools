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


public class AbbyyServerOCREngine extends AbstractOCREngine implements OCREngine {
	
	final static Logger logger = LoggerFactory.getLogger(AbbyyServerOCREngine.class);

	protected ConfigParser config;

	protected Hotfolder hotfolder;

	protected Queue<AbbyyOCRProcess> processesQueue = new ConcurrentLinkedQueue<AbbyyOCRProcess>();

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

	@Override
	public void addOcrProcess(OCRProcess process) {
		boolean processIsOk = checkProcessState(process);

		if (processIsOk) {
	     	processesQueue.add((AbbyyOCRProcess) process);
		}
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
	
	@Override
	public void recognize() {
		if (started) {
			logger.warn("Recognition is already running and cannot be started a second time.");
			return;
		}
		if (processesQueue.isEmpty()) {
			logger.warn("Cannot start recognition, there are no processes.");
			return;
		}
		startRecognition();
	}

	private void startRecognition() {
		started = true;
		
		try {
			String overwrite = userProperties.getProperty("lock.overwrite");
			boolean overwriteLock = "true".equals(overwrite);

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
		
		while (!processesQueue.isEmpty()) {
			AbbyyOCRProcess process = processesQueue.poll();
			process.setTime(new Date().getTime());
			if (!skipCharCoords) {
				Map<String, String> params = new HashMap<String, String>();
				params.put("charCoordinates", "true");
				OCROutput xmlOutput = process.getOcrOutputs().get(OCRFormat.XML);
				if (xmlOutput != null) {
					xmlOutput.setParams(params);
				}
			}
			boolean split = "true".equals(userProperties.getProperty("books.split"));
			if (split) {
				pool.executeWithSplit(process);
			} else {
				pool.execute(process);
			}
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
	 * Can be overridden by subclasses to implement a different state management.
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
	 * Factory method for an executor. Subclasses can override this method to
	 * return their own implementation.
	 * 
	 * @return an instance of a pool/executor
	 */
	protected OCRExecuter createPool() {
		// TODO: make a field
		return new OCRExecuter(config.getMaxThreads());
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
	
	@Override
	public int getEstimatedDurationInSeconds() {
		long durationInMillis = 0;
		
		for (OCRProcess process : processesQueue) {
			long imagesInProcess = process.getOcrImages().size();
			durationInMillis += imagesInProcess * config.minMillisPerFile;
		}
		return (int) (durationInMillis / 1000);
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

}
