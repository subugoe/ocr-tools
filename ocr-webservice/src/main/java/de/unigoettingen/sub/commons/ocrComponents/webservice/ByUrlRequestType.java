package de.unigoettingen.sub.commons.ocrComponents.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

import javax.xml.bind.annotation.XmlType;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority;

import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextTyp;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ByUrlRequestType", propOrder = { "ocrPriorityType",
		"textType", "outputFormat", "ocrlanguages", "inputUrl" })
public class ByUrlRequestType {

	@XmlElement(defaultValue = "Normal")
	protected OCRPriority ocrPriorityType;
	@XmlElement(defaultValue = "Gothic")
	protected OCRTextTyp textType;
	@XmlElement(defaultValue = "Text")
	protected OCRFormat outputFormat;
	protected RecognitionLanguages ocrlanguages;
	@XmlElement(required = true, defaultValue = "http://fue.onb.ac.at/impact/testdata/00000868_p.tif")
	@XmlSchemaType(name = "anyURI")
	protected String inputUrl;

	/**
	 * Gets the input priority type.
	 * 
	 * @return the input priority type
	 */
	public OCRPriority getOcrPriorityType() {
		return ocrPriorityType;
	}

	/**
	 * Sets the input priority type.
	 * 
	 * @param value
	 *            the new input priority type
	 */
	public void setOcrPriorityType(OCRPriority value) {
		this.ocrPriorityType = value;
	}

	/**
	 * Gets the value of the inputTextType property.
	 * 
	 * @return possible object is {@link InputTextType }
	 * 
	 */
	public OCRTextTyp getTextType() {
		return textType;
	}

	/**
	 * Sets the value of the inputTextType property.
	 * 
	 * @param textTypes
	 *            the new text types
	 */
	public void setTextType(OCRTextTyp value) {
		this.textType = value;
	}

	/**
	 * Gets the value of the outputFormat property.
	 * 
	 * @return possible object is {@link OutputFormatType }
	 * 
	 */
	public OCRFormat getOutputFormat() {
		return outputFormat;
	}

	/**
	 * Sets the value of the outputFormat property.
	 * 
	 * @param value
	 *            allowed object is {@link OutputFormatType }
	 * 
	 */
	public void setOutputFormat(OCRFormat value) {
		this.outputFormat = value;
	}

	/**
	 * Gets the value of the languages property.
	 * 
	 * @return possible object is {@link RecognitionLanguages }
	 * 
	 */

	public RecognitionLanguages getOcrlanguages() {
		return ocrlanguages;
	}

	/**
	 * Sets the value of the languages property.
	 * 
	 * @param value
	 *            allowed object is {@link RecognitionLanguages }
	 * 
	 */
	public void setOcrlanguages(RecognitionLanguages ocrlanguages) {
		this.ocrlanguages = ocrlanguages;
	}

	/**
	 * Gets the value of the inputUrl property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getInputUrl() {
		return inputUrl;
	}

	/**
	 * Sets the value of the inputUrl property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setInputUrl(String value) {
		this.inputUrl = value;
	}

}
