package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICondition;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;
import com.hazelcast.core.MapEvent;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyProcess;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ItemComparator;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.OcrExecutor;

public class HazelcastExecutor extends OcrExecutor implements ItemListener, EntryListener{

	private final static Logger logger = LoggerFactory.getLogger(HazelcastExecutor.class);
	
	private int maxProcesses;

	private PriorityQueue<AbbyyProcess> queuedProcessesSorted;

	private IMap<String, AbbyyProcess> queuedProcesses;
	private ISet<String> runningProcesses;

	private ILock clusterLock;
	private ICondition mightBeAllowedToExecute;
	
	public HazelcastExecutor(Integer maxParallelThreads, HazelcastInstance hazelcast) {
		super(maxParallelThreads);
		clusterLock = hazelcast.getLock("clusterLock");
		mightBeAllowedToExecute = clusterLock.newCondition("clusterCondition");
		maxProcesses = maxParallelThreads;
		queuedProcessesSorted = new PriorityQueue<AbbyyProcess>(100, new ItemComparator());

		queuedProcesses = hazelcast.getMap("queued");
		queuedProcesses.addEntryListener(this, true);

		runningProcesses = hazelcast.getSet("running");
		runningProcesses.addItemListener(this, true);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable process) {
		super.beforeExecute(t, process);
		AbbyyProcess abbyyProcess = (AbbyyProcess) process;

		clusterLock.lock();
		try {
			queuedProcesses.put(abbyyProcess.getProcessId(), abbyyProcess);
			while (!allowedToExecute(abbyyProcess)) {
				mightBeAllowedToExecute.await();
			}
			queuedProcesses.remove(abbyyProcess.getProcessId());
			runningProcesses.add(abbyyProcess.getProcessId());
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

		return thereAreFreeSlots && currentIsHead;
	}

	@Override
	protected void afterExecute(Runnable process, Throwable e) {
		super.afterExecute(process, e);
		AbbyyProcess abbyyProcess = (AbbyyProcess) process;
		clusterLock.lock();
		try {
			runningProcesses.remove(abbyyProcess.getProcessId());
		} finally {
			clusterLock.unlock();
		}
	}

	// if an item is removed from a Hazelcast set
	@Override
	public void itemRemoved(ItemEvent arg0) {
		logger.debug("Hazelcast Set item removed: " + arg0);
		signalAllWaiting();
	}

	private void signalAllWaiting() {
		clusterLock.lock();
		try {
			mightBeAllowedToExecute.signalAll();
		} finally {
			clusterLock.unlock();
		}
	}
	
	@Override
	public void itemAdded(ItemEvent arg0) {
		// don't care

	}

	@Override
	public void entryRemoved(EntryEvent arg0) {
		logger.debug("Hazelcast Map entry removed: " + arg0.getKey());
		signalAllWaiting();
		
	}

	@Override
	public void entryAdded(EntryEvent arg0) {
		// don't care
	}

	@Override
	public void entryEvicted(EntryEvent arg0) {
		// don't care
	}


	@Override
	public void entryUpdated(EntryEvent arg0) {
		// don't care
	}

	@Override
	public void mapCleared(MapEvent arg0) {
		// don't care
	}

	@Override
	public void mapEvicted(MapEvent arg0) {
		// don't care
	}

	
}
