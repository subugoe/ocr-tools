package de.unigoettingen.sub.commons.ocr.web;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class MailerTest {

	private Email emailMock;
	private Mailer mailer;
	private OcrParameters param;
	
	@Before
	public void before() {
		emailMock = mock(Email.class);
		mailer = new Mailer();
		mailer.setEmailStarted(emailMock);
		mailer.setEmailFinished(emailMock);
		param = new OcrParameters();
		param.email = "test@test.com";
	}
	
	@Test
	public void ocrStarted() throws EmailException {
		mailer.sendStarted(param, 1);
		
		verify(emailMock).addTo(param.email);
		verify(emailMock).send();
	}
	
	@Test
	public void ocrFinished() throws EmailException {
		mailer.sendFinished(param);
		
		verify(emailMock).addTo(param.email);
		verify(emailMock).send();
	}
	
	@Test
	public void ocrStartedAndFinished() throws EmailException {
		mailer.sendStarted(param, 1);
		mailer.sendFinished(param);
		
		verify(emailMock, times(2)).addTo(param.email);
		verify(emailMock, times(2)).send();
	}
}
