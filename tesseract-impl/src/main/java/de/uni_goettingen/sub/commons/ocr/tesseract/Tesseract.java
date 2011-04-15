package de.uni_goettingen.sub.commons.ocr.tesseract;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;

public class Tesseract {

	protected static Logger logger = LoggerFactory.getLogger(Tesseract.class);
	
	private final String tesseract = "tesseract";

	private OCRFormat format = OCRFormat.TXT;
	private File inputImage;
	private File outputBase;
	private String language = "deu";

	public Tesseract(File inputImage, File outputBase) {
		this.inputImage = inputImage;
		this.outputBase = outputBase;
	}

	public void setFormat(OCRFormat format) {
		this.format = format;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setGothic(boolean isGothic) {
		this.isGothic = isGothic;
	}

	private boolean isGothic = false;

	public void execute() {

		String postfix = isGothic ? "-frak" : "";

		String i = inputImage.getAbsolutePath();
		String o = outputBase.getAbsolutePath();
		String f = format == OCRFormat.HOCR ? "hocr" : "";

		logger.debug("Executing command: " + tesseract + " " + i + " " + o
				+ " " + "-l" + " " + language + postfix + " " + f);

		try {
			Process proc = new ProcessBuilder(tesseract, i, o, "-l", language
					+ postfix, f).start();

			logger.debug("Tesseract stdout: "
					+ IOUtils.toString(proc.getInputStream()));
			String tessError = IOUtils.toString(proc.getErrorStream());
			logger.debug("Tesseract stderr: " + tessError);

		} catch (IOException e) {
			logger.error("Error executing Tesseract.");
			e.printStackTrace();
		}
	}
}
