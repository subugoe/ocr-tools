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

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abbyy.fineReaderXml.fineReader6SchemaV1.DocumentDocument;
import com.abbyy.fineReaderXml.fineReader6SchemaV1.DocumentDocument.Document;

public class AbbyyXMLExportParseTest {

	protected static DocumentDocument documentDocument;
	Document doc;
	private static InputStream isDoc;
	final static Logger logger = LoggerFactory
			.getLogger(AbbyyXMLExportParseTest.class);

	// public static final String NAMESPACE =
	// "http://www.abbyy.com/FineReader_xml/FineReader6-schema-v1.xml";

	@Before
	public void init() throws XmlException, IOException {

		File filexmlExport = new File(PathConstants.LOCAL_INPUT,
				"xmlExport.xml");
		isDoc = new FileInputStream(filexmlExport);
		documentDocument = DocumentDocument.Factory.parse(isDoc);
		doc = documentDocument.getDocument();

	}

	@Test
	public void getSoftwareNameAndVersion() throws IOException {
		String xmlexport = doc.getProducer();
		String[] splittArray = xmlexport.split(" ");
		assertTrue(splittArray[0].equals("FineReader"));
		assertTrue(splittArray[1].equals("8.0"));

	}

	@Test
	public void getDocumentType() {
		String xmlexport = doc.toString();
		String[] splittArray1, documentType;
		splittArray1 = xmlexport.split("schemaLocation=");
		documentType = splittArray1[1].split(" ");
		assertTrue((documentType[0].substring(1))
				.equals("http://www.abbyy.com/FineReader_xml/FineReader6-schema-v1.xml"));
	}

	@Test
	public void getProcessingNote() throws IOException {
		String document = doc.toString();
		assertTrue(document != null);
	}

}
