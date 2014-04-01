package de.unigoettingen.sub.commons.ocr.util;

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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.tidy.Tidy;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;

/**
 * The Class FileMerger is a container for several static methods that can be
 * used to merge several types of OCR output
 * 
 * @version 0.9
 * @author cmahnke
 */
public class FileMerger {

	protected static Logger logger = LoggerFactory.getLogger(FileMerger.class);

	/** This list contains the SEGMENTABLE_FORMATS. */
	public final static List<OCRFormat> SEGMENTABLE_FORMATS;

	/** Abbyy version number. */
	public static String abbyyVersionNumber = "v10";
	/**
	 * This list the mapping from {@link OCRFormat} to methods for File based
	 * merging.
	 */
	private final static Map<OCRFormat, Method> fileMergers;

	/**
	 * This list the mapping from {@link OCRFormat} to methods for Stream based
	 * merging.
	 */
	private final static Map<OCRFormat, Method> streamMergers;

	static {

		fileMergers = new HashMap<OCRFormat, Method>();
		streamMergers = new HashMap<OCRFormat, Method>();

		// And now: Some black magic!
		try {
			// These represent the arguments for the methods
			Class<?> fileParams[] = new Class[2];
			fileParams[0] = List.class;
			fileParams[1] = File.class;
			// This are the methods for handling files
			fileMergers.put(OCRFormat.PDF,
					FileMerger.class.getMethod("mergePDF", fileParams));
			fileMergers.put(OCRFormat.XML,
					FileMerger.class.getMethod("mergeAbbyyXML", fileParams));
			fileMergers.put(OCRFormat.TXT,
					FileMerger.class.getMethod("mergeTXT", fileParams));
			fileMergers.put(OCRFormat.HOCR,
					FileMerger.class.getMethod("mergeHOCR", fileParams));

			// These represent the arguments for the methods
			Class<?> streamParams[] = new Class[2];
			streamParams[0] = List.class;
			streamParams[1] = OutputStream.class;
			// This are the methods for handling files
			streamMergers.put(OCRFormat.PDF,
					FileMerger.class.getMethod("mergePDF", streamParams));
			streamMergers.put(OCRFormat.XML,
					FileMerger.class.getMethod("mergeAbbyyXML", streamParams));
			streamMergers.put(OCRFormat.TXT,
					FileMerger.class.getMethod("mergeTXT", streamParams));
			streamMergers.put(OCRFormat.HOCR,
					FileMerger.class.getMethod("mergeHOCR", streamParams));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		// Get the public formats from the internal Map
		SEGMENTABLE_FORMATS = new ArrayList<OCRFormat>();
		SEGMENTABLE_FORMATS.addAll(fileMergers.keySet());
	}

	/**
	 * Merge Abbyy XML Streams for Version 8. This operates directly on Streams and should be
	 * suitable for processing over WebDAV for example
	 * 
	 * @param iss
	 *            the List of InputStreams
	 * @param os
	 *            the OutputStram to write to.
	 * @throws XMLStreamException
	 *             the xML stream exception
	 */
	public static void mergeABByyXMLv8(List<InputStream> iss, OutputStream os)
			throws XMLStreamException {

		Integer pageCount = iss.size();
		// Output
		XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
		// outFactory.setProperty("javax.xml.stream.isPrefixDefaulting",Boolean.TRUE);

		XMLStreamWriter writer = outFactory.createXMLStreamWriter(os);

		Integer f = 0;
		HashMap<String, String> nsPrefixes = new HashMap<String, String>();

		Boolean docStarted = false;
		Boolean headerWriten = false;
		String rootTag = "document";

		while (f < iss.size()) {

			// Input
			InputStream in = iss.get(f);
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
					if (!headerWriten
							|| !parser.getLocalName().equalsIgnoreCase(rootTag)) {
						if (parser.getNamespaceURI() != null) {
							writer.writeStartElement(parser.getNamespaceURI(),
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
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							String name = parser.getAttributeLocalName(i);
							String value = parser.getAttributeValue(i);
							if (name.equalsIgnoreCase("pagesCount")) {
								value = pageCount.toString();
								headerWriten = true;
							}
							if (parser.getAttributeNamespace(i) != null) {
								writer.writeAttribute(
										parser.getAttributeNamespace(i), name,
										value);
							} else {
								writer.writeAttribute(name, value);
							}
						}
					}
				} else if (event == XMLStreamConstants.CHARACTERS) {
					writer.writeCharacters(parser.getText());
				} else if (event == XMLStreamConstants.END_ELEMENT) {
					if (headerWriten
							&& !parser.getLocalName().equalsIgnoreCase(rootTag)) {
						writer.writeEndElement();
					}
				} else if (event == XMLStreamConstants.END_DOCUMENT) {
					// writer.writeEndElement();
					if (f == iss.size() - 1) {
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
	}

	/**
	 * Merge Abbyy XML Streams for Version 10. This operates directly on Streams and should be
	 * suitable for processing over WebDAV for example
	 * 
	 * @param iss
	 *            the List of InputStreams
	 * @param os
	 *            the OutputStram to write to.
	 * @throws XMLStreamException
	 *             the xML stream exception
	 */
	public static void mergeABByyXMLv10(List<InputStream> iss, OutputStream os)
			throws XMLStreamException {
		Set<String> ignoredElements = new HashSet<String>();
		ignoredElements.add("documentData");
		ignoredElements.add("sections");
		ignoredElements.add("section");
		ignoredElements.add("mainText");
		ignoredElements.add("stream");
		ignoredElements.add("elemId");
		ignoredElements.add("paragraphStyles");
		ignoredElements.add("paragraphStyle");
		

		Integer pageCount = iss.size();
		// Output
		XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
		// outFactory.setProperty("javax.xml.stream.isPrefixDefaulting",Boolean.TRUE);

		XMLStreamWriter writer = outFactory.createXMLStreamWriter(os);

		Integer f = 0;
		HashMap<String, String> nsPrefixes = new HashMap<String, String>();

		Boolean docStarted = false;
		Boolean headerWriten = false;
		String rootTag = "document";

		while (f < iss.size()) {

			// Input
			InputStream in = iss.get(f);
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
					if (f == iss.size() - 1) {
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
	}
	
	
	/**
	 * Merge Abbyy XMLResult(Abbyy Reports) Streams.  This operates directly on Streams and should be
	 * suitable for processing over WebDAV for example
	 * 
	 * @param iss
	 *            the List of InputStreams
	 * @param os
	 *            the OutputStram to write to.
	 * @throws XMLStreamException
	 *             the xML stream exception
	 */
	public static void mergeAbbyyXMLResults(List<InputStream> iss,
			OutputStream os) throws XMLStreamException {

		Integer pageCount = iss.size();
		// Output
		XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
		// outFactory.setProperty("javax.xml.stream.isPrefixDefaulting",Boolean.TRUE);

		XMLStreamWriter writer = outFactory.createXMLStreamWriter(os, "UTF-8");

		Integer f = 0;
		HashMap<String, String> nsPrefixes = new HashMap<String, String>();

		Boolean docStarted = false;
		Boolean headerWriten = false;
		String rootTag = "XmlResult";

		while (f < iss.size()) {

			// Input
			InputStream in = iss.get(f);
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
				} else if (event == XMLStreamConstants.CHARACTERS) {
					writer.writeCharacters(parser.getText());
				} else if (event == XMLStreamConstants.END_ELEMENT) {
						if (headerWriten
								&& !parser.getLocalName().equalsIgnoreCase(
										rootTag)) {
							writer.writeEndElement();
						}
				} else if (event == XMLStreamConstants.END_DOCUMENT) {
					// writer.writeEndElement();
					if (f == iss.size() - 1) {
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
	}
	
	
	/**
	 * Merge Abbyy XMLResult(Abbyy Reports) files.
	 * 
	 * @param files
	 *            the File's to merge
	 * @param outFile
	 *            the result file to write to
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws XMLStreamException
	 *             the XML stream exception
	 */
	public static void mergeAbbyyXMLResults(List<File> files, File outFile)
			throws IOException, XMLStreamException {
		OutputStream os = new FileOutputStream(outFile);
		List<InputStream> iss = new ArrayList<InputStream>();
		int f = 0;
		while (f < files.size()) {
			iss.add(new FileInputStream(files.get(f)));
			f++;
		}
		mergeAbbyyXMLResults(iss, os);
	}
	

	/**
	 * Merge Abbyy XML files.
	 * 
	 * @param files
	 *            the File's to merge
	 * @param outFile
	 *            the result file to write to
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws XMLStreamException
	 *             the XML stream exception
	 */
	public static void mergeAbbyyXML(List<File> files, File outFile)
			throws IOException, XMLStreamException {
		OutputStream os = new FileOutputStream(outFile);
		List<InputStream> iss = new ArrayList<InputStream>();
		int f = 0;
		while (f < files.size()) {
			iss.add(new FileInputStream(files.get(f)));
			f++;
		}	
			mergeAbbyyXML(iss, os);	
	}
	
	/**
	 * Merge Abbyy XML files.
	 * 
	 * @param iss
	 *            the InputStream's to merge
	 * @param os
	 *            the result OutputStream to write to
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws XMLStreamException
	 *             the XML stream exception
	 */
	public static void mergeAbbyyXML(List<InputStream> iss,
			OutputStream os) throws XMLStreamException {
		if(abbyyVersionNumber.equals("v8")){
			mergeABByyXMLv8(iss, os);
		}else
		mergeABByyXMLv10(iss, os);
	}
	
	/**
	 * Merge PDF Streams. This operates directly on Streams and should be
	 * suitable for processing over WebDAV for example. The iText library is
	 * used to perform this task.
	 * 
	 * @param iss
	 *            the List of InputStreams
	 * @param os
	 *            the OutputStram to write to.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws DocumentException
	 *             the document exception
	 */
	@SuppressWarnings("unchecked")
	public static void mergePDF(List<InputStream> iss, OutputStream os)
			throws IOException, DocumentException {
		// Stolen from itext (com.lowagie.tools.concat_pdf)

		int pageOffset = 0;
		List<HashMap<String, Object>> master = new ArrayList<HashMap<String, Object>>();
		int f = 0;
		Document document = null;
		PdfCopy writer = null;
		while (f < iss.size()) {
			// we create a reader for a certain document
			PdfReader reader = new PdfReader(iss.get(f));
			reader.consolidateNamedDestinations();
			// we retrieve the total number of pages
			int n = reader.getNumberOfPages();
			List<HashMap<String, Object>> bookmarks = SimpleBookmark
					.getBookmark(reader);
			if (bookmarks != null) {
				if (pageOffset != 0) {
					SimpleBookmark
							.shiftPageNumbers(bookmarks, pageOffset, null);
				}
				master.addAll(bookmarks);
			}
			pageOffset += n;

			if (f == 0) {
				// step 1: creation of a document-object
				document = new Document(reader.getPageSizeWithRotation(1));
				// step 2: we create a writer that listens to the document
				writer = new PdfCopy(document, os);
				// step 3: we open the document
				document.open();
			}
			// step 4: we add content
			PdfImportedPage page;
			for (int i = 0; i < n;) {
				++i;
				page = writer.getImportedPage(reader, i);
				writer.addPage(page);
			}
			writer.freeReader(reader);
			f++;
		}
		if (!master.isEmpty()) {
			writer.setOutlines(master);
		}
		// step 5: we close the document
		document.close();
	}

	/**
	 * Merge pdf. The iText library is used to perform this task.
	 * 
	 * @param files
	 *            the files to merge
	 * @param outFile
	 *            the out file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws DocumentException
	 *             the document exception
	 */
	public static void mergePDF(List<File> files, File outFile)
			throws IOException, DocumentException {
		OutputStream os = new FileOutputStream(outFile);
		List<InputStream> iss = new ArrayList<InputStream>();
		int f = 0;
		while (f < files.size()) {
			iss.add(new FileInputStream(files.get(f)));
			f++;
		}
		mergePDF(iss, os);
	}

	/**
	 * Merge TXT Streams. This operates directly on Streams and should be
	 * suitable for processing over WebDAV for example. Note that the result
	 * isn't platform independent, the line ending different on each. Look at
	 * the property "line.separator". the page break is encoded as ASCII DEC 12.
	 * 
	 * @param iss
	 *            the List of InputStreams
	 * @param os
	 *            the OutputStram to write to.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void mergeTXT(List<InputStream> iss, OutputStream os)
			throws IOException {
		OutputStreamWriter osw = new OutputStreamWriter(os);
		// Ascii page break dec 12, hex 0c
		char pb = (char) 12;
		// Use the platform dependent separator here
		String seperator = System.getProperty("line.separator");

		int f = 0;
		while (f < iss.size()) {
			InputStreamReader isr = new InputStreamReader(iss.get(f), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				osw.write(line);
				osw.write(seperator);
			}
			osw.write(pb);

			br.close();
			isr.close();
			f++;
		}
		osw.close();
	}

	/**
	 * Merge TXT. Note that the result isn't plattform independent, the line
	 * ending different on each. Look at the property "line.separator". the page
	 * break is encoded as ASCII DEC 12.
	 * 
	 * @param files
	 *            the files to merge
	 * @param outFile
	 *            the out file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void mergeTXT(List<File> files, File outFile)
			throws IOException {
		OutputStream os = new FileOutputStream(outFile);
		List<InputStream> iss = new ArrayList<InputStream>();
		int f = 0;
		while (f < files.size()) {
			iss.add(new FileInputStream(files.get(f)));
			f++;
		}
		mergeTXT(iss, os);
	}

	public static void mergeHOCR(List<InputStream> hocrStreams,
			OutputStream resultStream) throws IOException, XMLStreamException {

		int fileCounter = 1;

		Boolean docStarted = false;
		Boolean htmlStarted = false;
		Boolean insideHead = false;

		Map<String, String> nsPrefixes = new HashMap<String, String>();

		XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = outFactory.createXMLStreamWriter(resultStream);

		XMLInputFactory inFactory = XMLInputFactory.newInstance();
		inFactory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);

		for (InputStream html : hocrStreams) {
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
							&& fileCounter == hocrStreams.size();
					boolean isLastBody = elementName.equals("body")
							&& fileCounter == hocrStreams.size();
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
					if (fileCounter == hocrStreams.size()) {
						writer.writeEndDocument();
					}
				}

			}
			parser.close();
			fileCounter++;

		}
		writer.flush();
		writer.close();

	}

	private static InputStream tidy(InputStream html) throws IOException {
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

	public static void mergeHOCR(List<File> files, File outFile)
			throws IOException, XMLStreamException {
		OutputStream os = new FileOutputStream(outFile);
		List<InputStream> iss = new ArrayList<InputStream>();
		int f = 0;
		while (f < files.size()) {
			iss.add(new FileInputStream(files.get(f)));
			f++;
		}
		mergeHOCR(iss, os);
	}

	/**
	 * Checks if the given {@link OCRFormat} is segmentable.
	 * 
	 * @param f
	 *            the {@link OCRFormat} to check
	 * @return true if {@link OCRFormat} can be segmented, false otherwise
	 */
	public static Boolean isSegmentable(OCRFormat f) {
		if (SEGMENTABLE_FORMATS.contains(f)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if all of the given {@link OCRFormat}'s are segmentable.
	 * 
	 * @param sf
	 *            the Set of {@link OCRFormat}'s to check
	 * @return true if the whole Set of{@link OCRFormat}'scan be segmented,
	 *         false otherwise
	 */
	public static Boolean isSegmentable(Set<OCRFormat> sf) {
		for (OCRFormat f : sf) {
			if (!isSegmentable(f)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Merge files of the given format. Look at the used methods below for
	 * remarks on the different merge implementations.
	 * 
	 * @param format
	 *            the {@link OCRFormat} to merge
	 * @param files
	 *            the File's to merge
	 * @param outFile
	 *            the result file to write to
	 * @throws MergeException
	 *             if something went wrong, the MergeException is just a wrapper
	 * @see #mergeAbbyyXML(List, File)
	 * @see #mergePDF(List, File)
	 * @see #mergeTXT(List, File)
	 */
	public static void mergeFiles(OCRFormat format, List<File> files,
			File outFile) throws MergeException {
		try {
			fileMergers.get(format).invoke(null,
					new Object[] { files, outFile });
		} catch (Exception e) {
			throw new MergeException(e);
		}
	}

	/**
	 * Merge Streams of the given format. This operates directly on Streams and
	 * should be suitable for processing over WebDAV for example. Look at the
	 * used methods below for remarks on the different merge implementations.
	 * 
	 * @param format
	 *            the {@link OCRFormat} to merge
	 * @param iss
	 *            the List of InputStreams
	 * @param os
	 *            the OutputStram to write to.
	 * @throws MergeException
	 *             if something went wrong, the MergeException is just a wrapper
	 * @see #mergeAbbyyXML(List, OutputStream)
	 * @see #mergePDF(List, OutputStream)
	 * @see #mergeTXT(List, OutputStream)
	 */
	public static void mergeStreams(OCRFormat format, List<InputStream> iss,
			OutputStream os) throws MergeException {
		try {
			streamMergers.get(format).invoke(null, new Object[] { iss, os });
		} catch (Exception e) {
			throw new MergeException(e);
		}
	}

	/**
	 * The Class MergeException is a wrapper for other exceptions that may occur
	 * during the merge of files or streams
	 */
	public static class MergeException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2699212824820823674L;

		/**
		 * Instantiates a new MergeException.
		 * 
		 * @param t
		 *            the wrapped Throwable
		 */
		public MergeException(Throwable t) {
			super(t);
		}

	}
}
