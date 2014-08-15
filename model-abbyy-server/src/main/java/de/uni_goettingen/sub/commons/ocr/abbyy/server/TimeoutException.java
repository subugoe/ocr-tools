package de.uni_goettingen.sub.commons.ocr.abbyy.server;

public class TimeoutException extends Exception {
	
	private static final long serialVersionUID = -3002142265497735648L;

	public TimeoutException() {
		super();
	}
	public TimeoutException(String cause) {
		super(cause);
	}

}
