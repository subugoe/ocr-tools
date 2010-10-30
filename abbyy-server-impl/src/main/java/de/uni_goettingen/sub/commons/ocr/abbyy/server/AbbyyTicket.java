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
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.NotImplementedException;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abbyy.recognitionServer10Xml.xmlTicketV1.ExportParams;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.ImageProcessingParams;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.InputFile;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.OutputFileFormatSettings;
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
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

//TODO: one Locale might represent multiple langueages: <Language>GermanNewSpelling</Language>

@SuppressWarnings("serial")
public class AbbyyTicket extends AbstractOCRProcess implements OCRProcess {
	final static Logger logger = LoggerFactory.getLogger(AbbyyTicket.class);

	/** This Map contains the mapping from java.util.Locale to the Strings needed by Abbyy */
	public final static Map<Locale, String> LANGUAGE_MAP = new HashMap<Locale, String>();

	/** The namespace used for the AbbyyTicket files. */
	public final static String NAMESPACE = "http://www.abbyy.com/RecognitionServer1.0_xml/XmlTicket-v1.xsd";

	/** A Map containing predefined fragments (read settings) for different formats */
	protected final static Map<OCRFormat, OutputFileFormatSettings> FORMAT_FRAGMENTS;

	/** A Map containing mappings from the internal Enums to engine specific quality settings */
	protected final static Map<OCRQuality, String> QUALITY_MAP;

	/** A Map containing mappings from the internal Enums to engine specific format settings */
	public final static Map<OCRFormat, String> FORMAT_MAPPING;
	
	/** Predefined recognition parameters */
	protected final static RecognitionParams recognitionSettings;

	/** Predefined image processing parameters */
	protected final static ImageProcessingParams imageProcessingSettings;

	//TODO: get this two parameters from ConfigParser
	protected Boolean singleFile = false;

	protected Boolean convertToBW = true;
	
	//TODO: Add priorities: Low, BelowNormal, Normal, AboveNormal, High
	protected String priority = "Normal";
	
	//TODO: add a test for this
	//The timeout for the process
	protected Long processTimeout = null;
	
	// is represents the InputStream for files being read
	private InputStream is;

	// The configuration.
	protected ConfigParser config;

	protected static XmlOptions opts = new XmlOptions();

	static {
		//TODO: Add more values to this map.
		// See http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt for additional mappings
		LANGUAGE_MAP.put(Locale.GERMAN, "German");
		LANGUAGE_MAP.put(Locale.ENGLISH, "English");
		LANGUAGE_MAP.put(new Locale("la"), "Latin");
		LANGUAGE_MAP.put(new Locale("ru"), "Russian");

		opts.setSavePrettyPrint();
		opts.setSaveImplicitNamespaces(new HashMap<String, String>() {
			{
				put("", NAMESPACE);
			}
		});
		opts.setUseDefaultNamespace();

		FORMAT_FRAGMENTS = new HashMap<OCRFormat, OutputFileFormatSettings>();
		XMLExportSettings xmlSettings = XMLExportSettings.Factory.newInstance(opts);

		xmlSettings.setWriteCharactersFormatting(true);
		xmlSettings.setWriteCharAttributes(true);

		FORMAT_FRAGMENTS.put(OCRFormat.XML, (OutputFileFormatSettings) xmlSettings.changeType(OutputFileFormatSettings.type));

		PDFExportSettings pdfSettings = PDFExportSettings.Factory.newInstance(opts);

		pdfSettings.setPictureResolution(BigInteger.valueOf(300));
		pdfSettings.setQuality(BigInteger.valueOf(50));
		pdfSettings.setUseImprovedCompression(true);
		pdfSettings.setExportMode("ImageOnText");

		FORMAT_FRAGMENTS.put(OCRFormat.PDF, (OutputFileFormatSettings) pdfSettings.changeType(OutputFileFormatSettings.type));

		TextExportSettings txtSettings = TextExportSettings.Factory.newInstance(opts);

		txtSettings.setEncodingType("UTF8");

		FORMAT_FRAGMENTS.put(OCRFormat.TXT, (OutputFileFormatSettings) txtSettings.changeType(OutputFileFormatSettings.type));

		FORMAT_FRAGMENTS.put(OCRFormat.DOC, null);
		FORMAT_FRAGMENTS.put(OCRFormat.HTML, null);
		FORMAT_FRAGMENTS.put(OCRFormat.XHTML, null);
		FORMAT_FRAGMENTS.put(OCRFormat.PDFA, null);

		//This is one of Thorough, Balanced or Fast
		QUALITY_MAP = new HashMap<OCRQuality, String>();
		QUALITY_MAP.put(OCRQuality.BEST, "Thorough");
		QUALITY_MAP.put(OCRQuality.BALANCED, "Balanced");
		QUALITY_MAP.put(OCRQuality.FAST, "Fast");

		//TODO: Use the script map of OCRprocess for this
		recognitionSettings = RecognitionParams.Factory.newInstance();
		//Might be Normal, Typewriter, Matrix, OCR_A, OCR_B, MICR_E13B, Gothic
		recognitionSettings.setTextTypeArray(new String[] { "Normal" });

		imageProcessingSettings = ImageProcessingParams.Factory.newInstance();
		
		FORMAT_MAPPING = new HashMap<OCRFormat, String>();
		FORMAT_MAPPING.put(OCRFormat.DOC, "DOC");
		FORMAT_MAPPING.put(OCRFormat.HTML, "HTML");
		FORMAT_MAPPING.put(OCRFormat.XHTML, "HTML");
		FORMAT_MAPPING.put(OCRFormat.PDF, "PDF");
		FORMAT_MAPPING.put(OCRFormat.XML, "XML");
		FORMAT_MAPPING.put(OCRFormat.TXT, "Text");
	}

	public AbbyyTicket(OCRProcess process) {
		super(process);
	}

	protected AbbyyTicket() {
		super();
	}

	public AbbyyTicket(InputStream is) {
		this.is = is;
	}

	public AbbyyTicket(URL url) throws IOException {
		this(url.openStream());
	}

	public void parseTicket () throws MalformedURLException {
		//TODO: Finish this method
		XmlOptions options = new XmlOptions();
		// Set the namespace 
		options.setLoadSubstituteNamespaces(Collections.singletonMap("", NAMESPACE));
		// Load the Xml 
		XmlTicketDocument ticketDoc = null;
		try {
			ticketDoc = XmlTicketDocument.Factory.parse(is, options);
		} catch (XmlException e) {
			logger.error("Parsing of XML failed", e);
		} catch (IOException e) {
			logger.error("IO (read of ticket ) failed", e);
		}
		XmlTicket ticket = ticketDoc.getXmlTicket();
		ExportParams params = ticket.getExportParams();

		List<InputFile> fl = ticket.getInputFileList();
		for (InputFile i : fl) {
			AbbyyOCRImage aoi = new AbbyyOCRImage();
			aoi.setRemoteFileName(i.getName());
			super.addImage(aoi);
		}
		Map<OCRFormat, OCROutput> outputs = new HashMap<OCRFormat, OCROutput>();
		for (OutputFileFormatSettings offs : params.getExportFormatList()) {
			if (offs.isSetOutputFileFormat()) {
				OCRFormat format = OCRFormat.parseOCRFormat(offs.getOutputFileFormat());
				String location = offs.getOutputLocation();
				AbbyyOCROutput aoo = new AbbyyOCROutput();
				aoo.setRemoteLocation(location);
				outputs.put(format, aoo);
			}
		}
		setOcrOutputs(outputs);
	}

	public void write (OutputStream out, String identifier) throws IOException {
		//Sanity checks
		if (out == null) {
			throw new IllegalStateException();
		}
		if (getOcrOutputs().size() < 1) {
			throw new IllegalStateException("no outputs defined!");
		}
		if (config == null) {
			throw new IllegalStateException();
		}

		XmlTicketDocument ticketDoc = XmlTicketDocument.Factory.newInstance(opts);
		XmlTicket ticket = ticketDoc.addNewXmlTicket();

		if (processTimeout != null) {
			ticket.setOCRTimeout(BigInteger.valueOf(processTimeout));
		}

		for (OCRImage aoi : getOcrImages()) {
			InputFile inputFile = ticket.addNewInputFile();
			String file = ((AbbyyOCRImage) aoi).getRemoteFileName();
			inputFile.setName(file);
		}
		//Use predefined variables here
		ImageProcessingParams imageProcessingParams = (ImageProcessingParams) imageProcessingSettings.copy();
		if (convertToBW) {
			imageProcessingParams.setConvertToBWFormat(true);
		}
		imageProcessingParams.setDeskew(false);
		ticket.setImageProcessingParams(imageProcessingParams);
		
		if (priority != null) {
			ticket.setPriority(priority);
		}

		//Use predefined variables here
		RecognitionParams recognitionParams = (RecognitionParams) recognitionSettings.copy();

		if (langs == null) {
			throw new OCRException("No language given!");
		}
		for (Locale l : langs) {
			recognitionParams.addLanguage(LANGUAGE_MAP.get(l));
		}
		recognitionParams.setRecognitionQuality(QUALITY_MAP.get(quality));
		//Add default languages from config
		if (config.defaultLangs != null) {
			for (Locale l : config.defaultLangs) {
				if (!langs.contains(l)) {
					recognitionParams.addLanguage(LANGUAGE_MAP.get(l));
				}
			}
		}
		ticket.setRecognitionParams(recognitionParams);
		ExportParams exportParams = ticket.addNewExportParams();
		//TODO: check if we need to set a different string if we want seperate files
		if (!singleFile) {
			exportParams.setDocumentSeparationMethod("MergeIntoSingleFile");
		}

		if (getOcrOutputs() == null || getOcrOutputs().size() < 1) {
			throw new OCRException("No export options given!");
		}
		//Removed the array stuff here since the continue statements below made trouble by adding empty elements to the list.
		List<OutputFileFormatSettings> settings = new ArrayList<OutputFileFormatSettings>();
		
		Map<OCRFormat, OCROutput> output = getOcrOutputs();
		for (OCRFormat of : output.keySet()) {
			//the metadata is generated by default, no need to add it to the ticket
			if (of == OCRFormat.METADATA) {
				continue;
			}
			OutputFileFormatSettings exportFormat = FORMAT_FRAGMENTS.get(of);
			//The server can't handle this
			if (exportFormat == null) {
				logger.info("The server can't hand le the format " + of.toString() + ", ignoring it.");
				continue;
			}
			exportFormat.setOutputFlowType("SharedFolder");
			exportFormat.setOutputFileFormat(FORMAT_MAPPING.get(of));
		
			AbbyyOCROutput aoo = (AbbyyOCROutput) output.get(of);
			//TODO: Check what we need to set in single file mode
			exportFormat.setNamingRule(aoo.getRemoteFilename());

			if (aoo.getRemoteLocation() == null) {
				exportFormat.setOutputLocation(config.serverOutputLocation);
			} else {
				exportFormat.setOutputLocation(aoo.getRemoteLocation());
			}
			settings.add(exportFormat);
			//If single files should be created, te result files need to be added to the OCROuput object
			//TODO: Change the name of the files
			if (singleFile) {
				for (OCRImage aoi : getOcrImages()) {
					String file = ((AbbyyOCRImage) aoi).getRemoteUri().toString() + "." + of.toString().toLowerCase();
					try {
						aoo.setSingleFile(true);
						aoo.addResultFragment(new URI(file));
					} catch (URISyntaxException e) {
						logger.error("Error while setting URI in single file mode", e);
						throw new OCRException(e);
					}
				}
			} else {
				aoo.setSingleFile(true);
				//TODO: Finish this
				throw new NotImplementedException();
			}
		}
		
		for (int j = 0; j < settings.size(); j++) {
			exportParams.addNewExportFormat();
			exportParams.setExportFormatArray(j, settings.get(j));
		}
		
		ticketDoc.save(out, opts);
		if (config.validateTicket && !ticket.validate()) {
			logger.error("AbbyyTicket not valid!");
			throw new OCRException("AbbyyTicket not valid!");
		}

	}

	/*
	@Deprecated
	public void write (File ticketFile, String identifier) throws IOException {
		write(new FileOutputStream(ticketFile), identifier);
	}
	*/

	/**
	 * @return the processTimeout
	 */
	public Long getProcessTimeout () {
		return processTimeout;
	}

	/**
	 * @param processTimeout
	 *            the processTimeout to set
	 */
	public void setProcessTimeout (Long oCRTimeOut) {
		this.processTimeout = oCRTimeOut;
	}

	//TODO: Check if this is called if a List of OCRImage is set
	@Override
	public void addImage (OCRImage ocrImage) {
		AbbyyOCRImage aoi = new AbbyyOCRImage(ocrImage);
		String[] urlParts = ocrImage.getUri().toString().split("/");
		if (getName() == null) {
			logger.error("Name for process not set, to avoid errors if your using parallel processes, we generate one.");
			setName(UUID.randomUUID().toString());
		}
		aoi.setRemoteFileName(getName() + "-" + urlParts[urlParts.length - 1]);
		super.addImage(aoi);
	}

	//TODO: Try to remove this
	private static class TicketHelper {

		static Pattern p = Pattern.compile("(.*)\\\\(.*)");
		static Pattern n = Pattern.compile("(\\d.\\d*)");

		static public String getOutputName (String str) {
			Matcher m = n.matcher(str);
			m.find();
			return m.group(1);
		}

		static public String getName (String str) {
			Matcher m = p.matcher(str);
			m.find();
			return m.group(2);
		}

		static public String getLocation (String str) {
			Matcher m = p.matcher(str);
			m.find();
			return m.group(1);
		}

	}

	protected ConfigParser getConfig() {
		return config;
	}

	protected void setConfig(ConfigParser config) {
		this.config = config;
	}
	
}
