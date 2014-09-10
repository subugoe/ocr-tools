package de.unigoettingen.sub.commons.ocr.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pause {
	
	private final static Logger logger = LoggerFactory.getLogger(Pause.class);

	public void forMilliseconds(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			logger.warn("Interrupted while sleeping. Sleep time was " + millis + " milliseconds.");
		}
	}
}
