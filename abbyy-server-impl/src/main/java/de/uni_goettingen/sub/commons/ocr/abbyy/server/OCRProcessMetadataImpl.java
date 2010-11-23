package de.uni_goettingen.sub.commons.ocr.abbyy.server;

/*

 © 2010, SUB Göttingen. All rights reserved.
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;

/**
 * The Class OCRProcessMetadataImpl. used to obtain a description of the
 * {@link OCRProcess} and it's results This can be used to filter the results
 * for accuracy or to save it for further processing.
 */
public class OCRProcessMetadataImpl implements OCRProcessMetadata {

	/** The encoding. */
	private String encoding;
	private String linebrreak;
	private String format;
	private String documentType;
	private String documentTypeVersion;
	private String softwareName;
	private String softwareVersion;
	private List<Locale> langs;
	private List<String> scripts;
	private String textNote;
	private String processingNote;
	private BigDecimal characterAccuracy;
	private BigDecimal wordAccuracy;
	private Long duration;

	/**
	 * Instantiates a new oCR process metadata impl.
	 */
	public OCRProcessMetadataImpl() {
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
		return linebrreak;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setLinebreak
	 * (java.lang.String)
	 */
	@Override
	public void setLinebreak(String linebrreak) {
		this.linebrreak = linebrreak;
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
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setDocumentType
	 * (java.lang.String)
	 */
	@Override
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#
	 * getDocumentTypeVersion()
	 */
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
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getSoftwareName
	 * ()
	 */
	@Override
	public String getSoftwareName() {
		return softwareName;
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
		this.softwareName = softwareName;
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
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setSoftwareVersion
	 * (java.lang.String)
	 */
	@Override
	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
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
	@Override
	public void setTextNote(String textNote) {
		this.textNote = textNote;
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
		return processingNote;
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
		this.processingNote = processingNote;
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
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setCharacterAccuracy
	 * (java.math.BigDecimal)
	 */
	@Override
	public void setCharacterAccuracy(BigDecimal characterAccuracy) {
		this.characterAccuracy = characterAccuracy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getWordAccuracy
	 * ()
	 */
	@Override
	public BigDecimal getWordAccuracy() {
		return wordAccuracy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setWordAccuracy
	 * (java.math.BigDecimal)
	 */
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
