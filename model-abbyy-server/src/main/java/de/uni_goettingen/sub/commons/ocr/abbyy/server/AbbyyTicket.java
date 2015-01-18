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
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abbyy.recognitionServer10Xml.xmlTicketV1.ExportParams;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.ImageProcessingParams;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.InputFile;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.MSWordExportSettings;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.OutputFileFormatSettings;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.PDFAExportSettings;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.PDFExportSettings;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.RecognitionParams;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.TextExportSettings;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.XMLExportSettings;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.XmlTicketDocument;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.XmlTicketDocument.XmlTicket;

import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrPriority;
import de.uni_goettingen.sub.commons.ocr.api.OcrTextType;
import de.unigoettingen.sub.commons.ocr.util.abbyy.ToAbbyyMapper;


public class AbbyyTicket {

	private final static Logger logger = LoggerFactory.getLogger(AbbyyTicket.class);

	/** The namespace used for the AbbyyTicket files. */
	private final static String NAMESPACE = "http://www.abbyy.com/RecognitionServer1.0_xml/XmlTicket-v1.xsd";

	/**
	 * A Map containing predefined fragments (read settings) for different
	 * formats
	 */
	private final static Map<OcrFormat, OutputFileFormatSettings> FORMAT_FRAGMENTS;
	
	private static String encoding = "UTF8";

	private Long processTimeout = null;

	private static XmlOptions opts = new XmlOptions();
	private AbbyyProcess ocrProcess;

	private URI remoteInputFolder;

	private URI remoteErrorFolder;

	static {

		opts.setSavePrettyPrint();
		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", NAMESPACE);
		opts.setSaveImplicitNamespaces(namespaces);
		opts.setUseDefaultNamespace();

		FORMAT_FRAGMENTS = new HashMap<OcrFormat, OutputFileFormatSettings>();

		PDFExportSettings pdfSettings = PDFExportSettings.Factory
				.newInstance(opts);

		pdfSettings.setPictureResolution(BigInteger.valueOf(300));
		pdfSettings.setQuality(BigInteger.valueOf(50));
		pdfSettings.setUseImprovedCompression(true);
		pdfSettings.setExportMode("ImageOnText");

		FORMAT_FRAGMENTS.put(OcrFormat.PDF,
				(OutputFileFormatSettings) pdfSettings
						.changeType(OutputFileFormatSettings.type));

		PDFAExportSettings pdfaSettings = PDFAExportSettings.Factory
				.newInstance(opts);

		
		pdfaSettings.setPictureResolution(BigInteger.valueOf(300));
		pdfaSettings.setQuality(BigInteger.valueOf(50));
		pdfaSettings.setUseImprovedCompression(true);
		pdfaSettings.setExportMode("ImageOnText");

		FORMAT_FRAGMENTS.put(OcrFormat.PDFA,
				(OutputFileFormatSettings) pdfaSettings
						.changeType(OutputFileFormatSettings.type));

		TextExportSettings txtSettings = TextExportSettings.Factory
				.newInstance(opts);

		txtSettings.setEncodingType(encoding);

		FORMAT_FRAGMENTS.put(OcrFormat.TXT,
				(OutputFileFormatSettings) txtSettings
						.changeType(OutputFileFormatSettings.type));
		
		MSWordExportSettings docSettings = MSWordExportSettings.Factory
				.newInstance(opts);
		FORMAT_FRAGMENTS.put(OcrFormat.DOC,
				(OutputFileFormatSettings) docSettings
						.changeType(OutputFileFormatSettings.type));

		FORMAT_FRAGMENTS.put(OcrFormat.HTML, null);
		FORMAT_FRAGMENTS.put(OcrFormat.XHTML, null);
		
	}

	public AbbyyTicket(AbbyyProcess initProcess) {
		ocrProcess = initProcess;
	}

	public synchronized void write(final OutputStream out) throws IOException {

		if (out == null) {
			logger.error("OutputStream is not set! (" + ocrProcess.getName() + ")");
			throw new IllegalStateException("OutputStream is not set!");
		}		
		if (!ocrProcess.hasImagesAndOutputs()) {
			logger.error("No images or outputs given! (" + ocrProcess.getName() + ")");
			throw new IllegalStateException("No images or outputs given!");
		}
		
		XMLExportSettings xmlSettings = XMLExportSettings.Factory.newInstance(opts);

		// coordinates for each character in output abbyy xml
		xmlSettings.setWriteCharactersFormatting(true);
		xmlSettings.setWriteCharAttributes(true);

		// We have to change the type here, or else the server does not accept
		// the ticket containing the xsi:type attribute.
		// In effect, the ticket cannot be validated.
		FORMAT_FRAGMENTS.put(OcrFormat.XML,
				(OutputFileFormatSettings) xmlSettings
						.changeType(OutputFileFormatSettings.type));


		XmlTicketDocument ticketDoc = XmlTicketDocument.Factory
				.newInstance(opts);
		XmlTicket ticket = ticketDoc.addNewXmlTicket();

		if (processTimeout != null) {
			ticket.setOCRTimeout(BigInteger.valueOf(processTimeout));
		}
		OcrPriority priority = ocrProcess.getPriority();
		if (priority != null) {
			ticket.setPriority(ToAbbyyMapper.getPriority(priority));
		} else {
			ticket.setPriority("Normal");
		}

		for (String imageFileName : ocrProcess.getRemoteImageNames()) {
			InputFile inputFile = ticket.addNewInputFile();
			inputFile.setName(imageFileName);
		}
		
		ImageProcessingParams imageProcessingParams = ticket.addNewImageProcessingParams();
		imageProcessingParams.setDeskew(false);

		RecognitionParams recognitionParams = ticket.addNewRecognitionParams();

		recognitionParams.setRecognitionQuality(ToAbbyyMapper.getQuality(ocrProcess.getQuality()));

		OcrTextType textType = ocrProcess.getTextType();
		if (textType != null) {
			recognitionParams.addTextType(ToAbbyyMapper.getTextType(textType));
		}

		Set<Locale> langs = ocrProcess.getLanguages();
		for (Locale l : langs) {
			recognitionParams.addLanguage(ToAbbyyMapper.getLanguage(l));
		}


		ExportParams exportParams = ticket.addNewExportParams();
		exportParams.setDocumentSeparationMethod("MergeIntoSingleFile");

		int i = 0;
		for (OcrFormat outputFormat : ocrProcess.getAllOutputFormats()) {
			if (outputFormat == OcrFormat.METADATA) {
				continue;
			}

			OutputFileFormatSettings exportFormat = FORMAT_FRAGMENTS.get(outputFormat);
			if (exportFormat == null) {
				logger.warn("The server can't handle the format "
						+ outputFormat.toString() + ", ignoring it. (" + ocrProcess.getName() + ")");
				continue;
			}
			exportFormat.setOutputFlowType("SharedFolder");
			exportFormat.setOutputFileFormat(ToAbbyyMapper.getOutputFormat(outputFormat));
			
			String[] localUriParts = ocrProcess.getOutputUriForFormat(outputFormat).toString().split("/");
			String fileName = localUriParts[localUriParts.length - 1];
			exportFormat.setNamingRule(fileName);
				
			exportFormat.setOutputLocation(ocrProcess.getWindowsPathForServer());
			
			exportParams.addNewExportFormat();
			exportParams.setExportFormatArray(i, exportFormat);
			i++;
		}
		
		// goes into the global temp directory
		ticketDoc.save(out, opts);

	}

	public Long getProcessTimeout() {
		return processTimeout;
	}

	public void setProcessTimeout(Long newTimeout) {
		processTimeout = newTimeout;
	}

	public void setRemoteInputFolder(URI newFolder) {
		remoteInputFolder = newFolder;
	}
	public URI getRemoteInputUri() throws URISyntaxException {
		return new URI(remoteInputFolder.toString() + ocrProcess.getName() + ".xml");
	}
	public void setRemoteErrorFolder(URI newFolder) {
		remoteErrorFolder = newFolder;
	}
	public URI getRemoteErrorUri() throws URISyntaxException {
		return new URI(remoteErrorFolder.toString() + ocrProcess.getName() + ".xml");
	}


}
