package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyProcess;

public class HazelcastQueued extends HazelcastObserver {

	@Override
	public void itemAdded(Object item) {
		AbbyyProcess pr = (AbbyyProcess) item;
		System.out.println("ADDED to 'queued' " + pr.getName());
	}

	@Override
	public void itemRemoved(Object item) {
//		AbbyyProcess pr = (AbbyyProcess) item;
//		System.out.println("REMOVED from 'queued' " + pr.getName());
	}

	
}
