package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import java.util.Comparator;
import java.util.PriorityQueue;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemListener;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRProcess;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ItemComparator;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.OCRExecuter;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;

public class HazelcastOCRExecutor extends OCRExecuter implements ItemListener, EntryListener{

	protected static Comparator<AbbyyOCRProcess> ORDER;

	protected PriorityQueue<AbbyyOCRProcess> q;

	protected IMap<String, AbbyyOCRProcess> queuedProcesses;
	protected ISet<String> runningProcesses;

	public HazelcastOCRExecutor(Integer maxThreads, Hotfolder hotfolder,
			ConfigParser config) {
		super(maxThreads, hotfolder, config);
		ORDER = new ItemComparator();
		q = new PriorityQueue<AbbyyOCRProcess>(100, ORDER);

		queuedProcesses = Hazelcast.getMap("queued");
		queuedProcesses.addEntryListener(this, true);

		runningProcesses = Hazelcast.getSet("running");
		runningProcesses.addItemListener(this, true);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		if (r instanceof AbbyyOCRProcess) {
			AbbyyOCRProcess abbyyOCRProcess = (AbbyyOCRProcess) r;

			queuedProcesses.put(abbyyOCRProcess.getiD_Process(), abbyyOCRProcess);

			// TODO: deadlock danger? Maybe use hazelcast's distributed lock
			while (true) {

				boolean currentIsHead = false;
				boolean slotsFree = false;
				synchronized (queuedProcesses) {
					
					int maxProcesses = maxThreads;
					int actualProcesses = runningProcesses.size();
					slotsFree = actualProcesses < maxProcesses;

					q.clear();
					q.addAll(queuedProcesses.values());
					AbbyyOCRProcess head = q.poll();

					currentIsHead = head.equals(abbyyOCRProcess);
				}

				if (slotsFree && currentIsHead) {
					queuedProcesses.remove(abbyyOCRProcess.getiD_Process());
					runningProcesses.add(abbyyOCRProcess.getiD_Process());
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
		logger.info("Hazelcast Set item removed: " + arg0);
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
			runningProcesses.remove(abbyyOCRProcess.getiD_Process());
		} else {
			throw new IllegalStateException("Not a AbbyyOCRProcess object");
		}
	}

	@Override
	public void entryRemoved(EntryEvent arg0) {
		logger.info("Hazelcast Map entry removed: " + arg0.getKey());
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
