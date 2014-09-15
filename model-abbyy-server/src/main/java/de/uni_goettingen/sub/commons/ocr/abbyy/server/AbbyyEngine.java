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
import de.unigoettingen.sub.commons.ocr.util.FileAccess;


public class AbbyyEngine extends AbstractEngine implements OcrEngine {
	
	final static Logger logger = LoggerFactory.getLogger(AbbyyEngine.class);

	protected Hotfolder hotfolder;

	protected Queue<AbbyyProcess> processesQueue = new ConcurrentLinkedQueue<AbbyyProcess>();

	protected URI lockURI;
	
	private static Object monitor = new Object();
	
	private OcrExecutor pool;
	
	protected Properties userProps = new Properties();
	private Properties fileProps = new Properties();
	private HotfolderProvider hotfolderProvider = new HotfolderProvider();
	private FileAccess fileAccess = new FileAccess();
	private ProcessSplitter processSplitter = new ProcessSplitter();
	
	// for unit tests
	void setHotfolderProvider(HotfolderProvider newProvider) {
		hotfolderProvider = newProvider;
	}
	void setFileAccess(FileAccess newAccess) {
		fileAccess = newAccess;
	}
	void setProcessSplitter(ProcessSplitter newSplitter) {
		processSplitter = newSplitter;
	}
	
	public AbbyyEngine(Properties initUserProperties) {
		userProps = initUserProperties;
	}
	
	public void initialize() {
		String configFile = userProps.getProperty("abbyy.config", "gbv-antiqua.properties");
		fileProps = fileAccess.getPropertiesFromFile(configFile);
		
		String user = userProps.getProperty("user");
		String password = userProps.getProperty("password");
		if (user != null) {
			fileProps.setProperty("username", user);
		}
		if (password != null) {
			fileProps.setProperty("password", password);
		}
		hotfolder = hotfolderProvider.createHotfolder(fileProps.getProperty("serverUrl"), fileProps.getProperty("username"), fileProps.getProperty("password"));
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
			String overwrite = userProps.getProperty("lock.overwrite");
			boolean overwriteLock = "true".equals(overwrite);

			String serverLockFile = "server.lock";
			lockURI = new URI(fileProps.getProperty("serverUrl") + serverLockFile);
			
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
		
		pool = createPool(Integer.parseInt(fileProps.getProperty("maxThreads")));
		
		while (!processesQueue.isEmpty()) {
			AbbyyProcess process = processesQueue.poll();
			process.setStartedAt(new Date().getTime());
			boolean split = "true".equals(userProps.getProperty("books.split"));
			if (split) {
				int splitSize = Integer.parseInt(fileProps.getProperty("imagesNumberForSubprocess"));
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
	protected OcrExecutor createPool(int maxThreads) {
		// TODO: make a field
		return new OcrExecutor(maxThreads);
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
		
		for (OcrProcess process : processesQueue) {
			long imagesInProcess = process.getNumberOfImages();
			durationInMillis += imagesInProcess * Integer.parseInt(fileProps.getProperty("minMillisPerFile"));
		}
		return (int) (durationInMillis / 1000);
	}

	/* start JMX methods */
	public String getWaitingProcesses() {
		String names = "";
		for (Runnable r : pool.getQueue()) {
			OcrProcess p = (OcrProcess) r;
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