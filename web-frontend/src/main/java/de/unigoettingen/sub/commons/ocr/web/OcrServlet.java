package de.unigoettingen.sub.commons.ocr.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class OCR
 */
public class OcrServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private OcrStarter ocrStarter;
	
	// For unit tests
	void setOcrStarter(OcrStarter newStarter) {
		ocrStarter = newStarter;
	}
	
    /**
     * Default constructor. 
     */
    public OcrServlet() {
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		ocrStarter = new OcrStarter();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
		if (validationMessage.equals("OK")) {
			new Thread(ocrStarter).start(); 
			RequestDispatcher view = request.getRequestDispatcher("ocr-started");
			view.forward(request, response);
		} else {
			request.setAttribute("validationMessage", validationMessage);
			RequestDispatcher view = request.getRequestDispatcher("invalid-parameters");
			view.forward(request, response);
		}
		
		
	}

	private boolean someParametersMissing(HttpServletRequest request) {
		boolean parameterMissing = request.getParameter("inputFolder") == null || "".equals(request.getParameter("inputFolder"));
		parameterMissing |= request.getParameter("outputFolder") == null;
		parameterMissing |= request.getParameter("imageFormat") == null;
		parameterMissing |= request.getParameter("textType") == null;
//		parameterMissing |= request.getParameterValues("languages").length == 0;
//		parameterMissing |= request.getParameterValues("outputFormats").length == 0;
		parameterMissing |= request.getParameter("email") == null;
		return parameterMissing;
	}

}
