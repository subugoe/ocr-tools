package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import java.util.Comparator;
import java.util.PriorityQueue;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemListener;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRProcess;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ItemComparator;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.OCRExecuter;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;

public class HazelcastOCRExecutor extends OCRExecuter implements ItemListener{

	protected static Comparator<AbbyyOCRProcess> ORDER;

	protected PriorityQueue<AbbyyOCRProcess> q;

	protected ISet<AbbyyOCRProcess> queuedProcesses;
	protected ISet<String> runningProcesses;

	public HazelcastOCRExecutor(Integer maxThreads, Hotfolder hotfolder,
			ConfigParser config) {
		super(maxThreads, hotfolder, config);
		ORDER = new ItemComparator();
		q = new PriorityQueue<AbbyyOCRProcess>(100, ORDER);

		queuedProcesses = Hazelcast.getSet("queued");
		queuedProcesses.addItemListener(this, true);

		runningProcesses = Hazelcast.getSet("running");
		runningProcesses.addItemListener(this, true);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		if (r instanceof AbbyyOCRProcess) {
			AbbyyOCRProcess abbyyOCRProcess = (AbbyyOCRProcess) r;

			queuedProcesses.add(abbyyOCRProcess);

			// TODO: deadlock danger? Maybe use hazelcast's distributed lock
			while (true) {

				boolean currentIsHead = false;
				boolean slotsFree = false;
				synchronized (queuedProcesses) {
					
					int maxProcesses = maxThreads;
					int actualProcesses = runningProcesses.size();
					slotsFree = actualProcesses < maxProcesses;

					q.clear();
					q.addAll(queuedProcesses);
					AbbyyOCRProcess head = q.poll();

					currentIsHead = head.equals(abbyyOCRProcess);
				}

				if (slotsFree && currentIsHead) {
					// explicit searching is required
					for (AbbyyOCRProcess ab : queuedProcesses) {
						if (ab.equals(abbyyOCRProcess)){
							queuedProcesses.remove(ab);
							queuedProcesses.remove(abbyyOCRProcess);
						}
					}

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

	
}
