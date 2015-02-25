package de.unigoettingen.sub.commons.ocr.util.merge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.tidy.Tidy;

public class HocrMerger implements Merger {

	@Override
	public void merge(List<InputStream> inputs, OutputStream output) {
		try {
			int fileCounter = 1;

			Boolean docStarted = false;
			Boolean htmlStarted = false;
			Boolean insideHead = false;

			Map<String, String> nsPrefixes = new HashMap<String, String>();

			XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
			XMLStreamWriter writer = outFactory.createXMLStreamWriter(output);

			XMLInputFactory inFactory = XMLInputFactory.newInstance();
			inFactory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);

			for (InputStream html : inputs) {
				InputStream xhtml = tidy(html);
				XMLStreamReader parser = inFactory.createXMLStreamReader(xhtml);

				while (parser.hasNext()) {

					int event = parser.next();
					if (event == XMLStreamConstants.START_DOCUMENT) {
						if (!docStarted) {
							writer.writeStartDocument("UTF-8", "1.0");
							docStarted = true;
						}
					} else if (event == XMLStreamConstants.COMMENT) {
						writer.writeComment(parser.getText());
					} else if (event == XMLStreamConstants.START_ELEMENT) {
						String elementName = parser.getLocalName();
						if (elementName.equals("head")) {
							insideHead = true;
	                                        }
						boolean isFirstHtml = elementName.equals("html")
								&& fileCounter == 1;
						boolean isFirstBody = elementName.equals("body")
								&& fileCounter == 1;

						boolean ignoreMode = fileCounter > 1 && insideHead
								|| elementName.equals("html")
								|| elementName.equals("body");
						if (!ignoreMode || isFirstHtml || isFirstBody) {
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
							if (!htmlStarted
									|| !parser.getLocalName().equalsIgnoreCase(
											"html")) {
								if (parser.getNamespaceURI() != null) {
									writer.writeStartElement(
											parser.getNamespaceURI(),
											parser.getLocalName());
								} else {
									writer.writeStartElement(parser.getLocalName());
								}
								if (!htmlStarted) {
									writer.writeDefaultNamespace(nsPrefixes
											.get("default"));
									for (Map.Entry<String, String> entry : nsPrefixes.entrySet()) {
										String namespace = entry.getKey();
										if (!namespace.equalsIgnoreCase("default")) {
											writer.writeNamespace(namespace,
													entry.getValue());
										}
									}
									htmlStarted = true;
								}
								for (int i = 0; i < parser.getAttributeCount(); i++) {
									String name = parser.getAttributeLocalName(i);
									String value = parser.getAttributeValue(i);
									boolean isId = name.equals("id");
									if (isId && value.equals("page_1")) {
										value = "page_" + fileCounter;
									} else if (isId
											&& (value.startsWith("block_")
													|| value.startsWith("line_")
													|| value.startsWith("word_") || value
													.startsWith("xword_"))) {
										String[] parts = value.split("_");
										value = parts[0] + "_" + fileCounter + "_"
												+ parts[2];

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
						String elementName = parser.getLocalName();
						boolean isLastHtml = elementName.equals("html")
								&& fileCounter == inputs.size();
						boolean isLastBody = elementName.equals("body")
								&& fileCounter == inputs.size();
						boolean ignoreMode = fileCounter > 1 && insideHead
								|| elementName.equals("html")
								|| elementName.equals("body");
						if (!ignoreMode || isLastHtml || isLastBody) {
							writer.writeEndElement();
	                                        }
						if (elementName.equals("head")) {
							insideHead = false;
						}
					} else if (event == XMLStreamConstants.END_DOCUMENT) {
						if (fileCounter == inputs.size()) {
							writer.writeEndDocument();
						}
					}

				}
				parser.close();
				fileCounter++;

			}
			writer.flush();
			writer.close();
		} catch(XMLStreamException e) {
			throw new IllegalStateException("Error while merging files.", e);
		} catch(IOException e) {
			throw new IllegalStateException("Error while merging files.", e);
		}
	}

	private InputStream tidy(InputStream html) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		Tidy tidy = new Tidy();
		tidy.setXmlOut(true);
		tidy.setXHTML(true);
		tidy.setNumEntities(true);
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);

		tidy.parse(html, out);

		return (InputStream) new ByteArrayInputStream(out.toByteArray());

	}

}
