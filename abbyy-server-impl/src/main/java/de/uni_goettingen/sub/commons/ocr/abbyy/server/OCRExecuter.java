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

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;

/**
 * The Class OCRExecuter is a ThreadPoolExecutor. Which is used to control the
 * execution of tasks on the Recognition Server with respect of the resource
 * constrains, like total number of files and used storage.
 */
public class OCRExecuter extends ThreadPoolExecutor implements Executor {
	//TODO: Also document the differences to the overridden methods.
	//TODO: There is a bug in here currently only one process per time is started

	protected Integer maxThreads;

	private Boolean isPaused = false;
	private ReentrantLock pauseLock = new ReentrantLock();
	private Condition unpaused = pauseLock.newCondition();

	//TODO: Get this from ConfigParser
	private Long maxSize = 0l;

	private Long maxFiles = 0l;

	private Long totalFileSize;

	private Long totalFileCount;

	protected Hotfolder hotfolder;

	public OCRExecuter(Integer maxThreads, Hotfolder hotfolder) {
		super(maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		this.maxThreads = maxThreads;
		this.hotfolder = hotfolder;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ThreadPoolExecutor#beforeExecute(java.lang.Thread, java.lang.Runnable)
	 */
	@Override
	protected void beforeExecute (Thread t, Runnable r) {
		super.beforeExecute(t, r);
		if (r instanceof AbbyyOCRProcess) {
			AbbyyOCRProcess abbyyOCRProcess = (AbbyyOCRProcess) r;
			//TODO: Refresh server state here
			if (maxFiles != 0 && maxSize != 0) {
				if (abbyyOCRProcess.getOcrImages().size() + totalFileCount > maxFiles || getFileSize(abbyyOCRProcess) + totalFileSize > maxSize) {
					pause();
				}
			}

		} else {
			throw new IllegalStateException("Not a AbbyyOCRProcess object");
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
	protected void afterExecute (Runnable r, Throwable e) {
		super.afterExecute(r, e);
		if (r instanceof AbbyyOCRProcess) {
			AbbyyOCRProcess abbyyOCRProcess = (AbbyyOCRProcess) r;
			//TODO: Refresh server state here
			if (maxFiles != 0 && maxSize != 0) {
				if (abbyyOCRProcess.getOcrImages().size() + totalFileCount < maxFiles || getFileSize(abbyyOCRProcess) + totalFileSize < maxSize) {
					pause();
				}
			}

		} else {
			throw new IllegalStateException("Not a AbbyyOCRProcess object");
		}
	}

	//TODO: Check if this stops only the processing of the pool or all threads containt in it
	/**
	 * this method pauses the execution.
	 */
	protected void pause () {
		pauseLock.lock();
		try {
			isPaused = true;
		} finally {
			pauseLock.unlock();
		}
	}

	/**
	 * This Method resumes the execution of the executor.
	 */
	protected void resume () {
		pauseLock.lock();
		try {
			isPaused = false;
			unpaused.signalAll();
		} finally {
			pauseLock.unlock();
		}
	}

	protected Long getFileSize (AbbyyOCRProcess p) {
		return p.calculateSize();
	}

}
