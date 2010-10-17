package de.uni_goettingen.sub.commons.ocr.abbyy.sever;

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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.helpers.Loader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abbyy.recognitionServer10Xml.xmlTicketV1.ExportParams;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.OutputFileFormatSettings;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.RecognitionParams;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.XmlTicketDocument;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.XmlTicketDocument.XmlTicket;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRImage;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCROutput;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.Ticket;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

public class TicketTest {
	final static Logger logger = LoggerFactory.getLogger(TicketTest.class);
	protected static String extension = "tif";
	private Ticket ticket;
	private static File basefolderFile = null;

	private static OCRProcess ocrp = null;
	private static File ticketFile;
	private static FileOutputStream ticketStream;
	private static OCRImage ocri = null;
	public static String OUTPUT_LOCATION = "D:\\Recognition\\GDZ\\output";
	protected String name = "515";
	protected static HashMap<OCRFormat, OCROutput> outputDefinitions;

	public Hotfolder hotfolder;

	/**
	 * 
	 */
	private static final long serialVersionUID = 5384097471130557653L;

	@SuppressWarnings("serial")
	@BeforeClass
	public static void init () throws FileNotFoundException, MalformedURLException {
		basefolderFile = getBaseFolderAsFile();
		ocrp = mock(OCRProcess.class);
		ocrp.addLanguage(Locale.GERMAN);
		when(ocrp.getLangs()).thenReturn(new HashSet<Locale>() {
			{
				add(Locale.GERMAN);
				add(new Locale("la"));
			}
		});
		//This s just here to display the works of the mocking framework
		assertTrue(ocrp.getLangs().contains(Locale.GERMAN));

		ocrp.addOCRFormat(OCRFormat.PDF);

		ticketFile = new File(basefolderFile.getAbsolutePath() + "ticket.xml");

		//Create some mock images
		List<OCRImage> imgList = new ArrayList<OCRImage>();
		for (int i = 0; i < 10; i++) {
			ocri = mock(OCRImage.class);
			String imageUrl = basefolderFile.toURI().toURL().toString() + i;
			when(ocri.getUrl()).thenReturn(new URL(imageUrl));
			logger.debug("Added url to list: " + imageUrl);
			AbbyyOCRImage aoi = new AbbyyOCRImage(ocri);
			assertTrue(imageUrl.equals(aoi.getUrl().toString()));
			aoi.setRemoteFileName("remoteName" + i);
			imgList.add(aoi);
		}

		assertTrue(imgList.size() == 10);
		when(ocrp.getOcrImages()).thenReturn(imgList);
		assertTrue(ocrp.getOcrImages().size() == 10);

		try {
			when(ocri.getUrl()).thenReturn(new File("/tmp").toURI().toURL());
		} catch (MalformedURLException e) {
			logger.debug("This should never happen", e);
		}

		final AbbyyOCROutput aoo = new AbbyyOCROutput();
		aoo.setRemoteLocation(OUTPUT_LOCATION);

		outputDefinitions = new HashMap<OCRFormat, OCROutput>() {
			{
				put(OCRFormat.PDF, aoo);
				put(OCRFormat.XML, aoo);
			}
		};

		when(ocrp.getOcrOutput()).thenReturn(outputDefinitions);

	}

	@Test
	public void writeTicket () throws IOException {
		assertNotNull("base path is null", basefolderFile);
		ticket = new Ticket(ocrp);

		ticketStream = new FileOutputStream(ticketFile);
		ticket.write(ticketStream, name);

		String ticket = dumpTicket(new FileInputStream(ticketFile));
		logger.debug("This is the ticket\n" + ticket);

		assertTrue(ticketFile.exists());
	}

	@Test
	public void readTicket () throws XmlException, IOException {
		XmlOptions options = new XmlOptions();
		// Set the namespace 
		options.setLoadSubstituteNamespaces(Collections.singletonMap("", Ticket.NAMESPACE));
		// Load the Xml 
		XmlTicketDocument ticketDoc = XmlTicketDocument.Factory.parse(ticketFile, options);

		XmlTicket ticket = ticketDoc.getXmlTicket();
		ExportParams params = ticket.getExportParams();

		RecognitionParams rp = ticket.getRecognitionParams();

		//If this fails the ticket writing method has a problem with language mapping
		logger.debug("Checking languages");
		for (String lang : rp.getLanguageList()) {
			logger.debug("found language:" + lang);
			assertTrue(Ticket.LANGUAGE_MAP.containsValue(lang));
		}

		//Compare the files from the ticket with the mock object
		Integer numFiles = ocrp.getOcrImages().size();
		logger.debug("Checking " + numFiles + " files");
		List<String> ticketFiles = parseFilesFromTicket(ticketFile);
		for (int i = 0; i < numFiles; i++) {
			String mFilename = ((AbbyyOCRImage) ocrp.getOcrImages().get(i)).getRemoteFileName();
			String tFilename = ticketFiles.get(i);
			logger.debug("File from mock object: " + mFilename);

			logger.debug("File from ticket file: " + tFilename);
			assertTrue(mFilename.equals(tFilename));
		}
		//Check the definitions of outputs and their locations
		logger.debug("Checking output definitions");
		for (OutputFileFormatSettings offs : params.getExportFormatList()) {
			if (offs.isSetOutputFileFormat()) {
				OCRFormat format = OCRFormat.parseOCRFormat(offs.getOutputFileFormat());
				String location = offs.getOutputLocation();
				logger.debug("Output location for format " + format.name() + " is " + location);
				assertTrue(outputDefinitions.containsKey(format));
				assertTrue(((AbbyyOCROutput) outputDefinitions.get(format)).getRemoteLocation().equals(location));
			}

		}
	}

	public static List<String> parseFilesFromTicket (File ticketFile) throws XmlException, IOException {
		List<String> files = new ArrayList<String>();
		Ticket t = new Ticket(new FileInputStream(ticketFile));
		t.parseTicket();
		assertTrue(t.getOcrImages().size() == 10);
		for (OCRImage oi : t.getOcrImages()) {
			AbbyyOCRImage aoi = (AbbyyOCRImage) oi;
			logger.debug("Found reference to " + aoi.getRemoteFileName() + " in ticket.");
			files.add(aoi.getRemoteFileName());
		}

		return files;
	}

	public static File getBaseFolderAsFile () {
		File basefolder;
		// TODO: GDZ: Do wee really need to depend on Log4J here? I don't think so...
		URL url = Loader.getResource("");
		try {
			basefolder = new File(url.toURI());
		} catch (URISyntaxException ue) {
			basefolder = new File(url.getPath());
		}
		return basefolder;
	}

	public static String dumpTicket (InputStream in) throws IOException {
		if (in != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				in.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}
	
	@AfterClass
	public static void cleanup () {
		logger.debug("Cleaning up");
		ticketFile.delete();
		assertTrue(!ticketFile.exists());
	}

}
