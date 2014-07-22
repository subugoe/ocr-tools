package de.uni_goettingen.sub.commons.ocr.api;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "ocrPriority")
@XmlEnum
public enum OCRPriority {
	/**High job priority*/
	HIGH("High"),
	
	/** Above normal job priority */
	ABOVENORMAL("AboveNormal"),
	
	/** Normal job priority */
	NORMAL("Normal"),

	/** Below normal job priority */
	BELOWNORMAL("BelowNormal"),
	
	/** Low job priority */
	LOW("Low");

	
	private final String value;

	OCRPriority(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static OCRPriority fromValue(String v) {
		for (OCRPriority c : OCRPriority.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}
}
