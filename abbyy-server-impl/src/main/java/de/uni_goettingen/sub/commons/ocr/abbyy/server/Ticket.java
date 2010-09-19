package de.uni_goettingen.sub.commons.ocr.abbyy.server;


import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

import com.abbyy.recognitionServer10Xml.xmlTicketV1.ExportParams;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.ImageProcessingParams;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.InputFile;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.PDFExportSettings;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.RecognitionParams;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.TextExportSettings;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.XMLExportSettings;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.OutputFileFormatSettings;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.XmlTicketDocument;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.XmlTicketDocument.XmlTicket;


import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;




public class Ticket extends AbstractOCRProcess implements OCRProcess {


	/** The ticket file. */
	protected File ticketFile;

	/** The validate ticket. */
	protected Boolean validateTicket = false;
	// Two hours by default
	protected Long maxOCRTimeout = 3600000l * 2;
	// protected Integer secondsPerImage = 5;
	protected Integer millisPerFile = 1200;
	//Language
	protected String language;
	
	public static Map<Locale, String> languageMapping = null;
	
	static {
		//TODO: Finish this.
		languageMapping.put(Locale.GERMAN, "");
	}
	private static final String GERMAN_NAME = "de";
	private static final String ENGLISH_NAME = "en";
	private static final String RUSSIAN_NAME = "ru";
	
	protected String outPutLocation;
	
	public static String NAMESPACE = "http://www.abbyy.com/RecognitionServer1.0_xml/XmlTicket-v1.xsd";

	protected static Map<OCRFormat, OutputFileFormatSettings> FORMAT_FRAGMENTS = null;



	private static List<File> inputFiles = new ArrayList<File>();
	protected static XmlOptions opts = new XmlOptions();
	
	private InputStream is;


	static {
		opts.setSavePrettyPrint();
		opts.setSaveImplicitNamespaces(new HashMap() {
			{
				put("",	NAMESPACE);
			}
		});

		opts.setSaveImplicitNamespaces(new HashMap() {
			{
				put("", NAMESPACE);
			}
		});
		opts.setUseDefaultNamespace();
	}

	static {
		FORMAT_FRAGMENTS = new HashMap<OCRFormat, OutputFileFormatSettings>();
		XMLExportSettings xmlSettings = XMLExportSettings.Factory
				.newInstance(opts);
		
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

	}

	public Ticket(OCRProcess params) {
		super(params);
	}
	
	public Ticket (InputStream is) throws IOException {
		//TODO: Finish this constructor
		this.is = is;
		XmlOptions options = new XmlOptions(); 
		// Set the namespace 
		options.setLoadSubstituteNamespaces(Collections.singletonMap("", NAMESPACE)); 
		// Load the Xml 
		XmlTicketDocument ticketDoc = null;
		try {
			ticketDoc = XmlTicketDocument.Factory.parse(is, options);
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XmlTicket ticket = ticketDoc.getXmlTicket();
		ExportParams params = ticket.getExportParams();
		OutputFileFormatSettings offs = params.getExportFormatArray(0);
		RecognitionParams rp = ticket.getRecognitionParams();


	}
	
	public Ticket (URL url) throws IOException {
		this(url.openStream());
	}
	
	//TODO: use a Outputstream for this, the method accepting the file should only be a wrapper.
	public void write (File ticketFile) throws IOException {
		if (ticketFile == null) {
			throw new IllegalStateException();
		}

		XmlTicketDocument ticketDoc = XmlTicketDocument.Factory
				.newInstance(opts);
		XmlTicket ticket = ticketDoc.addNewXmlTicket();

		Integer OCRTimeOut = getInputFiles().size() * millisPerFile;

		if (maxOCRTimeout < OCRTimeOut) {

			throw new IllegalStateException("Calculated OCR Timeout to high: "
					+ OCRTimeOut);

		}

		ticket.setOCRTimeout(BigInteger.valueOf(OCRTimeOut));

		for (File f : getInputFiles()) {
			InputFile inputFile = ticket.addNewInputFile();
			String file = f.getName();
			inputFile.setName(file);
			// TODO:
		}

		ImageProcessingParams imageProcessingParams = ticket
				.addNewImageProcessingParams();
		imageProcessingParams.setDeskew(false);

		RecognitionParams recognitionParams = ticket.addNewRecognitionParams();

		
		for (Locale l : langs) {

			if (langs == null) {

				//TODO
				//	throw new OCRLanguageException();
			}
			recognitionParams.addLanguage(toLanguage(l.getLanguage()));
		}

		ExportParams exportParams = ticket.addNewExportParams();
		exportParams.setDocumentSeparationMethod("MergeIntoSingleFile");

		//TODO: REmove hard coded number
		OutputFileFormatSettings[] settings = new OutputFileFormatSettings[FORMAT_FRAGMENTS.size() - 4];

		Integer i = 0;
		// TODO:
		for (OCRFormat ef : FORMAT_FRAGMENTS.keySet()) {

			OutputFileFormatSettings exportFormat = FORMAT_FRAGMENTS.get(ef);
			// TODO Add one of the export fragments here
			if (exportFormat == null) {
				continue;
			}
			exportFormat.setOutputFlowType("SharedFolder");
			exportFormat.setOutputFileFormat(ef.name());

			exportFormat.setNamingRule(TicketHelper
					.getOutputName(getInputFiles().toString())
					+ "."
					+ ef.name().toLowerCase());
			

			exportFormat.setNamingRule(TicketHelper.getOutputName(getInputFiles().toString()) + "." + ef.name().toLowerCase());
			
			exportFormat.setOutputLocation(getOutPutLocation());

			settings[i] = exportFormat;
			i++;
		}

		exportParams.setExportFormatArray(settings);
	
		ticketDoc.save(ticketFile, opts);// opts
		if (validateTicket && !ticket.validate()) {

			//TODO: 
			//	logger.error("Ticket not valid!");

			throw new RuntimeException("Ticket not valid!");
		}
	}

	//TODO: Do we need this?
	public static String toLanguage (String name) {
	    if (name.toLowerCase().equals(GERMAN_NAME)) {
	    	return "German";
	    } else if (name.toLowerCase().equals(ENGLISH_NAME)) {
	    	return "English";
	    } else if (name.toLowerCase().equals(RUSSIAN_NAME)) {
	    	return "Russian";
	    }
	    throw new IllegalArgumentException();
	 }

	public static class TicketHelper {

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


	public List<File> getInputFiles () {

		return inputFiles;
	}

	public void setInputFiles (List<File> inputFiles) {

		Ticket.inputFiles = inputFiles;
	}

	public String getOutPutLocation () {

		return outPutLocation;
	}


	public void setOutPutLocation (String outPutLocation) {
		this.outPutLocation = outPutLocation;

	}

}
