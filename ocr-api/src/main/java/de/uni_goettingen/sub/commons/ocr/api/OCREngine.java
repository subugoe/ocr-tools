package de.uni_goettingen.sub.commons.ocr.api;

import java.util.Observer;

import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

/**
 * The Interface OCREngine.
 */
public interface OCREngine {

	/**
	 * Recognize. Start
	 */
	public void recognize () throws OCRException;

	/**
	 * Sets the oCR process.
	 * 
	 * @param process
	 *            the new oCR process
	 */
	public void setOCRProcess (OCRProcess process);

	/**
	 * Gets the oCR process.
	 * 
	 * @return the oCR process
	 */
	public OCRProcess getOCRProcess ();

	/**
	 * Gets the result.
	 * 
	 * @return the result
	 */
	public OCROutput getResult ();

	/**
	 * Sets the observer.
	 * 
	 * @param observer
	 *            the new observer
	 */
	public void setObserver (Observer observer);

	public OCRImage newImage ();
	
	public OCRProcess newProcess ();
	
}
