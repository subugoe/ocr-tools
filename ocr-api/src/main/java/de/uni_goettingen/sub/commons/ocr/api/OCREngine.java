package de.uni_goettingen.sub.commons.ocr.api;

import java.util.List;
import java.util.Observer;

/**
 * The Interface OCREngine.
 */
public interface OCREngine {

	/**
	 * Recognize. Start
	 */
	public Observer recognize (OCRProcess process);

	/**
	 * Adds a oCR process.
	 * 
	 * @param process
	 *            the new oCR process
	 */

	public void addOcrProcess(OCRProcess ocrp);
	
	/**
	 * Gets the oCR process.
	 * 
	 * @return the oCR process
	 */
	public List<OCRProcess> getOcrProcess();

	/**
	 * Sets the observer.
	 * 
	 * @param observer
	 *            the new observer
	 */
	//public void setObserver (Observer observer);

	public OCRImage newImage ();
	
	public OCRProcess newProcess();
	
}
