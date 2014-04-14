package de.unigoettingen.sub.commons.ocr.web;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextType;
import de.unigoettingen.sub.commons.ocr.util.OCRUtil;

public class OcrStarter implements Runnable {
	final static Logger LOGGER = LoggerFactory
			.getLogger(OcrStarter.class);
	
	private OcrParameters param;
	private EngineProvider engineProvider = new EngineProvider();
	private Mailer mailer = new Mailer();
	private LogSelector logSelector = new LogSelector();

	// for unit tests
	void setEngineProvider(EngineProvider newEngineProvider) {
		engineProvider = newEngineProvider;
	}
	void setMailer(Mailer newMailer) {
		mailer = newMailer;
	}
	void setLogSelector(LogSelector newSelector) {
		logSelector = newSelector;
	}
	
	public void setParameters(OcrParameters newParameters) {
		param = newParameters;
	}

	public String checkParameters() {
		String validationMessage = "";
		if (isEmpty(param.inputFolder)) {
			validationMessage += "No input folder. ";
		} else if (!isAbsolutePath(param.inputFolder)) {
			validationMessage += "Input folder must be absolute path. ";
		}
		if (isEmpty(param.outputFolder)) {
			validationMessage += "No output folder. ";
		} else if (!isAbsolutePath(param.outputFolder)) {
			validationMessage += "Output folder must be absolute path. ";
		}
		EmailValidator validator = EmailValidator.getInstance();
		if (isEmpty(param.email)) {
			validationMessage += "No email address. ";
		} else if (!validator.isValid(param.email)) {
			validationMessage += "Invalid email address. ";
		}
		if (isEmpty(param.languages)) {
			validationMessage += "No language. ";
		}
		if (isEmpty(param.outputFormats)) {
			validationMessage += "No output format. ";
		}
		if (validationMessage.equals("")) {
			return "OK";
		} else {
			return validationMessage;
		}
	}
	
	private boolean isEmpty(String string) {
		return string == null || string.isEmpty();
	}
	private boolean isEmpty(String[] array) {
		return array == null || array.length == 0;
	}
	private boolean isAbsolutePath(String path) {
		return new File(path).isAbsolute();
	}

	@Override
	public void run() {
		DateFormat f = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		String timeStamp = f.format(new Date()).replaceAll(" ", "-");
		String logFile = new File(new File(param.outputFolder), "log-" + timeStamp + ".txt").getAbsolutePath();
		logSelector.logToFile(logFile);
		OCREngine engine = createEngine();

		File mainFolder = new File(param.inputFolder);
		File[] subFolders = mainFolder.listFiles();
		for (File bookFolder : subFolders) {
			OCRProcess process = createProcess(engine, bookFolder);
			engine.addOcrProcess(process);
		}

		int estimatedDuration = engine.getEstimatedDurationInSeconds();
		mailer.sendStarted(param, estimatedDuration);
		engine.recognize();
		mailer.sendFinished(param);
	}

	private OCREngine createEngine() {
		OCREngine engine = null;
		Map<String,String> extraOptions = new HashMap<String,String>();
		if ("gbvAntiqua".equals(param.ocrEngine)) {
			engine = engineProvider.getFromContext("abbyy-multiuser");
			extraOptions.put("abbyy.config", "gbv-antiqua.properties");
		} else if ("gbvFraktur".equals(param.ocrEngine)) {
			engine = engineProvider.getFromContext("abbyy-multiuser");
			extraOptions.put("abbyy.config", "gbv-fraktur.properties");
		} else if ("abbyyCloud".equals(param.ocrEngine)) {
			engine = engineProvider.getFromContext("ocrsdk");
		} else {
			throw new IllegalArgumentException("Unknown engine: " + param.ocrEngine);
		}
		
		if (param.user != null && !param.user.isEmpty()) {
			extraOptions.put("user", param.user);
		}
		if (param.password != null && !param.password.isEmpty()) {
			extraOptions.put("password", param.password);
		}
		engine.setOptions(extraOptions);
		
		return engine;
	}

	private OCRProcess createProcess(OCREngine engine, File bookFolder) {
		OCRProcess process = engine.newOcrProcess();
		process.setName(bookFolder.getName());
		
		List<OCRImage> bookImages = new ArrayList<OCRImage>();
		for (File imageFile : OCRUtil.makeFileList(bookFolder, param.imageFormat)) {
			OCRImage image = engine.newOcrImage(imageFile.toURI());
			image.setSize(imageFile.length());
			bookImages.add(image);
		}
		process.setOcrImages(bookImages);
		process.setPriority(OCRPriority.NORMAL);
		
		Set<Locale> langs = new HashSet<Locale>();
		for (String lang : param.languages) {
			langs.add(new Locale(lang));
		}
		process.setLanguages(langs);
		process.setTextType(OCRTextType.valueOf(param.textType));
		process.setSplitProcess(true);
		
		try {
			for (String formatString : param.outputFormats) {
				OCRFormat format = OCRFormat.parseOCRFormat(formatString);
				OCROutput output = engine.newOcrOutput();
				URI uri = new URI(new File(param.outputFolder).toURI()
						+ bookFolder.getName()
						+ "." + format.toString().toLowerCase());
				output.setUri(uri);
				output.setlocalOutput(new File(param.outputFolder).getAbsolutePath());
				process.addOutput(format, output);
			}
		} catch(URISyntaxException e){
			LOGGER.error("Illegal URI", e);
		}
		return process;
	}

}
