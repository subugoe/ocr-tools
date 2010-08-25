package de.uni_goettingen.sub.commons.ocr.api;





import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;




/**
 * The Class OCRProcess.
 */
public class OCRProcess {
	
	/** The file. */
	private String file;
	
	/** The langs. Language */
	List<Locale> langs = new ArrayList<Locale>();
	
	/** The enums. */
	List<OCRFormat> enums = new ArrayList<OCRFormat>();
	
	/** The ocr image. */
	List<OCRImage> ocrImage = new ArrayList<OCRImage>();
	
	/** The ocr output. */
	List<OCROutput> ocrOutput = new ArrayList<OCROutput>();
	
	
	
	
	
	/**
	 * Instantiates a new oCR process.
	 */
	public OCRProcess() {
		
	}
	
	/**
	 * Instantiates a new oCR process.
	 *
	 * @param params the params
	 */
	public OCRProcess (OCRProcess params) {
		//Copy Constructor
		this.file = params.getFile();
		this.ocrImage = params.getOcrImage();
		this.enums = params.getEnums();
		this.langs = params.getLangs();
		//this.degrees = params.getDegrees();
		
	}
	
	/**
	 * Add a new language.
	 *
	 * @param locale the locale
	 */
	public void addLanguage(Locale locale) {
		langs.add(locale);
	}
	
	/**
	 * remove language from the list.
	 *
	 * @param locale the locale
	 */
	public void removeLanguage(Locale locale) {
		langs.remove(locale);
	}
	
	/**
	 * add Format in the list.
	 *
	 * @param format the format
	 */
	public void addOCRFormat(OCRFormat format) {
		enums.add(format);
    }
	
	/**
	 * remove Format from the list.
	 *
	 * @param format the format
	 */
	public void removeOCRFormat(OCRFormat format) {
		enums.remove(format);
		/*
		int i = enums.indexOf(format);
		if (i>=0){
			enums.remove(i);
		}
		*/
	}
	
	/**
	 * Gets the file.
	 *
	 * @return the file
	 */
	public String getFile() {
		if (file != null) {
			return new String(file);
		} else {
			return null;
		}
	}

	/**
	 * Sets the file.
	 *
	 * @param file the new file
	 */
/*
	public void setFile(File file) {
		this.file = file.getAbsolutePath();
	}
	
	*/
	/**
	 * Sets the file.
	 *
	 * @param file the new file
	 */
	/*
	public void setFile(String file) {
		this.file = file;
	}
	*/

	/**
	 * Gets the langs.
	 *
	 * @return the langs
	 */
	public List<Locale> getLangs() {
		return langs;
	}

	/**
	 * Gets the enums.
	 *
	 * @return the enums
	 */
	public List<OCRFormat> getEnums() {
		return enums;
	}

	/**
	 * Gets the ocr image.
	 *
	 * @return the ocr image
	 */
	public List<OCRImage> getOcrImage() {
		return ocrImage;
	}

	public void setOcrImage(List<OCRImage> ocrImage) {
		this.ocrImage = ocrImage;
	}
	
	public void addImage(OCRImage ocrImage) {
		this.ocrImage.add(ocrImage);
	}

	
}
