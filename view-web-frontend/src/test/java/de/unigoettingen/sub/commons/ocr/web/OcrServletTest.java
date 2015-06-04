package de.unigoettingen.sub.commons.ocr.web;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class OcrServletTest {
	
	private OcrStarter ocrStarterMock = mock(OcrStarter.class);
	private OcrServlet servletSut = spy(new OcrServlet());
	private MockHttpServletRequest requestMock  = new MockHttpServletRequest();
	private MockHttpServletResponse responseMock = new MockHttpServletResponse();

	@Before
	public void setUp() throws Exception {
		doReturn(ocrStarterMock).when(servletSut).initOcrStarter(any(HttpServletRequest.class));
	}

	@Test
	public void shouldStartOcr() throws ServletException, IOException {
		when(ocrStarterMock.checkParameters()).thenReturn("OK");
		servletSut.doPost(requestMock, responseMock);
		
		assertEquals("ocr-started.jsp", responseMock.getForwardedUrl());
	}

	@Test
	public void shouldRefuseToStartOcr() throws ServletException, IOException {		
		when(ocrStarterMock.checkParameters()).thenReturn("Error");
		servletSut.doPost(requestMock, responseMock);
		
		assertEquals("invalid-parameters.jsp", responseMock.getForwardedUrl());
	}

}
