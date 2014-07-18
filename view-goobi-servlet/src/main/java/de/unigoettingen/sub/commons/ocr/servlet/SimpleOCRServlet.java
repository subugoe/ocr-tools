package de.unigoettingen.sub.commons.ocr.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextType;
import de.unigoettingen.sub.commons.util.file.FileUtils;
import de.unigoettingen.sub.ocr.controller.FactoryProvider;
import de.unigoettingen.sub.ocr.controller.OcrParameters;

import java.io.BufferedReader;

public class SimpleOCRServlet extends HttpServlet {
	
	private static final long serialVersionUID = -6874162548956424669L;
	
	private static final String TITLE = "GDZ Simple-OCR 0.0.3 - Java";	
	
	private static final Logger logger = LoggerFactory.getLogger(SimpleOCRServlet.class);

	private String defaultLanguage;
	private String cacheDir;
	private String dirPrefix;
	private String fileExtension;

	@Override
	public void init() throws ServletException {
		defaultLanguage = getInitParam("defaultLang");
		cacheDir = getInitParam("cacheDir");
		dirPrefix = getInitParam("dirPrefix");
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
		String path = request.getParameter("path");
		if (path == null) {
			throw new ServletException("Fehlende Eingabedaten, z. B. ?path=myimages/ocr");
		}
		path = normalizePath(path);
		String imagesRange = request.getParameter("imgrange");
		if (imagesRange == null) {
			throw new ServletException("Fehlende Eingabedaten, z. B. ?imgrange=1-10");
		}
		List<String> files = createFileList(imagesRange);
		copyFiles(files, path);

		String workDir = cacheDir + path;
		List<File> images = new ArrayList<File>();
		for (String file : files) {
			images.add(new File(workDir + file));
		}
		
		try {
			ocr(images, lang, path);
		} catch (URISyntaxException e) {
			logger.warn("Malformed URL;", e);
		}
		
		pw.println(createResponse(path, getJobName(path)));

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

	private boolean copyFiles(List<String> files, String path) throws IOException, ServletException {
		String toDir = cacheDir + path + File.separator;
		String fromDir = dirPrefix + path + File.separator;

		if(!new File(fromDir).exists()) {
			throw new ServletException("Source path does not exist: " + fromDir);
		}
		
		File tmp = new File(toDir);
		if (!tmp.exists() && !tmp.mkdirs()) {
			throw new ServletException("Couldn't create temp directory!");
		}
		for (String file : files) {
			FileUtils.copyDirectory(new File(fromDir + file), new File(toDir + file));
		}
		return true;
	}

	private void ocr(List<File> images, final String lang, String path) throws URISyntaxException {
		FactoryProvider factoryProvider = new FactoryProvider();
		OcrFactory factory = factoryProvider.createFactory("abbyy-multiuser", new Properties());
		
		String workDir = cacheDir + path;
		File folder = new File(cacheDir + path);

		OCREngine engine = factory.createEngine();

		logger.debug("Creating Process for " + folder.toString());
		OCRProcess aop = factory.createProcess();
		String jobName = getJobName(path);
		aop.setName(jobName);
		if (jobName.equals("")) {
			logger.error("Name for process not set, to avoid errors if your using parallel processes, we generate one.");
			aop.setName(UUID.randomUUID().toString());
		}
		List<OCRImage> imgs = new ArrayList<OCRImage>();
		for (File imageFile : images) {
			OCRImage aoi = factory.createImage();
			aoi.setUri(imageFile.toURI());
			aoi.setSize(imageFile.length());
			imgs.add(aoi);
		}
		aop.setOcrImages(imgs);

		OCROutput aoo = factory.createOutput();						
		URI uri = new URI(new File(workDir).toURI()
				+ jobName
				+ fileExtension);
		logger.debug("output Location " + uri.toString());
		aoo.setUri(uri);
		aoo.setlocalOutput(new File(workDir).getAbsolutePath());
		aop.addOutput(OCRFormat.TXT, aoo);
		
		aop.addLanguage(new Locale(lang));
		aop.setTextType(OCRTextType.valueOf("NORMAL"));
		aop.setPriority(OCRPriority.ABOVENORMAL);
		engine.addOcrProcess(aop);

		
		
		logger.info("Starting recognize method");
		engine.recognize();
		logger.debug("recognize Finished");

	}
	
	
	
    private String getJobName(String path) {
        File folder = new File(cacheDir + path);
        return folder.getName();
    }
        

	private String createResponse(String path, String jobName) throws IOException {
		String workDir = cacheDir + path;

		StringBuilder response = new StringBuilder();
		String line;
		response.append("<pre>");
		String txtfile = workDir + jobName + fileExtension;
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(txtfile), "UTF-8"));
		while ((line = in.readLine()) != null) {
			response.append(line.replaceAll("(\\s+)", " ")).append("<br/>\n");
		}
		in.close();
		response.append("</pre><hr/>");

		return response.toString();
	}


}
