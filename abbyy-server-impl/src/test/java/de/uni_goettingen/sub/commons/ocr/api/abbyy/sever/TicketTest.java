package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;
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

import com.abbyy.recognitionServer10Xml.xmlTicketV1.ExportParams;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.OutputFileFormatSettings;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.RecognitionParams;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.XmlTicketDocument;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.XmlTicketDocument.XmlTicket;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.helpers.Loader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.Ticket;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

public class TicketTest {

	private Ticket ticket;
	private static File basefolderFile = null;
	private static List<File> inputFiles = new ArrayList<File>();
	private static OCRProcess ocrp = null;
	private static File ticketFile;
	private static OCRImage ocri = null;
	
	@BeforeClass
	public static void init () {
		basefolderFile = getBaseFolderAsFile();
		ocrp = mock(OCRProcess.class);
		ocrp.addLanguage(Locale.GERMAN);
		when(ocrp.getLangs()).thenReturn(new HashSet(){{
				add(Locale.GERMAN);
			}
		});
		//This s just here to diplay the works of the mocking framework
		assertTrue(ocrp.getLangs().contains(Locale.GERMAN));
		ocrp.addOCRFormat(OCRFormat.PDF);
		ticketFile = new File(basefolderFile.getAbsolutePath() + "ticket.xml");
		
		ocri = mock(OCRImage.class);
		try {
			when(ocri.getUrl()).thenReturn(new File("/tmp").toURI().toURL());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void writeTicket () throws Exception {
		//TODO: Remove hard coded paths
		inputFiles.add(new File("C:/Test/515-00000001.tif/"));
		inputFiles.add(new File("C:/Test/515-00000002.tif/"));
		inputFiles.add(new File("C:/Test/515-00000003.tif/"));
		inputFiles.add(new File("C:/Test/515-00000004.tif/"));
		inputFiles.add(new File("C:/Test/515-00000005.tif/"));
		inputFiles.add(new File("C:/Test/515-00000006.tif/"));
		inputFiles.add(new File("C:/Test/515-00000007.tif/"));

		assertNotNull("base path is null", basefolderFile);
		
		

		ocrp.addImage(ocri);

		ticket = new Ticket(ocrp);
		ticket.setOutPutLocation("D:/Recognition/GDZ/output");
		ticket.setInputFiles(inputFiles);
		ticket.write(ticketFile);

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
		OutputFileFormatSettings offs = params.getExportFormatArray(0);
		RecognitionParams rp = ticket.getRecognitionParams();
		//TODO: If this fails the ticket writng method has a problem with language mapping
		assertTrue("Expecting \"German\", got \"" + rp.getLanguageArray(0) +"\"", rp.getLanguageArray(0).equals("German"));
		
		
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
}
