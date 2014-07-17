package de.unigoettingen.sub.commons.ocr.web;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unigoettingen.sub.ocr.controller.OcrEngineStarter;
import de.unigoettingen.sub.ocr.controller.OcrParameters;
import de.unigoettingen.sub.ocr.controller.Validator;
import de.unigoettingen.sub.ocr.controller.ValidatorGerman;

public class OcrStarter implements Runnable {
	final static Logger LOGGER = LoggerFactory
			.getLogger(OcrStarter.class);
	
	private OcrParameters param;
	private Mailer mailer = new Mailer();
	private LogSelector logSelector = new LogSelector();
	private Validator paramsValidator = new ValidatorGerman();
	private OcrEngineStarter engineStarter = new OcrEngineStarter();

	// for unit tests
	void setMailer(Mailer newMailer) {
		mailer = newMailer;
	}
	void setLogSelector(LogSelector newSelector) {
		logSelector = newSelector;
	}
	void setValidator(Validator newValidator) {
		paramsValidator = newValidator;
	}
	void setOcrEngineStarter(OcrEngineStarter newStarter) {
		engineStarter = newStarter;
	}
	
	public void setParameters(OcrParameters initParameters) {
		param = initParameters;
	}
	
	public String checkParameters() {
		String validationMessage = paramsValidator.validateParameters(param);
		if ("OK".equals(validationMessage)) {
			validationMessage = "";
		}
		EmailValidator validator = EmailValidator.getInstance();
		String email = param.props.getProperty("email");
		if (isEmpty(email)) {
			validationMessage += "Keine Benachrichtigungsadresse. ";
		} else if (!validator.isValid(email)) {
			validationMessage += "Inkorrekte Benachrichtigungsadresse. ";
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

	@Override
	public void run() {
		DateFormat f = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		String timeStamp = f.format(new Date()).replaceAll(" ", "-").replaceAll(":", ".");
		String logFile = new File(new File(param.outputFolder), "log-" + timeStamp + ".txt").getAbsolutePath();
		logSelector.logToFile(logFile);
		
		int estimatedDuration = engineStarter.getEstimatedDurationInSeconds();
		mailer.sendStarted(param, estimatedDuration);
		
		engineStarter.startOcrWithParams(param);
		
		mailer.sendFinished(param);
	}

}
