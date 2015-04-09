package de.uni_goettingen.sub.commons.ocr.tesseract;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wrapper for the tesseract command-line tool. 
 * 
 * @author dennis
 *
 * 
 */

public class Tesseract {

	protected static Logger logger = LoggerFactory.getLogger(Tesseract.class);
	
	/**
	 * the command for the shell
	 */
	private final String tesseract = "tesseract";

	/**
	 * It this is empty, then tesseract will produce a text file.
	 * Another option is currently 'hocr'
	 */
	private String format = "";
	private File inputImage;
	private File outputBase;
	
	/**
	 * the corresponding language package must be installed on the system.
	 */
	private String language = "deu";

	private boolean isGothic = false;

	public Tesseract(File inputImage, File outputBase) {
		this.inputImage = inputImage;
		this.outputBase = outputBase;
	}

	/**
	 * tesseract v3 only supports txt and hocr
	 * @param format
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * the corresponding language package must be installed on the system.
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	public void setGothic(boolean isGothic) {
		this.isGothic = isGothic;
	}


	/**
	 * Starts tesseract on the command line using the parameter fields
	 */
	public void execute() {

		String postfix = isGothic ? "-frak" : "";

		String i = inputImage.getAbsolutePath();
		String o = outputBase.getAbsolutePath();

		logger.debug("Executing command: " + tesseract + " " + i + " " + o
				+ " " + "-l" + " " + language + postfix + " " + format);

		try {
			Process proc = new ProcessBuilder(tesseract, i, o, "-l", language
					+ postfix, format).start();

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
