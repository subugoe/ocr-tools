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


public class AbbyyEngine extends AbstractEngine implements OcrEngine {
	
	private final static Logger logger = LoggerFactory.getLogger(AbbyyEngine.class);

	private Queue<AbbyyProcess> processesQueue = new ConcurrentLinkedQueue<AbbyyProcess>();
	private Properties props;
	
	// for unit tests
	ProcessSplitter createProcessSplitter() {
		return new ProcessSplitter();
	}
	protected OcrExecutor createPool(int maxThreads) {
		return new OcrExecutor(maxThreads);
	}	
	protected LockFileHandler createLockHandler() {
		return new LockFileHandler();
	}
		
	public void initialize(Properties initProps) {
		props = initProps;
	}

	@Override
	public void addOcrProcess(OcrProcess process) {
		AbbyyProcess abbyyProcess = (AbbyyProcess) process;
		if (abbyyProcess.hasImagesAndOutputs()) {
	     	processesQueue.add(abbyyProcess);
		}
	}
	
	@Override
	public void recognize() {
		// This is not about multi-threading, it is just to prevent a second call of the method in the same thread
		if (started) {
			logger.warn("Recognition is already running and cannot be started a second time.");
			return;
		} else {
			started = true;
		}
		if (processesQueue.isEmpty()) {
			logger.warn("Cannot start recognition, there are no processes.");
			return;
		}
		performRecognition();
	}

	private void performRecognition() {
		
		String overwrite = props.getProperty("lock.overwrite");
		boolean overwriteLock = "true".equals(overwrite);
		LockFileHandler lockHandler = createLockHandler();
		lockHandler.initConnection(props.getProperty("serverUrl"), props.getProperty("user"), props.getProperty("password"));
		lockHandler.createOrOverwriteLock(overwriteLock);
			
		OcrExecutor pool = createPool(Integer.parseInt(props.getProperty("maxParallelProcesses")));
		
		while (!processesQueue.isEmpty()) {
			AbbyyProcess process = processesQueue.poll();
			process.setStartedAt(new Date().getTime());
			boolean split = "true".equals(props.getProperty("books.split"));
			if (split) {
				int splitSize = Integer.parseInt(props.getProperty("maxImagesInSubprocess"));
				ProcessSplitter processSplitter = createProcessSplitter();
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
			durationInMillis += imagesInProcess * Integer.parseInt(props.getProperty("minMillisPerFile"));
		}
		return (int) (durationInMillis / 1000);
	}

}