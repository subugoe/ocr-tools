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

	// for unit tests
	void setWaitingTime(long newTime) {
		waitingTimeInMillis = newTime;
	}
	Condition createCondition() {
		if (mightBeAllowedToExecute != null) {
			return mightBeAllowedToExecute;
		} else {
			return ((ILock)clusterLock).newCondition("clusterCondition");
		}
	}
	
	public HazelcastExecutor(int maxParallelThreads, HazelcastInstance hazelcast) {
		super(maxParallelThreads, maxParallelThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		clusterLock = hazelcast.getLock("clusterLock");
		maxProcesses = maxParallelThreads;
		queuedProcessesSorted = new PriorityQueue<AbbyyProcess>(100, new ItemComparator());

		queuedProcesses = hazelcast.getMap("queued");
		runningProcesses = hazelcast.getSet("running");
	}

	@Override
	protected void beforeExecute(Thread t, Runnable process) {
		super.beforeExecute(t, process);
		AbbyyProcess abbyyProcess = (AbbyyProcess) process;
		mightBeAllowedToExecute = createCondition();

		clusterLock.lock();
		try {
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
		
	}
	
	private boolean allowedToExecute(AbbyyProcess abbyyProcess) {
		boolean thereAreFreeSlots = runningProcesses.size() < maxProcesses;
		queuedProcessesSorted.clear();
		queuedProcessesSorted.addAll(queuedProcesses.values());
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
			runningProcesses.remove(abbyyProcess.getProcessId());
			mightBeAllowedToExecute.signalAll();
		} finally {
			clusterLock.unlock();
		}
	}
	
}
