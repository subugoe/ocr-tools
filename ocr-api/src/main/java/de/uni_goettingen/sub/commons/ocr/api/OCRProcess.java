package de.uni_goettingen.sub.commons.ocr.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class OCRProcess {
	

	List<Locale> langs = new ArrayList<Locale>();
	List<OCRFormat> enums = new ArrayList<OCRFormat>();
	
	
	/**
	 * Add a new language 
	 */
	public void addLanguage(Locale locale) {
		langs.add(locale);
	}
	/**
	 * remove language from the list
	 */
	public void removeLanguage(Locale locale) {
		langs.remove(locale);
	}
	/**
	 * add Format in the list
	 */
	public void addOCRFormat(OCRFormat format) {
		enums.add(format);
    }
	/**
	 * remove Format from the list
	 */
	public void removeOCRFormat(OCRFormat format) {
		int i = enums.indexOf(format);
		if (i>=0){
			enums.remove(i);
		}
	}

	
}
