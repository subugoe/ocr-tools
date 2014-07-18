package de.unigoettingen.sub.commons.ocr.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import de.unigoettingen.sub.ocr.controller.OcrEngineStarter;
import de.unigoettingen.sub.ocr.controller.OcrParameters;

public class SimpleOCRServlet extends HttpServlet {
	
	private static final long serialVersionUID = -6874162548956424669L;
	
	private static final String TITLE = "GDZ Simple-OCR 0.0.3 - Java";	
	
	private String defaultLanguage;
	private String tempRootDir;
	private String sourceRootDir;
	private String fileExtension;
	
	// TODO: inline these
	private String sourceImagesDir;
	private String tempImagesDir;
	private String tempResultsDir;

	// for unit tests
	protected OcrEngineStarter getEngineStarter() {
		return new OcrEngineStarter();
	}

	@Override
	public void init() throws ServletException {
		defaultLanguage = getInitParam("defaultLanguage");
		tempRootDir = getInitParam("tempRootDir");
		sourceRootDir = getInitParam("sourceRootDir");
		fileExtension = getInitParam("fileExtension");		
	}

	private String getInitParam(String paramName) throws ServletException {
		String initParam = getServletConfig().getInitParameter(paramName).toString();
		if (initParam == null) {
			throw new ServletException("Kein Init-Parameter in der web.xml: " + paramName);
		}
		return initParam;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		pw.println("<html><head>");
		pw.println("<title>" + TITLE + "</title></head><body>");
		pw.println("<h1>Ergebnis:</h1><hr>");
        String lang = request.getParameter("lang");
		if (lang == null) {
			lang = defaultLanguage;
        }
		String restOfSourceDir = request.getParameter("path");
		if (restOfSourceDir == null) {
			throw new ServletException("Fehlende Eingabedaten, z. B. ?path=myimages/ocr");
		}
		restOfSourceDir = normalizePath(restOfSourceDir);
		sourceImagesDir = sourceRootDir + restOfSourceDir;
		tempImagesDir = tempRootDir + restOfSourceDir;
		tempResultsDir = tempRootDir + restOfSourceDir;
		
		String imagesRange = request.getParameter("imgrange");
		if (imagesRange == null) {
			throw new ServletException("Fehlende Eingabedaten, z. B. ?imgrange=1-10");
		}
		List<String> imageNames = createFileList(imagesRange);
		copyFiles(imageNames);

		List<File> images = new ArrayList<File>();
		for (String file : imageNames) {
			images.add(new File(tempImagesDir + file));
		}
		
		OcrParameters params = new OcrParameters();
		params.inputFolder = tempImagesDir;
		params.outputFolder = tempResultsDir;
		params.inputLanguages = new String[]{lang};
		params.inputTextType = "NORMAL";
		params.ocrEngine = "abbyy-multiuser";
		params.outputFormats = new String[]{"TXT"};
		params.priority = "1";
		
		OcrEngineStarter engineStarter = getEngineStarter();
		engineStarter.startOcrWithParams(params);
		
		pw.println(createResponse(getJobName()));

	}

	private String normalizePath(String path) throws ServletException {
		String r = null;
		r = path.replaceAll("^\\/(.*)[\\/]?", "$1") + "/";
		r = r.replaceAll("[/\\\\]+", "\\" + File.separator);
		return r;
	}

	private List<String> createFileList(String range) throws ServletException {
		ArrayList<String> files = new ArrayList<String>();
		if (range.contains("-")) {
			String[] r = range.split("-");
			Integer from = Integer.decode(r[0]);
			Integer to = Integer.decode(r[1]);
			if (from > to) {
				throw new ServletException("Startwert größer als Endwert");
			}
			for (int i = from; i <= to; i++) {
				files.add(genFilename(i));
			}
		} else {
			files.add(genFilename(range));
		}
		return files;
	}

	private String genFilename(Integer n) {
		return genFilename(n.toString());
	}

	private String genFilename(String n) {
		String pattern = "00000000.tif";
		String regex = "\\d{" + n.length() + "}\\.";
		return pattern.replaceAll(regex, n + ".");
	}

	private boolean copyFiles(List<String> files) throws IOException, ServletException {

		if(!new File(sourceImagesDir).exists()) {
			throw new ServletException("Source path does not exist: " + sourceImagesDir);
		}
		
		for (String file : files) {
			FileUtils.copyFile(new File(sourceImagesDir + file), new File(tempImagesDir + file));
		}
		return true;
	}
	
    private String getJobName() {
        File folder = new File(sourceImagesDir);
        return folder.getName();
    }

	private String createResponse(String jobName) throws IOException {
		StringBuilder response = new StringBuilder();
		String line;
		response.append("<pre>");
		String txtfile = tempResultsDir + jobName + fileExtension;
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(txtfile), "UTF-8"));
		while ((line = in.readLine()) != null) {
			response.append(line.replaceAll("(\\s+)", " ")).append("<br/>\n");
		}
		in.close();
		response.append("</pre><hr/>");

		return response.toString();
	}


}
