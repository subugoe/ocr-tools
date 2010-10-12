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

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;



/**
 * The Class OCRExecuter is a ThreadPoolExecutor. Which is used to control the
 * execution of tasks on the Recognition Server with respect of the resource
 * constrains, like total number of files and used storage. 
 */
public class OCRExecuter extends ThreadPoolExecutor implements Executor {
	//TODO: Also document the differences to the overridden methods.
	
	public Integer maxThreads ;

	private boolean isPaused;
	private ReentrantLock pauseLock = new ReentrantLock();
	private Condition unpaused = pauseLock.newCondition();

	private int maxSize;

	private int maxFiles;

	private int totalFileSize;

	private int totalFileCount;

	public OCRExecuter(Integer maxThreads) {
		super(maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		this.maxThreads = maxThreads;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ThreadPoolExecutor#beforeExecute(java.lang.Thread, java.lang.Runnable)
	 */
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		if (r instanceof Process) {
			Process process = (Process) r;
			//TODO: Refresh server state here
			if (maxFiles != 0 && maxSize != 0) {
				if (process.getOcrImages().size() + totalFileCount > maxFiles || getFileSize(process) + totalFileSize > maxSize) {
					pause();
				}
			}

		} else {
			throw new IllegalStateException("Not a Process object");
		}

		pauseLock.lock();
		try {
			while (isPaused) {
				unpaused.await();
			}
		} catch (InterruptedException ie) {
			t.interrupt();
		} finally {
			pauseLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable, java.lang.Throwable)
	 */
	@Override
	protected void afterExecute(Runnable r, Throwable e) {
		super.afterExecute(r, e);
		if (r instanceof Process) {
			Process process = (Process) r;
			//TODO: Refresh server state here
			if (maxFiles != 0 && maxSize != 0) {
				if (process.getOcrImages().size() + totalFileCount < maxFiles || getFileSize(process) + totalFileSize < maxSize) {
					pause();
				}
			}

		} else {
			throw new IllegalStateException("Not a Process object");
		}
	}

	//TODO: Check if this stops only the processing of the pool or all threads containt in it
	//TODO: Check if this needs to be public.
	/**
	 * this method pauses the execution.
	 */
	public void pause() {
		pauseLock.lock();
		try {
			isPaused = true;
		} finally {
			pauseLock.unlock();
		}
	}

	//TODO: Check if this needs to be public.

	/**
	 * This Method resumes the execution of the executor.
	 */
	public void resume() {
		pauseLock.lock();
		try {
			isPaused = false;
			unpaused.signalAll();
		} finally {
			pauseLock.unlock();
		}
	}

	//TODO: Check size here, this is just a place holder for now.
	protected Integer getFileSize(Process p) {
		return 0;
	}

	
}
