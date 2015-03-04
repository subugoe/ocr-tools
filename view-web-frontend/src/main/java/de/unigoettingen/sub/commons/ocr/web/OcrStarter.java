package de.unigoettingen.sub.commons.ocr.web;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.validator.routines.EmailValidator;

import de.unigoettingen.sub.commons.ocr.util.Mailer;
import de.unigoettingen.sub.commons.ocr.util.OcrParameters;
import de.unigoettingen.sub.ocr.controller.OcrEngineStarter;
import de.unigoettingen.sub.ocr.controller.Validator;
import de.unigoettingen.sub.ocr.controller.ValidatorGerman;

public class OcrStarter implements Runnable {
	
	private OcrParameters params;
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
		params = initParameters;
	}
	
	public String checkParameters() {
		String validationMessage = paramsValidator.validateParameters(params);
		if ("OK".equals(validationMessage)) {
			validationMessage = "";
		}
		EmailValidator validator = EmailValidator.getInstance();
		String email = params.props.getProperty("email");
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
		String logFile = new File(new File(params.outputFolder), "log-" + timeStamp + ".txt").getAbsolutePath();
		logSelector.logToFile(logFile);
		
		int estimatedDuration = engineStarter.getEstimatedDurationInSeconds();
		mailer.sendStarted(params, estimatedDuration);
		
		engineStarter.startOcrWithParams(params);
		
		mailer.sendFinished(params);	
		logSelector.useDefaults();
	}

}
