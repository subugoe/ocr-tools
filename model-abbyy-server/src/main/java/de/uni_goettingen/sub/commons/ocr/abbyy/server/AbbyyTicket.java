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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRTextType;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;
import de.unigoettingen.sub.commons.ocr.util.abbyy.ToAbbyyMapper;

//TODO: one Locale might represent multiple langueages: <Language>GermanNewSpelling</Language>


public class AbbyyTicket extends AbstractOCRProcess implements OCRProcess {

	private static final long serialVersionUID = -1775048479151012925L;

	private final static Logger logger = LoggerFactory.getLogger(AbbyyTicket.class);

	/** The namespace used for the AbbyyTicket files. */
	public final static String NAMESPACE = "http://www.abbyy.com/RecognitionServer1.0_xml/XmlTicket-v1.xsd";

	/**
	 * A Map containing predefined fragments (read settings) for different
	 * formats
	 */
	protected final static Map<OCRFormat, OutputFileFormatSettings> FORMAT_FRAGMENTS;

	/**
	 * A Map containing mappings from the internal Enums to engine specific
	 * quality settings
	 */
	protected final static Map<OCRQuality, String> QUALITY_MAP;

	/**
	 * A Map containing mappings from the internal Enums to engine specific
	 * format settings
	 */
	public final static Map<OCRFormat, String> FORMAT_MAPPING;


	/** Predefined recognition parameters */
	protected static RecognitionParams recognitionSettings;

	/** Predefined image processing parameters */
	protected final static ImageProcessingParams imageProcessingSettings;

	protected final static Map<OCRPriority, String> PRIORITY_MAP;
	
	protected static String encoding = "UTF8";

	/** The timeout for the process */
	protected Long processTimeout = null;

	// The configuration.
	protected static ConfigParser config;

	private static XmlOptions opts = new XmlOptions();

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

		// This is one of Thorough, Balanced or Fast
		QUALITY_MAP = new HashMap<OCRQuality, String>();
		QUALITY_MAP.put(OCRQuality.BEST, "Thorough");
		QUALITY_MAP.put(OCRQuality.BALANCED, "Balanced");
		QUALITY_MAP.put(OCRQuality.FAST, "Fast");

		recognitionSettings = RecognitionParams.Factory.newInstance();

		//priorities: Low, BelowNormal, Normal, AboveNormal, High
		PRIORITY_MAP = new HashMap<OCRPriority, String>();		
		PRIORITY_MAP.put(OCRPriority.HIGH, "High");
		PRIORITY_MAP.put(OCRPriority.ABOVENORMAL, "AboveNormal");
		PRIORITY_MAP.put(OCRPriority.NORMAL, "Normal");
		PRIORITY_MAP.put(OCRPriority.BELOWNORMAL, "BelowNormal");
		PRIORITY_MAP.put(OCRPriority.LOW, "Low");
		
		imageProcessingSettings = ImageProcessingParams.Factory.newInstance();

		FORMAT_MAPPING = new HashMap<OCRFormat, String>();
		FORMAT_MAPPING.put(OCRFormat.DOC, "MSWord");
		FORMAT_MAPPING.put(OCRFormat.HTML, "HTML");
		FORMAT_MAPPING.put(OCRFormat.XHTML, "HTML");
		FORMAT_MAPPING.put(OCRFormat.PDF, "PDF");
		FORMAT_MAPPING.put(OCRFormat.PDFA, "PDFA");
		FORMAT_MAPPING.put(OCRFormat.XML, "XML");
		FORMAT_MAPPING.put(OCRFormat.TXT, "Text");
	}

	public AbbyyTicket(OCRProcess process) {
		super(process);
	}

	protected AbbyyTicket() {
		super();
	}

	public synchronized void write(final OutputStream out,
			final String identifier) throws IOException {

		// Sanity checks
		if (out == null || config == null) {
			logger.error("OutputStream and / or configuration is not set! (" + getName() + ")");
			throw new IllegalStateException();
		}
		if (getOcrOutputs().size() < 1) {
			throw new IllegalStateException("no outputs defined!");
		}
		
		
		XMLExportSettings xmlSettings = XMLExportSettings.Factory
				.newInstance(opts);

		// coordinates for each character in output abbyy xml
		// default is false. Might be reset later if the parameter is set
		xmlSettings.setWriteCharactersFormatting(true);
		xmlSettings.setWriteCharAttributes(true);

		OCROutput xmlOutput = getOcrOutputs().get(OCRFormat.XML);
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

		for (OCRImage aoi : getOcrImages()) {
			InputFile inputFile = ticket.addNewInputFile();
			String file = ((AbbyyOCRImage) aoi).getRemoteFileName();
			inputFile.setName(file);

		}
		// Use predefined variables here
		ImageProcessingParams imageProcessingParams = (ImageProcessingParams) imageProcessingSettings
				.copy();
		if (config.convertToBW) {
			imageProcessingParams.setConvertToBWFormat(true);
		}
		imageProcessingParams.setDeskew(false);
		ticket.setImageProcessingParams(imageProcessingParams);

		if (priority != null) {
			ticket.setPriority(PRIORITY_MAP.get(getPriority()));
		}else {
			ticket.setPriority("Normal");
		}

		if (textType != null) {
			recognitionSettings.setTextTypeArray(new String[] { ToAbbyyMapper.getTextType(textType) });
		}
		// Use predefined variables here
		RecognitionParams recognitionParams = (RecognitionParams) recognitionSettings
				.copy();

		if (langs == null) {
			throw new OCRException("No language given!");
		}

		for (Locale l : langs) {
			recognitionParams.addLanguage(ToAbbyyMapper.getLanguage(l));

		}

		recognitionParams.setRecognitionQuality(QUALITY_MAP.get(quality));

		// Add default languages from config
		if (config.defaultLangs != null) {
			for (Locale l : config.defaultLangs) {
				if (!langs.contains(l)) {
					recognitionParams.addLanguage(ToAbbyyMapper.getLanguage(l));
				}
			}
		}
		ticket.setRecognitionParams(recognitionParams);
		ExportParams exportParams = ticket.addNewExportParams();
		// TODO: check if we need to set a different string if we want seperate
		// files
		if (!config.singleFile) {
			exportParams.setDocumentSeparationMethod("MergeIntoSingleFile");
		}

		if (getOcrOutputs() == null || getOcrOutputs().size() < 1) {
			throw new OCRException("No export options given!");
		}

		// Removed the array stuff here since the continue statements below made
		// trouble by adding empty elements to the list.
		List<OutputFileFormatSettings> settings = new ArrayList<OutputFileFormatSettings>();

		Map<OCRFormat, OCROutput> output = getOcrOutputs();
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
						+ of.toString() + ", ignoring it. (" + getName() + ")");
				continue;
			}
			exportFormat.setOutputFlowType("SharedFolder");
			exportFormat.setOutputFileFormat(FORMAT_MAPPING.get(of));

			AbbyyOCROutput aoo = (AbbyyOCROutput) entry.getValue();
			// TODO: Check what we need to set in single file mode
			if (aoo.getRemoteFilename().equals(
					identifier + "." + of.toString().toLowerCase())) {
				exportFormat.setNamingRule(aoo.getRemoteFilename());

			}
			if (aoo.getRemoteLocation() == null) {
				exportFormat.setOutputLocation(config.serverOutputLocation);
			} else {
				exportFormat.setOutputLocation(aoo.getRemoteLocation());
			}
			settings.add(exportFormat);
			// If single files should be created, the result files need to be
			// added to the OCROuput object

			if (config.singleFile) {
				for (OCRImage aoi : getOcrImages()) {
					String file = ((AbbyyOCRImage) aoi).getRemoteUri()
							.toString() + "." + of.toString().toLowerCase();
					try {
						aoo.setSingleFile(true);
						aoo.addResultFragment(new URI(file));
					} catch (URISyntaxException e) {
						logger.error(
								"Error while setting URI in single file mode (" + getName() + ")",
								e);
						throw new OCRException(e);
					}
				}
			}
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

		// TODO: Validation cannot be used, because the server does not accept
		// valid tickets with xsi:type attributes
		if (config.validateTicket && !ticketDoc.validate(validationOptions)) {
			logger.error("AbbyyTicket not valid! " + identifier);

			Iterator iter = validationErrors.iterator();
		    while (iter.hasNext()) {
		        logger.error(">>>>> " + iter.next() + "\n");
		    }
			throw new OCRException("AbbyyTicket not valid! " + identifier);
		}

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

	// TODO: Check if this is called if a List of OCRImage is set
	@Override
	public void addImage(OCRImage ocrImage) {
		AbbyyOCRImage aoi = new AbbyyOCRImage(ocrImage);
		String[] urlParts = ocrImage.getUri().toString().split("/");
		if (getName() == null) {
			logger.warn("Name for process not set, to avoid errors if your using parallel processes, we generate one.");
			setName(UUID.randomUUID().toString());
		}
		aoi.setRemoteFileName(getName() + "-" + urlParts[urlParts.length - 1]);
		super.addImage(aoi);
	}

	protected ConfigParser getConfig() {
		return config;
	}

	protected void setConfig(ConfigParser config) {
		this.config = config;
	}

	//
	protected void clearOcrImageList(){
		ocrImages.clear();
	}
}
