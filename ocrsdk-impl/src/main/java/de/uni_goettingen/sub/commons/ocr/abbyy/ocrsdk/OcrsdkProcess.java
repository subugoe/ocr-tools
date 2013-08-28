package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;

public class OcrsdkProcess extends AbstractOCRProcess {

	private static final long serialVersionUID = -2328068189131102581L;
	private Map<Locale, String> languageMapping = new HashMap<Locale, String>();
	private Map<OCRFormat, String> outputFormatMapping = new HashMap<OCRFormat, String>();
	private Map<OCRTextType, String> textTypeMapping = new HashMap<OCRTextType, String>();

	private Http http;
	private OcrsdkClient client;

	public OcrsdkProcess(String user, String password) {
		http = new Http(user, password);
		client = new OcrsdkClient(http);
		languageMapping.put(Locale.ENGLISH, "English");
		languageMapping.put(Locale.GERMAN, "German");
		outputFormatMapping.put(OCRFormat.XML, "xml");
		outputFormatMapping.put(OCRFormat.TXT, "txt");

		textTypeMapping.put(OCRTextType.NORMAL, "normal");
		textTypeMapping.put(OCRTextType.GOTHIC, "gothic");
	}
	
	public void start() {
		for(int i = 0; i < ocrImages.size(); i++) {
			byte[] imageBytes = ((OcrsdkImage)ocrImages.get(i)).getAsBytes();
			client.submitImage(imageBytes);
		}
		client.addLanguage(abbyy(Locale.ENGLISH));
		client.addExportFormat(abbyy(OCRFormat.XML));
		client.addExportFormat(abbyy(OCRFormat.TXT));
		client.addTextType(abbyy(OCRTextType.GOTHIC));
		client.addTextType(abbyy(OCRTextType.NORMAL));
		client.processDocument();
		
		InputStream txtResult = client.getResultForFormat(abbyy(OCRFormat.TXT));
		
		try {
			System.out.println(IOUtils.toString(txtResult));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String abbyy(OCRTextType textType) {
		return textTypeMapping.get(textType);
	}

	private String abbyy(Locale loc) {
		return languageMapping.get(loc);
	}

	private String abbyy(OCRFormat format) {
		return outputFormatMapping.get(format);
	}
	

}
