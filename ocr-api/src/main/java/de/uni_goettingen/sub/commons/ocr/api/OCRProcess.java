package de.uni_goettingen.sub.commons.ocr.api;

import java.util.List;
import java.util.Locale;
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
	 * add Format in the list.
	 * 
	 * @param format
	 *            the format
	 */
	public void addOCRFormat (OCRFormat format);

	/**
	 * remove Format from the list.
	 * 
	 * @param format
	 *            the format
	 */
	public void removeOCRFormat (OCRFormat format);

	/**
	 * Gets the langs.
	 * 
	 * @return the langs
	 */
	public Set<Locale> getLangs ();

	/**
	 * Gets the formats.
	 * 
	 * @return the formats
	 */
	public Set<OCRFormat> getFormats ();

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

	public void setOcrOutput (List<OCROutput> ocrOutput);

	public void setOutputLocation (String outputLocation);
	
	public void setName (String name);

	public String getName () ;
	
}
