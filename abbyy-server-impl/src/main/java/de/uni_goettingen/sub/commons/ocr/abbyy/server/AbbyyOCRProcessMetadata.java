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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;

import java.util.Collections;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abbyy.fineReaderXml.fineReader6SchemaV1.DocumentDocument;
import com.abbyy.fineReaderXml.fineReader6SchemaV1.DocumentDocument.Document;
import com.abbyy.recognitionServer10Xml.xmlResultSchemaV1.XmlResultDocument;
import com.abbyy.recognitionServer10Xml.xmlResultSchemaV1.XmlResultDocument.XmlResult;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcessMetadata;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;

/**
 * The Class OCRProcessMetadataImpl. used to obtain a description of the
 * {@link OCRProcess} and it's results This can be used to filter the results
 * for accuracy or to save it for further processing.
 * 
 * @version 0.9
 * @author abergna
 * 
 */
public class AbbyyOCRProcessMetadata extends AbstractOCRProcessMetadata
		implements OCRProcessMetadata, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The xml result document. */
	protected static XmlResultDocument xmlResultDocument;

	/** The xml export document. */
	protected static DocumentDocument xmlExportDocument;

	/** The xml resul. */
	protected XmlResult xmlResultEngine;

	/** The xml export. */
	protected Document xmlExport;
	
	protected BigDecimal totalChar = new BigDecimal(0.0), totalUncerChar = new BigDecimal(0.0);
	
	/** The Constant NAMESPACE. */
	public static final String NAMESPACE = "http://www.abbyy.com/RecognitionServer1.0_xml/XmlResult-schema-v1.xsd";
	// The Constant logger.
	/** The Constant logger. */
	public final static Logger logger = LoggerFactory
			.getLogger(AbbyyOCRProcessMetadata.class);

	/**
	 * Instantiates a new oCR process metadata impl.
	 */
	public AbbyyOCRProcessMetadata() {
		super();
	}	

	/**
	 * Parses the xml result.
	 * 
	 * @param xmlResult
	 *            the xml result
	 */
	public void parseXmlResult(InputStream xmlResult) {
		XmlOptions options = new XmlOptions();
		// Set the namespace
		options.setLoadSubstituteNamespaces(Collections.singletonMap("",
				NAMESPACE));
		try {
			xmlResultDocument = XmlResultDocument.Factory.parse(xmlResult,
					options);
		} catch (XmlException e) {
			logger.error(
					"XMLResult can not Parse, Missing xmlResult Reports for : ",
					e);
		} catch (IOException e) {
			logger.error(
					"XMLResult can not Parse, Missing xmlResult Reports for : ",
					e);
		}
		if (xmlResultDocument != null) {
			xmlResultEngine = xmlResultDocument.getXmlResult();
			setTotalChar(new BigDecimal(xmlResultEngine
					.getStatistics().getTotalCharacters()));
			setTotalUncerChar( new BigDecimal(xmlResultEngine
					.getStatistics().getUncertainCharacters()));
			this.setCharacterAccuracy(totalChar, totalUncerChar);
			this.setProcessingNote(xmlResultEngine.toString());
		}
	}

	/**
	 * Parses the xml export.
	 * 
	 * @param is
	 *            the is
	 */
	public void parseXmlExport(InputStream is) {
		try {
			xmlExportDocument = DocumentDocument.Factory.parse(is);
		} catch (XmlException e) {
			logger.error(
					"XMLExport can not Parse, Missing xmlExport Reports: ", e);
		} catch (IOException e) {
			logger.error(
					"XMLExport can not Parse, Missing xmlExport Reports for: ",
					e);
		}
		if (xmlExportDocument != null) {
			xmlExport = xmlExportDocument.getDocument();
			this.setDocumentType(xmlExport.toString());
//			this.setProcessingNote(xmlExport.toString());
			this.setSoftwareName(xmlExport.getProducer());
			this.setSoftwareVersion(xmlExport.getProducer());
		}

	}

	public BigDecimal getTotalChar() {
		return totalChar;
	}

	public void setTotalChar(BigDecimal totalChar) {
		this.totalChar = this.totalChar.add(totalChar);
	}

	public BigDecimal getTotalUncerChar() {
		return totalUncerChar;
	}

	public void setTotalUncerChar(BigDecimal totalUncerChar) {
		this.totalUncerChar = this.totalUncerChar.add(totalUncerChar);
	}

		
}
