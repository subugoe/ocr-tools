package de.unigoettingen.sub.commons.ocrComponents.webservice;
/*

Copyright 2010 SUB Goettingen. All rights reserved.
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

*/
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

import javax.xml.bind.annotation.XmlType;

import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrPriority;

import de.uni_goettingen.sub.commons.ocr.api.OcrTextType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ByUrlRequestType", propOrder = { "ocrPriorityType",
		"textType", "outputFormat", "ocrlanguages", "inputUrl" })
public class ByUrlRequestType {

	@XmlElement(defaultValue = "HIGH")
	private OcrPriority ocrPriorityType;
	@XmlElement(defaultValue = "NORMAL")
	private OcrTextType textType;
	@XmlElement(defaultValue = "TXT")
	private OcrFormat outputFormat;
	private RecognitionLanguages ocrlanguages;
	@XmlElement(required = true, defaultValue = "http://localhost:9003/test.tif")
	@XmlSchemaType(name = "anyURI")
	private String inputUrl;

	
	
	public ByUrlRequestType(){
		
	}
	/**
	 * Gets the input priority type.
	 * 
	 * @return the input priority type
	 */
	public OcrPriority getOcrPriorityType() {
		return ocrPriorityType;
	}

	/**
	 * Sets the input priority type.
	 * 
	 * @param value
	 *            the new input priority type
	 */
	public void setOcrPriorityType(OcrPriority value) {
		this.ocrPriorityType = value;
	}

	/**
	 * Gets the value of the inputTextType property.
	 * 
	 * @return possible object is {@link InputTextType }
	 * 
	 */
	public OcrTextType getTextType() {
		return textType;
	}

	/**
	 * Sets the value of the inputTextType property.
	 * 
	 * @param textTypes
	 *            the new text types
	 */
	public void setTextType(OcrTextType value) {
		this.textType = value;
	}

	/**
	 * Gets the value of the outputFormat property.
	 * 
	 * @return possible object is {@link OutputFormatType }
	 * 
	 */
	public OcrFormat getOutputFormat() {
		return outputFormat;
	}

	/**
	 * Sets the value of the outputFormat property.
	 * 
	 * @param value
	 *            allowed object is {@link OutputFormatType }
	 * 
	 */
	public void setOutputFormat(OcrFormat value) {
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
