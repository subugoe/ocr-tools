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
	
	static {
		formats.put("TXT", OCRFormat.TXT);
		formats.put("PDF", OCRFormat.PDF);
		formats.put("PDFA", OCRFormat.PDFA);
		formats.put("DOC", OCRFormat.DOC);
		formats.put("HTML", OCRFormat.HTML);
		formats.put("XHTML", OCRFormat.XHTML);
	}
	
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
		if (formats.containsKey(format.toUpperCase())) {
			return formats.get(format.toUpperCase());
		} else {
			return null;
		}
	}
	
}
