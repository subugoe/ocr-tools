package de.uni_goettingen.sub.commons.ocr.api;

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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public abstract class AbstractOCRProcessMetadata implements OCRProcessMetadata {

	/** The encoding. */
	private String encoding;

	/** The linebrreak. */
	private String linebreak;

	/** The format. */
	private String format;

	/** The processing note. */
	private String processingNote = null;

	/** The software name. */
	private String softwareName;

	/** The software version. */
	private String softwareVersion;

	/** The document type version. */
	private String documentTypeVersion;

	/** The document type. */
	private String documentType;

	/** The langs. */
	private List<Locale> langs;

	/** The scripts. */
	private List<String> scripts;

	/** The text note. */
	private String textNote;

	/** The word accuracy. */
	private BigDecimal wordAccuracy;

	/** The character accuracy. */
	private BigDecimal characterAccuracy = new BigDecimal(0.0);

	/** The duration. */
	private Long duration;

	/** The Constant NAMESPACE. */
	public static final String NAMESPACE = "http://www.abbyy.com/RecognitionServer1.0_xml/XmlResult-schema-v1.xsd";

	/**
	 * Instantiates a new abstract ocr process metadata.
	 */
	public AbstractOCRProcessMetadata() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getEncoding()
	 */
	@Override
	public String getEncoding() {
		return encoding;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setEncoding(
	 * java.lang.String)
	 */
	@Override
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getLinebreak()
	 */
	@Override
	public String getLinebreak() {
		return linebreak;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setLinebreak
	 * (java.lang.String)
	 */
	@Override
	public void setLinebreak(String linebreak) {
		this.linebreak = linebreak;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getFormat()
	 */
	@Override
	public String getFormat() {
		return format;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setFormat(java
	 * .lang.String)
	 */
	@Override
	public void setFormat(String format) {
		this.format = format;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setDocumentType
	 * (java.lang.String)
	 */
	@Override
	public void setDocumentType(String documentType) {
		String[] splittArray, documentTypeSplit;
		splittArray = documentType.split("schemaLocation=");
		documentTypeSplit = splittArray[1].split(" ");
		this.documentType = documentTypeSplit[0].substring(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getDocumentType
	 * ()
	 */
	@Override
	public String getDocumentType() {
		return documentType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#
	 * getDocumentTypeVersion()
	 */
	// TODO
	@Override
	public String getDocumentTypeVersion() {
		return documentTypeVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#
	 * setDocumentTypeVersion(java.lang.String)
	 */

	@Override
	public void setDocumentTypeVersion(String documentTypeVersion) {
		this.documentTypeVersion = documentTypeVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setSoftwareName
	 * (java.lang.String)
	 */
	@Override
	public void setSoftwareName(String softwareName) {
		String[] splittArray = softwareName.split(" ");
		this.softwareName = splittArray[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getSoftwareName
	 * ()
	 */
	@Override
	public String getSoftwareName() {
		/*
		 * String xmlexport = xmlExport.getProducer(); String[] splittArray =
		 * xmlexport.split(" ");
		 */
		return softwareName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setSoftwareVersion
	 * (java.lang.String)
	 */
	@Override
	public void setSoftwareVersion(String softwareVersion) {
		String[] splittArray = softwareVersion.split(" ");
		this.softwareVersion = splittArray[1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getSoftwareVersion
	 * ()
	 */
	@Override
	public String getSoftwareVersion() {
		return softwareVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getLanguages()
	 */
	@Override
	public List<Locale> getLanguages() {
		return langs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setLanguages
	 * (java.util.List)
	 */
	@Override
	public void setLanguages(List<Locale> langs) {
		this.langs = langs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getScripts()
	 */
	// TODO
	@Override
	public List<String> getScripts() {
		return scripts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setScripts(java
	 * .util.List)
	 */
	// TODO
	@Override
	public void setScripts(List<String> scripts) {
		this.scripts = scripts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getTextNote()
	 */
	// TODO
	@Override
	public String getTextNote() {
		return textNote;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setTextNote(
	 * java.lang.String)
	 */
	// TODO
	@Override
	public void setTextNote(String textNote) {
		this.textNote = textNote;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setProcessingNote
	 * (java.lang.String)
	 */
	@Override
	public void setProcessingNote(String processingNote) {
		this.processingNote = this.processingNote + processingNote;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getProcessingNote
	 * ()
	 */
	@Override
	public String getProcessingNote() {
		// return xmlExport.toString();
		return processingNote;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setCharacterAccuracy
	 * (java.math.BigDecimal, java.math.BigDecimal)
	 */
	@Override
	public void setCharacterAccuracy(BigDecimal totalChar,
			BigDecimal totalUncerChar) {
		this.characterAccuracy = (totalUncerChar.divide(totalChar, 8,
				BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getCharacterAccuracy
	 * ()
	 */
	@Override
	public BigDecimal getCharacterAccuracy() {
		return characterAccuracy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getWordAccuracy
	 * ()
	 */
	// TODO
	@Override
	public BigDecimal getWordAccuracy() throws IOException {
		// TODO NotImplementedException
		return wordAccuracy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setWordAccuracy
	 * (java.math.BigDecimal)
	 */
	// TODO
	@Override
	public void setWordAccuracy(BigDecimal wordAccuracy) {
		this.wordAccuracy = wordAccuracy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getDuration()
	 */
	@Override
	public Long getDuration() {
		return duration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setDuration(
	 * java.lang.Long)
	 */
	@Override
	public void setDuration(Long duration) {
		this.duration = duration;
	}

}
