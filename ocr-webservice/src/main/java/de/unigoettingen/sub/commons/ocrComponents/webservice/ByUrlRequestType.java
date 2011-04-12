package de.unigoettingen.sub.commons.ocrComponents.webservice;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

import javax.xml.bind.annotation.XmlType;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ByUrlRequestType", propOrder = {
	"priorityType",
	"textType",
    "outputFormat",
    "languages",
    "inputUrl"
})
public class ByUrlRequestType {

	@XmlElement(defaultValue = "High")
	protected InputPriorityType priorityType;
	@XmlElement(defaultValue = "Gothic")
	protected InputTextType textType;
	@XmlElement(defaultValue = "Text")
    protected OutputFormatType outputFormat;
    protected RecognitionLanguages languages;
    @XmlElement(required = true, defaultValue = "http://fue.onb.ac.at/impact/testdata/00000868_p.tif")
    @XmlSchemaType(name = "anyURI")
    protected String inputUrl;

    /**
     * Gets the value of the inputTextType property.
     * 
     * @return
     *     possible object is
     *     {@link InputTextType }
     *     
     */
    public InputTextType getTextTypes() {
		return textType;
	}

    /**
     * Sets the value of the inputTextType property.
     *
     * @param textTypes the new text types
     */
    public void setTextTypes(InputTextType value) {
		this.textType = value;
	}

    /**
     * Gets the value of the outputFormat property.
     * 
     * @return
     *     possible object is
     *     {@link OutputFormatType }
     *     
     */
    public OutputFormatType getOutputFormat() {
        return outputFormat;
    }

    /**
     * Sets the value of the outputFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link OutputFormatType }
     *     
     */
    public void setOutputFormat(OutputFormatType value) {
        this.outputFormat = value;
    }

    /**
     * Gets the value of the languages property.
     * 
     * @return
     *     possible object is
     *     {@link RecognitionLanguages }
     *     
     */
    public RecognitionLanguages getLanguages() {
        return languages;
    }

   

	/**
     * Sets the value of the languages property.
     * 
     * @param value
     *     allowed object is
     *     {@link RecognitionLanguages }
     *     
     */
    public void setLanguages(RecognitionLanguages value) {
        this.languages = value;
    }

	

    /**
     * Gets the value of the inputUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInputUrl() {
        return inputUrl;
    }

    /**
     * Sets the value of the inputUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInputUrl(String value) {
        this.inputUrl = value;
    }

	/**
	 * Gets the input priority type.
	 *
	 * @return the input priority type
	 */
    public InputPriorityType getPriorityTypes() {
		return priorityType;
	}

	
	/**
	 * Sets the input priority type.
	 *
	 * @param value the new input priority type
	 */
    public void setPriorityTypes(InputPriorityType value) {
		this.priorityType = value;
	}


	
}
