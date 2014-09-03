package de.uni_goettingen.sub.commons.ocr.api;

/*
 * This file is part of the SUB Commons project.
 * Visit the websites for more information. 
 * 		- http://www.sub.uni-goettingen.de 
 * 
 * Copyright 2009, 2010, SUB Goettingen.
 * 
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import javax.xml.bind.annotation.XmlEnum;

import javax.xml.bind.annotation.XmlType;



/**
 * The Enum OCRFormat. The formats which are supported.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
@XmlType(name = "outputFormatType")
@XmlEnum
public enum OCRFormat {

	/** The Format for Text, UTF-8 is assumed. */
	TXT("TXT"),
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
	HOCR("HOCR"),
	/**
	 * The Format for METADATA can be used as wrapper for engine specific
	 * metadata, this should be mapped to the right filename and type inside the
	 * engine implementation.
	 */
	METADATA("METADATA");

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new OCR format.
	 * 
	 * @param format
	 *            the format
	 */
	private OCRFormat(String format) {
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
	
	public static OCRFormat fromValue(String v) {
        for (OCRFormat c: OCRFormat.values()) {
            if (c.name.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
