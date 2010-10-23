package de.uni_goettingen.sub.commons.ocr.api;

import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Enum OCRFormat. The formats which are supported.
 */
public enum OCRFormat {

	/** The TXT. */
	TXT("TXT"),
	/** The PDF. */
	PDF("PDF"),
	/** The XML. */
	XML("XML"),
	/** The PDFA. */
	PDFA("PDFA"),
	/** The DOC. */
	DOC("DOC"),
	/** The HTML. */
	HTML("HTML"),
	/** The XHTML. */
	XHTML("XHTML"),
	/** The HOCR. */
	HOCR("HOCR");

	/** The name. */
	private final String name;

	/** The formats. */
	protected static Map<String, OCRFormat> formats = new HashMap<String, OCRFormat>();

	/**
	 * Instantiates a new oCR format.
	 * 
	 * @param format
	 *            the format
	 */
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

	/**
	 * Parses the ocr format.
	 * 
	 * @param format
	 *            the format
	 * @return the oCR format
	 */
	public static OCRFormat parseOCRFormat (String format) {
		return Enum.valueOf(OCRFormat.class, format.toUpperCase());
	}

}
