package de.uni_goettingen.sub.commons.ocr.api;

import java.util.HashMap;
import java.util.Map;

/**
 * The Enum OCRFormat. The formats which are supported.
 */
public enum OCRFormat {
	TXT("TXT"), PDF("PDF"), XML("XML"), PDFA("PDFA"), DOC("DOC"), HTML("HTML"), XHTML("XHTML");

	private final String name;

	protected static Map<String, OCRFormat> formats = new HashMap<String, OCRFormat>();

	OCRFormat(String format) {
		this.name = format;
	}

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString () {
		return name;
	}

	public static OCRFormat parseOCRFormat (String format) {
		return Enum.valueOf(OCRFormat.class, format.toUpperCase());
	}
	
}
