package de.uni_goettingen.sub.commons.ocr.api;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public abstract class AbstractOCRProcess implements OCRProcess {

	/** The file. */
	private String file;
	/** Outputlocation for Abbyy*/
	private String outputLocation;
	
	/** The langs. The languages which are supported */
	protected Set<Locale> langs = new HashSet<Locale>();

	/** The enums. The issue formats which are supported */
	protected Set<OCRFormat> enums = new HashSet<OCRFormat>();

	/** The ocr image. The Images which should be converted */
	protected List<OCRImage> ocrImage = new ArrayList<OCRImage>();

	/** The ocr output. The Images converted are put in this Output Folder */
	protected List<OCROutput> ocrOutput = new ArrayList<OCROutput>();

	
	
	
	
	/**
	 * Instantiates a new oCR process.
	 */
	public AbstractOCRProcess() {
	}

	/**
	 * Instantiates a new oCR process.
	 * 
	 * @param params
	 *            the params
	 */
	public AbstractOCRProcess(OCRProcess params) {
		//Copy Constructor
		this.ocrImage = params.getOcrImages();
		this.enums = params.getFormats();
		this.langs = params.getLangs();
		//this.degrees = params.getDegrees();
	}

	/**
	 * Add a new language.
	 * 
	 * @param locale
	 *            the locale
	 */
	public void addLanguage (Locale locale) {
		langs.add(locale);
	}

	/**
	 * remove language from the list.
	 * 
	 * @param locale
	 *            the locale
	 */
	public void removeLanguage (Locale locale) {
		langs.remove(locale);
	}

	/**
	 * add Format in the list.
	 * 
	 * @param format
	 *            the format
	 */
	public void addOCRFormat (OCRFormat format) {
		enums.add(format);
	}

	/**
	 * remove Format from the list.
	 * 
	 * @param format
	 *            the format
	 */
	public void removeOCRFormat (OCRFormat format) {
		enums.remove(format);

	}

	/**
	 * Gets the file.
	 * 
	 * @return the file
	 */
	public String getFile () {
		if (file != null) {
			return new String(file);
		} else {
			return null;
		}
	}

	/**
	 * Gets the langs.
	 * 
	 * @return the langs
	 */
	public Set<Locale> getLangs () {
		return langs;
	}

	/**
	 * Gets the enums.
	 * 
	 * @return the enums
	 */
	public Set<OCRFormat> getFormats () {
		return new HashSet<OCRFormat>(enums);
	}

	/**
	 * Gets the ocr image.
	 * 
	 * @return the ocr image
	 */
	public List<OCRImage> getOcrImages () {
		return ocrImage;
	}

	/**
	 * Sets the ocr image.
	 * 
	 * @param ocrImage
	 *            the new ocr image
	 */
	public void setOcrImages (List<OCRImage> ocrImage) {
		this.ocrImage = ocrImage;
	}

	/**
	 * Adds the image.
	 * 
	 * @param ocrImage
	 *            the ocr image
	 */
	public void addImage (OCRImage ocrImage) {
		this.ocrImage.add(ocrImage);
	}

	public void setOcrOutput(List<OCROutput> ocrOutput) {
		this.ocrOutput = ocrOutput;
	}

	public String getOutputLocation() {
		return outputLocation;
	}

	public void setOutputLocation(String outputLocation) {
		this.outputLocation = outputLocation;
	}
	
	

}
