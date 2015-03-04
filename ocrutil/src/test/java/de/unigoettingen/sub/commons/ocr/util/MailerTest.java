package de.unigoettingen.sub.commons.ocr.util;


import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class MailerTest {

	private Email emailMock;
	private Mailer mailer;
	
	@Before
	public void before() {
		emailMock = mock(Email.class);
		mailer = new Mailer();
		mailer.setEmailStarted(emailMock);
		mailer.setEmailFinished(emailMock);
	}
	
	@Test
	public void ocrStarted() throws EmailException {
		mailer.sendStarted("test@test.com", 1);
		
		verify(emailMock).addTo("test@test.com");
		verify(emailMock).send();
	}
	
	@Test
	public void ocrFinished() throws EmailException {
		mailer.sendFinished("test@test.com", "/tmp");
		
		verify(emailMock).addTo("test@test.com");
		verify(emailMock).send();
	}
	
	@Test
	public void ocrStartedAndFinished() throws EmailException {
		mailer.sendStarted("test@test.com", 1);
		mailer.sendFinished("test@test.com", "/tmp");
		
		verify(emailMock, times(2)).addTo("test@test.com");
		verify(emailMock, times(2)).send();
	}
}
