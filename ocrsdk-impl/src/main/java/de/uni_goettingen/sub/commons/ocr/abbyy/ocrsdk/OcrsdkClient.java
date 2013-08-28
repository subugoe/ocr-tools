package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class OcrsdkClient {

	private final String sdkServer = "http://cloud.ocrsdk.com/";
	public String taskId;
	private Http http;
	
	private ArrayList<String> languagesToUse = new ArrayList<String>();
	private ArrayList<String> formatsToUse = new ArrayList<String>();
	private ArrayList<String> textTypesToUse = new ArrayList<String>();
	
	private Element completedTask;
	private List<String> resultUrls = new ArrayList<String>();
	
	public OcrsdkClient(Http http) {
		this.http = http;
	}
	
	public void submitImage(byte[] imageBytes) {
		InputStream returnedXml = http.submitPost(submitImageUrl(), imageBytes);
		if (taskId == null) {
			Element task = getTaskElement(returnedXml);
			taskId = task.getAttribute("id");
		}
	}

	private String submitImageUrl() {
		String url = sdkServer + "submitImage";
		if (taskId != null) {
			url += "?taskId" + taskId;
		}
		return url;
	}
	
	private Element getTaskElement(InputStream xml) {
		Element task = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(xml);
			NodeList taskNodes = doc.getElementsByTagName("task");
			task = (Element) taskNodes.item(0);
		} catch (Exception e) {
			throw new IllegalStateException("Error while parsing returned XML.", e);
		}
		return task;
	}
	
	public void addLanguage(String lang) {
		languagesToUse.add(lang);
	}
	
	public void addExportFormat(String format) {
		if (formatsToUse.size() == 3) {
			throw new IllegalStateException("Max 3 result formats allowed!");
		}
		formatsToUse.add(format);
	}

	public void addTextType(String textType) {
		textTypesToUse.add(textType);		
	}

	public void processDocument() {
		http.submitGet(processDocumentUrl());
		waitUntilTaskCompletes();
	}

	private String processDocumentUrl() {
		String url = sdkServer + "processDocument?taskId=" + taskId;
		if (!languagesToUse.isEmpty()) {
			url += parameter("language", languagesToUse);
		}
		if (!formatsToUse.isEmpty()) {
			url += parameter("exportFormat", formatsToUse);
		}
		if (!textTypesToUse.isEmpty()) {
			url += parameter("textType", textTypesToUse);
		}
		System.err.println(url);
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

	private void waitUntilTaskCompletes() {
		while(true) {
			System.out.println("waiting");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			String url = sdkServer + "getTaskStatus?taskId=" + taskId;
			InputStream resultXml = http.submitGet(url);
			completedTask = getTaskElement(resultXml);
			String status = completedTask.getAttribute("status");
			if (status.equals("Completed")) {
				populateResultUrls();
				return;
			}
		}
	}
	
	private void populateResultUrls() {
		resultUrls.add(completedTask.getAttribute("resultUrl"));
		String url2 = completedTask.getAttribute("resultUrl2");
		String url3 = completedTask.getAttribute("resultUrl3");
		if (!"".equals(url2))
			resultUrls.add(url2);
		if (!"".equals(url3))
			resultUrls.add(url3);
	}

	public InputStream getResultForFormat(String format) {
		int listIndex = formatsToUse.indexOf(format);
		if (listIndex == -1) {
			throw new IllegalArgumentException("No result for format " + format);
		}
		InputStream is = null;
		try {
			URL formatUrl = new URL(resultUrls.get(listIndex));
			is = formatUrl.openStream();
		} catch (IOException e) {
			throw new IllegalStateException("Could not read the result for " + format, e);
		}
		return is;
	}

}
