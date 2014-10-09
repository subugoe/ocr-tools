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

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.AbstractEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;
import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;


public class AbbyyEngine extends AbstractEngine implements OcrEngine {
	
	private final static Logger logger = LoggerFactory.getLogger(AbbyyEngine.class);

	private Queue<AbbyyProcess> processesQueue = new ConcurrentLinkedQueue<AbbyyProcess>();
		
	private OcrExecutor pool;
	
	private Properties combinedProps;
	private BeanProvider beanProvider = new BeanProvider();
	private ProcessSplitter processSplitter = new ProcessSplitter();
	private LockFileHandler lockHandler;
	
	// for unit tests
	void setBeanProvider(BeanProvider newProvider) {
		beanProvider = newProvider;
	}
	void setProcessSplitter(ProcessSplitter newSplitter) {
		processSplitter = newSplitter;
	}
	protected OcrExecutor createPool(int maxThreads) {
		return new OcrExecutor(maxThreads);
	}	
	protected LockFileHandler createLockHandler() {
		return new LockFileHandler();
	}
		
	public void initialize(Properties userProps) {
		String configFile = userProps.getProperty("abbyy.config", "gbv-antiqua.properties");
		FileAccess fileAccess = beanProvider.getFileAccess();
		Properties fileProps = fileAccess.getPropertiesFromFile(configFile);

		combinedProps = PropertiesCombiner.combinePropsPreferringFirst(userProps, fileProps);
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
		performRecognition();
	}

	private void performRecognition() {
		started = true;
		
		String overwrite = combinedProps.getProperty("lock.overwrite");
		boolean overwriteLock = "true".equals(overwrite);
		lockHandler = createLockHandler();
		lockHandler.setConnectionData(combinedProps.getProperty("serverUrl"), combinedProps.getProperty("user"), combinedProps.getProperty("password"));
		lockHandler.createOrOverwriteLock(overwriteLock);
			
		pool = createPool(Integer.parseInt(combinedProps.getProperty("maxParallelProcesses")));
		
		while (!processesQueue.isEmpty()) {
			AbbyyProcess process = processesQueue.poll();
			process.setStartedAt(new Date().getTime());
			boolean split = "true".equals(combinedProps.getProperty("books.split"));
			if (split) {
				int splitSize = Integer.parseInt(combinedProps.getProperty("maxImagesInSubprocess"));
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
		
		lockHandler.deleteLockAndCleanUp();

		started = false;
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