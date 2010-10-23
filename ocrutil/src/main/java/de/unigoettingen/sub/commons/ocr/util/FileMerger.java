package de.unigoettingen.sub.commons.ocr.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

public class FileMerger {
	public static void mergeAbbyyXML(List<InputStream> iss, OutputStream os) throws XMLStreamException {
		
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
			
			//Eingang
			InputStream in = iss.get(f); 
			XMLInputFactory inFactory = XMLInputFactory.newInstance();
			XMLStreamReader parser = inFactory.createXMLStreamReader(in);
			
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
							for (String namespace: nsPrefixes.keySet()) {
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
	
	public static void mergeAbbyyXML (List<String> files, File outFile) throws XMLStreamException, FileNotFoundException {
		OutputStream os = new FileOutputStream(outFile);
		List<InputStream> iss = new ArrayList<InputStream>();
		int f = 0;
		while (f < files.size()) {
			iss.add(new FileInputStream(files.get(f)));
		}
	}
	
	public static void mergePDF (List<InputStream> iss, OutputStream os) throws IOException, DocumentException {
		//Stolen from itext (com.lowagie.tools.concat_pdf) 
		
		int pageOffset = 0;
		List master = new ArrayList();
		int f = 0;
		//String outFile = args[args.length - 1];
		Document document = null;
		PdfCopy writer = null;
		while (f < iss.size()) {
			// we create a reader for a certain document
			PdfReader reader = new PdfReader(iss.get(f));
			reader.consolidateNamedDestinations();
			// we retrieve the total number of pages
			int n = reader.getNumberOfPages();
			List bookmarks = SimpleBookmark.getBookmark(reader);
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
	
	public static void mergePDF (List<String> files, File outFile) throws IOException, DocumentException {
		OutputStream os = new FileOutputStream(outFile);
		List<InputStream> iss = new ArrayList<InputStream>();
		int f = 0;
		while (f < files.size()) {
			iss.add(new FileInputStream(files.get(f)));
		}
		mergePDF(iss, os);
	}

	public static void mergeTXT (List<InputStream> iss, OutputStream os) throws IOException {
		OutputStreamWriter osw = new OutputStreamWriter(os);
		//Ascii page break dec 12 hex 0c		
		char pb = (char) 12;
		
		int f = 0;
		while (f < iss.size()) {
			InputStreamReader isr = new InputStreamReader(iss.get(f), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				  osw.write(line);
				  osw.write(System.getProperty("line.separator"));
			}
			osw.write(pb);
			
			isr.close();
			f++;
		}
		osw.close();
	}
	
	
	public static void mergeTXT (List<String> files, File outFile) throws IOException {
		OutputStream os = new FileOutputStream(outFile);
		List<InputStream> iss = new ArrayList<InputStream>();
		int f = 0;
		while (f < files.size()) {
			iss.add(new FileInputStream(files.get(f)));
		}
		mergeTXT (iss, os);
	}
}
