package de.unigoettingen.sub.commons.ocr.util;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.Test;

import de.unigoettingen.sub.commons.ocr.util.OCRUtil;

public class OCRUtilTest {
	@Test
	public void testLanguageParser () {
		List<Locale> langs = OCRUtil.parseLangs("de,en");
		assertTrue(langs.contains(Locale.GERMAN));
		assertTrue(langs.contains(Locale.ENGLISH));
		langs = OCRUtil.parseLangs("ru");
		assertTrue(langs.contains(new Locale("ru")));
		//this shouldn't work
		langs = OCRUtil.parseLangs("Deutsch,English");
		assertTrue(!langs.contains(Locale.GERMAN));
		assertTrue(!langs.contains(Locale.ENGLISH));
		
	}
}
