package de.unigoettingen.sub.commons.ocr.web;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class Mailer {

	private Email email = new SimpleEmail();
	
	void setEmail(Email newEmail) {
		email = newEmail;
	}
	
	public void sendFinished(OcrParameters param) {
		
		try {
			email.setHostName("localhost");
			//email.setSmtpPort(25);
			//email.setAuthenticator(new DefaultAuthenticator("username", "password"));
			//email.setSSLOnConnect(true);
			email.setFrom("no-reply@gwdg.de");
			email.setSubject("Process finished");
			email.setMsg("Your results should be in " + param.outputFolder);
			email.addTo(param.email);
			email.send();
		} catch (EmailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
