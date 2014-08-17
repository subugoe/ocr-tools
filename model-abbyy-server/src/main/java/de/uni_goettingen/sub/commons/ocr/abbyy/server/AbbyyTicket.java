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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRTextType;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;
import de.unigoettingen.sub.commons.ocr.util.abbyy.ToAbbyyMapper;

//TODO: one Locale might represent multiple langueages: <Language>GermanNewSpelling</Language>


public class AbbyyTicket {

	private static final long serialVersionUID = -1775048479151012925L;

	private final static Logger logger = LoggerFactory.getLogger(AbbyyTicket.class);

	/** The namespace used for the AbbyyTicket files. */
	private final static String NAMESPACE = "http://www.abbyy.com/RecognitionServer1.0_xml/XmlTicket-v1.xsd";

	/**
	 * A Map containing predefined fragments (read settings) for different
	 * formats
	 */
	private final static Map<OCRFormat, OutputFileFormatSettings> FORMAT_FRAGMENTS;


	/** Predefined recognition parameters */
	protected static RecognitionParams recognitionSettings;

	/** Predefined image processing parameters */
	protected final static ImageProcessingParams imageProcessingSettings;

	
	protected static String encoding = "UTF8";

	/** The timeout for the process */
	protected Long processTimeout = null;

	private static XmlOptions opts = new XmlOptions();
	private OCRProcess ocrProcess;

	private URI remoteInputFolder;

	private URI remoteErrorFolder;

	static {

		opts.setSavePrettyPrint();
		opts.setSaveImplicitNamespaces(new HashMap<String, String>() {
			{
				put("", NAMESPACE);
			}
		});
		opts.setUseDefaultNamespace();

		FORMAT_FRAGMENTS = new HashMap<OCRFormat, OutputFileFormatSettings>();

		PDFExportSettings pdfSettings = PDFExportSettings.Factory
				.newInstance(opts);

		pdfSettings.setPictureResolution(BigInteger.valueOf(300));
		pdfSettings.setQuality(BigInteger.valueOf(50));
		pdfSettings.setUseImprovedCompression(true);
		pdfSettings.setExportMode("ImageOnText");

		FORMAT_FRAGMENTS.put(OCRFormat.PDF,
				(OutputFileFormatSettings) pdfSettings
						.changeType(OutputFileFormatSettings.type));

		PDFAExportSettings pdfaSettings = PDFAExportSettings.Factory
				.newInstance(opts);

		
		pdfaSettings.setPictureResolution(BigInteger.valueOf(300));
		pdfaSettings.setQuality(BigInteger.valueOf(50));
		pdfaSettings.setUseImprovedCompression(true);
		pdfaSettings.setExportMode("ImageOnText");

		FORMAT_FRAGMENTS.put(OCRFormat.PDFA,
				(OutputFileFormatSettings) pdfaSettings
						.changeType(OutputFileFormatSettings.type));

		TextExportSettings txtSettings = TextExportSettings.Factory
				.newInstance(opts);

		txtSettings.setEncodingType(encoding);

		FORMAT_FRAGMENTS.put(OCRFormat.TXT,
				(OutputFileFormatSettings) txtSettings
						.changeType(OutputFileFormatSettings.type));
		
		MSWordExportSettings docSettings = MSWordExportSettings.Factory
				.newInstance(opts);
		FORMAT_FRAGMENTS.put(OCRFormat.DOC,
				(OutputFileFormatSettings) docSettings
						.changeType(OutputFileFormatSettings.type));

		FORMAT_FRAGMENTS.put(OCRFormat.HTML, null);
		FORMAT_FRAGMENTS.put(OCRFormat.XHTML, null);

		recognitionSettings = RecognitionParams.Factory.newInstance();

		
		imageProcessingSettings = ImageProcessingParams.Factory.newInstance();

	}

	public AbbyyTicket(OCRProcess initProcess) {
		ocrProcess = initProcess;
	}

	public synchronized void write(final OutputStream out,
			final String identifier) throws IOException {

		// Sanity checks
		if (out == null) {
			logger.error("OutputStream and / or configuration is not set! (" + ocrProcess.getName() + ")");
			throw new IllegalStateException();
		}
		if (ocrProcess.getOcrOutputs().size() < 1) {
			throw new IllegalStateException("no outputs defined!");
		}
		
		
		XMLExportSettings xmlSettings = XMLExportSettings.Factory
				.newInstance(opts);

		// coordinates for each character in output abbyy xml
		// default is false. Might be reset later if the parameter is set
		xmlSettings.setWriteCharactersFormatting(true);
		xmlSettings.setWriteCharAttributes(true);

		OCROutput xmlOutput = ocrProcess.getOcrOutputs().get(OCRFormat.XML);
		if (xmlOutput != null) {
			String charCoords = xmlOutput.getParams().get("charCoordinates");
			if ("true".equals(charCoords)) {
				xmlSettings.setWriteCharactersFormatting(true);
				xmlSettings.setWriteCharAttributes(true);
			}
		}

		// We have to change the type here, or else the server does not accept
		// the ticket containing the xsi:type attribute.
		// In effect, the ticket cannot be validated.
		FORMAT_FRAGMENTS.put(OCRFormat.XML,
				(OutputFileFormatSettings) xmlSettings
						.changeType(OutputFileFormatSettings.type));


		XmlTicketDocument ticketDoc = XmlTicketDocument.Factory
				.newInstance(opts);
		XmlTicket ticket = ticketDoc.addNewXmlTicket();

		if (processTimeout != null) {
			ticket.setOCRTimeout(BigInteger.valueOf(processTimeout));
		}

		for (OCRImage aoi : ocrProcess.getOcrImages()) {
			InputFile inputFile = ticket.addNewInputFile();
			String file = ((AbbyyOCRImage) aoi).getRemoteFileName();
			inputFile.setName(file);

		}
		// Use predefined variables here
		ImageProcessingParams imageProcessingParams = (ImageProcessingParams) imageProcessingSettings
				.copy();
		imageProcessingParams.setDeskew(false);
		ticket.setImageProcessingParams(imageProcessingParams);

		OCRPriority priority = ocrProcess.getPriority();
		if (priority != null) {
			ticket.setPriority(ToAbbyyMapper.getPriority(priority));
		}else {
			ticket.setPriority("Normal");
		}

		OCRTextType textType = ocrProcess.getTextType();
		if (textType != null) {
			recognitionSettings.setTextTypeArray(new String[] { ToAbbyyMapper.getTextType(textType) });
		}
		// Use predefined variables here
		RecognitionParams recognitionParams = (RecognitionParams) recognitionSettings
				.copy();

		Set<Locale> langs = ocrProcess.getLanguages();
		if (langs == null) {
			throw new OCRException("No language given!");
		}

		for (Locale l : langs) {
			recognitionParams.addLanguage(ToAbbyyMapper.getLanguage(l));

		}

		recognitionParams.setRecognitionQuality(ToAbbyyMapper.getQuality(ocrProcess.getQuality()));

		ticket.setRecognitionParams(recognitionParams);
		ExportParams exportParams = ticket.addNewExportParams();
			
		exportParams.setDocumentSeparationMethod("MergeIntoSingleFile");

		if (ocrProcess.getOcrOutputs() == null || ocrProcess.getOcrOutputs().size() < 1) {
			throw new OCRException("No export options given!");
		}

		// Removed the array stuff here since the continue statements below made
		// trouble by adding empty elements to the list.
		List<OutputFileFormatSettings> settings = new ArrayList<OutputFileFormatSettings>();

		Map<OCRFormat, OCROutput> output = ocrProcess.getOcrOutputs();
		for (Map.Entry<OCRFormat, OCROutput> entry : output.entrySet()) {
			// the metadata is generated by default, no need to add it to the
			// ticket
			OCRFormat of = entry.getKey();
			if (of == OCRFormat.METADATA) {
				continue;
			}

			OutputFileFormatSettings exportFormat = FORMAT_FRAGMENTS.get(of);
			// The server can't handle this
			if (exportFormat == null) {
				logger.warn("The server can't handle the format "
						+ of.toString() + ", ignoring it. (" + ocrProcess.getName() + ")");
				continue;
			}
			exportFormat.setOutputFlowType("SharedFolder");
			exportFormat.setOutputFileFormat(ToAbbyyMapper.getOutputFormat(of));

			AbbyyOCROutput aoo = (AbbyyOCROutput) entry.getValue();
			// TODO: Check what we need to set in single file mode
			if (aoo.getRemoteFilename().equals(
					identifier + "." + of.toString().toLowerCase())) {
				exportFormat.setNamingRule(aoo.getRemoteFilename());

			}
			exportFormat.setOutputLocation(aoo.getRemoteLocation());
			
			settings.add(exportFormat);
		}

		for (int j = 0; j < settings.size(); j++) {
			exportParams.addNewExportFormat();
			exportParams.setExportFormatArray(j, settings.get(j));
		}

		ArrayList validationErrors = new ArrayList();
		XmlOptions validationOptions = new XmlOptions();
		validationOptions.setErrorListener(validationErrors);

		// goes into the global temp directory
		ticketDoc.save(out, opts);

	}

	/**
	 * @return the processTimeout
	 */
	public Long getProcessTimeout() {
		return processTimeout;
	}

	/**
	 * @param processTimeout
	 *            the processTimeout to set
	 */
	public void setProcessTimeout(Long oCRTimeOut) {
		this.processTimeout = oCRTimeOut;
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

	// TODO: Check if this is called if a List of OCRImage is set
//	@Override
//	public void addImage(OCRImage ocrImage) {
//		AbbyyOCRImage aoi = new AbbyyOCRImage(ocrImage);
//		String[] urlParts = ocrImage.getUri().toString().split("/");
//		if (getName() == null) {
//			logger.warn("Name for process not set, to avoid errors if your using parallel processes, we generate one.");
//			setName(UUID.randomUUID().toString());
//		}
//		aoi.setRemoteFileName(getName() + "-" + urlParts[urlParts.length - 1]);
//		super.addImage(aoi);
//	}


}
