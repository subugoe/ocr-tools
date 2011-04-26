package de.uni_goettingen.sub.commons.ocr.tesseract;

import java.util.Observable;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
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
		return "Tesseract";
	}

	@Override
	public String getVersion() {
		return "3";
	}

	@Override
	public Observable addOcrProcess(OCRProcess process) {
		if (process instanceof TesseractOCRProcess) {
			ocrProcess.add((TesseractOCRProcess) process);
		} else {
			ocrProcess.add(new TesseractOCRProcess(process));
		}
		return null;
	}

	@Override
	public Boolean init() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Observable recognize(OCRProcess process) {
		addOcrProcess(process);
		recognize();
		return null;
	}

	@Override
	public Observable recognize() {

		for (OCRProcess process : ocrProcess) {
			((TesseractOCRProcess) process).start();
		}

		return null;
	}

	@Override
	public Boolean stop() {
		throw new UnsupportedOperationException();
	}

	public OCRProcess newOcrProcess() {
		return new TesseractOCRProcess();
	}
}
