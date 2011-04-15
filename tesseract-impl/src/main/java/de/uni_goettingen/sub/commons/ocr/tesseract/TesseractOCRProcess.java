package de.uni_goettingen.sub.commons.ocr.tesseract;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

public class TesseractOCRProcess extends AbstractOCRProcess implements
		OCRProcess {

	private static Map<String, String> languages = new HashMap<String, String>();
	
	static {
		languages.put("german", "deu");
		languages.put("english", "eng");
	}
	
	public TesseractOCRProcess(OCRProcess process) {
		super(process);
	}

	public TesseractOCRProcess() {
		super();
	}

	@Override
	public void addOutput(OCRFormat format, OCROutput output) {

		if (ocrOutputs == null) {
			// We use a LinkedHashMap to get the order of the elements
			// predictable
			ocrOutputs = new LinkedHashMap<OCRFormat, OCROutput>();
		}
		ocrOutputs.put(format, output);
	}

	public void start() {
		for (Map.Entry<OCRFormat, OCROutput> formatToOutput : ocrOutputs
				.entrySet()) {
			for (OCRImage image : ocrImages) {

				String imagePath = image.getUri().toString();
				String outputPath = formatToOutput.getValue().getUri()
						.toString();
				if (imagePath.startsWith("file:")
						&& outputPath.startsWith("file:")) {
					File inputImage = new File(imagePath.substring(5));
					File output = new File(outputPath.substring(5));
					Locale locale = new ArrayList<Locale>(langs).get(0);
					OCRFormat format = formatToOutput.getKey();

					Tesseract tesseract = new Tesseract(inputImage, output);
					tesseract.setFormat(format);
					tesseract.setLanguage( languages.get(locale.getLanguage()) );
					
					if(getTextTyp() == OCRTextTyp.Gothic) {
						tesseract.setGothic(true);
					}
					
					tesseract.execute();
				} else {
					System.out.println("--------------- error");
				}

			}
		}
	}

}
