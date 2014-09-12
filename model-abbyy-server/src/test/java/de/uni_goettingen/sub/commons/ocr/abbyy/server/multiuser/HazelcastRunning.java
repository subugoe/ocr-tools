package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyProcess;

public class HazelcastRunning extends HazelcastObserver {

	
	@Override
	public void itemAdded(Object item) {
		AbbyyProcess pr = (AbbyyProcess) item;
		System.out.println("ADDED to 'running' " + pr.getName());
	}

	@Override
	public void itemRemoved(Object item) {
//		AbbyyProcess pr = (AbbyyProcess) item;
//		System.out.println("REMOVED from 'running' " + pr.getName());
	}

}
