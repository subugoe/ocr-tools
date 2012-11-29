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
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;

import java.util.Collections;

import org.apache.xmlbeans.XmlOptions;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abbyy.recognitionServer10Xml.xmlResultSchemaV1.InputFile;

import com.abbyy.recognitionServer10Xml.xmlResultSchemaV1.XmlResultDocument;
import com.abbyy.recognitionServer10Xml.xmlResultSchemaV1.XmlResultDocument.XmlResult;

public class AbbyyXMLResultParseTest {

	protected static XmlResultDocument xmlResultDocument;
	XmlResult xm;
	private static InputStream isResult;
	final static Logger logger = LoggerFactory
			.getLogger(AbbyyXMLResultParseTest.class);

	public static final String NAMESPACE = "http://www.abbyy.com/RecognitionServer1.0_xml/XmlResult-schema-v1.xsd";

	// private static final Factory NewInstanceInstantiator = null;

	@Before
	public void init() throws Exception {
		File fileresult = new File(PathConstants.RESOURCES
				+ "/hotfolder/" + "xmlresult.xml.result.xml");
		isResult = new FileInputStream(fileresult);
		XmlOptions options = new XmlOptions();
		// Set the namespace
		options.setLoadSubstituteNamespaces(Collections.singletonMap("",
				NAMESPACE));
		xmlResultDocument = XmlResultDocument.Factory.parse(isResult, options);
		xm = xmlResultDocument.getXmlResult();
	}

	@Test
	public void getTotalCharactersfromInputfileListe() throws Exception {
		BigDecimal a = new BigDecimal(0);
		for (InputFile l : xm.getInputFileList()) {
			BigDecimal bd = new BigDecimal(l.getStatistics()
					.getTotalCharacters());
			a = a.add(bd);
		}
		assertTrue(a.equals(new BigDecimal(719)));
	}

	@Test
	public void getTotalUncertainCharacters() throws Exception {
		BigDecimal c = new BigDecimal(xm.getStatistics()
				.getUncertainCharacters());
		String strr = c.toString();
		assertTrue(strr.equals("101"));
	}

	@Test
	public void getTotalUncertainCharactersfromInputfileListe()
			throws Exception {
		BigDecimal d = new BigDecimal(0);
		for (InputFile l : xm.getInputFileList()) {
			BigDecimal bd = new BigDecimal(l.getStatistics()
					.getUncertainCharacters());
			d = d.add(bd);
		}
		assertTrue(d.equals(new BigDecimal(101)));
	}

	@Test
	public void getTotalCharacters() throws Exception {
		BigDecimal b = new BigDecimal(xm.getStatistics().getTotalCharacters());
		String str = b.toString();
		assertTrue(str.equals("719"));
	}

	@Test
	public void getCharacterAccuracy() throws Exception {
		BigDecimal totalChar = new BigDecimal(xm.getStatistics()
				.getTotalCharacters());
		BigDecimal totalUncerChar = new BigDecimal(xm.getStatistics()
				.getUncertainCharacters());
		BigDecimal prozent = (totalUncerChar.divide(totalChar, 8,
				BigDecimal.ROUND_UP)).multiply(new BigDecimal(100));
		assertTrue((prozent.toString()).equals("14.04728800"));

	}

}
