package de.uni_goettingen.sub.commons.ocr.abbyy.server;

/*

 Copyright 2010 SUB Goettingen. All rights reserved.
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

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlParser {

	/** The Constant logger. */
	public final static Logger logger = LoggerFactory
			.getLogger(XmlParser.class);

	/**
	 * parse Xml result in error folder and get Error description.
	 * 
	 * @param is
	 *            the xml result in Error folder
	 * @param identifier
	 *            the name of Process
	 * @throws XMLStreamException
	 *             the xML stream exception
	 */
	protected String xmlresultErrorparse(InputStream is, String identifier)
			throws XMLStreamException {
		String error = null;
		// final InputStream osmHamburgInStream = new FileInputStream(file);
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(is);
		try {
			while (xmlStreamReader.hasNext()) {
				int event = xmlStreamReader.next();
				if (event == XMLStreamConstants.START_ELEMENT) {
					// Error description
					if (xmlStreamReader.getName().toString().equals("Error")) {
						error = xmlStreamReader.getElementText();
						break;
					}
				}
			}
		} finally {
			xmlStreamReader.close();
		}
		logger.error("Band Name " + identifier + " Error Reports: " + error);
		return ("Band Name " + identifier + " Error Reports: " + error);
	}
}
