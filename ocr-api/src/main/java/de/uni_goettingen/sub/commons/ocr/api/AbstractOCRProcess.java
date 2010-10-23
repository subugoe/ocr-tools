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

/**
 * The Class AbstractOCRProcess is a abstract super class for {@link OCRProcess}
 * implementations. It also adds a few static utility methods for easier creation
 * of processes.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public abstract class AbstractOCRProcess implements OCRProcess {

	/** The name of this process, this is needed for serialization of a process */
	protected String name;
		
	/** The ocr image. The Images which should be converted */
	protected List<OCRImage> ocrImages = new ArrayList<OCRImage>();
	
	/** The langs. The languages which should be recognized */
	protected Set<Locale> langs = new HashSet<Locale>();

	/** The ocr output. The Images converted, are stored in the given format at the given location */
	protected Map<OCRFormat, OCROutput> ocrOutput;
	
	/**
	 * Instantiates a new abstract OCR process.
	 */
	protected AbstractOCRProcess() {
	}

	/**
	 * Instantiates a new abtract OCR process.
	 *
	 * @param process the process
	 */
	public AbstractOCRProcess(OCRProcess process) {
		//Copy Constructor
		this(process.getOcrImages(), process.getLangs(), process.getOcrOutput());
	}
	
	/**
	 * Instantiates a new abstract OCR process.
	 *
	 * @param ocrImages a {@link List} of {@link OCRImage}
	 * @param langs a {@link Set} of {@link Locale} repreenting the languages that should be recognized
	 * @param output the output
	 */
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
	 * @param ocrImage the new ocr images
	 */
	public void setOcrImages (List<OCRImage> ocrImage) {
		this.ocrImages = ocrImage;
	}

	/**
	 * Adds the image.
	 *
	 * @param ocrImage the ocr image
	 */
	public void addImage (OCRImage ocrImage) {
		this.ocrImages.add(ocrImage);
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setOcrOutput(java.util.Map)
	 */
	public void setOcrOutput (Map<OCRFormat, OCROutput> ocrOutput) {
		this.ocrOutput = ocrOutput;
		
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getOcrOutput()
	 */
	public Map<OCRFormat, OCROutput> getOcrOutput () {
		return this.ocrOutput;
	}

	//TODO: Split Abstract classes and API into two packages
	
	/**
	 * Gets the image directories for a given directory. It checks recursively if any subfolder 
	 *
	 * @param dir the dir
	 * @param extension the extension
	 * @return the image directories
	 */
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
	 * makeFileList is a simple static method to create a list of files ending with the given extension.
	 *
	 * @param inputFile the input file, where are all images
	 * @param filter or Extension
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
