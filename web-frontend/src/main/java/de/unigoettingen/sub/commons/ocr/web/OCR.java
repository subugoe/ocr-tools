package de.unigoettingen.sub.commons.ocr.web;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class OCR
 */
public class OCR extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public OCR() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String inputFolder = request.getParameter("inputFolder");
		System.out.println("-" + inputFolder + "-");
		System.out.println(request.getParameter("outputFolder"));
		System.out.println(request.getParameter("imageFormat"));
		System.out.println(request.getParameter("textType"));
		System.out.println(Arrays.toString(request.getParameterValues("languages")));
		System.out.println(Arrays.toString(request.getParameterValues("outputFormats")));
		System.out.println(request.getParameter("email"));
		
		if (someParametersMissing(request)) {
			System.out.println("missing");
		}
		
		RequestDispatcher view = request.getRequestDispatcher("ocr-started.jsp");
		view.forward(request, response);
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
