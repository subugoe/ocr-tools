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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;

/**
 * The Class FileMerger is a container for several static methods that can be used 
 * to merge several types of OCR output
 * 
 * @version 0.9
 * @author cmahnke
 */
public class FileMerger {
	
	/** This list contains the SEGMENTABLE_FORMATS. */
	public final static List<OCRFormat> SEGMENTABLE_FORMATS;
	
	/** This list the mapping from {@link OCRFormat} to methods for File based merging. */
	private final static Map<OCRFormat, Method> fileMergers;
	
	/** This list the mapping from {@link OCRFormat} to methods for Stream based merging. */
	private final static Map<OCRFormat, Method> streamMergers;

	static {

		fileMergers = new HashMap<OCRFormat, Method>();
		streamMergers = new HashMap<OCRFormat, Method>();

		//And now: Some black magic!
		try {
			//These represent the arguments for the methods
			Class<?> fileParams[] = new Class[2];
			fileParams[0] = List.class;
			fileParams[1] = File.class;
			//This are the methods for handling files
			fileMergers.put(OCRFormat.PDF, FileMerger.class.getMethod("mergePDF", fileParams));
			fileMergers.put(OCRFormat.XML, FileMerger.class.getMethod("mergeAbbyyXML", fileParams));
			fileMergers.put(OCRFormat.TXT, FileMerger.class.getMethod("mergeTXT", fileParams));

			//These represent the arguments for the methods
			Class<?> streamParams[] = new Class[2];
			streamParams[0] = List.class;
			streamParams[1] = OutputStream.class;
			//This are the methods for handling files
			streamMergers.put(OCRFormat.PDF, FileMerger.class.getMethod("mergePDF", streamParams));
			streamMergers.put(OCRFormat.XML, FileMerger.class.getMethod("mergeAbbyyXML", streamParams));
			streamMergers.put(OCRFormat.TXT, FileMerger.class.getMethod("mergeTXT", streamParams));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		//Get the public formats from the internal Map
		SEGMENTABLE_FORMATS = new ArrayList<OCRFormat>();
		SEGMENTABLE_FORMATS.addAll(fileMergers.keySet());
	}

	/**
	 * Merge Abbyy XML Streams. This operates directly on Streams and should be 
	 * suitable for processing over WebDAV for example
	 *
	 * @param iss the List of InputStreams
	 * @param os the OutputStram to write to.
	 * @throws XMLStreamException the xML stream exception
	 */
	public static void mergeAbbyyXML (List<InputStream> iss, OutputStream os) throws XMLStreamException {

		Integer pageCount = iss.size();
		//Output
		XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
		//outFactory.setProperty("javax.xml.stream.isPrefixDefaulting",Boolean.TRUE);

		XMLStreamWriter writer = outFactory.createXMLStreamWriter(os);

		Integer f = 0;
		HashMap<String, String> nsPrefixes = new HashMap<String, String>();

		Boolean docStarted = false;
		Boolean headerWriten = false;
		String rootTag = "document";

		while (f < iss.size()) {

			//Input
			InputStream in = iss.get(f);
			XMLInputFactory inFactory = XMLInputFactory.newInstance();
			XMLStreamReader parser = inFactory.createXMLStreamReader(in);

			//Handle the events
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
							writer.setDefaultNamespace(parser.getNamespaceURI(i));
						} else {
							writer.setPrefix(prefix, uri);
						}
						nsPrefixes.put(prefix, uri);
					}
					if (!headerWriten || !parser.getLocalName().equalsIgnoreCase(rootTag)) {
						if (parser.getNamespaceURI() != null) {
							writer.writeStartElement(parser.getNamespaceURI(), parser.getLocalName());
						} else {
							writer.writeStartElement(parser.getLocalName());
						}
						if (!headerWriten) {
							writer.writeDefaultNamespace(nsPrefixes.get("default"));
							for (String namespace : nsPrefixes.keySet()) {
								if (!namespace.equalsIgnoreCase("default")) {
									writer.writeNamespace(namespace, nsPrefixes.get(namespace));
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
								writer.writeAttribute(parser.getAttributeNamespace(i), name, value);
							} else {
								writer.writeAttribute(name, value);
							}
						}
					}
				} else if (event == XMLStreamConstants.CHARACTERS) {
					writer.writeCharacters(parser.getText());
				} else if (event == XMLStreamConstants.END_ELEMENT) {
					if (headerWriten && !parser.getLocalName().equalsIgnoreCase(rootTag)) {
						writer.writeEndElement();
					}
				} else if (event == XMLStreamConstants.END_DOCUMENT) {
					//writer.writeEndElement();
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
	 * Merge Abbyy XML files.
	 *
	 * @param files the File's to merge
	 * @param outFile the result file to write to
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws XMLStreamException the XML stream exception
	 */
	public static void mergeAbbyyXML (List<File> files, File outFile) throws IOException, XMLStreamException {
		OutputStream os = new FileOutputStream(outFile);
		List<InputStream> iss = new ArrayList<InputStream>();
		int f = 0;
		while (f < files.size()) {
			iss.add(new FileInputStream(files.get(f)));
		}
		mergeAbbyyXML(iss, os);
	}

	/**
	 * Merge PDF Streams. This operates directly on Streams and should be 
	 * suitable for processing over WebDAV for example.  The iText library is 
	 * used to perform this task.
	 *
	 * @param iss the List of InputStreams
	 * @param os the OutputStram to write to.
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DocumentException the document exception
	 */
	@SuppressWarnings("unchecked")
	public static void mergePDF (List<InputStream> iss, OutputStream os) throws IOException, DocumentException {
		//Stolen from itext (com.lowagie.tools.concat_pdf) 

		int pageOffset = 0;
		List<HashMap<String,Object>> master = new ArrayList<HashMap<String,Object>>();
		int f = 0;
		Document document = null;
		PdfCopy writer = null;
		while (f < iss.size()) {
			// we create a reader for a certain document
			PdfReader reader = new PdfReader(iss.get(f));
			reader.consolidateNamedDestinations();
			// we retrieve the total number of pages
			int n = reader.getNumberOfPages();
			List<HashMap<String,Object>> bookmarks = SimpleBookmark.getBookmark(reader);
			if (bookmarks != null) {
				if (pageOffset != 0) {
					SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
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
				System.out.println("Processed page " + i);
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
	 * @param files the files to merge
	 * @param outFile the out file
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DocumentException the document exception
	 */
	public static void mergePDF (List<File> files, File outFile) throws IOException, DocumentException {
		OutputStream os = new FileOutputStream(outFile);
		List<InputStream> iss = new ArrayList<InputStream>();
		int f = 0;
		while (f < files.size()) {
			iss.add(new FileInputStream(files.get(f)));
		}
		mergePDF(iss, os);
	}

	/**
	 * Merge TXT Streams. This operates directly on Streams and should be 
	 * suitable for processing over WebDAV for example. Note that the result isn't
	 * platform independent, the line ending different on each. Look 
	 * at the property "line.separator".
	 * the page break is encoded as ASCII DEC 12.
	 *
	 * @param iss the List of InputStreams
	 * @param os the OutputStram to write to.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void mergeTXT (List<InputStream> iss, OutputStream os) throws IOException {
		OutputStreamWriter osw = new OutputStreamWriter(os);
		//Ascii page break dec 12, hex 0c		
		char pb = (char) 12;
		//Use the platform dependent separator here
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

			isr.close();
			f++;
		}
		osw.close();
	}

	/**
	 * Merge TXT.  Note that the result isn't plattform independent, the line ending
	 * different on each. Look at the property "line.separator".
	 * the page break is encoded as ASCII DEC 12.
	 *
	 * @param files the files to merge
	 * @param outFile the out file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void mergeTXT (List<File> files, File outFile) throws IOException {
		OutputStream os = new FileOutputStream(outFile);
		List<InputStream> iss = new ArrayList<InputStream>();
		int f = 0;
		while (f < files.size()) {
			iss.add(new FileInputStream(files.get(f)));
		}
		mergeTXT(iss, os);
	}

	/**
	 * Checks if the given {@link OCRFormat} is segmentable.
	 *
	 * @param f the {@link OCRFormat} to check
	 * @return true if {@link OCRFormat} can be segmented, false otherwise
	 */
	public static Boolean isSegmentable (OCRFormat f) {
		if (SEGMENTABLE_FORMATS.contains(f)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if all of the given {@link OCRFormat}'s are segmentable.
	 *
	 * @param f the Set of {@link OCRFormat}'s to check
	 * @return true if the whole Set of{@link OCRFormat}'scan be segmented, false otherwise
	 */
	public static Boolean isSegmentable (Set<OCRFormat> sf) {
		for (OCRFormat f: sf) {
			if (!isSegmentable(f)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Merge files of the given format. Look at the used methods below
	 * for remarks on the different merge implementations.
	 * 
	 * @see #mergeAbbyyXML(List, File)
	 * @see #mergePDF(List, File)
	 * @see #mergeTXT(List, File)
	 *
	 * @param format the {@link OCRFormat} to merge
	 * @param files the File's to merge
	 * @param outFile the result file to write to
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void mergeFiles (OCRFormat format, List<File> files, File outFile) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		fileMergers.get(format).invoke(null, new Object[] { files, outFile });
	}

	/**
	 * Merge Streams of the given format. This operates directly on Streams and should be 
	 * suitable for processing over WebDAV for example. Look at the used methods below
	 * for remarks on the different merge implementations.
	 * 
	 * @see #mergeAbbyyXML(List, OutputStream)
	 * @see #mergePDF(List, OutputStream)
	 * @see #mergeTXT(List, OutputStream)
	 *
	 * @param format the {@link OCRFormat} to merge
	 * @param iss the List of InputStreams
	 * @param os the OutputStram to write to.
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void mergeStreams (OCRFormat format, List<InputStream> iss, OutputStream os) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		streamMergers.get(format).invoke(null, new Object[] { iss, os });
	}
}
