package de.unigoettingen.sub.commons.ocr.util.merge;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class AbbyyXmlMerger extends Merger {

	@Override
	public void merge(List<InputStream> inputs, OutputStream output) {
		BufferedOutputStream bufferedOutput = new BufferedOutputStream(output, 8*1024);
		try {
			Set<String> ignoredElements = new HashSet<String>();
			ignoredElements.add("documentData");
			ignoredElements.add("sections");
			ignoredElements.add("section");
			ignoredElements.add("mainText");
			ignoredElements.add("stream");
			ignoredElements.add("elemId");
			ignoredElements.add("paragraphStyles");
			ignoredElements.add("paragraphStyle");
				
			Integer pageCount = inputs.size();
			XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
	
			XMLStreamWriter writer = outFactory.createXMLStreamWriter(bufferedOutput, "UTF-8");
	
			Integer f = 0;
			HashMap<String, String> nsPrefixes = new HashMap<String, String>();
	
			Boolean docStarted = false;
			Boolean headerWriten = false;
			String rootTag = "document";
	
			while (f < inputs.size()) {
	
				InputStream in = inputs.get(f);
				XMLInputFactory inFactory = XMLInputFactory.newInstance();
				XMLStreamReader parser = inFactory.createXMLStreamReader(in);
	
				// Handle the events
				while (true) {
					int event = parser.getEventType();
					if (event == XMLStreamConstants.START_DOCUMENT) {
						if (!docStarted) {
							writer.writeStartDocument("UTF-8", "1.0");
							docStarted = true;
						}
					} else if (event == XMLStreamConstants.COMMENT) {
						writer.writeComment(parser.getText());
					} else if (event == XMLStreamConstants.START_ELEMENT) {
						for (int i = 0; i < parser.getNamespaceCount(); i++) {
							String prefix = parser.getNamespacePrefix(i);
							String uri = parser.getNamespaceURI(i);
							if (prefix == null) {
								prefix = "default";
								writer.setDefaultNamespace(parser
										.getNamespaceURI(i));
							} else {
								writer.setPrefix(prefix, uri);
							}
							nsPrefixes.put(prefix, uri);
						}
	
						boolean ignoredElement = ignoredElements.contains(parser
								.getLocalName());
						if (!ignoredElement) {
							if (!headerWriten
									|| !parser.getLocalName().equalsIgnoreCase(
											rootTag)) {
								if (parser.getNamespaceURI() != null) {
									writer.writeStartElement(
											parser.getNamespaceURI(),
											parser.getLocalName());
								} else {
									writer.writeStartElement(parser.getLocalName());
								}
								if (!headerWriten) {
									writer.writeDefaultNamespace(nsPrefixes
											.get("default"));
									for (Map.Entry<String, String> entry : nsPrefixes.entrySet()) {
										String namespace = entry.getKey();
										if (!namespace.equalsIgnoreCase("default")) {
											writer.writeNamespace(namespace,
													entry.getValue());
										}
									}
								}
								if (parser.getLocalName().equalsIgnoreCase(rootTag)) {
									headerWriten = true;
	                                                        }
								for (int i = 0; i < parser.getAttributeCount(); i++) {
									String name = parser.getAttributeLocalName(i);
									String value = parser.getAttributeValue(i);
									if (name.equalsIgnoreCase("pagesCount")) {
										value = pageCount.toString();
										headerWriten = true;
									}
									if (parser.getAttributeNamespace(i) != null) {
										writer.writeAttribute(
												parser.getAttributeNamespace(i),
												name, value);
									} else {
										writer.writeAttribute(name, value);
									}
								}
							}
						}
					} else if (event == XMLStreamConstants.CHARACTERS) {
						writer.writeCharacters(parser.getText());
					} else if (event == XMLStreamConstants.END_ELEMENT) {
						boolean ignoredElement = ignoredElements.contains(parser
								.getLocalName());
						if (!ignoredElement) {
							if (headerWriten
									&& !parser.getLocalName().equalsIgnoreCase(
											rootTag)) {
								writer.writeEndElement();
							}
						}
					} else if (event == XMLStreamConstants.END_DOCUMENT) {
						// writer.writeEndElement();
						if (f == inputs.size() - 1) {
							writer.writeEndDocument();
						}
						break;
					}
					if (!parser.hasNext()) {
						break;
					}
					parser.next();
				}
				parser.close();
				f++;
			}
			writer.writeEndDocument();
			writer.flush();
			writer.close();
		} catch(XMLStreamException e) {
			throw new IllegalStateException("Error while merging files.", e);
		} finally {
			try {
				bufferedOutput.flush();
				bufferedOutput.close();
			} catch (IOException e) {
				System.out.println("Error while merging files");
			}
		}
	}

}
