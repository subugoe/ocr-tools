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

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyImage;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOutput;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyTicket;
import de.uni_goettingen.sub.commons.ocr.api.AbstractImage;
import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrImage;
import de.uni_goettingen.sub.commons.ocr.api.OcrOutput;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrTextType;
import de.unigoettingen.sub.commons.util.stream.StreamUtils;


public class AbbyyTicketTestOld {
	private final static Logger logger = LoggerFactory.getLogger(AbbyyTicketTestOld.class);
	public static File TICKET_FILE;
	public static File LOCAL_TICKET_FILE;
	public static HashMap<OcrFormat, OcrOutput> OUTPUT_DEFINITIONS;

	private static OcrProcess process = null;

	private static FileOutputStream ticketStream;
	private static OcrImage ocri = null;

	private AbbyyTicket abbyyTicket;

	static {
		process = mock(OcrProcess.class);
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

		final AbbyyOutput aoo = new AbbyyOutput();
		aoo.setLocalUri(resultUri);

		OUTPUT_DEFINITIONS = new HashMap<OcrFormat, OcrOutput>();
		OUTPUT_DEFINITIONS.put(OcrFormat.XML, aoo);
	}

//	@BeforeClass
//	public static void init() throws FileNotFoundException,
//			MalformedURLException, URISyntaxException {
//
//		// Create some mock images
//		List<OcrImage> imgList = new ArrayList<OcrImage>();
//		for (int i = 0; i < 10; i++) {
//			ocri = mock(AbstractImage.class);
//			String imageUrl = RESOURCES.toURI().toURL().toString() + i;
//			when(ocri.getUri()).thenReturn(new URI(imageUrl));
//			AbbyyImage aoi = new AbbyyImage(ocri);
//			assertTrue(imageUrl.equals(aoi.getUri().toString()));
//			aoi.setRemoteFileName("remoteName" + i);
//			imgList.add(aoi);
//		}
//
//		assertTrue(imgList.size() == 10);
////		when(process.getOcrImages()).thenReturn(imgList);
//		assertTrue(process.getNumberOfImages() == 10);
//
//		when(ocri.getUri()).thenReturn(new File("/tmp").toURI());
//
////		when(process.getOcrOutputs()).thenReturn(OUTPUT_DEFINITIONS);
//
//	}

//	@Test
//	public void writeTicket() throws IOException {
//		abbyyTicket = new AbbyyTicket(process);
//		abbyyTicket.setConfig(new ConfigParser().parse());
//		abbyyTicket.processTimeout = AbbyyTicket.config.maxMillisPerFile
//				* process.getOcrImages().size();
//		assertTrue((AbbyyTicket.config.maxMillisPerFile * process.getOcrImages()
//				.size()) == 100000);
//
//		abbyyTicket.setTextType(OcrTextType.NORMAL);
//		// Use a stream to check if we to write it directly into a Stream
//		ticketStream = new FileOutputStream(TICKET_FILE);
//		abbyyTicket.write(ticketStream, "testTicket");
//
//		String ticket = StreamUtils.dumpInputStream(new FileInputStream(
//				TICKET_FILE));
//		logger.debug("This is the abbyyTicket\n" + ticket);
//
//		assertTrue(TICKET_FILE.exists());
//	}

}