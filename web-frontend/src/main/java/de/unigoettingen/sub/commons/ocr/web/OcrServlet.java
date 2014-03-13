package de.unigoettingen.sub.commons.ocr.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OcrServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// TODO: the servlet should not have a state, because there will be several threads
	// use ThreadLocal? use PowerMock?
	protected OcrStarter ocrStarter;
	
	// For unit testing
	protected void initOcrStarter(HttpServletRequest request) {
		ocrStarter = new OcrStarter();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		initOcrStarter(request);
		OcrParameters param = new OcrParameters();
		param.inputFolder = request.getParameter("inputFolder");
		param.outputFolder = request.getParameter("outputFolder");
		param.imageFormat = request.getParameter("imageFormat");
		param.textType = request.getParameter("textType");
		param.languages = request.getParameterValues("languages");
		param.outputFormats = request.getParameterValues("outputFormats");
		param.email = request.getParameter("email");
		ocrStarter.setParameters(param);
		
		String validationMessage = ocrStarter.checkParameters();
		System.out.println(validationMessage);
		if (validationMessage.equals("OK")) {
			new Thread(ocrStarter).start();
			goToView("ocr-started.jsp", request, response);
		} else {
			request.setAttribute("validationMessage", validationMessage);
			goToView("invalid-parameters.jsp", request, response);
		}
		
		
	}

	protected void goToView(String viewName, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher view = request.getRequestDispatcher(viewName);
		view.forward(request, response);
	}
	
}
