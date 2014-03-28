package de.unigoettingen.sub.commons.ocr.web;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mailer {
	final static Logger LOGGER = LoggerFactory
			.getLogger(Mailer.class);
	
	private Email emailStarted = new SimpleEmail();
	private Email emailFinished = new SimpleEmail();
	
	// for unit tests
	void setEmailStarted(Email newEmail) {
		emailStarted = newEmail;
	}
	void setEmailFinished(Email newEmail) {
		emailFinished = newEmail;
	}
	
	public void sendStarted(OcrParameters param, int estimatedDuration) {
		send(emailStarted, param.email, "OCR Prozess gestartet", "Voraussichtliche Dauer: " + (estimatedDuration/60) + " Minuten");
	}
	public void sendFinished(OcrParameters param) {
		String message = "Ausgabeordner: " + param.outputFolder;
		send(emailFinished, param.email, "OCR Prozess beendet", message);
	}
	
	private void send(Email email, String toAddress, String subject, String message) {
		try {
			email.setHostName("localhost");
			email.setFrom("no-reply@gwdg.de");
			email.setSubject(subject);
			email.setMsg(message);
			email.addTo(toAddress);
			email.send();
		} catch (EmailException e) {
			LOGGER.error("Could not send mail to " + toAddress, e);
		}

	}

}
