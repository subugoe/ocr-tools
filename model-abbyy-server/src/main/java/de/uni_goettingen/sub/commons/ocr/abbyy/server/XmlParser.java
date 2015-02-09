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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlParser {

	private final static Logger logger = LoggerFactory
			.getLogger(XmlParser.class);

	public String readErrorFromResultXml(InputStream errorResultXml, String bookIdentifier)
			throws IOException {
		String error = null;
		
		XMLInputFactory factory = XMLInputFactory.newInstance();
		try {
			XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(errorResultXml);
			while (xmlStreamReader.hasNext()) {
				int event = xmlStreamReader.next();
				if (event == XMLStreamConstants.START_ELEMENT) {
					
					if (xmlStreamReader.getName().toString().equals("Error")) {
						error = xmlStreamReader.getElementText();
						break;
					}
				}
			}
			xmlStreamReader.close();
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
		logger.error("Book: " + bookIdentifier + ", Error: " + error);
		return error;
	}
}
