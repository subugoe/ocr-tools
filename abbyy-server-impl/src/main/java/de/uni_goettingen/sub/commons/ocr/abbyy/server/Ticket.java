package de.uni_goettingen.sub.commons.ocr.abbyy.server;



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
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.xmlbeans.XmlOptions;



/**
* @author mabergn
*
*/
public class Ticket extends OCRProcess{
	
	
    
	
	
	/** The ticket file. */
	protected File ticketFile;
	
	/** The validate ticket. */
	protected Boolean validateTicket = false;
	// Two hours by default
	protected Long maxOCRTimeout = 3600000l * 2;
	//protected Integer secondsPerImage = 5;
	protected Integer millisPerFile = 1200;
	protected String language;
	Map<Locale, String> langMapping = new HashMap<Locale, String>();
	
	
	
	protected static Map<OCRFormat, OutputFileFormatSettings> FORMAT_FRAGMENTS = null;
	protected static OCRFormat XML = OCRFormat.XML;
	protected static OCRFormat PDF = OCRFormat.PDF;
	protected static OCRFormat TXT = OCRFormat.TXT;
	
	private List<OCRProcess> inputFiles = new ArrayList<OCRProcess>();
	/** The opts. */
	protected static XmlOptions opts = new XmlOptions();
	
	private OCRProcess defaultParams = new OCRProcess();
	
	
	
	static {
		//Initialize some settings for the XMLBeans
		opts.setSavePrettyPrint();
		//Stupid server, doesn't understend it's own namespace
		opts.setSaveImplicitNamespaces(new HashMap() {{
			put("", "http://www.abbyy.com/RecognitionServer1.0_xml/XmlTicket-v1.xsd");
		}});
		opts.setUseDefaultNamespace();
	}
	
	static {
		FORMAT_FRAGMENTS = new HashMap<OCRFormat, OutputFileFormatSettings>();
		
		//We need to cast each element, since the stupid server gets confused by "xsi:type"
		XMLExportSettings xmlSettings = XMLExportSettings.Factory.newInstance(opts);
		xmlSettings.setWriteCharactersFormatting(true);
		xmlSettings.setWriteCharAttributes(true);
		FORMAT_FRAGMENTS.put(XML, (OutputFileFormatSettings) xmlSettings.changeType(OutputFileFormatSettings.type));
		
		PDFExportSettings pdfSettings = PDFExportSettings.Factory.newInstance(opts);
		pdfSettings.setPictureResolution(BigInteger.valueOf(300));
		pdfSettings.setQuality(BigInteger.valueOf(50));
		pdfSettings.setUseImprovedCompression(true);
		pdfSettings.setExportMode("ImageOnText");
		FORMAT_FRAGMENTS.put(PDF, (OutputFileFormatSettings) pdfSettings.changeType(OutputFileFormatSettings.type));
		
		TextExportSettings txtSettings = TextExportSettings.Factory.newInstance(opts);
		//Stupid Abbyy idiots: It's "UTF8" not "UTF-8"
		txtSettings.setEncodingType("UTF8");
		FORMAT_FRAGMENTS.put(TXT, (OutputFileFormatSettings) txtSettings.changeType(OutputFileFormatSettings.type));
		
		FORMAT_FRAGMENTS.put(OCRFormat.DOC, null);
		FORMAT_FRAGMENTS.put(OCRFormat.HTML, null);
		FORMAT_FRAGMENTS.put(OCRFormat.XHTML, null);
		FORMAT_FRAGMENTS.put(OCRFormat.PDFA, null);
		
	}
	
	public Ticket(OCRProcess params) {
		super(params);
		// TODO Auto-generated constructor stub
	}
	/*
	public Ticket() {
		// TODO Auto-generated constructor stub
	}
*/
	public void write(File ticketFile) throws IOException {
		if (ticketFile == null) {
			throw new IllegalStateException();
		}

		XmlTicketDocument ticketDoc = XmlTicketDocument.Factory.newInstance(opts);
		XmlTicket ticket = ticketDoc.addNewXmlTicket();
		
		//Integer OCRTimeOut = engineConfig.getInputFiles().size() * 1000 * secondsPerImage;
		Integer OCRTimeOut = getInputFiles().size() * millisPerFile;
		
		//TODO: this doesn't seem to work
		/*
		if (maxOCRTimeout < OCRTimeOut) {
			throw new IllegalStateException("Calculated OCR Timeout to high: " + OCRTimeOut);
		}
		
		ticket.setOCRTimeout(BigInteger.valueOf(OCRTimeOut));
		*/
		//TODO: The Method doesn't return anything yet
		for (OCRProcess params : getInputFiles()) {
			InputFile inputFile = ticket.addNewInputFile();
			String file = params.getFile();
			inputFile.setName(file);
			//TODO: 
			//logger.trace("Datei " + file + "hinzugefŸgt");
		}

		ImageProcessingParams imageProcessingParams = ticket.addNewImageProcessingParams();
		imageProcessingParams.setDeskew(false);

		RecognitionParams recognitionParams = ticket.addNewRecognitionParams();
		//TODO:  Language
		langMapping.put(Locale.GERMAN, "German");
		//langMapping.put(Locale.RUSSIAN, "Russian");
		langMapping.put(Locale.ENGLISH, "English");
		
		for (Locale l : langMapping.keySet()) {

			if (langMapping.get(l) == null) {
				//TODO
			//	throw new OCRLanguageException();
			}
			recognitionParams.addLanguage(langMapping.get(l));
		}
		
		ExportParams exportParams = ticket.addNewExportParams();
		exportParams.setDocumentSeparationMethod("MergeIntoSingleFile");
		//TODO: REmove hard coded number
		OutputFileFormatSettings[] settings = new OutputFileFormatSettings[FORMAT_FRAGMENTS.size()-4];
		Integer i = 0;
		//TODO: 
		for (OCRFormat ef : OCRFormat.values()) {
			
			//OutputFileFormatSettings exportFormat = exportParams.addNewExportFormat();
			
			OutputFileFormatSettings exportFormat = FORMAT_FRAGMENTS.get(ef);
			//TODO Add one of the export fragments here
			if (exportFormat == null) {
				continue;
			}
			exportFormat.setOutputFlowType("SharedFolder");
			exportFormat.setOutputFileFormat(ef.name());
		
			//exportFormat.setNamingRule(TicketHelper.getName(ef.name()));
			//exportFormat.setOutputLocation(TicketHelper.getLocation(ef.name()));
			
			settings[i] = exportFormat;
			i++;
		}
		//This doesn't work
		exportParams.setExportFormatArray(settings);
		//TODO: 
		ticketDoc.save(ticketFile, opts);//opts
		if (validateTicket && !ticket.validate()) {
			//TODO: 
		//	logger.error("Ticket not valid!");
			throw new RuntimeException("Ticket not valid!");
		}
	}

	
	
	public static class TicketHelper {
		//TODO catch Exceptions
		
		static Pattern p = Pattern.compile("(.*)\\\\(.*)");
		
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
	
	/*public String finereadLanguage(String language){
		
		langMapping = new HashMap<Locale, String>();
    	langMapping.put(Locale.GERMAN, "German");
		//langMapping.put(Locale.RUSSIAN, "Russian");
		langMapping.put(Locale.ENGLISH, "English");
		
    	if (language.toLowerCase().equals("german")) {
	    	language = "German";
	    } else if (language.toLowerCase().equals("english")) {
	    	language = "English";
	    } 
    	
		return language;
    }*/
	
/*	
	public OCRProcess getDefaultParams() {
		if (defaultParams != null) {
			return new OCRProcess(defaultParams);
		} else {
			return null;
		}
	}
	*/
	public List<OCRProcess> getInputFiles() {
		return inputFiles;
	}

	
	
	
	
	
	
}
