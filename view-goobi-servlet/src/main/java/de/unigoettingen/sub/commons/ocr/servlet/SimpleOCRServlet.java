package de.unigoettingen.sub.commons.ocr.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextType;
import de.unigoettingen.sub.commons.util.file.FileUtils;
import java.io.BufferedReader;

/**
 * The Class SimpleOCRServlet.
 */
public class SimpleOCRServlet extends HttpServlet {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6874162548956424669L;
	
	/** The Constant title. */
	static final String TITLE = "GDZ Simple-OCR 0.0.3 - Java";
		
	/** The Constant LANG_PARAMETER. */
	static final String LANG_PARAMETER = "defaultLang";
	
	/** The Constant CACHEDIR_PARAMETER. */
	static final String CACHEDIR_PARAMETER = "cacheDir";
	
	/** The Constant DIRPREFIX_PARAMETER. */
	static final String DIRPREFIX_PARAMETER = "dirPrefix";
	
	/** The Constant OCRSCRIPT_PARAMETER. */
	static final String OCRSCRIPT_PARAMETER = "ocrScript";
	
	/** The Constant SUFFIX_PARAMETER. */
	static final String SUFFIX_PARAMETER = "suffix";
	
	/** The Constant logger. */
	static final Logger logger = LoggerFactory.getLogger(SimpleOCRServlet.class);

	/** The Constant DOWNLOAD_PATH. */
	static final String DOWNLOAD_PATH = "url-download";

	//String lang = "German";
	/** The default language */
	private String defaultLang = null;
	
	//String cacheDir = new String("c:\\tmp\\");
	/** The cache dir. */
	private String cacheDir = null;
	//String dirPrefix = new String("\\\\gdz-wrk1\\goobi-ocr$\\");
	/** The dir prefix. */
	private String dirPrefix = null;
	//String ocrScript = "c:\\Programme\\Abbyy FineReader Engine 8.1\\Samples\\Visual C++ (Raw)\\CLEI2\\Release\\clei.exe";
	/** The ocr script. */
	private String ocrScript = null;
	//String suffix = ".txt";
	/** The suffix. */
	private String suffix = null;

	//String jobName;
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		//TODO: URL Einbauen
		/*
                URL url = null;
		if (request.getParameter("url") != null) {
			try {
				url = new URL(request.getParameter("url"));
			} catch (MalformedURLException e) {
				logger.error("Malformed URL: " + request.getParameter("url"));
			}
			//String urlPath = download(url);
		}
                */
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		pw.println("<html><head>");
		pw.println("<title>" + TITLE + "</title></head><body>");
		pw.println("<h1>Ergebnis:</h1><hr>");
                String lang, path;
		if (request.getParameter("lang") != null) {
			lang = request.getParameter("lang");
		} else {
                    lang = defaultLang;
                }
		if (request.getParameter("path") != null) {
			path = normalizePath(request.getParameter("path"));
		} else {
			throw new ServletException("<h1>fehlende Eingabedaten</h1>");
		}
		if (request.getParameter("imgrange") == null) {
			throw new ServletException("<h1>fehlende Eingabedaten</h1>");
		}
		List<String> files = createFileList(request.getParameter("imgrange"));
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

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init () throws ServletException {
		defaultLang = getServletConfig().getInitParameter(LANG_PARAMETER).toString();
		if (defaultLang == null) {
			throw new ServletException("Keine Defaultsprache angegeben");
		}

		cacheDir = getServletConfig().getInitParameter(CACHEDIR_PARAMETER).toString();
		if (cacheDir == null) {
			throw new ServletException("Keine Zwischenspeicherort angegeben");
		}

		dirPrefix = getServletConfig().getInitParameter(DIRPREFIX_PARAMETER).toString();
		if (dirPrefix == null) {
			throw new ServletException("Kein Startpunkt für das auflösen der Pfade gefunden.");
		}

		suffix = getServletConfig().getInitParameter(SUFFIX_PARAMETER).toString();
		if (suffix == null) {
			throw new ServletException("Kein Suffix für die Ergebnisse angegeben");
		}
		
	}

	/**
	 * Creates the response.
	 *
	 * @param filelist the filelist
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String createResponse (String path, String jobName) throws IOException {
		String workDir = cacheDir + path;

		StringBuilder response = new StringBuilder();
		String line;
			response.append("<pre>");
			String txtfile = workDir + jobName + suffix;
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(txtfile), "UTF-8"));
			if (!in.ready()) {
				throw new IOException();
			}
			while ((line = in.readLine()) != null) {
				response.append(line.replaceAll("(\\s+)", " ")).append("<br/>\n");
			}
			in.close();
			response.append("</pre><hr/>");

		return response.toString();
	}

	/**
	 * Creates the file list.
	 *
	 * @param range the range
	 * @return the list
	 * @throws ServletException the servlet exception
	 */
	private List<String> createFileList (String range) throws ServletException {
		ArrayList<String> files = new ArrayList<String>();
		if (range.contains("-")) {
			String[] r = range.split("-");
			Integer from;
			Integer to;
			from = Integer.decode(r[0]);
			to = Integer.decode(r[1]);
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

	/**
	 * Gen filename.
	 *
	 * @param n the n
	 * @return the string
	 */
	private String genFilename (Integer n) {
		return genFilename(n.toString());
	}

	/**
	 * Gen filename.
	 *
	 * @param n the n
	 * @return the string
	 */
	private String genFilename (String n) {
		String pattern = "00000000.tif";
		String regex = "\\d{" + n.length() + "}\\.";
		return pattern.replaceAll(regex, n + ".");
	}

	/**
	 * Normalize path.
	 *
	 * @param path the path
	 * @return the string
	 * @throws ServletException the servlet exception
	 */
	private String normalizePath (String path) throws ServletException {
		String r = null;
		r = path.replaceAll("^\\/(.*)[\\/]?", "$1") + "/";
		//r = r.replaceAll("\\/", File.separator);
		r = r.replaceAll("[/\\\\]+", "\\" + File.separator);
		return r;
	}

	/**
	 * Copy files.
	 *
	 * @param files the files
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ServletException the servlet exception
	 */
	private boolean copyFiles (List<String> files, String path) throws IOException, ServletException {
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

        private String getJobName (String path) {
            File folder = new File(cacheDir + path);
            return folder.getName();
        }
        
	private void ocr(List<File> images, final String lang, String path) throws URISyntaxException {
                //String jobName
		String workDir = cacheDir + path;
		File folder = new File(cacheDir + path);
		XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource(
				"context.xml"));

		OCREngine engine = (OCREngine) factory.getBean("ocrEngine");

			logger.debug("Creating Process for " + folder.toString());
			OCRProcess aop = engine.newOcrProcess();
			String jobName = getJobName(path);
			aop.setName(jobName);
			if (jobName.equals("")) {
				logger.error("Name for process not set, to avoid errors if your using parallel processes, we generate one.");
				aop.setName(UUID.randomUUID().toString());
			}
			List<OCRImage> imgs = new ArrayList<OCRImage>();
			for (File imageFile : images) {
				OCRImage aoi = engine.newOcrImage(imageFile.toURI());
				aoi.setSize(imageFile.length());
				imgs.add(aoi);
			}
			aop.setOcrImages(imgs);

				OCROutput aoo = engine.newOcrOutput();						
				URI uri = new URI(new File(workDir).toURI()
						+ jobName
						+ suffix);
				logger.debug("output Location " + uri.toString());
				aoo.setUri(uri);
				aoo.setlocalOutput(new File(workDir).getAbsolutePath());
				aop.addOutput(OCRFormat.TXT, aoo);
			// add language
			aop.setLanguages(new HashSet<Locale>(){{add(new Locale(lang));}});
			aop.setTextType(OCRTextType.valueOf("NORMAL"));
			aop.setPriority(OCRPriority.ABOVENORMAL);
			engine.addOcrProcess(aop);

			logger.info("Starting recognize method");
			engine.recognize();
			logger.debug("recognize Finished");

	}
}
