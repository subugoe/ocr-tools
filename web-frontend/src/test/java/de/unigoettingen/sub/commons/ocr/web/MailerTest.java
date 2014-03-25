package de.unigoettingen.sub.commons.ocr.web;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class MailerTest {

	@Test
	public void test() throws EmailException {
		Email emailMock = mock(Email.class);

		Mailer mailer = new Mailer();
		mailer.setEmail(emailMock);
		
		OcrParameters param = new OcrParameters();
		param.email = "test@test.com";
		mailer.sendFinished(param);
		
		verify(emailMock).addTo(param.email);
		verify(emailMock).send();
	}
}
