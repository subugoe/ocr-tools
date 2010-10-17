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
import java.io.IOException;
import java.io.InputStream;
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
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

public class Ticket extends AbstractOCRProcess implements OCRProcess {

	/** The ticket file. */
	protected File ticketFile;

	// Should the ticket be validated.
	protected Boolean validateTicket = false;
	// Two hours by default
	protected Long maxOCRTimeout = 3600000l * 2;
	// protected Integer secondsPerImage = 5;
	protected Integer millisPerFile = 1200;
	//Language
	protected String language;

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

	protected static List<File> inputFiles = new ArrayList<File>();

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

	public Ticket(OCRProcess params) {
		super(params);
	}

	/*protected Ticket () {
		super();
	}*/

	public Ticket(InputStream is) throws IOException {
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
			// TODO Auto-generated catch block (log this)
			e.printStackTrace();
		}
		XmlTicket ticket = ticketDoc.getXmlTicket();
		ExportParams params = ticket.getExportParams();
		OutputFileFormatSettings offs = params.getExportFormatArray(0);
		RecognitionParams rp = ticket.getRecognitionParams();

	}

	public Ticket(URL url) throws IOException {
		this(url.openStream());
	}

	//TODO: use a Outputstream for this, the method accepting the file should only be a wrapper.
	public void write (File ticketFile, String identifier) throws IOException {
		if (ticketFile == null) {
			throw new IllegalStateException();
		}

		XmlTicketDocument ticketDoc = XmlTicketDocument.Factory.newInstance(opts);
		XmlTicket ticket = ticketDoc.addNewXmlTicket();
		Integer OCRTimeOut = getInputFiles().size() * millisPerFile;
		if (maxOCRTimeout < OCRTimeOut) {
			throw new IllegalStateException("Calculated OCR Timeout to high: " + OCRTimeOut);
		}

		ticket.setOCRTimeout(BigInteger.valueOf(OCRTimeOut));

		for (File f : getInputFiles()) {
			InputFile inputFile = ticket.addNewInputFile();
			String file = f.getName();
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

		ticketDoc.save(ticketFile, opts);// opts
		if (validateTicket && !ticket.validate()) {

			//TODO: 
			//	logger.error("Ticket not valid!");

			throw new RuntimeException("Ticket not valid!");
		}
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

	public Long getMaxOCRTimeout () {
		return maxOCRTimeout;
	}

	public void setMaxOCRTimeout (Long maxOCRTimeout) {
		this.maxOCRTimeout = maxOCRTimeout;
	}

	public Integer getMillisPerFile () {
		return millisPerFile;
	}

	public void setMillisPerFile (Integer millisPerFile) {
		this.millisPerFile = millisPerFile;
	}

}
