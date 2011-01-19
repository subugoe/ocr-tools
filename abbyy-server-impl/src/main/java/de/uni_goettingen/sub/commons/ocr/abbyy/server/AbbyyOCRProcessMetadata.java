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
 */
public class AbbyyOCRProcessMetadata extends AbstractOCRProcessMetadata
		implements OCRProcessMetadata {

	/** The xml result document. */
	protected static XmlResultDocument xmlResultDocument;

	/** The xml export document. */
	protected static DocumentDocument xmlExportDocument;

	/** The xml resul. */
	protected XmlResult xmlResul;

	/** The xml export. */
	protected Document xmlExport;

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
	 * Instantiates a new oCR process metadata impl.
	 * 
	 * @param inputStreamResult
	 *            the input stream result
	 */
	public AbbyyOCRProcessMetadata(InputStream inputStreamResult) {
		XmlOptions options = new XmlOptions();
		// Set the namespace
		options.setLoadSubstituteNamespaces(Collections.singletonMap("",
				NAMESPACE));
		try {
			xmlResultDocument = XmlResultDocument.Factory.parse(
					inputStreamResult, options);
		} catch (XmlException e) {
			logger.error("Error in XML parse", e);
		} catch (IOException e) {
			logger.error("Error ", e);
		}
		xmlResul = xmlResultDocument.getXmlResult();
	}

	/**
	 * Instantiates a new oCR process metadata impl.
	 * 
	 * @param inputStreamResult
	 *            the input stream result
	 * @param inputStreamXmlExport
	 *            the input stream xml export
	 * @param processingNote
	 *            the processing note
	 */
	public AbbyyOCRProcessMetadata(InputStream inputStreamResult,
			InputStream inputStreamXmlExport, InputStream processingNote) {
		XmlOptions options = new XmlOptions();
		// Set the namespace
		options.setLoadSubstituteNamespaces(Collections.singletonMap("",
				NAMESPACE));
		try {
			xmlExportDocument = DocumentDocument.Factory
					.parse(inputStreamXmlExport);
			xmlResultDocument = XmlResultDocument.Factory.parse(
					inputStreamResult, options);
		} catch (XmlException e) {
			logger.error("Error in XML parse", e);
		} catch (IOException e) {
			logger.error("Error ", e);
		}
		xmlResul = xmlResultDocument.getXmlResult();
		xmlExport = xmlExportDocument.getDocument();
	}

}
