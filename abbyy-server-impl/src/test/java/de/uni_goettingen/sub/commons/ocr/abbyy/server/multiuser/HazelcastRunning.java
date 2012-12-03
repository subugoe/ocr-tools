package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRProcess;

public class HazelcastRunning extends HazelcastObserver {

	
	@Override
	public void itemAdded(Object item) {
		AbbyyOCRProcess pr = (AbbyyOCRProcess) item;
		System.out.println("ADDED to 'running' " + pr.getName());
	}

	@Override
	public void itemRemoved(Object item) {
//		AbbyyOCRProcess pr = (AbbyyOCRProcess) item;
//		System.out.println("REMOVED from 'running' " + pr.getName());
	}

}
