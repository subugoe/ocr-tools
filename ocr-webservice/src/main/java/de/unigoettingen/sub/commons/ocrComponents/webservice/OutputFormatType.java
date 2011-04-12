package de.unigoettingen.sub.commons.ocrComponents.webservice;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlType(name = "outputFormatType")
@XmlEnum
public enum OutputFormatType {
	/** The Format for Text, UTF-8 is assumed. */
	@XmlEnumValue("TEXT")
    TEXT("TEXT"),
	/**
	 * The Format for PDF, note that different Implementations may generate
	 * different Versions of PDF.
	 */
	PDF("PDF"),
	/** The Format for XML, UTF-8 is assumed. */
	XML("XML"),
	/**
	 * The Format PDF/a, don't use this without proper validations, there are
	 * some severe errors in some implementations.
	 */
	PDFA("PDFA"),
	/**
	 * The Format for Microsoft Word Doc files, version 2003 should be assumed -
	 * try to avoid this.
	 */
	DOC("DOC"),
	/**
	 * The Format for HTML, ISO 8859-1 and HTML 4.01 should be expected - try to
	 * avoid this.
	 */
	HTML("HTML"),
	/** The Format for XHTML, UTF-8 is assumed. */
	XHTML("XHTML"),
	/**
	 * The Format for hOCR, UTF-8 is assumed, implementations should expect the
	 * worse, based on HTML 4.01
	 */
	HOCR("HOCR");

	private final String value;

    OutputFormatType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OutputFormatType fromValue(String v) {
        for (OutputFormatType c: OutputFormatType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
