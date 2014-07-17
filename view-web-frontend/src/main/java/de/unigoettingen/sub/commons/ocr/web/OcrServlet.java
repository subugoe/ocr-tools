package de.unigoettingen.sub.commons.ocr.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.unigoettingen.sub.ocr.controller.OcrParameters;

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
		param.inputTextType = request.getParameter("textType");
		param.inputLanguages = request.getParameterValues("languages");
		param.outputFormats = request.getParameterValues("outputFormats");
		setProperty(request, param, "email");
		setProperty(request, param, "user");
		setProperty(request, param, "password");
		setEngineParameters(request, param);
		param.props.setProperty("splitJobs", "500");
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

	private void setProperty(HttpServletRequest request, OcrParameters param, String key) {
		String requestValue = request.getParameter(key);
		if (requestValue != null && !requestValue.isEmpty()) {
			param.props.setProperty(key, requestValue);
		}
	}
	
	private void setEngineParameters(HttpServletRequest request, OcrParameters param) {
		String ocrService = request.getParameter("ocrEngine");
		if ("gbvAntiqua".equals(ocrService)) {
			param.ocrEngine = "abbyy-multiuser";
			param.props.setProperty("abbyy.config", "gbv-antiqua.properties");
			// TODO: kleine Buchstaben
			param.inputTextType = "NORMAL";
		} else if ("gbvFraktur".equals(ocrService)) {
			param.ocrEngine = "abbyy-multiuser";
			param.props.setProperty("abbyy.config", "gbv-fraktur.properties");
		} else if ("abbyyCloud".equals(ocrService)) {
			param.ocrEngine = "ocrsdk";
		}
	}

	protected void goToView(String viewName, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher view = request.getRequestDispatcher(viewName);
		view.forward(request, response);
	}
	
}
