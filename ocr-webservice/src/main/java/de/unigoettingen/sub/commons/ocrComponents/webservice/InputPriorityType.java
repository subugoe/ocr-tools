package de.unigoettingen.sub.commons.ocrComponents.webservice;



import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "inputPriorityType")
@XmlEnum
public enum InputPriorityType {

	@XmlEnumValue("High")
	/** High job priority */
	HIGH("High"),
	
	/** Normal job priority */
	NORMAL("Normal"),

	/** Low job priority */
	LOW("Low"),

	/** Below normal job priority */
	BELOWNORMAL("BelowNormal"),

	/** Above normal job priority */
	ABOVENORMAL("AboveNormal");

	private final String value;

	InputPriorityType(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static InputPriorityType fromValue(String v) {
		for (InputPriorityType c : InputPriorityType.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
