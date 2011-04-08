package de.uni_goettingen.sub.commons.ocr.tesseract;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;


public class TesseractOCREngine extends AbstractOCREngine implements OCREngine {

	private static TesseractOCREngine _instance;

	public static TesseractOCREngine getInstance() {
		
		if (_instance == null) {
			_instance = new TesseractOCREngine();
		}
		return _instance;
	}

	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable addOcrProcess(OCRProcess ocrp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean init() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable recognize(OCRProcess process) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable recognize() {
		System.out.println("-------------  engine.recognize() ---------------");
		return null;
	}

	@Override
	public Boolean stop() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public OCRProcess newOcrProcess() {
		return new TesseractOCRProcess() {
			@Override
			public void setName(String name) {
				super.setName(name);
			}

			@Override
			public void addLanguage(Locale lang) {
				super.addLanguage(lang);
			}

			@Override
			public void setOcrOutputs(Map<OCRFormat, OCROutput> ocrOutput) {
				super.setOcrOutputs(ocrOutput);
			}

			@Override
			public void setOcrImages(List<OCRImage> ocrImages) {
				super.setOcrImages(ocrImages);
			}
		};
	}

	
}
