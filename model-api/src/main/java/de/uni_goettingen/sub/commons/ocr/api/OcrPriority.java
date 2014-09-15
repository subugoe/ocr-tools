package de.uni_goettingen.sub.commons.ocr.api;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "ocrPriority")
@XmlEnum
public enum OcrPriority {
	HIGH("2"),
	
	ABOVENORMAL("1"),
	
	NORMAL("0"),

	BELOWNORMAL("-1"),
	
	LOW("-2");

	private final String value;

	OcrPriority(String v) {
		value = v;
	}

	public static OcrPriority fromValue(String v) {
		for (OcrPriority prio : OcrPriority.values()) {
			if (prio.value.equals(v)) {
				return prio;
			}
		}
		throw new IllegalArgumentException("Undefined priority: " + v);
	}
}
