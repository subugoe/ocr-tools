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
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.HotfolderProvider;
import de.uni_goettingen.sub.commons.ocr.api.AbstractEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;
import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;


public class AbbyyEngine extends AbstractEngine implements OcrEngine {
	
	final static Logger logger = LoggerFactory.getLogger(AbbyyEngine.class);

	protected Hotfolder hotfolder;

	protected Queue<AbbyyProcess> processesQueue = new ConcurrentLinkedQueue<AbbyyProcess>();

	protected URI lockUri;
	
	private static Object monitor = new Object();
	
	private OcrExecutor pool;
	
	private Properties combinedProps;
	private HotfolderProvider hotfolderProvider = new HotfolderProvider();
	private BeanProvider beanProvider = new BeanProvider();
	private ProcessSplitter processSplitter = new ProcessSplitter();
	
	// for unit tests
	void setHotfolderProvider(HotfolderProvider newProvider) {
		hotfolderProvider = newProvider;
	}
	void setBeanProvider(BeanProvider newProvider) {
		beanProvider = newProvider;
	}
	void setProcessSplitter(ProcessSplitter newSplitter) {
		processSplitter = newSplitter;
	}
		
	public void initialize(Properties userProps) {
		String configFile = userProps.getProperty("abbyy.config", "gbv-antiqua.properties");
		FileAccess fileAccess = beanProvider.getFileAccess();
		Properties fileProps = fileAccess.getPropertiesFromFile(configFile);

		combinedProps = PropertiesCombiner.combinePropsPreferringFirst(userProps, fileProps);
		
		hotfolder = hotfolderProvider.createHotfolder(combinedProps.getProperty("serverUrl"), combinedProps.getProperty("user"), combinedProps.getProperty("password"));
	}

	@Override
	public void addOcrProcess(OcrProcess process) {
		AbbyyProcess abbyyProcess = (AbbyyProcess) process;
		if (abbyyProcess.canBeStarted()) {
	     	processesQueue.add(abbyyProcess);
		}
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
			String overwrite = combinedProps.getProperty("lock.overwrite");
			boolean overwriteLock = "true".equals(overwrite);

			String serverLockFile = "server.lock";
			lockUri = new URI(combinedProps.getProperty("serverUrl") + serverLockFile);
			
			// need to synchronize because of the Web Service
			synchronized(monitor) {
				if (overwriteLock) {
					// the lock is deleted here, but a new one is created later
					hotfolder.deleteIfExists(lockUri);
				}
				handleLock();
			}
			
		} catch (IOException e1) {
			logger.error("Error with server lock file " + lockUri, e1);
		} catch (URISyntaxException e) {
			logger.error("Error with server lock file " + lockUri, e);
		}
		
		pool = createPool(Integer.parseInt(combinedProps.getProperty("maxThreads")));
		
		while (!processesQueue.isEmpty()) {
			AbbyyProcess process = processesQueue.poll();
			process.setStartedAt(new Date().getTime());
			boolean split = "true".equals(combinedProps.getProperty("books.split"));
			if (split) {
				int splitSize = Integer.parseInt(combinedProps.getProperty("imagesNumberForSubprocess"));
				List<AbbyyProcess> subProcesses = processSplitter.split(process, splitSize);
				for (AbbyyProcess subProcess : subProcesses) {
					pool.execute(subProcess);
				}
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
		boolean lockExists = hotfolder.exists(lockUri);
		
		if (lockExists) {
			throw new ConcurrentModificationException("Another client instance is running! See the lock file at " + lockUri);
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
		hotfolder.copyTmpFile("lock", lockUri);
		hotfolder.deleteTmpFile("lock");
		
	}
	
	/**
	 * Factory method for an executor. Subclasses can override this method to
	 * return their own implementation.
	 * 
	 * @return an instance of a pool/executor
	 */
	protected OcrExecutor createPool(int maxThreads) {
		// TODO: make a field
		return new OcrExecutor(maxThreads);
	}
	
	protected void cleanUp() {
		try {
			if (hotfolder.exists(lockUri)) {
				hotfolder.delete(lockUri);
			}
		} catch (IOException e) {
			logger.error("Error while deleting lock file: " + lockUri, e);
		}
	}
	
	@Override
	public int getEstimatedDurationInSeconds() {
		long durationInMillis = 0;
		
		for (OcrProcess process : processesQueue) {
			long imagesInProcess = process.getNumberOfImages();
			durationInMillis += imagesInProcess * Integer.parseInt(combinedProps.getProperty("minMillisPerFile"));
		}
		return (int) (durationInMillis / 1000);
	}

}
