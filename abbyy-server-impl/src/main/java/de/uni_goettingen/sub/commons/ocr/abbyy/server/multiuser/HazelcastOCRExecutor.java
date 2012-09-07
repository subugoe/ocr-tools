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

public class HazelcastOCRExecutor extends OCRExecuter implements ItemListener<AbbyyOCRProcess>{

	protected static Comparator<AbbyyOCRProcess> ORDER;

	protected PriorityQueue<AbbyyOCRProcess> q;

	protected ISet<AbbyyOCRProcess> queuedProcesses;
	protected ISet<AbbyyOCRProcess> runningProcesses;

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

			// TODO: deadlock danger
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

//					System.out.println(currentIsHead);
//					System.out.println(head.getName());
//					while ((head = q.poll()) != null) {
//						System.out.println(head.getName());
//					}
				}

				if (slotsFree && currentIsHead) {
					// explicit searching is required
					for (AbbyyOCRProcess ab : queuedProcesses) {
						if (ab.equals(abbyyOCRProcess))
							queuedProcesses.remove(ab);
					}
					runningProcesses.add(abbyyOCRProcess);
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
	public void itemRemoved(AbbyyOCRProcess arg0) {
		resume();

	}

	@Override
	public void itemAdded(AbbyyOCRProcess arg0) {
		// don't care

	}

	@Override
	protected void afterExecute(Runnable r, Throwable e) {
		super.afterExecute(r, e);
		if (r instanceof AbbyyOCRProcess) {
			AbbyyOCRProcess abbyyOCRProcess = (AbbyyOCRProcess) r;

			// hazelcast does not use the custom equals method, so you cannot
			// delete abbyyOcrProcess directly
			for (AbbyyOCRProcess ab : runningProcesses) {
				if (ab.equals(abbyyOCRProcess)) {
					runningProcesses.remove(ab);
				}
			}
		} else {
			throw new IllegalStateException("Not a AbbyyOCRProcess object");
		}
	}

	
}
