package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


public class XmlParser {
	
	/** The Constant logger. */
	public final static Logger logger = LoggerFactory.getLogger(XmlParser.class);

	/**
	 * parse Xml result in output folder.
	 * 
	 * @param file
	 *            xml result in output folder
	 * @return the sets of all files Name, wich are in the xml file
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws XMLStreamException
	 *             the xML stream exception
	 */
	//TODO: Change this to handle streams
	protected Set<String> xmlresultOutputparse (File file) throws FileNotFoundException, XMLStreamException {
		Set<String> ocrFormatFile = new LinkedHashSet<String>();
		String filename = null;
		final InputStream osmHamburgInStream = new FileInputStream(file);
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(osmHamburgInStream);
		try {
			while (xmlStreamReader.hasNext()) {
				int event = xmlStreamReader.next();
				if (event == XMLStreamConstants.START_ELEMENT) {
					// if the element 'NamingRule' found
					if (xmlStreamReader.getName().toString().equals("NamingRule")) {
						filename = xmlStreamReader.getElementText().toString();
						ocrFormatFile.add(filename);
					}
				}
			}
			logger.debug("the files which should be in output folder: " + ocrFormatFile);
		} finally {
			xmlStreamReader.close();
		}
		return ocrFormatFile;
	}

	/**
	 * parse Xml result in error folder and get Error description
	 * 
	 * @param file
	 *            xml result in error folder
	 * @return the sets of all files Name, wich are in the xml file
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws XMLStreamException
	 *             the xML stream exception
	 */
	//TODO: Change this to handle streams
	protected Set<String> xmlresultErrorparse (File file , String identifier) throws FileNotFoundException, XMLStreamException {
		Set<String> ocrErrorFile = new LinkedHashSet<String>();
		String error = null;

		final InputStream osmHamburgInStream = new FileInputStream(file);
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(osmHamburgInStream);
		try {
			while (xmlStreamReader.hasNext()) {
				int event = xmlStreamReader.next();
				if (event == XMLStreamConstants.START_ELEMENT) {
					// Error description
					if (xmlStreamReader.getName().toString().equals("Error")) {
						error = xmlStreamReader.getElementText();
					}
					// bilder die in verzeichnis befinden
					if (xmlStreamReader.getName().toString().equals("InputFile")) {
						// ber alle Attribute
						for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
							String attributeName = xmlStreamReader.getAttributeName(i).toString();
							// wenn version gefunden wurde
							if (attributeName.equals("Name")) {
								String str = xmlStreamReader.getAttributeValue(i);
								String[] results = str.split("}_");
								Boolean image = true;
								for (int j = 0; j < results.length; j++) {
									if (image) {
										image = false;
									} else {
										ocrErrorFile.add(results[j]);
										image = true;
									}
								}
							}
						}
					}
				}
			}
		} finally {
			xmlStreamReader.close();
		}
		logger.debug("Band Name " + identifier + " Error :" + error);
		return ocrErrorFile;
	}
}
