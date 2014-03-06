package de.unigoettingen.sub.commons.ocr.web;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OcrServletChild extends OcrServlet {

	private static final long serialVersionUID = 1L;
	private String fakeValidationMessage;

	@Override
	protected void initOcrStarter(HttpServletRequest request) {
		ocrStarter = mock(OcrStarter.class);
		fakeValidationMessage = request.getParameter("fakeValidationMessage");
		when(ocrStarter.checkParameters()).thenReturn(fakeValidationMessage);
	}
	
	@Override
	protected void goToView(String viewName, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if ("OK".equals(fakeValidationMessage)) {
			verify(ocrStarter, times(1)).run();
		}
		PrintWriter out = response.getWriter();
		out.print("Forwarded to view: " + viewName);
		
	}
	
}
