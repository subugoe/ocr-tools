package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyProcess;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ItemComparator;

public class HazelcastExecutor extends ThreadPoolExecutor implements Executor {

	private final static Logger logger = LoggerFactory.getLogger(HazelcastExecutor.class);
	
	private int maxProcesses;

	// This is where all the started processes of the whole cluster reside. They are all at least 
	// in the beforeExecute() method. Their number can be up to maxProcesses*nodesInCluster.
	private Map<String, AbbyyProcess> queuedProcesses;
	
	// This is used to temporarily sort the processes by their priority to find out which one 
	// is allowed to leave beforeExecute() next.
	private PriorityQueue<AbbyyProcess> queuedProcessesSorted;

	// Here are all the processes that made it through beforeExecute() and are actively running.
	// Their maximum number cluster-wide is maxProcesses.
	private Set<String> runningProcesses;

	private Lock clusterLock;
	private Condition mightBeAllowedToExecute;
	private long waitingTimeInMillis = 1000 * 60 * 10;
	private long startedExecutingAt = Long.MAX_VALUE;

	// for unit tests
	void setLock(Lock newLock) {
		clusterLock = newLock;
	}
	void setCondition(Condition newCondition) {
		mightBeAllowedToExecute = newCondition;
	}
	void setQueuedProcesses(Map<String, AbbyyProcess> newQueued) {
		queuedProcesses = newQueued;
	}
	void setRunningProcesses(Set<String> newRunning) {
		runningProcesses = newRunning;
	}
	void setWaitingTime(long newTime) {
		waitingTimeInMillis = newTime;
	}
	long getStartedLastProcessAt() {
		return startedExecutingAt;
	}
	
	public HazelcastExecutor(int maxParallelThreads, HazelcastInstance hazelcast) {
		super(maxParallelThreads, maxParallelThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		clusterLock = hazelcast.getLock("clusterLock");
		mightBeAllowedToExecute = ((ILock)clusterLock).newCondition("clusterCondition");
		maxProcesses = maxParallelThreads;
		queuedProcessesSorted = new PriorityQueue<AbbyyProcess>(100, new ItemComparator());

		queuedProcesses = hazelcast.getMap("queued");
		runningProcesses = hazelcast.getSet("running");
	}

	@Override
	protected void beforeExecute(Thread t, Runnable process) {
		super.beforeExecute(t, process);
		AbbyyProcess abbyyProcess = (AbbyyProcess) process;

		clusterLock.lock();
		try {
			// maybe set the time here?
			queuedProcesses.put(abbyyProcess.getProcessId(), abbyyProcess);
			while (!allowedToExecute(abbyyProcess)) {
				mightBeAllowedToExecute.await(waitingTimeInMillis, TimeUnit.MILLISECONDS);
			}
			queuedProcesses.remove(abbyyProcess.getProcessId());
			runningProcesses.add(abbyyProcess.getProcessId());
			mightBeAllowedToExecute.signalAll();
		} catch (InterruptedException e) {
			logger.error("Waiting thread was interrupted: " + abbyyProcess.getName(), e);
		} finally {
			clusterLock.unlock();
		}
		startedExecutingAt = System.nanoTime();
	}
	
	private boolean allowedToExecute(AbbyyProcess abbyyProcess) {
		boolean thereAreFreeSlots = runningProcesses.size() < maxProcesses;
		queuedProcessesSorted.clear();
		queuedProcessesSorted.addAll(queuedProcesses.values());
		System.out.println(abbyyProcess.getProcessId() + ": " + queuedProcessesSorted);
		AbbyyProcess head = queuedProcessesSorted.poll();
		boolean currentIsHead = head.equals(abbyyProcess);

		return thereAreFreeSlots && currentIsHead && abbyyProcess.hasEnoughSpaceForExecution();
	}

	@Override
	protected void afterExecute(Runnable process, Throwable e) {
		super.afterExecute(process, e);
		AbbyyProcess abbyyProcess = (AbbyyProcess) process;
		clusterLock.lock();
		try {
			System.out.println(abbyyProcess.getProcessId() + " finished");
			runningProcesses.remove(abbyyProcess.getProcessId());
			mightBeAllowedToExecute.signalAll();
		} finally {
			clusterLock.unlock();
		}
	}
	
}
