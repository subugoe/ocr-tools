package de.uni_goettingen.sub.commons.ocr.api;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "inputTextType")
@XmlEnum
public enum OCRTextType{
	
	/** The Normal type, This value corresponds to common typographic text. */
	NORMAL("Normal"),
	
	/** The Typewriter type,This value tells Open API to presume that the text 
	 * on the image was typed on a typewriter. 
	 * */
	TYPEWRITER("Typewriter"), 
	
	/** The Matrix type, This value tells Open API to presume that the text on 
	 * the image was printed by means of a dot-matrix printer. 
	 * */
	 MATRIX("Matrix"),
	
	/** The OCR_A type, This value corresponds to a monospaced font designed 
	 * specifically for Optical Character Recognition. Largely used by banks, 
	 * credit card companies and similar businesses. 
	 * */
	 OCR_A("OCR_A"),
	
	/** The OCR_B type,This value corresponds to a font designed specifically 
	 * for Optical Character Recognition. 
	 * */
	 OCR_B("OCR_B"),
	
	/** The MICR_E13B type, This value corresponds to a special set of numeric 
	 * characters printed with special magnetic inks. MICR (Magnetic Ink Character 
	 * Recognition) characters are found in a variety of places, including personal 
	 * checks. 
	 * */
	MICR_E13B("MICR_E13B"), 
	
	/** The Gothic type, This value tells Open API to presume that the text on the 
	 * image was printed in Gothic type. 
	 * */
	GOTHIC("Gothic");
	
	 private final String value;

	 OCRTextType(String v) {
	        value = v;
	    }

	    public String value() {
	        return value;
	    }

	    public static OCRTextType fromValue(String v) {
	        for (OCRTextType c: OCRTextType.values()) {
	            if (c.value.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	
}
