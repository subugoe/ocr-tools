package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import java.util.Comparator;
import java.util.PriorityQueue;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemListener;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRProcess;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ItemComparator;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.OCRExecuter;

public class HazelcastOCRExecutor extends OCRExecuter implements ItemListener, EntryListener{

	protected int maxProcesses;
	protected Comparator<AbbyyOCRProcess> order;

	protected PriorityQueue<AbbyyOCRProcess> q;

	protected IMap<String, AbbyyOCRProcess> queuedProcesses;
	protected ISet<String> runningProcesses;

	public HazelcastOCRExecutor(Integer maxThreads, HazelcastInstance hazelcast) {
		super(maxThreads);
		maxProcesses = maxThreads;
		order = new ItemComparator();
		q = new PriorityQueue<AbbyyOCRProcess>(100, order);

		queuedProcesses = hazelcast.getMap("queued");
		queuedProcesses.addEntryListener(this, true);

		runningProcesses = hazelcast.getSet("running");
		runningProcesses.addItemListener(this, true);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		if (r instanceof AbbyyOCRProcess) {
			AbbyyOCRProcess abbyyOCRProcess = (AbbyyOCRProcess) r;

			queuedProcesses.put(abbyyOCRProcess.getProcessId(), abbyyOCRProcess);
			System.out.println("----------------  " + abbyyOCRProcess.getProcessId());
			System.out.println("----------------  " + queuedProcesses.keySet());
			System.out.println("----------------  " + queuedProcesses.get(abbyyOCRProcess.getProcessId()));

			// TODO: deadlock danger? Maybe use hazelcast's distributed lock
			while (true) {

				boolean currentIsHead = false;
				boolean slotsFree = false;
				synchronized (queuedProcesses) {
					
					int actualProcesses = runningProcesses.size();
					slotsFree = actualProcesses < maxProcesses;

					q.clear();
					System.out.println("----------------  " + queuedProcesses);
					q.addAll(queuedProcesses.values());
					AbbyyOCRProcess head = q.poll();

					currentIsHead = head.equals(abbyyOCRProcess);
				}

				if (slotsFree && currentIsHead) {
					queuedProcesses.remove(abbyyOCRProcess.getProcessId());
					runningProcesses.add(abbyyOCRProcess.getProcessId());
					break;
				} else {
					pause();
				}
				waitIfPaused(t);
			}

		} else {
			throw new IllegalStateException("Not an AbbyyOCRProcess object");
		}

	}

	// if an item is removed from a Hazelcast set
	@Override
	public void itemRemoved(Object arg0) {
		logger.debug("Hazelcast Set item removed: " + arg0);
		resume();

	}

	@Override
	public void itemAdded(Object arg0) {
		// don't care

	}

	@Override
	protected void afterExecute(Runnable r, Throwable e) {
		super.afterExecute(r, e);
		if (r instanceof AbbyyOCRProcess) {
			AbbyyOCRProcess abbyyOCRProcess = (AbbyyOCRProcess) r;
			runningProcesses.remove(abbyyOCRProcess.getProcessId());
		} else {
			throw new IllegalStateException("Not a AbbyyOCRProcess object");
		}
	}

	@Override
	public void entryRemoved(EntryEvent arg0) {
		logger.debug("Hazelcast Map entry removed: " + arg0.getKey());
		resume();
		
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

	
}
