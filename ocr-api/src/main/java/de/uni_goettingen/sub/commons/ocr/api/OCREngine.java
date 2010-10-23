package de.uni_goettingen.sub.commons.ocr.api;

import java.util.List;
import java.util.Observer;

// TODO: Auto-generated Javadoc
/**
 * The Interface OCREngine.
 */
public interface OCREngine {

	/**
	 * Recognize. Start
	 *
	 * @param process the process
	 * @return the observer
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
	 *
	 * @return the boolean
	 */
	public Boolean stop ();

	/**
	 * Adds a oCR process.
	 *
	 * @param ocrp the ocrp
	 * @return the observer
	 */

	public Observer addOcrProcess(OCRProcess ocrp);
	
	/**
	 * Gets the oCR process.
	 * 
	 * @return the oCR process
	 */
	public List<OCRProcess> getOcrProcess();

	/**
	 * New ocr image.
	 *
	 * @return the oCR image
	 */
	public OCRImage newOCRImage ();
	
	/**
	 * New ocr process.
	 *
	 * @return the oCR process
	 */
	public OCRProcess newOCRProcess();
	
	/**
	 * New ocr output.
	 *
	 * @return the oCR output
	 */
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
