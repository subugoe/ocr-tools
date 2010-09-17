package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

public class OCRExecuter extends ThreadPoolExecutor implements Executor {



	public Integer maxThreads;

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

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		if (r instanceof OCRProcess) {
			OCRProcess process = (OCRProcess) r;
			//TODO: Refresh server state here
			if (maxFiles != 0 && maxSize != 0) {
				if (process.getOcrImages().size() + totalFileCount > maxFiles || getFileSize(process) + totalFileSize > maxSize) {
					pause();
				}
			}

		} else {
			throw new IllegalStateException("Not a OCRProcess object");
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

	@Override
	protected void afterExecute(Runnable r, Throwable e) {
		super.afterExecute(r, e);
		if (r instanceof OCRProcess) {
			OCRProcess process = (OCRProcess) r;
			//TODO: Refresh server state here
			if (maxFiles != 0 && maxSize != 0) {
				if (process.getOcrImages().size() + totalFileCount < maxFiles || getFileSize(process) + totalFileSize < maxSize) {
					pause();
				}
			}

		} else {
			throw new IllegalStateException("Not a OCRProcess object");
		}
	}

	//TODO: Check if this stops only the processing of the pool or all threads containt in it
	public void pause() {
		pauseLock.lock();
		try {
			isPaused = true;
		} finally {
			pauseLock.unlock();
		}
	}

	public void resume() {
		pauseLock.lock();
		try {
			isPaused = false;
			unpaused.signalAll();
		} finally {
			pauseLock.unlock();
		}
	}

	//TODO: Check size here
	protected Integer getFileSize(OCRProcess p) {
		return 0;
	}

	
}
