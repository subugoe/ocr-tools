package de.unigoettingen.sub.commons.ocr.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class OCRUtil {
	/**
	 * Parses the language.
	 * 
	 * @param str
	 *            the str
	 * @return the list of language
	 */
	public static List<Locale> parseLangs (String str) {
		List<Locale> langs = new ArrayList<Locale>();
		//TODO: Test this, remove the if
		if (str.contains(",")) {
			for (String lang : Arrays.asList(str.split(","))) {
				langs.add(new Locale(lang));
				//process.addLanguage(new Locale(lang));
			}
		} else {
			langs.add(new Locale(str));
			//process.addLanguage(new Locale(str));
		}
		return langs;
	}
}
