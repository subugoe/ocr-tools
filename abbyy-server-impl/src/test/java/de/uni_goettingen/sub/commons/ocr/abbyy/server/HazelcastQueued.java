package de.uni_goettingen.sub.commons.ocr.abbyy.server;

public class HazelcastQueued extends HazelcastObserver {

	@Override
	public void itemAdded(Object item) {
		AbbyyOCRProcess pr = (AbbyyOCRProcess) item;
		System.out.println("ADDED to 'queued' " + pr.getName());
	}

	@Override
	public void itemRemoved(Object item) {
//		AbbyyOCRProcess pr = (AbbyyOCRProcess) item;
//		System.out.println("REMOVED from 'queued' " + pr.getName());
	}

	
}
