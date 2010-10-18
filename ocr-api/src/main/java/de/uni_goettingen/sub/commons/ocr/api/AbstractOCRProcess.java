package de.uni_goettingen.sub.commons.ocr.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.unigoettingen.sub.commons.util.file.FileExtensionsFilter;

public abstract class AbstractOCRProcess implements OCRProcess {

	protected String name;
		
	/** The image directory. */
	//protected String imageDirectory;
	/** The langs. The languages which are supported */
	protected Set<Locale> langs = new HashSet<Locale>();


	/** The ocr image. The Images which should be converted */
	protected List<OCRImage> ocrImages = new ArrayList<OCRImage>();

	/** The ocr output. The Images converted are put in this Output Folder */
	protected Map<OCRFormat, OCROutput> ocrOutput;
	
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
		this.langs = process.getLangs();
		this.ocrOutput = process.getOcrOutput();
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
	 * Gets the langs.
	 * 
	 * @return the langs
	 */
	public Set<Locale> getLangs () {
		return langs;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOcrOutput (Map<OCRFormat, OCROutput> ocrOutput) {
		this.ocrOutput = ocrOutput;
		
	}

	public Map<OCRFormat, OCROutput> getOcrOutput () {
		return this.ocrOutput;
	}

	/**
	 *makeFileList is a simple static method to create a list of files ending with the given extension
	 * 
	 * @param inputFile
	 *            the input file, where are all images
	 * @param filter
	 *            or Extension
	 * @return the list of all files have "filter" as ending
	 */
	public static List<File> makeFileList (File inputFile, String filter) {
		List<File> fileList;
		if (inputFile.isDirectory()) {
	
			fileList = Arrays.asList(inputFile.listFiles(new FileExtensionsFilter(filter)));
			Collections.sort(fileList);
	
		} else {
			fileList = new ArrayList<File>();
			fileList.add(inputFile);
		}
		return fileList;
	}
	
}
