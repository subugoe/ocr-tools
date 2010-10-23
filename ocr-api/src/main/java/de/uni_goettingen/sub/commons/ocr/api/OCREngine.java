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
	 * Recognize the list of given OCRProcess. Throws an IllegalStateException if
	 * no process was added. Does nothing the recognizer is already working.
	 *
	 * @return the observer
	 */
	public Observer recognize ();
	
	/**
	 * Stops a running recognizer. Returns false if the recognizer isn't running.
	 */
	public Boolean stop ();

	/**
	 * Adds a oCR process.
	 * 
	 * @param process
	 *            the new oCR process
	 */

	public Observer addOcrProcess(OCRProcess ocrp);
	
	/**
	 * Gets the oCR process.
	 * 
	 * @return the oCR process
	 */
	public List<OCRProcess> getOcrProcess();

	public OCRImage newOCRImage ();
	
	public OCRProcess newOCRProcess();
	
	public OCROutput newOCROutput();
	
	/**
	 * Inits the OCREngine. This an be used to check if the engine is operational.
	 * Implementtions should implement this metho to chek if an Engine is licenced or
	 * a server component can be reached.
	 *
	 * @return the boolean
	 */
	public Boolean init();
	
}
