package de.unigoettingen.sub.commons.ocr.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ParameterLister extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Default constructor. 
     */
    public ParameterLister() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.println(request.getParameter("inputFolder"));
		out.println(request.getParameter("outputFolder"));
		out.println(request.getParameter("imageFormat"));
		out.println(request.getParameter("textType"));
		out.println(Arrays.toString(request.getParameterValues("languages")));
		out.println(Arrays.toString(request.getParameterValues("outputFormats")));
		out.println(request.getParameter("email"));
		
	}

}
