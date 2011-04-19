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

	/**
	 * This Map contains the mapping from java.util.Locale to the Strings needed
	 * by Abbyy
	 */
	public final static Map<Locale, String> LANGUAGE_MAP = new HashMap<Locale, String>();

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

	protected final static Map<OCRTextTyp, String> TEXTTYP_MAP;

	/** Predefined recognition parameters */
	protected static RecognitionParams recognitionSettings;

	/** Predefined image processing parameters */
	protected final static ImageProcessingParams imageProcessingSettings;

	// TODO: Add priorities: Low, BelowNormal, Normal, AboveNormal, High
	protected String priority = "Normal";

	protected static String encoding = "UTF8";

	/** The timeout for the process */
	protected Long processTimeout = null;

	/** is represents the InputStream for files being read */
	private InputStream is;

	// The configuration.
	protected ConfigParser config;

	private static XmlOptions opts = new XmlOptions();

	static {

		// See http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt 
		/**Technical contents of ISO 639:1988 (E/F)
		 * "Code for the representation of names of languages".
		 * The Registration Authority for ISO 639 is Infoterm, Osterreichisches
		 * Normungsinstitut (ON), Postfach 130, A-1021 Vienna, Austria.
		*/
		// only Abbyy Recognition Languages
		LANGUAGE_MAP.put(new Locale("ab"), "Abkhazian");	LANGUAGE_MAP.put(new Locale("az"), "Azerbaijani");
		LANGUAGE_MAP.put(new Locale("af"), "Afrikaans"); 	LANGUAGE_MAP.put(new Locale("am"), "Amharic");
		LANGUAGE_MAP.put(new Locale("ay"), "Aymara"); 	
		/*LANGUAGE_MAP.put(new Locale("ar"), "Arabic"); 		LANGUAGE_MAP.put(new Locale("as"), "Assamese");
		LANGUAGE_MAP.put(new Locale("aa"), "Afar"); */
		
		
		LANGUAGE_MAP.put(new Locale("ba"), "Bashkir"); 		LANGUAGE_MAP.put(new Locale("be"), "Byelorussian");
		LANGUAGE_MAP.put(new Locale("bg"), "Bulgarian");	LANGUAGE_MAP.put(new Locale("br"), "Breton");
		/*LANGUAGE_MAP.put(new Locale("bo"), "Tibetan"); 		LANGUAGE_MAP.put(new Locale("bh"), "Bihari");
		LANGUAGE_MAP.put(new Locale("bi"), "Bislama"); 		LANGUAGE_MAP.put(new Locale("bn"), "Bengali");*/
		
		LANGUAGE_MAP.put(new Locale("ca"), "Catalan"); 		LANGUAGE_MAP.put(new Locale("co"), "Corsican");
		LANGUAGE_MAP.put(new Locale("cs"), "Czech"); 		LANGUAGE_MAP.put(new Locale("cy"), "Welsh");
		
		LANGUAGE_MAP.put(new Locale("da"), "Danish"); 		LANGUAGE_MAP.put(Locale.GERMAN, "German");
		/*LANGUAGE_MAP.put(new Locale("dz"), "Bhutani");*/
		
		LANGUAGE_MAP.put(new Locale("el"), "Greek");		LANGUAGE_MAP.put(Locale.ENGLISH, "English");
		LANGUAGE_MAP.put(new Locale("es"), "Spanish");      LANGUAGE_MAP.put(new Locale("et"), "Estonian"); 	
		LANGUAGE_MAP.put(new Locale("eu"), "Basque");
		/*LANGUAGE_MAP.put(new Locale("eo"), "Esperanto");*/ 
		
		LANGUAGE_MAP.put(new Locale("fi"), "Finnish");      LANGUAGE_MAP.put(new Locale("fo"), "Faroese");
		LANGUAGE_MAP.put(new Locale("fr"), "French"); 		LANGUAGE_MAP.put(new Locale("fy"), "Frisian");
		/*LANGUAGE_MAP.put(new Locale("fa"), "Persian");  LANGUAGE_MAP.put(new Locale("fj"), "Fiji"); */
		
		LANGUAGE_MAP.put(new Locale("ga"), "Irish");		LANGUAGE_MAP.put(new Locale("gd"), "Scots Gaelic");
		LANGUAGE_MAP.put(new Locale("gl"), "Galician"); 	LANGUAGE_MAP.put(new Locale("gn"), "Guarani");
		LANGUAGE_MAP.put(new Locale("gu"), "Gujarati"); 	
		
		LANGUAGE_MAP.put(new Locale("ha"), "Hausa");		LANGUAGE_MAP.put(new Locale("he"), "Hebrew");
		LANGUAGE_MAP.put(new Locale("hr"), "Croatian");		LANGUAGE_MAP.put(new Locale("hy"), "Armenian");
		LANGUAGE_MAP.put(new Locale("hu"), "Hungarian"); 	
		/*LANGUAGE_MAP.put(new Locale("hi"), "Hindi"); */
		
		LANGUAGE_MAP.put(new Locale("id"), "Indonesian"); 	LANGUAGE_MAP.put(new Locale("it"), "Italian");
		/*LANGUAGE_MAP.put(new Locale("ie"), "Interlingue"); 	LANGUAGE_MAP.put(new Locale("ik"), "Inupiak");
		LANGUAGE_MAP.put(new Locale("is"), "Icelandic"); 	 LANGUAGE_MAP.put(new Locale("ia"), "Interlingua");
		LANGUAGE_MAP.put(new Locale("iu"), "Inuktitut");  */ 
		
		LANGUAGE_MAP.put(new Locale("ja"), "Japanese");		
		/*LANGUAGE_MAP.put(new Locale("jw"), "Javanese");*/
		
		LANGUAGE_MAP.put(new Locale("ko"), "Korean");		LANGUAGE_MAP.put(new Locale("ku"), "Kurdish");
		LANGUAGE_MAP.put(new Locale("ky"), "Kirghiz"); 		LANGUAGE_MAP.put(new Locale("kk"), "Kazakh");
		/*LANGUAGE_MAP.put(new Locale("ka"), "Georgian");		LANGUAGE_MAP.put(new Locale("kn"), "Kannada"); 		
		LANGUAGE_MAP.put(new Locale("kl"), "Greenlandic"); 	LANGUAGE_MAP.put(new Locale("km"), "Cambodian");
		LANGUAGE_MAP.put(new Locale("ks"), "Kashmiri");*/
		
		LANGUAGE_MAP.put(new Locale("la"), "Latin"); 		LANGUAGE_MAP.put(new Locale("lt"), "Lithuanian");	
		LANGUAGE_MAP.put(new Locale("lv"), "Latvian");
		/*LANGUAGE_MAP.put(new Locale("ln"), "Lingala");  LANGUAGE_MAP.put(new Locale("lo"), "Laothian");	*/
	 	
		
		LANGUAGE_MAP.put(new Locale("mg"), "Malagasy");		LANGUAGE_MAP.put(new Locale("mi"), "Maori");
		LANGUAGE_MAP.put(new Locale("mk"), "Macedonian"); 	LANGUAGE_MAP.put(new Locale("ms"), "Malay");	
		LANGUAGE_MAP.put(new Locale("mn"), "Mongolian"); 	LANGUAGE_MAP.put(new Locale("mo"), "Moldavian");
		LANGUAGE_MAP.put(new Locale("mt"), "Maltese"); 		
		/*LANGUAGE_MAP.put(new Locale("my"), "Burmese"); LANGUAGE_MAP.put(new Locale("ml"), "Malayalam");
		LANGUAGE_MAP.put(new Locale("mr"), "Marathi");
		*/
		
		LANGUAGE_MAP.put(new Locale("nl"), "Dutch"); 		LANGUAGE_MAP.put(new Locale("no"), "Norwegian");
		/*LANGUAGE_MAP.put(new Locale("na"), "Nauru");		LANGUAGE_MAP.put(new Locale("ne"), "Nepali");*/
	
			LANGUAGE_MAP.put(new Locale("oc"), "Occitan");		
		/*LANGUAGE_MAP.put(new Locale("om"), "Oromo");LANGUAGE_MAP.put(new Locale("or"), "Oriya"); */
		
		LANGUAGE_MAP.put(new Locale("pl"), "Polish");		LANGUAGE_MAP.put(new Locale("pt"), "Portuguese");
		/*LANGUAGE_MAP.put(new Locale("pa"), "Punjabi");	LANGUAGE_MAP.put(new Locale("ps"), "Pashto"); */	
		
		LANGUAGE_MAP.put(new Locale("qu"), "Quechua");
		
		LANGUAGE_MAP.put(new Locale("rm"),"Rhaeto-Romance");  LANGUAGE_MAP.put(new Locale("ru"), "Russian");
		LANGUAGE_MAP.put(new Locale("ro"), "Romanian");		
		/*LANGUAGE_MAP.put(new Locale("rw"), "Kinyarwanda"); LANGUAGE_MAP.put(new Locale("rn"), "Kirundi");*/
		
		LANGUAGE_MAP.put(new Locale("sk"), "Slovak");		LANGUAGE_MAP.put(new Locale("sv"), "Swedish");
		LANGUAGE_MAP.put(new Locale("sl"), "Slovenian");	LANGUAGE_MAP.put(new Locale("sm"), "Samoan");
		LANGUAGE_MAP.put(new Locale("sn"), "Shona"); 		LANGUAGE_MAP.put(new Locale("so"), "Somali");
		LANGUAGE_MAP.put(new Locale("sq"), "Albanian"); 	LANGUAGE_MAP.put(new Locale("sr"), "Serbian");
		LANGUAGE_MAP.put(new Locale("sw"), "Swahili");
		/*LANGUAGE_MAP.put(new Locale("sa"), "Sanskrit");		LANGUAGE_MAP.put(new Locale("sd"), "Sindhi");
		LANGUAGE_MAP.put(new Locale("sg"), "Sangho"); 		LANGUAGE_MAP.put(new Locale("sh"), "Serbo-Croatian");
		LANGUAGE_MAP.put(new Locale("si"), "Sinhalese");	LANGUAGE_MAP.put(new Locale("su"), "Sundanese");
		LANGUAGE_MAP.put(new Locale("ss"), "Siswati");		LANGUAGE_MAP.put(new Locale("st"), "Sesotho");*/
		
		
		
		LANGUAGE_MAP.put(new Locale("tg"), "Tajik"); 		LANGUAGE_MAP.put(new Locale("th"), "Thai");
		LANGUAGE_MAP.put(new Locale("tk"), "Turkmen");		LANGUAGE_MAP.put(new Locale("tl"), "Tagalog");	
		LANGUAGE_MAP.put(new Locale("to"), "Tonga"); 		LANGUAGE_MAP.put(new Locale("tr"), "Turkish");
		LANGUAGE_MAP.put(new Locale("tt"), "Tatar");
		/*LANGUAGE_MAP.put(new Locale("ts"), "Tsonga"); 		LANGUAGE_MAP.put(new Locale("ti"), "Tigrinya"); 	  		
		LANGUAGE_MAP.put(new Locale("tn"), "Setswana");		LANGUAGE_MAP.put(new Locale("tw"), "Twi");
		LANGUAGE_MAP.put(new Locale("ta"), "Tamil");		LANGUAGE_MAP.put(new Locale("te"), "Telugu");*/
	
		LANGUAGE_MAP.put(new Locale("ug"), "Uighur");		LANGUAGE_MAP.put(new Locale("uk"), "Ukrainian");
		LANGUAGE_MAP.put(new Locale("uz"), "Uzbek");
		/*LANGUAGE_MAP.put(new Locale("ur"), "Urdu");*/ 	
		
		/*LANGUAGE_MAP.put(new Locale("vi"), "Vietnamese"); 	LANGUAGE_MAP.put(new Locale("vo"), "Volapuk");*/
		
		LANGUAGE_MAP.put(new Locale("wo"), "Wolof");
		
		LANGUAGE_MAP.put(new Locale("xh"), "Xhosa");
		
		LANGUAGE_MAP.put(new Locale("yi"), "Yiddish"); 		
		/*LANGUAGE_MAP.put(new Locale("yo"), "Yoruba");*/
		
		LANGUAGE_MAP.put(new Locale("zu"), "Zulu");
		LANGUAGE_MAP.put(new Locale("zh"), "Chinese");
		/*LANGUAGE_MAP.put(new Locale("za"), "Zhuang");	*/

		opts.setSavePrettyPrint();
		opts.setSaveImplicitNamespaces(new HashMap<String, String>() {
			{
				put("", NAMESPACE);
			}
		});
		opts.setUseDefaultNamespace();

		FORMAT_FRAGMENTS = new HashMap<OCRFormat, OutputFileFormatSettings>();
		XMLExportSettings xmlSettings = XMLExportSettings.Factory
				.newInstance(opts);

		xmlSettings.setWriteCharactersFormatting(true);
		xmlSettings.setWriteCharAttributes(true);

		FORMAT_FRAGMENTS.put(OCRFormat.XML,
				(OutputFileFormatSettings) xmlSettings
						.changeType(OutputFileFormatSettings.type));

		PDFExportSettings pdfSettings = PDFExportSettings.Factory
				.newInstance(opts);

		pdfSettings.setPictureResolution(BigInteger.valueOf(300));
		pdfSettings.setQuality(BigInteger.valueOf(50));
		pdfSettings.setUseImprovedCompression(true);
		pdfSettings.setExportMode("ImageOnText");

		FORMAT_FRAGMENTS.put(OCRFormat.PDF,
				(OutputFileFormatSettings) pdfSettings
						.changeType(OutputFileFormatSettings.type));

		TextExportSettings txtSettings = TextExportSettings.Factory
				.newInstance(opts);

		txtSettings.setEncodingType(encoding);

		FORMAT_FRAGMENTS.put(OCRFormat.TXT,
				(OutputFileFormatSettings) txtSettings
						.changeType(OutputFileFormatSettings.type));

		FORMAT_FRAGMENTS.put(OCRFormat.DOC, null);
		FORMAT_FRAGMENTS.put(OCRFormat.HTML, null);
		FORMAT_FRAGMENTS.put(OCRFormat.XHTML, null);
		FORMAT_FRAGMENTS.put(OCRFormat.PDFA, null);

		// This is one of Thorough, Balanced or Fast
		QUALITY_MAP = new HashMap<OCRQuality, String>();
		QUALITY_MAP.put(OCRQuality.BEST, "Thorough");
		QUALITY_MAP.put(OCRQuality.BALANCED, "Balanced");
		QUALITY_MAP.put(OCRQuality.FAST, "Fast");

		recognitionSettings = RecognitionParams.Factory.newInstance();
		// Might be Normal, Typewriter, Matrix, OCR_A, OCR_B, MICR_E13B, Gothic
		TEXTTYP_MAP = new HashMap<OCRTextTyp, String>();
		TEXTTYP_MAP.put(OCRTextTyp.NORMAL, "Normal");
		TEXTTYP_MAP.put(OCRTextTyp.TYPEWRITER, "Typewriter");
		TEXTTYP_MAP.put(OCRTextTyp.MATRIX, "Matrix");
		TEXTTYP_MAP.put(OCRTextTyp.OCR_A, "OCR_A");
		TEXTTYP_MAP.put(OCRTextTyp.OCR_B, "OCR_B");
		TEXTTYP_MAP.put(OCRTextTyp.MICR_E13B, "MICR_E13B");
		TEXTTYP_MAP.put(OCRTextTyp.GOTHIC, "Gothic");

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

	public void parseTicket() throws MalformedURLException {
		// TODO: Finish this method
		XmlOptions options = new XmlOptions();
		// Set the namespace
		options.setLoadSubstituteNamespaces(Collections.singletonMap("",
				NAMESPACE));
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
				OCRFormat format = OCRFormat.parseOCRFormat(offs
						.getOutputFileFormat());
				String location = offs.getOutputLocation();
				AbbyyOCROutput aoo = new AbbyyOCROutput();
				aoo.setRemoteLocation(location);
				outputs.put(format, aoo);
			}
		}
		setOcrOutputs(outputs);
	}

	public synchronized void write(final OutputStream out,
			final String identifier) throws IOException {

		// Sanity checks
		if (out == null || config == null) {
			logger.error("OutputStream and / or configuration is not set!");
			throw new IllegalStateException();
		}
		if (getOcrOutputs().size() < 1) {
			throw new IllegalStateException("no outputs defined!");
		}

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

		if (ticket.getPriority()== null) {
			ticket.setPriority(priority);
		}else ticket.getPriority();

		if (texttyp != null) {
			recognitionSettings.setTextTypeArray(new String[] { TEXTTYP_MAP
					.get(getTextTyp()) });
		}
		// Use predefined variables here
		RecognitionParams recognitionParams = (RecognitionParams) recognitionSettings
				.copy();

		if (langs == null) {
			throw new OCRException("No language given!");
		}

		for (Locale l : langs) {
			recognitionParams.addLanguage(LANGUAGE_MAP.get(l));

		}

		recognitionParams.setRecognitionQuality(QUALITY_MAP.get(quality));

		// Add default languages from config
		if (config.defaultLangs != null) {
			for (Locale l : config.defaultLangs) {
				if (!langs.contains(l)) {
					recognitionParams.addLanguage(LANGUAGE_MAP.get(l));
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
		for (OCRFormat of : output.keySet()) {
			// the metadata is generated by default, no need to add it to the
			// ticket
			if (of == OCRFormat.METADATA) {
				continue;
			}

			OutputFileFormatSettings exportFormat = FORMAT_FRAGMENTS.get(of);
			// The server can't handle this
			if (exportFormat == null) {
				logger.info("The server can't hand le the format "
						+ of.toString() + ", ignoring it.");
				continue;
			}
			exportFormat.setOutputFlowType("SharedFolder");
			exportFormat.setOutputFileFormat(FORMAT_MAPPING.get(of));

			AbbyyOCROutput aoo = (AbbyyOCROutput) output.get(of);
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
								"Error while setting URI in single file mode",
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

		ticketDoc.save(out, opts);
		if (config.validateTicket && !ticket.validate()) {
			logger.error("AbbyyTicket not valid!");
			throw new OCRException("AbbyyTicket not valid!");
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
			logger.error("Name for process not set, to avoid errors if your using parallel processes, we generate one.");
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

}
