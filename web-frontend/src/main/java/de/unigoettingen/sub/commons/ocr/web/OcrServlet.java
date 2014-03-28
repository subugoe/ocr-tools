package de.unigoettingen.sub.commons.ocr.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OcrServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// For unit testing
	protected OcrStarter initOcrStarter(HttpServletRequest request) {
		return new OcrStarter();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		OcrStarter ocrStarter = initOcrStarter(request);
		OcrParameters param = new OcrParameters();
		param.inputFolder = request.getParameter("inputFolder");
		param.outputFolder = request.getParameter("outputFolder");
		param.imageFormat = request.getParameter("imageFormat");
		param.textType = request.getParameter("textType");
		param.languages = request.getParameterValues("languages");
		param.outputFormats = request.getParameterValues("outputFormats");
		param.email = request.getParameter("email");
		param.ocrEngine = request.getParameter("ocrEngine");
		param.user = request.getParameter("user");
		param.password = request.getParameter("password");
		param.logFile = request.getParameter("logFile");
		param.logLevel = request.getParameter("logLevel");
		ocrStarter.setParameters(param);
		
		String validationMessage = ocrStarter.checkParameters();
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
