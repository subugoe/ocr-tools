package de.uni_goettingen.sub.commons.ocr.api;


/**
 * The Enum OCRFormat.
 * The formats which are supported.
 */
public enum OCRFormat {
			TXT("TXT"),
			PDF("PDF"),
			XML("XML"),
			PDFA("PDFA"),
			DOC("DOC"),
			HTML("HTML"),
			XHTML("XHTML");
			
			private final String name;
			
			OCRFormat (String format) {
				this.name = format;
			}

			/* (non-Javadoc)
			 * @see java.lang.Enum#toString()
			 */
			@Override
			public String toString () {
				return name;
			}
			
			//TODO: add utility methods to create an enum from a string
			
			
}


