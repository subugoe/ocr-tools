package de.unigoettingen.sub.commons.ocr.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRImage;
import de.uni_goettingen.sub.commons.ocr.api.AbstractOCROutput;
import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCREngineFactory;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;
import de.unigoettingen.sub.commons.util.file.FileUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleOCRServlet.
 */
public class SimpleOCRServlet extends HttpServlet {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6874162548956424669L;
	
	/** The Constant title. */
	final static String title = "GDZ Simple-OCR 0.0.3 - Java";
	
	/** The path. */
	protected String path;
	
	/** The Constant LANG_PARAMETER. */
	final static String LANG_PARAMETER = "defaultLang";
	
	/** The Constant CACHEDIR_PARAMETER. */
	final static String CACHEDIR_PARAMETER = "cacheDir";
	
	/** The Constant DIRPREFIX_PARAMETER. */
	final static String DIRPREFIX_PARAMETER = "dirPrefix";
	
	/** The Constant OCRSCRIPT_PARAMETER. */
	final static String OCRSCRIPT_PARAMETER = "ocrScript";
	
	/** The Constant SUFFIX_PARAMETER. */
	final static String SUFFIX_PARAMETER = "suffix";
	
	/** The Constant logger. */
	final static Logger logger = LoggerFactory.getLogger(SimpleOCRServlet.class);

	/** The Constant DOWNLOAD_PATH. */
	final static String DOWNLOAD_PATH = "url-download";

	//String lang = "German";
	/** The lang. */
	String lang = null;
	
	/** The files. */
	List<String> files = null;
	//String cacheDir = new String("c:\\tmp\\");
	/** The cache dir. */
	String cacheDir = null;
	//String dirPrefix = new String("\\\\gdz-wrk1\\goobi-ocr$\\");
	/** The dir prefix. */
	String dirPrefix = null;
	//String ocrScript = "c:\\Programme\\Abbyy FineReader Engine 8.1\\Samples\\Visual C++ (Raw)\\CLEI2\\Release\\clei.exe";
	/** The ocr script. */
	String ocrScript = null;
	//String suffix = ".txt";
	/** The suffix. */
	String suffix = null;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		//TODO: URL Einbauen
		URL url = null;
		if (request.getParameter("url") != null) {
			try {
				url = new URL(request.getParameter("url"));
			} catch (MalformedURLException e) {
				logger.error("Malformed URL: " + request.getParameter("url"));
			}
			//String urlPath = download(url);
		}

		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		pw.println("<html><head>");
		pw.println("<title>" + title + "</title></head><body>");
		pw.println("<h1>Ergebnis:</h1><hr>");

		if (request.getParameter("lang") != null) {
			lang = request.getParameter("lang");
		}
		if (request.getParameter("path") != null) {
			path = normalizePath(request.getParameter("path"));
		} else {
			throw new ServletException("<h1>fehlende Eingabedaten</h1>");
		}
		if (request.getParameter("imgrange") == null) {
			throw new ServletException("<h1>fehlende Eingabedaten</h1>");
		}
		files = createFileList(request.getParameter("imgrange"));
		copyFiles(files);

		for (String file : files) {
			try {
				logger.debug("Ocring " + file + " to " + file + ".txt");

				ocr(file, file + ".txt");
			} catch (Exception e) {
				logger.error("Error while performing OCR: ", e);
				throw new ServletException(e);
			}
		}

		pw.println(createResponse(files));

	}

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init () throws ServletException {
		lang = getServletConfig().getInitParameter(LANG_PARAMETER).toString();
		if (lang == null) {
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

		ocrScript = getServletConfig().getInitParameter(OCRSCRIPT_PARAMETER).toString();
		System.setProperty("FineReaderEngine.cmd", ocrScript);
		if (ocrScript == null) {
			throw new ServletException("Kein OCR Programm angegeben.");
		}

		suffix = getServletConfig().getInitParameter(SUFFIX_PARAMETER).toString();
		if (suffix == null) {
			throw new ServletException("Kein Suffix für die Ergebnisse angegeben");
		}

		//getServletConfig().getInitParameter()
	}

	/**
	 * Creates the response.
	 *
	 * @param filelist the filelist
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String createResponse (List<String> filelist) throws IOException {
		String workDir = cacheDir + path;

		StringBuilder response = new StringBuilder();
		String line;
		for (String file : files) {
			response.append("<pre>");
			String txtfile = workDir + file + ".txt";
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(txtfile), "UTF-8"));
			if (!in.ready()) {
				throw new IOException();
			}
			while ((line = in.readLine()) != null) {
				response.append(line.replaceAll("(\\s+)", " ")).append("<br/>\n");
			}
			in.close();
			response.append("</pre><hr/>");
		}

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
	private boolean copyFiles (List<String> files) throws IOException, ServletException {
		String toDir = cacheDir + path + File.separator;
		String fromDir = dirPrefix + path + File.separator;

		File tmp = new File(toDir);
		if (!tmp.exists() && !tmp.mkdirs()) {
			throw new ServletException("Couldn't create temp directory!");
		}
		for (String file : files) {
			FileUtils.copyDirectory(new File(fromDir + file), new File(toDir + file));
		}
		return true;
	}

	/**
	 * Ocr.
	 *
	 * @param in the in
	 * @param out the out
	 * @throws OCRException the oCR exception
	 * @throws URISyntaxException the uRI syntax exception
	 */
	@SuppressWarnings("serial")
	private void ocr (String in, String out) throws OCRException, URISyntaxException {
		String workDir = cacheDir + path;

		URI infile = new URI(new File(workDir).toURI().toString() + in);
		URI outfile = new URI(new File(workDir).toURI().toString() + out);

		OCREngineFactory oef = OCREngineFactory.getInstance();
		OCREngine oe = oef.newOcrEngine();
		if (oe.init() == true) {

		logger.trace("OCR engine loaded successfuly");
		}else {
			throw new OCRException("Couldn't initialize engine");
		}

		//Create output directory
		new File(out).getParentFile().mkdirs();

		//Setup the engine
		//Create a image;
		OCRImage oi = oe.newOcrImage();
		oi.setUri(infile);
		//create the output
		final OCROutput oo = oe.newOcrOutput();
		oo.setUri(outfile);
		Map<OCRFormat, OCROutput> conf = new HashMap<OCRFormat, OCROutput>(){{
			put(OCRFormat.TXT, oo);
		}};
		//create the process;
		OCRProcess op = oe.newOcrProcess();
		
		op.setName("Goobi OCR Servlet");
		((AbstractOCRProcess) op).addLanguage(new Locale(lang));
		op.setOcrOutputs(conf);
		
		oe.recognize(op);

	}
}
