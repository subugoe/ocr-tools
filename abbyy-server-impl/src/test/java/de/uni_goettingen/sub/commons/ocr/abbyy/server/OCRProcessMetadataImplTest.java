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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OCRProcessMetadataImplTest {
	private static InputStream isResult, isDoc;
	AbbyyOCRProcessMetadata ocrProcessMetadataImpl;
	final static Logger logger = LoggerFactory
			.getLogger(OCRProcessMetadataImplTest.class);

	@Before
	public void init() throws Exception {
		File fileresult = new File(AbbyyTicketTest.BASEFOLDER_FILE
				+ "/hotfolder/" + "xmlresult.xml.result.xml");
		isResult = new FileInputStream(fileresult);
		File filexmlexport;
		filexmlexport = new File(AbbyyTicketTest.BASEFOLDER_FILE
				+ "/hotfolder/" + "xmlExport.xml");
		isDoc = new FileInputStream(filexmlexport);
		ocrProcessMetadataImpl = new AbbyyOCRProcessMetadata();
		ocrProcessMetadataImpl.parseXmlExport(isDoc);
		ocrProcessMetadataImpl.parseXmlResult(isResult);
	}
	
	@Ignore
	@Test
	public void getDocumentType() {
		logger.debug(ocrProcessMetadataImpl.getDocumentType());
		assertTrue((ocrProcessMetadataImpl.getDocumentType())
				.equals("http://www.abbyy.com/FineReader_xml/FineReader6-schema-v1.xml"));
		System.out.println(ocrProcessMetadataImpl.getDocumentType());
	}

	@Test
	public void getSoftwareName() {
		assertTrue((ocrProcessMetadataImpl.getSoftwareName())
				.equals("FineReader"));
	}

	@Test
	public void getSoftwareVersion() {
		assertTrue((ocrProcessMetadataImpl.getSoftwareVersion()).equals("8.0"));
	}

	@Test
	public void getCharacterAccuracy() {
		logger.debug(ocrProcessMetadataImpl.getCharacterAccuracy().toString());
		assertTrue((ocrProcessMetadataImpl.getCharacterAccuracy().toString())
				.equals("14.04728800"));
	}

	@Test
	public void getProcessingNote() throws IOException {
		String inputStreamprocessingNote = ocrProcessMetadataImpl
				.getProcessingNote();
		logger.debug(inputStreamprocessingNote);
		assertTrue(inputStreamprocessingNote.toString() != "");

	}

}
