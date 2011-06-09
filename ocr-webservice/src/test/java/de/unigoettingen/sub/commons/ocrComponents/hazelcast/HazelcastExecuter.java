package de.unigoettingen.sub.commons.ocrComponents.hazelcast;

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

import static java.lang.Thread.sleep;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;

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






public class HazelcastExecuter extends ThreadPoolExecutor implements Executor {

	public final static Logger logger = LoggerFactory
			.getLogger(HazelcastExecuter.class);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.locks.ReentrantLock.html#newCondition()
	 */
	private Condition unpaused = pauseLock.newCondition();
	static final Comparator<AbbyyOCRProcess> ORDER = new ItemComparator();
	protected HazelcastInstance h;
	protected PriorityQueue<AbbyyOCRProcess> q ;
	protected Set <AbbyyOCRProcess> set; 
	
	public HazelcastExecuter(Integer maxThreads, HazelcastInstance h) {
		super(maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
		this.h = h;
		q = new PriorityQueue<AbbyyOCRProcess>(100, ORDER);
		set = h.getSet("default");
		this.maxThreads = maxThreads;

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
			AbbyyOCRProcess a = q.poll();
			System.out.println("----------"+ abbyyOCRProcess.getValue());
			while(!a.getValue().equals(abbyyOCRProcess.getValue())){ 
				
				q.clear();
				q.addAll(set);
				a = q.poll();
				System.out.println("++++++++"+ a.getValue());
				for(AbbyyOCRProcess p : set){
					System.out.println("###" + p.getValue());
				}
				System.out.println("###########PAUSE ################");
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
			set.remove(abbyyOCRProcess);
			System.out.println("#######SIZE############ " +set.size());
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

	
	protected void getFileSize(AbbyyOCRProcess p) throws IOException, URISyntaxException, IllegalStateException {
		System.out.println(" checkspace ");
	}
	
}
