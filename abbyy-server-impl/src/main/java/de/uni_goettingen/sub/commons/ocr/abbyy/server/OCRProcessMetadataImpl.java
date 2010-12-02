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



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.NotImplementedException;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abbyy.fineReaderXml.fineReader6SchemaV1.DocumentDocument;
import com.abbyy.fineReaderXml.fineReader6SchemaV1.DocumentDocument.Document;
import com.abbyy.recognitionServer10Xml.xmlResultSchemaV1.XmlResultDocument;
import com.abbyy.recognitionServer10Xml.xmlResultSchemaV1.XmlResultDocument.XmlResult;


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
	private String documentTypeVersion;
	private List<Locale> langs;
	private List<String> scripts;
	private String textNote;
	private BigDecimal wordAccuracy;
	private Long duration;
	private static InputStream inputStreamprocessingNote;
	protected static XmlResultDocument xmlResultDocument; 
	protected static DocumentDocument xmlExportDocument;
	protected XmlResult xmlResul;
	protected Document xmlExport;
	public static final String NAMESPACE = "http://www.abbyy.com/RecognitionServer1.0_xml/XmlResult-schema-v1.xsd";
	// The Constant logger.
	public final static Logger logger = LoggerFactory.getLogger(OCRProcessMetadataImpl.class);

	/**
	 * Instantiates a new oCR process metadata impl.
	 */
	public OCRProcessMetadataImpl() {
	}

	public OCRProcessMetadataImpl(InputStream inputStreamResult){
		XmlOptions options = new XmlOptions();
		// Set the namespace 
		options.setLoadSubstituteNamespaces(Collections.singletonMap("", NAMESPACE));		
		try {
			xmlResultDocument = XmlResultDocument.Factory.parse(inputStreamResult, options);
		} catch (XmlException e) {
			logger.error("Error in XML parse", e);
		} catch (IOException e) {
			logger.error("Error ", e);
		}
		xmlResul = xmlResultDocument.getXmlResult();
	}
	
	public OCRProcessMetadataImpl(InputStream inputStreamResult, InputStream inputStreamXmlExport , InputStream processingNote ){
		this.inputStreamprocessingNote = processingNote;
		XmlOptions options = new XmlOptions();
		// Set the namespace 
		options.setLoadSubstituteNamespaces(Collections.singletonMap("", NAMESPACE));		
		try {
			xmlExportDocument = DocumentDocument.Factory.parse(inputStreamXmlExport);
			xmlResultDocument = XmlResultDocument.Factory.parse(inputStreamResult, options);
		} catch (XmlException e) {
			logger.error("Error in XML parse", e);
		} catch (IOException e) {
			logger.error("Error ", e);
		}
		xmlResul = xmlResultDocument.getXmlResult();
		xmlExport = xmlExportDocument.getDocument();
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
		String xmlexport = xmlExport.toString();
		String[] splittArray , documentType;
		splittArray = xmlexport.split("schemaLocation=");
		documentType = splittArray[1].split(" ");
		return documentType[0].substring(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#
	 * getDocumentTypeVersion()
	 */
	//TODO
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
	//TODO
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
		String xmlexport = xmlExport.getProducer();
		String[] splittArray = xmlexport.split(" ");
		return splittArray[0];
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
		String xmlexport = xmlExport.getProducer();
		String[] splittArray = xmlexport.split(" ");
		return splittArray[1];
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
	//TODO
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
	//TODO
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
	//TODO
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
	//TODO
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
	public String getProcessingNote(){
		return xmlExport.toString();
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
		BigDecimal totalChar = new BigDecimal(xmlResul.getStatistics().getTotalCharacters());
		BigDecimal totalUncerChar = new BigDecimal(xmlResul.getStatistics().getUncertainCharacters());
		return (totalUncerChar.divide(totalChar, 8, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getWordAccuracy
	 * ()
	 */
	//TODO
	@Override
	public BigDecimal getWordAccuracy() throws IOException {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#setWordAccuracy
	 * (java.math.BigDecimal)
	 */
	//TODO
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
