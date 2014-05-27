package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;

/**
 * Implementation of an OCR process for the Abbyy OCRSDK online service.
 * Combines inputs, outputs, and settings for a set of images belonging 
 * to one work which will be OCRed into one result document.
 * 
 * @author dennis
 *
 */
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
		languageMapping.put(new Locale("ru"), "Russian");
		languageMapping.put(new Locale("fr"), "French");
		outputFormatMapping.put(OCRFormat.XML, "xml");
		outputFormatMapping.put(OCRFormat.TXT, "txt");
		outputFormatMapping.put(OCRFormat.PDF, "pdfSearchable");
		outputFormatMapping.put(OCRFormat.PDFA, "pdfa");

		textTypeMapping.put(OCRTextType.NORMAL, "normal");
		textTypeMapping.put(OCRTextType.GOTHIC, "gothic");
	}
	
	/**
	 * for unit tests
	 */
	void setClient(OcrsdkClient client) {
		this.client = client;
	}
	
	/**
	 * Starts the execution of this process. Input images, output formats, and 
	 * other settings must be set by now.
	 */
	public void start() {
		for (Locale language : langs) {
			client.addLanguage(abbyy(language));
		}
		for (OCRFormat format : ocrOutputs.keySet()) {
			client.addExportFormat(abbyy(format));
		}
		client.addTextType(abbyy(textType));
		
		for(OCRImage image : ocrImages) {
			byte[] imageBytes = ((OcrsdkImage)image).getAsBytes();
			client.submitImage(imageBytes);
		}
		client.processDocument();
		
		for (Map.Entry<OCRFormat, OCROutput> entry : ocrOutputs.entrySet()) {
			OCRFormat format = entry.getKey();
			InputStream result = client.getResultForFormat(abbyy(format));
			OcrsdkOutput output = (OcrsdkOutput) entry.getValue();
			output.save(result);
		}
		
	}

	private String abbyy(OCRTextType textType) {
		String abbyyType = textTypeMapping.get(textType);
		if (abbyyType == null) {
			throw new IllegalArgumentException("No corresponding mapping defined for text type: " + textType);
		}
		return abbyyType;
	}

	private String abbyy(Locale loc) {
		String abbyyLanguage = languageMapping.get(loc);
		if (abbyyLanguage == null) {
			throw new IllegalArgumentException("No corresponding mapping defined for language: " + loc);
		}
		return abbyyLanguage;
	}

	private String abbyy(OCRFormat format) {
		String abbyyOutputFormat = outputFormatMapping.get(format);
		if (abbyyOutputFormat == null) {
			throw new IllegalArgumentException("No corresponding mapping defined for output format: " + format);
		}
		return abbyyOutputFormat;
	}
	

}
