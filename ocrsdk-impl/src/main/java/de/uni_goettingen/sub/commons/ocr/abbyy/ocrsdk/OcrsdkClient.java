package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;

public class OcrsdkClient {

	private final String sdkServer = "http://cloud.ocrsdk.com/";
	public String taskId;
	private Http http;
	
	private Map<Locale, String> languageMapping = new HashMap<Locale, String>();
	private ArrayList<String> languagesToUse = new ArrayList<String>();
	
	private Map<OCRFormat, String> formatMapping = new HashMap<OCRFormat, String>();
	private ArrayList<String> formatsToUse = new ArrayList<String>();
	
	public OcrsdkClient(Http http) {
		this.http = http;
		languageMapping.put(Locale.ENGLISH, "English");
		languageMapping.put(Locale.GERMAN, "German");
		formatMapping.put(OCRFormat.XML, "xml");
		formatMapping.put(OCRFormat.TXT, "txt");
	}
	
	public void submitImage(byte[] imageBytes) {
		InputStream returnedXml = http.submitPost(submitImageUrl(), imageBytes);
		if (taskId == null) {
			taskId = getTaskId(returnedXml);
		}
	}

	private String submitImageUrl() {
		String url = sdkServer + "submitImage";
		if (taskId != null) {
			url += "?taskId" + taskId;
		}
		return url;
	}
	
	private String getTaskId(InputStream xml) {
		Element task = getTaskElement(xml);
		return task.getAttribute("id");
	}

	private Element getTaskElement(InputStream xml) {
		Element task = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(xml);
			NodeList taskNodes = doc.getElementsByTagName("task");
			task = (Element) taskNodes.item(0);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return task;
	}
	
	public void addLanguage(Locale loc) {
		languagesToUse.add(languageMapping.get(loc));
	}
	
	public void addExportFormat(OCRFormat format) {
		if (formatsToUse.size() == 3) {
			throw new IllegalStateException("Max 3 result formats allowed!");
		}
		formatsToUse.add(formatMapping.get(format));
	}

	public void processDocument() {
		http.submitGet(processDocumentUrl());
	}

	private String processDocumentUrl() {
		String url = sdkServer + "processDocument?taskId=" + taskId;
		if (!languagesToUse.isEmpty()) {
			url += parameter("language", languagesToUse);
		}
		if (!formatsToUse.isEmpty()) {
			url += parameter("exportFormat", formatsToUse);
		}
		return url;
	}

	private String parameter(String name, ArrayList<String> arguments) {
		String urlPart = "&" + name + "=";
		boolean first = true;
		for (String arg : arguments) {
			if (first) {
				first = false;
			} else {
				urlPart += ",";
			}
			urlPart += arg;
		}
		return urlPart;
	}
}
