package de.uni_goettingen.sub.commons.ocr.tesseract;

import java.util.Map;
import java.util.Observable;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

/**
 * Implementation of the engine that uses the tesseract cli tool
 */
public class TesseractOCREngine extends AbstractOCREngine implements OCREngine {

	/** The _instance. */
	private static TesseractOCREngine _instance;

	/**
	 * Gets the single instance of TesseractOCREngine.
	 *
	 * @return single instance of TesseractOCREngine
	 */
	public static TesseractOCREngine getInstance() {

		if (_instance == null) {
			_instance = new TesseractOCREngine();
		}
		return _instance;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine#getName()
	 */
	@Override
	public String getName() {
		return "Tesseract";
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine#getVersion()
	 */
	@Override
	public String getVersion() {
		return "3";
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCREngine#addOcrProcess(de.uni_goettingen.sub.commons.ocr.api.OCRProcess)
	 */
	@Override
	public Observable addOcrProcess(OCRProcess process) {
		if (process instanceof TesseractOCRProcess) {
			ocrProcess.add((TesseractOCRProcess) process);
		} else {
			ocrProcess.add(new TesseractOCRProcess(process));
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCREngine#init()
	 */
	@Override
	public Boolean init() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCREngine#recognize(de.uni_goettingen.sub.commons.ocr.api.OCRProcess)
	 */
	@Override
	public Observable recognize(OCRProcess process) {
		addOcrProcess(process);
		recognize();
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCREngine#recognize()
	 */
	@Override
	public Observable recognize() {

		for (OCRProcess process : ocrProcess) {
			((TesseractOCRProcess) process).start();
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCREngine#stop()
	 */
	@Override
	public Boolean stop() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine#newOcrProcess()
	 */
	public OCRProcess newOcrProcess() {
		return new TesseractOCRProcess();
	}

	@Override
	public void setOptions(Map<String, String> params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, String> getOptions() {
		// TODO Auto-generated method stub
		return null;
	}
}
