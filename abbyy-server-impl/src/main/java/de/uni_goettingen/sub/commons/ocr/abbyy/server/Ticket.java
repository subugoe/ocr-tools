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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

public class Ticket extends AbstractOCRProcess implements OCRProcess {
	final static Logger logger = LoggerFactory.getLogger(Ticket.class);

	// Should the ticket be validated.
	protected Boolean validateTicket = false;
	//The timeout for the ticket
	protected Integer oCRTimeOut = null;

	//This Map contains the mapping from java.util.Locale to the Strings needed by Abbyy
	public final static Map<Locale, String> LANGUAGE_MAP = new HashMap<Locale, String>();

	static {
		// See http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt for additional mappings
		LANGUAGE_MAP.put(Locale.GERMAN, "German");
		LANGUAGE_MAP.put(Locale.ENGLISH, "English");
		LANGUAGE_MAP.put(new Locale("la"), "Latin");
		LANGUAGE_MAP.put(new Locale("ru"), "Russian");
	}

	protected String outPutLocation;

	//The namespace used for the Ticket files.
	public static String NAMESPACE = "http://www.abbyy.com/RecognitionServer1.0_xml/XmlTicket-v1.xsd";

	protected static Map<OCRFormat, OutputFileFormatSettings> FORMAT_FRAGMENTS = null;

	// is represents the InputStream for files being read
	private InputStream is;

	protected static XmlOptions opts = new XmlOptions();
	static {
		opts.setSavePrettyPrint();
		opts.setSaveImplicitNamespaces(new HashMap<String, String>() {
			{
				put("", NAMESPACE);
			}
		});
		opts.setUseDefaultNamespace();
	}

	static {
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

	}

	public Ticket() {
	}

	public Ticket(OCRProcess process) {
		super(process);
	}

	/*protected Ticket () {
		super();
	}*/

	public Ticket(InputStream is) {
		this.is = is;
	}

	public Ticket(URL url) throws IOException {
		this(url.openStream());
	}
	
	protected void parseTicket () throws MalformedURLException {
		//TODO: Finish this method
		XmlOptions options = new XmlOptions();
		// Set the namespace 
		options.setLoadSubstituteNamespaces(Collections.singletonMap("", NAMESPACE));
		// Load the Xml 
		XmlTicketDocument ticketDoc = null;
		try {
			ticketDoc = XmlTicketDocument.Factory.parse(is, options);
		} catch (XmlException e) {
			// TODO Auto-generated catch block (log this)
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XmlTicket ticket = ticketDoc.getXmlTicket();
		ExportParams params = ticket.getExportParams();

		RecognitionParams rp = ticket.getRecognitionParams();
		List<InputFile> fl = ticket.getInputFileList();
		for (InputFile i: fl) {
			addImage(new AbbyyOCRImage(new URL(i.getName())));
		}
		for (OutputFileFormatSettings offs: params.getExportFormatList()) {
			if (offs.isSetOutputFileFormat()) {
				String format = offs.getOutputFileFormat();
				String location = offs.getOutputLocation();
			}
			
		}
			
	}
	
	public void write (OutputStream out, String identifier) throws IOException {
		if (out == null) {
			throw new IllegalStateException();
		}
		
		XmlTicketDocument ticketDoc = XmlTicketDocument.Factory.newInstance(opts);
		XmlTicket ticket = ticketDoc.addNewXmlTicket();
		
		/*
		Integer OCRTimeOut = getOcrImages().size() * millisPerFile;
		if (maxOCRTimeout < OCRTimeOut) {
			throw new IllegalStateException("Calculated OCR Timeout to high: " + OCRTimeOut);
		}
		*/
		if (oCRTimeOut != null) {
			ticket.setOCRTimeout(BigInteger.valueOf(oCRTimeOut));
		}

		for (OCRImage aoi : getOcrImages()) {
			InputFile inputFile = ticket.addNewInputFile();
			String file = ((AbbyyOCRImage) aoi).getRemoteFileName();
			inputFile.setName(file);
		}

		ImageProcessingParams imageProcessingParams = ticket.addNewImageProcessingParams();
		imageProcessingParams.setDeskew(false);
		RecognitionParams recognitionParams = ticket.addNewRecognitionParams();

		for (Locale l : langs) {
			if (langs == null) {
				throw new OCRException("No language given!");
			}
			recognitionParams.addLanguage(LANGUAGE_MAP.get(l));
		}
		ExportParams exportParams = ticket.addNewExportParams();
		exportParams.setDocumentSeparationMethod("MergeIntoSingleFile");

		//TODO: Remove hard coded number
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

			/*exportFormat.setNamingRule(TicketHelper
					.getOutputName(getInputFiles().toString())
					+ "."
					+ ef.name().toLowerCase());
			*/

			exportFormat.setNamingRule(identifier + "." + ef.name().toLowerCase());
			exportFormat.setOutputLocation(getOutPutLocation());
			settings[i] = exportFormat;
			i++;
		}

		exportParams.setExportFormatArray(settings);
		ticketDoc.save(out, opts);
		if (validateTicket && !ticket.validate()) {
			logger.error("Ticket not valid!");
			throw new RuntimeException("Ticket not valid!");
		}
		
	}

	public void write (File ticketFile, String identifier) throws IOException {
		write(new FileOutputStream(ticketFile), identifier);
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

	public String getOutPutLocation () {
		return outPutLocation;
	}

	public void setOutPutLocation (String outPutLocation) {
		this.outPutLocation = outPutLocation;
	}

	/**
	 * @return the oCRTimeOut
	 */
	public Integer getoCRTimeOut () {
		return oCRTimeOut;
	}

	/**
	 * @param oCRTimeOut the oCRTimeOut to set
	 */
	public void setoCRTimeOut (Integer oCRTimeOut) {
		this.oCRTimeOut = oCRTimeOut;
	}


	
	/*
	public List<AbbyyOCRImage> getOcrImages() {
		return null;
	}
	*/
}
