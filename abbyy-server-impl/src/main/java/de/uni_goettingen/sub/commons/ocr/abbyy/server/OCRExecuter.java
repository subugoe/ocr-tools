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
import java.net.URISyntaxException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;


/**
 * The Class OCRExecuter is a ThreadPoolExecutor. Which is used to control the
 * execution of tasks on the Recognition Server with respect of the resource
 * constrains, like total number of files and used storage.
 * 
 * Two of the Templatemethods are used in connection with the 
 * implementation of an activity by a Pool-Thread.
 */
public class OCRExecuter extends ThreadPoolExecutor implements Executor {
	//TODO: There is a bug in here currently only one process per time is started
	// The Constant logger.
	public final static Logger logger = LoggerFactory.getLogger(OCRExecuter.class);
	
	// The max threads. 
	protected Integer maxThreads;

	// The ispaused. 
	private Boolean isPaused = false;
	
	// The pauselock. 
	private ReentrantLock pauseLock = new ReentrantLock();
	
	// The unpaused. 
	private Condition unpaused = pauseLock.newCondition();

	// The maxsize. 
	private Long maxSize = 0l;

	// The maxfiles. 
	private Long maxFiles = 0l;

	// The total file size in Server. 
	private Long totalFileSize;

	// The total file count. 
	private Long totalFileCount;

	// The hotfolder. 
	protected Hotfolder hotfolder;

	/**
	 * Instantiates a new oCR executer.
	 *
	 * @param maxThreads the max threads
	 * @param hotfolder the hotfolder
	 */
	public OCRExecuter(Integer maxThreads, Hotfolder hotfolder) {
		super(maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		this.maxThreads = maxThreads;
		this.hotfolder = hotfolder;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ThreadPoolExecutor#beforeExecute(java.lang.Thread, java.lang.Runnable)
	 */
	/**
	 * Before execute. it is called, before the Thread t 
	 * explains the asynchronous activity r
	 *
	 * @param t the Thread
	 * @param r the activity
	 */
	@Override
	protected void beforeExecute (Thread t, Runnable r) {
		super.beforeExecute(t, r);
		if (r instanceof AbbyyOCRProcess) {
			AbbyyOCRProcess abbyyOCRProcess = (AbbyyOCRProcess) r;
			try {
				abbyyOCRProcess.checkServerState();
			} catch (IOException e) {
				logger.debug("IOException in checkServerState method: "+ e.getMessage());
			} catch (URISyntaxException e) {
				logger.debug("URISyntaxException in checkServerState method: "+ e.getMessage());
			}
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
	/**
	 * After execute. it is called, after the Thread has 
	 * explained the asynchronous activity 
	 *
	 * @param r the Runnable
	 * @param e the Throwable
	 */
	@Override
	protected void afterExecute (Runnable r, Throwable e) {
		super.afterExecute(r, e);
		if (r instanceof AbbyyOCRProcess) {
			AbbyyOCRProcess abbyyOCRProcess = (AbbyyOCRProcess) r;
			try {
				abbyyOCRProcess.checkServerState();
			} catch (IOException e1) {
				logger.debug("IOException in checkServerState method: "+ e1.getMessage());
			} catch (URISyntaxException e1) {
				logger.debug("URISyntaxException in checkServerState method: "+ e1.getMessage());
			}
			if (maxFiles != 0 && maxSize != 0) {
				if (abbyyOCRProcess.getOcrImages().size() + totalFileCount < maxFiles || getFileSize(abbyyOCRProcess) + totalFileSize < maxSize) {
					pause();
				}
			}

		} else {
			throw new IllegalStateException("Not a AbbyyOCRProcess object");
		}
	}
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

	/**
	 * Gets the file size.
	 *
	 * @param p the p
	 * @return the file size
	 */
	protected Long getFileSize (AbbyyOCRProcess p) {
		return p.calculateSize();
	}

}
