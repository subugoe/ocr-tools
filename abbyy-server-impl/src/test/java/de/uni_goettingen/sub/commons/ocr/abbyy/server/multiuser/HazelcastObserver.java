package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;


import java.util.EventListener;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemListener;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRProcess;

public class HazelcastObserver implements ItemListener, EntryListener {

	private static IMap<String, AbbyyOCRProcess> queued;
	private static ISet<String> running;
	
	public static void main(String[] args) throws InterruptedException {
		queued = Hazelcast.getMap("queued");
		running = Hazelcast.getSet("running");
		EntryListener queuedObserver = new HazelcastQueued();
		ItemListener runningObserver = new HazelcastRunning();
		EventListener allObserver = new HazelcastObserver();
		
		queued.addEntryListener(queuedObserver, true);
		running.addItemListener(runningObserver, true);

		queued.addEntryListener((EntryListener)allObserver, true);
		running.addItemListener((ItemListener)allObserver, true);

		Thread.sleep(10000000);
	}
	
	@Override
	public void itemAdded(Object item) {
	}

	@Override
	public void itemRemoved(Object item) {
		System.err.println("-----------------");
		System.err.println("queued");
		for (AbbyyOCRProcess pr : queued.values()) {
			System.err.println(pr.getName());
		}

		System.err.println();

		System.err.println("running");
		for (String pr : running) {
			System.err.println(pr);
		}

		System.err.println("-----------------");
	}

	@Override
	public void entryAdded(EntryEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryEvicted(EntryEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryRemoved(EntryEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryUpdated(EntryEvent arg0) {
		// TODO Auto-generated method stub
		
	}


}
