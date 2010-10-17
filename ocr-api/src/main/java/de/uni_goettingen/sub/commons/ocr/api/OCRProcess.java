package de.uni_goettingen.sub.commons.ocr.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class OCRProcess.
 */
public interface OCRProcess {

	/**
	 * Add a new language.
	 * 
	 * @param locale
	 *            the locale
	 */
	public void addLanguage (Locale locale);

	/**
	 * remove language from the list.
	 * 
	 * @param locale
	 *            the locale
	 */
	public void removeLanguage (Locale locale);

	/**
	 * Gets the langs.
	 * 
	 * @return the langs
	 */
	public Set<Locale> getLangs ();

	/**
	 * Gets the ocr image.
	 * 
	 * @return the ocr image
	 */
	public List<OCRImage> getOcrImages ();

	/**
	 * Sets the ocr images.
	 * 
	 * @param ocrImages
	 *            the new ocr images
	 */
	public void setOcrImages (List<OCRImage> ocrImages);

	/**
	 * Adds the image.
	 * 
	 * @param ocrImages
	 *            the ocr image
	 */
	public void addImage (OCRImage ocrImage);

	public void setOcrOutput (Map<OCRFormat, OCROutput> ocrOutput);
	
	public Map<OCRFormat, OCROutput> getOcrOutput ();
	
	public void setName (String name);

	public String getName () ;
	
}
