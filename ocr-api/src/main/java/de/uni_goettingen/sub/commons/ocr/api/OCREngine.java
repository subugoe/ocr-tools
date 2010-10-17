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
	public void recognize ();

	/**
	 * Sets the oCR process.
	 * 
	 * @param process
	 *            the new oCR process
	 */
	//public void setOCRProcess (OCRProcess process);
	public void addOcrProcess(OCRProcess ocrp);
	/**
	 * Gets the oCR process.
	 * 
	 * @return the oCR process
	 */
	public List<OCRProcess> getOcrProcess();

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
	
	public OCRProcess newProcess();


	
}
