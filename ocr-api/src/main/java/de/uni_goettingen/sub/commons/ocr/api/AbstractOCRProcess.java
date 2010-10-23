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
		
	/** The ocr image. The Images which should be converted */
	protected List<OCRImage> ocrImages = new ArrayList<OCRImage>();
	
	/** The langs. The languages which are supported */
	protected Set<Locale> langs = new HashSet<Locale>();

	/** The ocr output. The Images converted are put in this Output Folder */
	protected Map<OCRFormat, OCROutput> ocrOutput;
	
	/**
	 * Instantiates a new oCR process.
	 */
	protected AbstractOCRProcess() {
	}

	/**
	 * Instantiates a new oCR process.
	 * 
	 * @param params
	 *            the params
	 */
	public AbstractOCRProcess(OCRProcess process) {
		//Copy Constructor
		this(process.getOcrImages(), process.getLangs(), process.getOcrOutput());
	}
	
	public AbstractOCRProcess (List<OCRImage> ocrImages, Set<Locale> langs, Map<OCRFormat, OCROutput> output) {
		this.ocrImages = ocrImages;
		this.langs = langs;
		this.ocrOutput = output;
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

	//TODO: Move this
	public static List<File> getImageDirectories (File dir, String extension) {
		List<File> dirs = new ArrayList<File>();
	
		if (makeFileList(dir, extension).size() > 0) {
			dirs.add(dir);
		}
	
		List<File> fileList;
		if (dir.isDirectory()) {
			fileList = Arrays.asList(dir.listFiles());
			for (File file : fileList) {
				if (file.isDirectory()) {
					List<File> files = makeFileList(dir, extension);
					if (files.size() > 0) {
						dirs.addAll(files);
					} else {
						dirs.addAll(getImageDirectories(file, extension));
					}
				}
			}
		} else {
			throw new IllegalStateException(dir.getAbsolutePath() + " is not a directory");
		}
		return dirs;
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
