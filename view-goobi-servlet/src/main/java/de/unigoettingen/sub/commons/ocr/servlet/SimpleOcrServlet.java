package de.unigoettingen.sub.commons.ocr.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocr.util.OcrParameters;
import de.unigoettingen.sub.ocr.controller.OcrEngineStarter;

public class SimpleOcrServlet extends HttpServlet {
	
	private static final long serialVersionUID = -6874162548956424669L;
	
	private static final String TITLE = "GDZ Simple-OCR 0.0.3 - Java";	
	
	private String defaultLanguage;
	private String tempRootDir;
	private String sourceRootDir;
	private String fileExtension;
	
	// for unit tests
	protected OcrEngineStarter getEngineStarter() {
		return new OcrEngineStarter();
	}
	protected FileAccess getFileAccess() {
		return new FileAccess();
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
        String lang = request.getParameter("lang");
		if (lang == null) {
			lang = defaultLanguage;
        }
		String restOfSourceDir = request.getParameter("path");
		if (restOfSourceDir == null) {
			throw new ServletException("Fehlende Eingabedaten, z. B. ?path=myimages/ocr");
		}
		restOfSourceDir = normalizePath(restOfSourceDir);
		String sourceImagesDir = sourceRootDir + restOfSourceDir;
		String tempImagesDir = tempRootDir + restOfSourceDir;
		String tempResultsDir = tempRootDir + restOfSourceDir;
		
		String imagesRange = request.getParameter("imgrange");
		if (imagesRange == null) {
			throw new ServletException("Fehlende Eingabedaten, z. B. ?imgrange=1-10");
		}
		List<String> imageNames = createFileList(imagesRange);
		FileAccess fileAccess = getFileAccess();
		for (String imageName : imageNames) {
			fileAccess.copyFile(new File(sourceImagesDir + imageName), new File(tempImagesDir + imageName));
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

		File ocrTextResult = new File(tempResultsDir + getJobName(sourceImagesDir) + fileExtension);
		fillResponse(response, ocrTextResult);
		
		fileAccess.deleteDir(new File(tempImagesDir));
	}

	private String normalizePath(String path) {
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

    private String getJobName(String sourceImagesDir) {
        File folder = new File(sourceImagesDir);
        return folder.getName();
    }

	private void fillResponse(HttpServletResponse response, File ocrTextResult) throws IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>" + TITLE + "</title></head><body>");
		out.println("<h1>Ergebnis:</h1><hr>");
		out.println("<pre>");
		
		FileAccess fileAccess = getFileAccess();
		String fileContents = fileAccess.readFileToString(ocrTextResult);
		out.println(fileContents);
		out.println("</pre><hr/></body></html>");
	}


}
