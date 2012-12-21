package de.unigoettingen.sub.commons.ocrComponents.webservice;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Observable;

import org.apache.commons.io.FileUtils;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

public class WebServiceTestOCREngine extends AbstractOCREngine {

	public static WebServiceTestOCREngine newOCREngine() {
		return new WebServiceTestOCREngine();
	}

	@Override
	public Observable recognize() {
		Map<OCRFormat, OCROutput> outputs = ocrProcess.get(0).getOcrOutputs();
		OCROutput output = outputs.values().iterator().next();
		File inputDir = new File(
				System.getProperty("user.dir") + "/src/test/resources/input");
		File sample = new File(inputDir, "sample.txt");
		try {
			FileUtils.copyFile(sample, new File(output.getUri()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Observable addOcrProcess(OCRProcess ocrp) {
		ocrProcess.add(ocrp);
		return null;
	}

	
	
	
	
	
	@Override
	public Observable recognize(OCRProcess process) {
		return null;
	}
	
	@Override
	public Boolean stop() {
		return null;
	}


	@Override
	public Boolean init() {
		return null;
	}

	@Override
	public void setOptions(Map<String, String> params) {
		
	}

	@Override
	public Map<String, String> getOptions() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getVersion() {
		return null;
	}

}