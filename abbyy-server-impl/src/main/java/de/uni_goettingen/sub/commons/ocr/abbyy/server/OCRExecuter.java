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



import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import com.hazelcast.core.HazelcastInstance;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;



/**
 * The Class OCRExecuter is a ThreadPoolExecutor. Which is used to control the
 * execution of tasks on the Recognition Server with respect of the resource
 * constrains, like total number of files and used storage.
 * 
 * Two of the Templatemethods are used in connection with the implementation of
 * an activity by a Pool-Thread.
 */
public class OCRExecuter extends ThreadPoolExecutor implements Executor {
	// TODO: There is a bug in here currently only one process per time is
	// started
	// The Constant logger.
	public final static Logger logger = LoggerFactory
			.getLogger(OCRExecuter.class);

	/** The max number of threads. */
	protected Integer maxThreads;

	/** The ispaused. paused the execution if true */
	private Boolean isPaused = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.locks.ReentrantLock.html#ReentrantLock()
	 */
	private ReentrantLock pauseLock = new ReentrantLock();
	protected ConfigParser config;
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.locks.ReentrantLock.html#newCondition()
	 */
	private Condition unpaused = pauseLock.newCondition();

	//Comparator 
	protected static Comparator<AbbyyOCRProcess> ORDER;
	
	//Hazelcast
	protected HazelcastInstance h;
	
	// PriorityQueue for AbbyyOCRProcess
	protected PriorityQueue<AbbyyOCRProcess> q ;
	
	protected Set <AbbyyOCRProcess> set; 

	/**
	 * hotfolder is used to access any file system like backend. This can be
	 * used to integrate external systems like Grid storage or WebDAV based
	 * hotfolders.
	 * */
	protected Hotfolder hotfolder;

	/**
	 * Instantiates a new oCR executer.Creates a new ThreadPoolExecutor with the
	 * given initial parameters and default thread factory and handler.
	 * 
	 * @param maxThreads
	 *            the max threads
	 * @param hotfolder
	 *            is used to access any file system like backend. This can be
	 *            used to integrate external systems like Grid storage or WebDAV
	 *            based hotfolders.
	 * 
	 *            maxThreads - the number of threads to keep in the pool, even
	 *            if they are idle maxThreads - the maximum number of threads to
	 *            allow in the pool. keepAliveTime - when the number of threads
	 *            is greater than the core, this is the maximum time that excess
	 *            idle threads will wait for new tasks before terminating.
	 *            TimeUnit.MILLISECONDS - the time unit for the keepAliveTime
	 *            argument. workQueue - the queue to use for holding tasks
	 *            before they are executed. This queue will hold only the
	 *            Runnable tasks submitted by the execute method.
	 */
	public OCRExecuter(Integer maxThreads, Hotfolder hotfolder, HazelcastInstance h, ConfigParser config) {
		super(maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
		this.config = config;
		this.maxThreads = maxThreads;
		this.hotfolder = hotfolder;
		this.h = h;
		ORDER = new ItemComparator();
		q = new PriorityQueue<AbbyyOCRProcess>(100, ORDER);
		set = h.getSet("ocrProcess");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.ThreadPoolExecutor#beforeExecute(java.lang.Thread,
	 * java.lang.Runnable)
	 */
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		if (r instanceof AbbyyOCRProcess) {
			AbbyyOCRProcess abbyyOCRProcess = (AbbyyOCRProcess) r;
			set.add(abbyyOCRProcess);
			q.clear();
			q.addAll(set);
			AbbyyOCRProcess abbyy = q.poll();
			while (!abbyy.getiD_Process().equals(abbyyOCRProcess.getiD_Process())) {
				q.clear();
				q.addAll(set);
				abbyy = q.poll();
			}
			set.remove(abbyyOCRProcess);
			
			try {
				getFileSize(abbyyOCRProcess);
			} catch (IllegalStateException e1) {
				logger.debug("wait because :", e1);
				pause();
			} catch (IOException e1) {
				logger.error("Could not execute MultiStatus method", e1);
			} catch (URISyntaxException e1) {
				logger.error("Error seting URI for OCR Engine", e1);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable,
	 * java.lang.Throwable)
	 */
	@Override
	protected void afterExecute(Runnable r, Throwable e) {
		super.afterExecute(r, e);
		if (r instanceof AbbyyOCRProcess) {
			AbbyyOCRProcess abbyyOCRProcess = (AbbyyOCRProcess) r;
			try {
				getFileSize(abbyyOCRProcess);				
			} catch (IllegalStateException e1) {
				logger.debug("wait because :", e1);
				pause();
			} catch (IOException e1) {
				logger.error("Could not execute MultiStatus method", e1);
			} catch (URISyntaxException e1) {
				logger.error("Error seting URI for OCR Engine", e1);
			}

		} else {
			throw new IllegalStateException("Not a AbbyyOCRProcess object");
		}
	}

	/**
	 * this method pauses the execution.
	 */
	protected void pause() {
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
	protected void resume() {
		pauseLock.lock();
		try {
			isPaused = false;
			unpaused.signalAll();
		} finally {
			pauseLock.unlock();
		}
	}

	/**
	 * Gets the alle filesize representing this process.
	 * 
	 * @param p
	 *            represent the process
	 * @return the Calculate size of the OCRImages representing this process
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	protected void getFileSize(AbbyyOCRProcess p) throws IOException, URISyntaxException, IllegalStateException {
		p.checkServerState();
	}
	
	@Override
	public void execute(Runnable process) {
		List<AbbyyOCRProcess> sp = ((AbbyyOCRProcess)process).split();
		for (Runnable p: sp){
			//TODO trick with it we are got not null pointer
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			super.execute(p);
		}
		
	}
	
}







