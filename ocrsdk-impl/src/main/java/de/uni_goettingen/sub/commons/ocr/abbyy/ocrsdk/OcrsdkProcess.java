package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;

public class OcrsdkProcess extends AbstractOCRProcess {

	private static final long serialVersionUID = -2328068189131102581L;
	private Map<Locale, String> languageMapping = new HashMap<Locale, String>();
	private Map<OCRFormat, String> outputFormatMapping = new HashMap<OCRFormat, String>();
	private Map<OCRTextType, String> textTypeMapping = new HashMap<OCRTextType, String>();

	private OcrsdkClient client;

	public OcrsdkProcess(String user, String password) {
		client = new OcrsdkClient(user, password);
		languageMapping.put(Locale.ENGLISH, "English");
		languageMapping.put(Locale.GERMAN, "German");
		outputFormatMapping.put(OCRFormat.XML, "xml");
		outputFormatMapping.put(OCRFormat.TXT, "txt");

		textTypeMapping.put(OCRTextType.NORMAL, "normal");
		textTypeMapping.put(OCRTextType.GOTHIC, "gothic");
	}
	
	void setClient(OcrsdkClient client) {
		this.client = client;
	}
	
	public void start() {
		for(OCRImage image : ocrImages) {
			byte[] imageBytes = ((OcrsdkImage)image).getAsBytes();
			client.submitImage(imageBytes);
		}
		for (Locale language : langs) {
			client.addLanguage(abbyy(language));
		}
		for (OCRFormat format : ocrOutputs.keySet()) {
			client.addExportFormat(abbyy(format));
		}
		client.addTextType(abbyy(textType));
		
		client.processDocument();
		
		for (Map.Entry<OCRFormat, OCROutput> entry : ocrOutputs.entrySet()) {
			OCRFormat format = entry.getKey();
			InputStream result = client.getResultForFormat(abbyy(format));
			OcrsdkOutput output = (OcrsdkOutput) entry.getValue();
			output.save(result);
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
