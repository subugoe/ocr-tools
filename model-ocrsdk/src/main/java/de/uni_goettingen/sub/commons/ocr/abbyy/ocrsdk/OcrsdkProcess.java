package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import de.uni_goettingen.sub.commons.ocr.api.AbstractProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrImage;
import de.uni_goettingen.sub.commons.ocr.api.OcrOutput;
import de.uni_goettingen.sub.commons.ocr.api.OcrTextType;

/**
 * Implementation of an OCR process for the Abbyy OCRSDK online service.
 * Combines inputs, outputs, and settings for a set of images belonging 
 * to one work which will be OCRed into one result document.
 * 
 * @author dennis
 *
 */
public class OcrsdkProcess extends AbstractProcess {

	private static final long serialVersionUID = -2328068189131102581L;
	private Map<Locale, String> languageMapping = new HashMap<Locale, String>();
	private Map<OcrFormat, String> outputFormatMapping = new HashMap<OcrFormat, String>();
	private Map<OcrTextType, String> textTypeMapping = new HashMap<OcrTextType, String>();

	private OcrsdkClient client;

	public OcrsdkProcess(String user, String password) {
		if (user == null || user.equals("") || password == null || password.equals("")) {
			throw new IllegalArgumentException("You have to provide the AppId and the password.");
		}
		client = new OcrsdkClient(user, password);
		languageMapping.put(Locale.ENGLISH, "English");
		languageMapping.put(Locale.GERMAN, "German");
		languageMapping.put(new Locale("ru"), "Russian");
		languageMapping.put(new Locale("fr"), "French");
		outputFormatMapping.put(OcrFormat.XML, "xml");
		outputFormatMapping.put(OcrFormat.TXT, "txt");
		outputFormatMapping.put(OcrFormat.PDF, "pdfSearchable");
		outputFormatMapping.put(OcrFormat.PDFA, "pdfa");

		textTypeMapping.put(OcrTextType.NORMAL, "normal");
		textTypeMapping.put(OcrTextType.GOTHIC, "gothic");
	}
	
	public OcrsdkProcess(Properties userProperties) {
		this(userProperties.getProperty("user"), userProperties.getProperty("password"));
	}

	/**
	 * for unit tests
	 */
	void setClient(OcrsdkClient client) {
		this.client = client;
	}
	
	@Override
	public void addImage(URI localUri, long fileSize) {
		OcrsdkImage image = new OcrsdkImage();
		image.setLocalUri(localUri);
		image.setFileSize(fileSize);
		ocrImages.add(image);
	}
	
	@Override
	public void addOutput(OcrFormat format) {
		OcrsdkOutput output = new OcrsdkOutput();
		output.setLocalUri(constructLocalUri(format));
		output.setFormat(format);
		ocrOutputs.add(output);
	}
	
	/**
	 * Starts the execution of this process. Input images, output formats, and 
	 * other settings must be set by now.
	 */
	public void start() {
		for (Locale language : langs) {
			client.addLanguage(abbyy(language));
		}
		for (OcrFormat format : getAllOutputFormats()) {
			client.addExportFormat(abbyy(format));
		}
		client.addTextType(abbyy(textType));
		
		for(OcrImage image : ocrImages) {
			byte[] imageBytes = ((OcrsdkImage)image).getAsBytes();
			client.submitImage(imageBytes);
		}
		client.processDocument();
		
		for (OcrOutput entry : ocrOutputs) {
			OcrFormat format = entry.getFormat();
			InputStream result = client.getResultForFormat(abbyy(format));
			OcrsdkOutput output = (OcrsdkOutput) entry;
			output.save(result);
		}
		
	}

	private String abbyy(OcrTextType textType) {
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

	private String abbyy(OcrFormat format) {
		String abbyyOutputFormat = outputFormatMapping.get(format);
		if (abbyyOutputFormat == null) {
			throw new IllegalArgumentException("No corresponding mapping defined for output format: " + format);
		}
		return abbyyOutputFormat;
	}
	

}
