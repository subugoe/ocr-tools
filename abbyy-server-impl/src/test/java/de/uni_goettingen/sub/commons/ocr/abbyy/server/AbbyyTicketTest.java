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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

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
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyTicket;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;
import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextType;
import de.unigoettingen.sub.commons.util.stream.StreamUtils;


public class AbbyyTicketTest {
	private final static Logger logger = LoggerFactory.getLogger(AbbyyTicketTest.class);
	public static File TICKET_FILE;
	public static File LOCAL_TICKET_FILE;
	public static HashMap<OCRFormat, OCROutput> OUTPUT_DEFINITIONS;

	private static OCRProcess process = null;

	private static FileOutputStream ticketStream;
	private static OCRImage ocri = null;

	private AbbyyTicket abbyyTicket;

	static {
		process = mock(OCRProcess.class);
		when(process.getLanguages()).thenReturn(new HashSet<Locale>() {
			private static final long serialVersionUID = -847225577844475697L;
			{
				add(Locale.GERMAN);
				add(new Locale("la"));
			}
		});
		TICKET_FILE = new File(MISC, "abbyyTicket.xml");
		LOCAL_TICKET_FILE = new File(LOCAL_INPUT, "abbyyTicket.xml");

		URI resultUri = null;
		try {
			resultUri = new URI(DAV_OUTPUT.toURI() + "/" + "result" + "/"
					+ "result");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		final AbbyyOCROutput aoo = new AbbyyOCROutput(resultUri);
		aoo.setRemoteFilename("result");

		OUTPUT_DEFINITIONS = new HashMap<OCRFormat, OCROutput>();
		OUTPUT_DEFINITIONS.put(OCRFormat.XML, aoo);
	}

	@BeforeClass
	public static void init() throws FileNotFoundException,
			MalformedURLException, URISyntaxException {

		// Create some mock images
		List<OCRImage> imgList = new ArrayList<OCRImage>();
		for (int i = 0; i < 10; i++) {
			ocri = mock(AbstractOCRImage.class);
			String imageUrl = RESOURCES.toURI().toURL().toString() + i;
			when(ocri.getUri()).thenReturn(new URI(imageUrl));
			AbbyyOCRImage aoi = new AbbyyOCRImage(ocri);
			assertTrue(imageUrl.equals(aoi.getUri().toString()));
			aoi.setRemoteFileName("remoteName" + i);
			imgList.add(aoi);
		}

		assertTrue(imgList.size() == 10);
		when(process.getOcrImages()).thenReturn(imgList);
		assertTrue(process.getOcrImages().size() == 10);

		when(ocri.getUri()).thenReturn(new File("/tmp").toURI());

		when(process.getOcrOutputs()).thenReturn(OUTPUT_DEFINITIONS);

	}

	@Test
	public void writeTicket() throws IOException {
		abbyyTicket = new AbbyyTicket(process);
		abbyyTicket.setConfig(new ConfigParser().parse());
		abbyyTicket.processTimeout = AbbyyTicket.config.maxMillisPerFile
				* process.getOcrImages().size();
		assertTrue((AbbyyTicket.config.maxMillisPerFile * process.getOcrImages()
				.size()) == 100000);

		abbyyTicket.setTextType(OCRTextType.NORMAL);
		// Use a stream to check if we to write it directly into a Stream
		ticketStream = new FileOutputStream(TICKET_FILE);
		abbyyTicket.write(ticketStream, "testTicket");

		String ticket = StreamUtils.dumpInputStream(new FileInputStream(
				TICKET_FILE));
		logger.debug("This is the abbyyTicket\n" + ticket);

		assertTrue(TICKET_FILE.exists());
	}

	@Test
	public void readTicket() throws XmlException, IOException {
		XmlOptions options = new XmlOptions();
		// Set the namespace
		options.setLoadSubstituteNamespaces(Collections.singletonMap("",
				AbbyyTicket.NAMESPACE));
		// Load the Xml
		XmlTicketDocument ticketDoc = XmlTicketDocument.Factory.parse(
				LOCAL_TICKET_FILE, options);

		XmlTicket ticket = ticketDoc.getXmlTicket();
		ExportParams params = ticket.getExportParams();

		RecognitionParams rp = ticket.getRecognitionParams();

		// If this fails the abbyyTicket writing method has a problem with
		// language mapping
		logger.debug("Checking languages");
		for (String lang : rp.getLanguageList()) {
			logger.debug("found language:" + lang);
			assertTrue(AbbyyTicket.LANGUAGE_MAP.containsValue(lang));
		}

		// Compare the files from the abbyyTicket with the mock object
		Integer numFiles = process.getOcrImages().size();
		logger.debug("Checking " + numFiles + " files");
		List<String> ticketFiles = parseFilesFromTicket(LOCAL_TICKET_FILE, 10);
		for (int i = 0; i < numFiles; i++) {
			String mFilename = ((AbbyyOCRImage) process.getOcrImages().get(i))
					.getRemoteFileName();
			String tFilename = ticketFiles.get(i);
			logger.debug("File from mock object: " + mFilename);
			logger.debug("File from abbyyTicket file: " + tFilename);
			assertTrue(mFilename.equals(tFilename));
		}
		// Check the definitions of outputs and their locations
		logger.debug("Checking output definitions");
		for (OutputFileFormatSettings offs : params.getExportFormatList()) {
			if (offs.isSetOutputFileFormat()) {
				OCRFormat format = OCRFormat.parseOCRFormat(offs
						.getOutputFileFormat());
				String location = offs.getOutputLocation();
				logger.debug("Output location for format " + format.name()
						+ " is " + location);
				assertTrue(OUTPUT_DEFINITIONS.containsKey(format));
			}
		}
	}

	public static List<String> parseFilesFromTicket(File ticketFile)
			throws XmlException, IOException {
		return parseFilesFromTicket(ticketFile, null);
	}

	public static List<String> parseFilesFromTicket(File ticketFile,
			Integer expectedSize) throws XmlException, IOException {
		List<String> files = new ArrayList<String>();
		AbbyyTicket t = new AbbyyTicket(new FileInputStream(ticketFile));
		t.parseTicket();
		if (expectedSize != null) {
			assertTrue("Expected size of " + expectedSize, t.getOcrImages()
					.size() == expectedSize);
		}

		for (OCRImage oi : t.getOcrImages()) {
			AbbyyOCRImage aoi = (AbbyyOCRImage) oi;
			assertTrue("File is set but contains no file name", aoi
					.getRemoteFileName().length() > 0);
			logger.debug("Found reference to " + aoi.getRemoteFileName()
					+ " in abbyyTicket.");
			files.add(aoi.getRemoteFileName());
		}

		return files;
	}

	@AfterClass
	public static void cleanup() {
		logger.debug("Cleaning up");
		
		//TICKET_FILE.delete();
	}

}
