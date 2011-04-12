package de.unigoettingen.sub.commons.ocrComponents.webservice;




import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "inputTextType")
@XmlEnum
public enum InputTextType {

    @XmlEnumValue("Normal")
    NORMAL("Normal"),
//    @XmlEnumValue("Typewriter")
    TYPEWRITER("Typewriter"),
//    @XmlEnumValue("Gothic")
    GOTHIC("Gothic"),
//    @XmlEnumValue("Matrix")
    MATRIX("Matrix"),
//    @XmlEnumValue("OCR_A")
    OCR_A("OCR_A"),
//    @XmlEnumValue("OCR_B")
    OCR_B("OCR_B"),;
    private final String value;

    InputTextType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static InputTextType fromValue(String v) {
        for (InputTextType c: InputTextType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
