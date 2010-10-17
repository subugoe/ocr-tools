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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.vfs.FileSystemException;
import org.apache.log4j.helpers.Loader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abbyy.recognitionServer10Xml.xmlTicketV1.ExportParams;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.InputFile;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.OutputFileFormatSettings;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.RecognitionParams;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.XmlTicketDocument;
import com.abbyy.recognitionServer10Xml.xmlTicketV1.XmlTicketDocument.XmlTicket;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerEngine;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.Ticket;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

public class TicketTest {
	final static Logger logger = LoggerFactory.getLogger(TicketTest.class);
	public static OCREngine abbyy;
	protected static String extension = "tif";
	private Ticket ticket;
	private static File basefolderFile = null;
	private static List<File> inputFiles = new ArrayList<File>();
	private static OCRProcess ocrp = null;
	private static File ticketFile;
	private static OCRImage ocri = null;
	String name = "515";
	
	public Hotfolder hotfolder;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5384097471130557653L;
	
	@BeforeClass
	public static void init () throws FileSystemException, ConfigurationException {
		basefolderFile = getBaseFolderAsFile();
		ocrp = mock(OCRProcess.class);
		ocrp.addLanguage(Locale.GERMAN);
		when(ocrp.getLangs()).thenReturn(new HashSet() {
			{
				add(Locale.GERMAN);
			}
		});
		//This s just here to display the works of the mocking framework
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
		

			abbyy = AbbyyServerEngine.getInstance();
			assertNotNull(abbyy);
			


	}

	@Test
	public void writeTicket () throws Exception {
		//TODO: Remove hard coded paths

		assertNotNull("base path is null", basefolderFile);

		ocrp.addImage(ocri);

		ticket = new Ticket(ocrp);
		//TODO: Check this.
		ticket.setOutPutLocation("D:/Recognition/GDZ/output");
		
		ticket.setInputFiles(inputFiles);
		ticket.write(ticketFile, name);

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
		//TODO: If this fails the ticket writing method has a problem with language mapping
		assertTrue("Expecting \"German\", got \"" + rp.getLanguageArray(0) + "\"", rp.getLanguageArray(0).equals("German"));

	}
	
	public static List<String> parseFilesFromTicket (File ticketFile) throws XmlException, IOException {
		List<String> files = new ArrayList<String>();
		XmlOptions options = new XmlOptions();
		// Set the namespace 
		options.setLoadSubstituteNamespaces(Collections.singletonMap("", Ticket.NAMESPACE));
		// Load the Xml 
		XmlTicketDocument ticketDoc = XmlTicketDocument.Factory.parse(ticketFile, options);

		XmlTicket ticket = ticketDoc.getXmlTicket();
		List<InputFile> fl = ticket.getInputFileList();
		for (InputFile i: fl) {
			files.add(i.getName());
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
	
	@Test
	public void testMultipleTickets () throws IOException, ConfigurationException, XmlException {	
		List <String> inputFile = new ArrayList<String>();
		List<String> imageInput = new ArrayList<String>();
		String inputfile= "file://./src/test/resources/input";
		String inputhotfolder = "file://./src/test/resources/hotfolder/input";
		String reportSuffix = ".xml";
		
		List<File> listFolders = new ArrayList<File>();
		hotfolder = new Hotfolder();
	
		inputfile = AbbyyServerEngineTest.parseString(inputfile);
		File inputfilepath = new File(inputfile);
		listFolders = AbbyyServerEngineTest.getImageDirectories(new File(inputfilepath.getAbsolutePath()));
		//Loop over listFolder to get the files, create OCR Images and add them to the process
		List<File> directories = new ArrayList<File>();
		
		for (File file : listFolders) {
			if (file.isDirectory()) {
				directories.add(file);
			} else {
				logger.trace(file.getAbsolutePath() + " is not a directory!");
			}
		}
	
		List<File> fileListimage = null;
		for (File files : directories){
			fileListimage = AbbyyServerEngineTest.makeFileList(files, extension);
		//	System.out.println(fileListimage);
			OCRProcess p = abbyy.newProcess();
			p.setName(files.getName());
			for (File fileImage : fileListimage){
				OCRImage image = abbyy.newImage();
				//System.out.println("fehler "+ fileImage.getAbsolutePath());
				
				image.setUrl(hotfolder.fileToURL(fileImage));
				p.addImage(image);
			}
			//p.setImageDirectory(files.getAbsolutePath());
			abbyy.addOcrProcess(p);
			
			
		}
		
		abbyy.recognize();
		for(File filelist : fileListimage){
			String folderName = filelist.getName();
			folderName = inputhotfolder + "/" + folderName + "/" + folderName + reportSuffix;
			inputFile = parseFilesFromTicket(new File(folderName));	
			File folder = new File(inputfile);
			File [] inputfiles = folder.listFiles();
			for(File currentFile: inputfiles )
			{			
				imageInput.add(currentFile.getName());
			}
			
			assertTrue(inputFile==imageInput);
		}
		//check for results
		assertNotNull(abbyy);

	}

	public static List<File> getInputFiles() {
		return inputFiles;
	}

	public static void setInputFiles(List<File> inputFiles) {
		TicketTest.inputFiles = inputFiles;
	}
	
}
