package de.uni_goettingen.sub.commons.ocr.tesseract;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.unigoettingen.sub.commons.ocr.util.FileMerger;

/**
 * Represents an OCR job with several images.
 */
public class TesseractOCRProcess extends AbstractOCRProcess implements
		OCRProcess {

	/** The logger. */
	protected static Logger logger = LoggerFactory
			.getLogger(TesseractOCRProcess.class);

	/**
	 * The temp files which are generated for each run of tesseract. Are merged
	 * into one file at the end.
	 */
	private List<File> tempFiles = new ArrayList<File>();

	/**
	 * Languages of the images, mapped to strings which tesseract understands.
	 * Tesseract can only use one for each image.
	 */
	private static Map<String, String> languages = new HashMap<String, String>();

	/**
	 * The extensions that are generated by tesseract. txt for text results,
	 * html for hocr results
	 */
	private static Map<OCRFormat, String> extensions = new HashMap<OCRFormat, String>();

	/** Mappings of the interface formats to tesseract-specific ones */
	private static Map<OCRFormat, String> formats = new HashMap<OCRFormat, String>();

	static {
		languages.put("german", "deu");
		languages.put("english", "eng");

		extensions.put(OCRFormat.TXT, "txt");
		extensions.put(OCRFormat.HOCR, "html");

		formats.put(OCRFormat.TXT, "");
		formats.put(OCRFormat.HOCR, "hocr");
	}

	/**
	 * Instantiates a new tesseract ocr process.
	 * 
	 * @param process
	 *            the process
	 */
	public TesseractOCRProcess(OCRProcess process) {
		super(process);
	}

	/**
	 * Instantiates a new tesseract ocr process.
	 */
	public TesseractOCRProcess() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess#addOutput(de
	 * .uni_goettingen.sub.commons.ocr.api.OCRFormat,
	 * de.uni_goettingen.sub.commons.ocr.api.OCROutput)
	 */
	@Override
	public void addOutput(OCRFormat format, OCROutput output) {

		if (ocrOutputs == null) {
			ocrOutputs = new LinkedHashMap<OCRFormat, OCROutput>();
		}
		ocrOutputs.put(format, output);
	}

	/**
	 * Manages the input images and output files, then starts tesseract once for
	 * each image.
	 */
	public void start() {
		for (Map.Entry<OCRFormat, OCROutput> formatToOutput : ocrOutputs
				.entrySet()) {

			// eg TXT
			OCRFormat format = formatToOutput.getKey();

			OCROutput output = formatToOutput.getValue();

			// to have a different file name for each OCRed text
			int i = 1;

			for (OCRImage image : ocrImages) {
				String tempPath = System.getProperty("user.dir")
						+ System.getProperty("file.separator") + "temp.tif";
				File localImage = getLocalImage(image, tempPath);
				File localTempOutput = getLocalOutput(output, i + "");
				i++;

				executeTesseract(localImage, format, localTempOutput);
				// localImage.delete();

				// eg html for HOCR files, is automatically added by tesseract
				String actualExtension = extensions.get(format);

				String actualOutput = localTempOutput.getAbsolutePath() + "."
						+ actualExtension;
				tempFiles.add(new File(actualOutput));

			}

			File localOutput = getLocalOutput(output, "");

			FileMerger.mergeFiles(format, tempFiles, localOutput);

			for (File file : tempFiles) {
				logger.debug("Deleting file " + file.getAbsolutePath());
				file.delete();
			}
		}
	}

	private File getLocalImage(OCRImage image, String tempPath) {
		File result = null;

		String protocol = image.getUri().getScheme();

		if (protocol.equals("file")) {
			result = new File(image.getUri().getPath());

		} else {
			try {
				InputStream is = image.getUri().toURL().openStream();
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(result));

				byte[] buffer = new byte[32 * 1024];
				while ((is.read(buffer)) != -1) {
					bos.write(buffer);
				}

				is.close();
				bos.close();

			} catch (MalformedURLException e) {
				logger.error("Not a URL: " + image.getUri());
				e.printStackTrace();
			} catch (IOException e) {
				logger.error("Error while downloading or saving image.");
				e.printStackTrace();
			}

		}

		return result;
	}

	private File getLocalOutput(OCROutput output, String postfix) {
		String protocol = output.getUri().getScheme();
		if (protocol.equals("file")) {
			return new File(output.getUri().getPath() + postfix);
		} else {
			// TODO handle remote output uris
			throw new RuntimeException("Unsupported protocol for outputs: "
					+ protocol);
		}
	}

	/**
	 * Execute tesseract.
	 * 
	 * @param image
	 *            the image
	 * @param format
	 *            the format
	 * @param output
	 *            the output
	 */
	private void executeTesseract(File image, OCRFormat format, File output) {

		Tesseract tesseract = new Tesseract(image, output);
		tesseract.setFormat(formats.get(format));

		// tesseract only takes one language
		Locale locale = new ArrayList<Locale>(langs).get(0);
		tesseract.setLanguage(languages.get(locale.getLanguage()));

		if (getTextTyp() == OCRTextTyp.GOTHIC) {
			tesseract.setGothic(true);
		}

		tesseract.execute();

	}
}
