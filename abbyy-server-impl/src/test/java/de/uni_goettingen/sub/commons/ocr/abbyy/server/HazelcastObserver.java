package de.uni_goettingen.sub.commons.ocr.abbyy.server;


import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemListener;

public class HazelcastObserver implements ItemListener {

	private static ISet<AbbyyOCRProcess> queued;
	private static ISet<AbbyyOCRProcess> running;
	
	public static void main(String[] args) throws InterruptedException {
		queued = Hazelcast.getSet("queued");
		running = Hazelcast.getSet("running");
		ItemListener queuedObserver = new HazelcastQueued();
		ItemListener runningObserver = new HazelcastRunning();
		ItemListener allObserver = new HazelcastObserver();
		
		queued.addItemListener(queuedObserver, true);
		running.addItemListener(runningObserver, true);

		queued.addItemListener(allObserver, true);
		running.addItemListener(allObserver, true);

		Thread.sleep(10000000);
	}
	
	@Override
	public void itemAdded(Object item) {
	}

	@Override
	public void itemRemoved(Object item) {
		System.err.println("-----------------");
		System.err.println("queued");
		for (AbbyyOCRProcess pr : queued) {
			System.err.println(pr.getName());
		}

		System.err.println();

		System.err.println("running");
		for (AbbyyOCRProcess pr : running) {
			System.err.println(pr.getName());
		}

		System.err.println("-----------------");
	}


}
