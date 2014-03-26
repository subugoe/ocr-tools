package de.unigoettingen.sub.commons.ocr.web;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class Mailer {

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
		send(emailFinished, param.email, "OCR Prozess beendet", "Ausgabeordner: " + param.outputFolder);
	}
	
	private void send(Email email, String toAddress, String subject, String message) {
		try {
			email.setHostName("localhost");
			//email.setSmtpPort(25);
			//email.setAuthenticator(new DefaultAuthenticator("username", "password"));
			//email.setSSLOnConnect(true);
			email.setFrom("no-reply@gwdg.de");
			email.setSubject(subject);
			email.setMsg(message);
			email.addTo(toAddress);
			email.send();
		} catch (EmailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
