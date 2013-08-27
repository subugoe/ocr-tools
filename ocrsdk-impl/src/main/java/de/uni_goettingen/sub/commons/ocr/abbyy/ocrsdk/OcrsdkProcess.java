package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;

public class OcrsdkProcess extends AbstractOCRProcess {

	private static final long serialVersionUID = -2328068189131102581L;

	private final String sdkServer = "http://cloud.ocrsdk.com/";
	private Http http;
	private OcrsdkClient client;

	private Element completedTask;
	
	public OcrsdkProcess(String user, String password) {
		http = new Http(user, password);
		client = new OcrsdkClient(http);
	}
	
	public void start() {
		for(int i = 0; i < ocrImages.size(); i++) {
			byte[] imageBytes = ((OcrsdkImage)ocrImages.get(i)).getAsBytes();
			client.submitImage(imageBytes);
		}
		client.addLanguage(Locale.ENGLISH);
		client.addExportFormat(OCRFormat.XML);
		client.addExportFormat(OCRFormat.TXT);
		client.processDocument();
		
		List<String> resultUrls = waitUntilTaskCompletes(client.taskId);

		System.err.println(resultUrls.size());
		for (String resultUrl : resultUrls) {
			InputStream completedResult = http.submitGetWithoutAuthentication(resultUrl);
			try {
				
				System.out.println(IOUtils.toString(completedResult));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private List<String> waitUntilTaskCompletes(String taskId) {
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
				return getResultUrls();
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
	
	private List<String> getResultUrls() {
		List<String> resultUrls = new ArrayList<String>();
		resultUrls.add(completedTask.getAttribute("resultUrl"));
		String url2 = completedTask.getAttribute("resultUrl2");
		String url3 = completedTask.getAttribute("resultUrl3");
		if (!"".equals(url2))
			resultUrls.add(url2);
		if (!"".equals(url3))
			resultUrls.add(url3);
		return resultUrls;
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
