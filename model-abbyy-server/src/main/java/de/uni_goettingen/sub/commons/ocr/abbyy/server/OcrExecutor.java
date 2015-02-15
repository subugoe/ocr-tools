package de.uni_goettingen.sub.commons.ocr.abbyy.server;

/*

 © 2010, SUB Göttingen. All rights reserved.
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
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class OcrExecutor is a ThreadPoolExecutor. Which is used to control the
 * execution of tasks on the Recognition Server with respect of the resource
 * constrains, like total number of files and used storage.
 * 
 * Two of the Templatemethods are used in connection with the implementation of
 * an activity by a Pool-Thread.
 * 
 */
public class OcrExecutor extends ThreadPoolExecutor implements Executor {
	private final static Logger logger = LoggerFactory.getLogger(OcrExecutor.class);

	private ReentrantLock waitLock = new ReentrantLock();
	private Condition thereIsMoreSpace = waitLock.newCondition();
	private long waitingTimeInMillis = 1000 * 60 * 30;

	// for unit tests
	void setWaitingTime(long newTime) {
		waitingTimeInMillis = newTime;
	}
	
	public OcrExecutor(Integer maxParallelThreads) {
		super(maxParallelThreads, maxParallelThreads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
	}

	@Override
	protected void beforeExecute(Thread t, Runnable process) {
		super.beforeExecute(t, process);
		AbbyyProcess abbyyProcess = (AbbyyProcess) process;
		waitLock.lock();
		try {
			while(!abbyyProcess.hasEnoughSpaceForExecution()) {
				thereIsMoreSpace.await(waitingTimeInMillis, TimeUnit.MILLISECONDS);
			}
			thereIsMoreSpace.signalAll();
		} catch (IOException e1) {
			logger.error("Could not determine free space in hotfolder (" + abbyyProcess.getName() + ")", e1);
		} catch (InterruptedException e) {
			logger.error("Waiting thread was interrupted: " + abbyyProcess.getName(), e);
		} finally {
			waitLock.unlock();
		}

	}

	@Override
	protected void afterExecute(Runnable process, Throwable e) {
		super.afterExecute(process, e);
		waitLock.lock();
		try {
			thereIsMoreSpace.signalAll();
		} finally {
			waitLock.unlock();
		}
	}

}
