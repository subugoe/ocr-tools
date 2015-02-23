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

/**
 * This executor uses Hazelcast to implement coordination of abbyy processes across several JVMs
 * or even different hosts. It includes a kind of waiting queue for abbyy processes. In general,
 * processes with a higher priority and a lower timestamp are preferred and the executor will try
 * to start them before the other ones. 
 * 
 * When there are only two concurrent users, the priority
 * will not make any difference and the execution time will just be split fifty-fifty between the
 * users.
 * 
 * When there are three or more users and one of them has a higher priority, he will get fifty
 * percent of the time, and all the others will split the rest. This way, one high-priority 
 * user cannot block all the others. However, if there are two or more higher-priority users,
 * they will consume all the execution time (splitting it evenly), so that the lower priority
 * ones will have to wait.
 * 
 * Processes with the same priority are sorted by their timestamp. The timestamp is set right
 * here in the executor just before the run() method of each process, and not at the time of
 * creating the processes. Otherwise, two long-running batches of processes would alternate 
 * in their execution and never let a third one come through to be executed.
 * 
 * @author dennis
 *
 */
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
		abbyyProcess.setStartedAt(System.currentTimeMillis());
		
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
		startedExecutingAt = System.nanoTime();
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
