package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;

public class OcrsdkProcess extends AbstractOCRProcess {

	private static final long serialVersionUID = -2328068189131102581L;

	private final String sdkServer = "http://cloud.ocrsdk.com/";
	private Http http;

	private Element completedTask;
	
	public OcrsdkProcess(String user, String password) {
		http = new Http(user, password);
	}
	
	public void setHttp(Http http) {
		this.http = http;
	}
	
	public void start() {
		String taskId = "";
		for(int i = 0; i < ocrImages.size(); i++) {
			byte[] imageBytes = ((OcrsdkImage)ocrImages.get(i)).getAsBytes();
			if (i == 0) {
				String url = sdkServer + "submitImage";
				InputStream result = http.submitPost(url, imageBytes);
				taskId = getTaskId(result);
			} else {
				String url = sdkServer + "submitImage?taskId=" + taskId;
				http.submitPost(url, imageBytes);
			}
		}
		String url = sdkServer + "processDocument?taskId=" + taskId + "&language=English&exportFormat=xml";
		http.submitGet(url);
		
		String resultUrl = waitUntilTaskCompletes(taskId);

		InputStream completedResult = http.submitGetWithoutAuthentication(resultUrl);
		try {
			System.out.println(IOUtils.toString(completedResult));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String waitUntilTaskCompletes(String taskId) {
		while(true) {
			System.out.println("waiting");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String url = sdkServer + "getTaskStatus?taskId=" + taskId;
			InputStream resultXml = http.submitGet(url);
			String status = getTaskStatus(resultXml);
			if (status.equals("Completed")) {
				return getResultUrl();
			}
		}
	}

	private String getTaskId(InputStream xml) {
		Element task = getTaskElement(xml);
		return task.getAttribute("id");
	}

	private String getTaskStatus(InputStream xml) {
		completedTask = getTaskElement(xml);
		return completedTask.getAttribute("status");
	}
	
	private String getResultUrl() {
		return completedTask.getAttribute("resultUrl");
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
}
