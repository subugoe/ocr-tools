package de.uni_goettingen.sub.commons.ocr.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public abstract class AbstractOCRProcess implements OCRProcess {

	/** The file. */
	//private String file;
	/** Outputlocation for Abbyy */
	//private String outputLocation;

	protected String name;
		
	/** The image directory. */
	protected String imageDirectory;
	/** The langs. The languages which are supported */
	protected Set<Locale> langs = new HashSet<Locale>();

	/** The enums. The issue formats which are supported */
	protected Set<OCRFormat> enums = new HashSet<OCRFormat>();

	/** The ocr image. The Images which should be converted */
	protected List<OCRImage> ocrImages = new ArrayList<OCRImage>();

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
	public AbstractOCRProcess(OCRProcess process) {
		//Copy Constructor
		this.ocrImages = process.getOcrImages();
		this.enums = process.getFormats();
		this.langs = process.getLangs();
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
	//TODO: Remove this
	/*
	public String getFile () {
		if (file != null) {
			return new String(file);
		} else {
			return null;
		}
	}
	 */
	
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
		return ocrImages;
	}

	/**
	 * Sets the ocr image.
	 * 
	 * @param ocrImages
	 *            the new ocr image
	 */
	public void setOcrImages (List<OCRImage> ocrImage) {
		this.ocrImages = ocrImage;
	}

	/**
	 * Adds the image.
	 * 
	 * @param ocrImages
	 *            the ocr image
	 */
	public void addImage (OCRImage ocrImage) {
		this.ocrImages.add(ocrImage);
	}

	public void setOcrOutput (List<OCROutput> ocrOutput) {
		this.ocrOutput = ocrOutput;
	}

	//TODO: Remove this
	/*
	public String getOutputLocation () {
		return outputLocation;
	}

	public void setOutputLocation (String outputLocation) {
		this.outputLocation = outputLocation;
	}
	*/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
