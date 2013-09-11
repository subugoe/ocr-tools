package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Client for the Abbyy ocrsdk online service.
 * 
 * @author dennis
 *
 */
public class OcrsdkClient {

	private final String sdkServer = "http://cloud.ocrsdk.com/";
	private String taskId;
	private Http http;
	
	private ArrayList<String> languagesToUse = new ArrayList<String>();
	private ArrayList<String> formatsToUse = new ArrayList<String>();
	private ArrayList<String> textTypesToUse = new ArrayList<String>();
	
	private List<String> resultUrls = new ArrayList<String>();
	
	public OcrsdkClient(String username, String password) {
		http = new Http(username, password);
	}
	
	/**
	 * for unit tests
	 */
	void setHttp(Http http) {
		this.http = http;
	}
	
	/**
	 * Sends an image to the abbyy ocr service.
	 * @param imageBytes
	 */
	public void submitImage(byte[] imageBytes) {
		String returnedXml = http.submitPost(submitImageUrl(), imageBytes);
		if (taskId == null) {
			Element task = getTaskElement(returnedXml);
			taskId = task.getAttribute("id");
		}
	}

	private String submitImageUrl() {
		String url = sdkServer + "submitImage";
		if (taskId != null) {
			url += "?taskId=" + taskId;
		}
		return url;
	}
	
	private Element getTaskElement(String xml) {
		Element task = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			InputStream xmlStream = new ByteArrayInputStream(xml.getBytes());
			Document doc = builder.parse(xmlStream);
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

	/**
	 * Orders the Abbyy service to process the uploaded images.
	 */
	public void processDocument() {
		String url = processDocumentUrl();
		System.out.println("Starting to process the images. URL is: " + url);
		http.submitGet(url);
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
		String url = sdkServer + "getTaskStatus?taskId=" + taskId;
		while(true) {
			String resultXml = http.submitGet(url);
			String status = getTaskElement(resultXml).getAttribute("status");
			if (status.equals("Completed")) {
				populateResultUrls(resultXml);
				return;
			}
			System.out.println("Waiting");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void populateResultUrls(String xml) {
		Element completedTask = getTaskElement(xml);
		resultUrls.add(completedTask.getAttribute("resultUrl"));
		String url2 = completedTask.getAttribute("resultUrl2");
		String url3 = completedTask.getAttribute("resultUrl3");
		if (!"".equals(url2))
			resultUrls.add(url2);
		if (!"".equals(url3))
			resultUrls.add(url3);
	}

	/**
	 * Retrieves the result document for the specified output format.
	 * 
	 * @param format
	 * @return
	 */
	public InputStream getResultForFormat(String format) {
		int listIndex = formatsToUse.indexOf(format);
		if (listIndex == -1) {
			throw new IllegalArgumentException("No result for format " + format);
		}
		String resultUrl = resultUrls.get(listIndex);
		return http.submitGetWithoutAuthentication(resultUrl);
	}

}
