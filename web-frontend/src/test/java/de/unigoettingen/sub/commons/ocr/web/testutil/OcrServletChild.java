package de.unigoettingen.sub.commons.ocr.web.testutil;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.unigoettingen.sub.commons.ocr.web.OcrServlet;
import de.unigoettingen.sub.commons.ocr.web.OcrStarter;

public class OcrServletChild extends OcrServlet {

	private static final long serialVersionUID = 1L;
	private ThreadLocal<String> fakeValidationMessage = new ThreadLocal<String>();
	private ThreadLocal<OcrStarter> ocrStarterLocal = new ThreadLocal<OcrStarter>();

	@Override
	protected OcrStarter initOcrStarter(HttpServletRequest request) {
		OcrStarter ocrStarter = mock(OcrStarter.class);
		System.out.println(ocrStarter);
		fakeValidationMessage.set(request.getParameter("fakeValidationMessage"));
		System.out.println("message: " + fakeValidationMessage.get());
		when(ocrStarter.checkParameters()).thenReturn(fakeValidationMessage.get());
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// demonstrates a synch problem in OcrServlet
			e.printStackTrace();
		}
		ocrStarterLocal.set(ocrStarter);
		System.out.println(ocrStarter);
		return ocrStarter;
	}
	
	@Override
	protected void goToView(String viewName, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// without this, unit tests will not work with two or more requests
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if ("OK".equals(fakeValidationMessage.get())) {
			verify(ocrStarterLocal.get(), times(1)).run();
		}
		PrintWriter out = response.getWriter();
		out.print("Forwarded to view: " + viewName);
	}
	
}
