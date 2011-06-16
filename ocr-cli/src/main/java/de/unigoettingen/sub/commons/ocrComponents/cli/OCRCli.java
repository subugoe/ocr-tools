package de.unigoettingen.sub.commons.ocrComponents.cli;

/*

 © 2010, SUB Goettingen. All rights reserved.
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.

 */

import java.io.File;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.HierarchicalConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCREngineFactory;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextTyp;

import de.unigoettingen.sub.commons.ocr.util.OCRUtil;

/**
 * The Class OCRCli. command line is special for the input parametres which the
 * API abbyy-server-impl needs. these parametres are language, format ,
 * directories, OCRTextTyp and outputlocation
 * 
 * @author Mohamed Abergna
 * @version 1.0
 */
public class OCRCli {

	/** The Constant version. */
	public final static String version = "0.0.4";

	/** The logger. */
	protected static Logger logger = LoggerFactory
			.getLogger(de.unigoettingen.sub.commons.ocrComponents.cli.OCRCli.class);

	/** The opts. */
	protected static Options opts = new Options();

	/** The local output dir. */
	protected static String localOutputDir = null;

	/** The extension. */
	protected static String extension = "tif";

	/** The language. */
	protected static Set<Locale> langs;

	/** The config. */
	protected HierarchicalConfiguration config;
	// public String defaultConfig = "server-config.xml";
	// Settings for Ticket creation
	/** The recursive mode. */
	protected Boolean recursiveMode = true;

	/** The args. */
	String[] args;

	/** The write remote prefix. */
	protected static Boolean writeRemotePrefix = true;

	/** The _instance. */
	protected static OCRCli _instance;

	/** The foramt. as example TXT, XML or PDF.. */
	static List<OCRFormat> f = new ArrayList<OCRFormat>();

	/** The ocr text typ. */
	private static String ocrTextTyp = null;
	
	/** The ocr ocrPriority typ. */
	private static String ocrPriority = null;
	
	/** The engine. */
	protected static OCREngine engine;

	/** The ocr process. */
	protected static OCRProcess ocrProcess;

	/** The OUTPU t_ definitions. */
	protected static HashMap<OCRFormat, OCROutput> OUTPUT_DEFINITIONS;

	static List<File> dirs = new ArrayList<File>();

	/**
	 * Inits the opts.
	 */
	protected static void initOpts() {
		// Parameters
		opts.addOption("r", false, "Recursive - scan for subdirectories");
		opts.addOption("f", true, "Output format");
		opts.addOption("l", true, "Languages - seperated by \",\"");
		opts.addOption("h", false, "Help");
		opts.addOption("v", false, "Version");
		opts.addOption("d", true, "Debuglevel");
		opts.addOption("e", true, "File extension (default \"tif\")");
		opts.addOption("t", true, "OCRTextTyp");
		opts.addOption("o", true, "Output folder");
		opts.addOption("p", true, "Priority");
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws URISyntaxException
	 *             the uRI syntax exception
	 */
	public static void main(String[] args) throws IOException,
			URISyntaxException {
		logger.debug("Creating OCRCli instance");
		OCRCli ocr = OCRCli.getInstance();
		logger.debug("Creating OCREngineFactory instance");
		XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource(
				"context.xml"));
		OCREngineFactory ocrEngineFactory = (OCREngineFactory) factory
				.getBean("OCREngineFactory");

		engine = ocrEngineFactory.newOcrEngine();

		List<String> files = ocr.defaultOpts(args);
		if (files.size() > 1) {
			logger.error("there are more folders, should be only one folder as Input");
			System.exit(0);
		}
		if (files.size() == 1) {
			startRecognize(files);
		}

	}

	/**
	 * Start recognize. Starting recognize method
	 * 
	 * @param files
	 *            , the list files from directories where the different images
	 *            in it.
	 * @throws URISyntaxException
	 *             the uRI syntax exception
	 */
	public static void startRecognize(List<String> files)
			throws URISyntaxException {
		for (String book : files) {
			// logger.debug("Input Location " +
			// System.getProperty("user.dir")+book);
			File directory = new File(System.getProperty("user.dir")+book);
			getDirectories(directory);
			if (dirs.size() == 0) {
				logger.error("Directories is Empty : " + book);
				System.exit(0);
			}
			for (File id : dirs) {
				if (OCRUtil.makeFileList(id, extension).size() != 0) {

					logger.debug("Creating Process for " + id.toString());
					OCRProcess aop = engine.newOcrProcess();
					List<OCRImage> imgs = new ArrayList<OCRImage>();
					String jobName = id.getName();
					for (File imageFile : OCRUtil.makeFileList(id, extension)) {
						aop.setName(jobName);
						OCRImage aoi = engine.newOcrImage(imageFile.toURI());
						aoi.setSize(imageFile.length());
						if (jobName == null) {
							logger.error("Name for process not set, to avoid errors if your using parallel processes, we generate one.");
							aop.setName(UUID.randomUUID().toString());
						}
						imgs.add(aoi);
					}
					aop.setOcrImages(imgs);

					// add format
					OUTPUT_DEFINITIONS = new HashMap<OCRFormat, OCROutput>();

					for (OCRFormat ocrformat : f) {
						OCROutput aoo = engine.newOcrOutput();						
						URI uri = new URI(
						new File(System.getProperty("user.dir")).toURI()
								+ localOutputDir + "/" + id.getName()
								+ "." + ocrformat.toString().toLowerCase());
						// logger.debug("output Location " + uri.toString());
						aoo.setUri(uri);
						OUTPUT_DEFINITIONS.put(ocrformat, aoo);
						aop.addOutput(ocrformat, aoo);
					}
					// add language
					aop.setLanguages(langs);
					aop.setPriority(OCRPriority.valueOf(ocrPriority));
					aop.setTextTyp(OCRTextTyp.valueOf(ocrTextTyp));
					engine.addOcrProcess(aop);
				}
			}
		}
		logger.info("Starting recognize method");
		engine.recognize();
		logger.debug("recognize Finished");
	}

	static void getDirectories(File aFile) {
		if (aFile.isDirectory()) {
			dirs.add(aFile);
			File[] listOfFiles = aFile.listFiles();
			if (listOfFiles != null) {
				for (int i = 0; i < listOfFiles.length; i++) {
					getDirectories(listOfFiles[i]);
				}
			} else {
				logger.error(" [ACCESS DENIED]");
			}
		}
	}

	/**
	 * Configure from args.
	 * 
	 * @param args
	 *            the arguments
	 * @return the list
	 */
	public List<String> configureFromArgs(String[] args) {
		// list of the directory
		List<String> files = defaultOpts(args);
		return files;

	}

	/**
	 * Gets the single instance of OCRCli.
	 * 
	 * @return single instance of OCRCli
	 */
	public static OCRCli getInstance() {
		if (_instance == null) {
			_instance = new OCRCli();
		}
		return _instance;
	}

	/**
	 * Instantiates a new oCR cli.
	 */
	protected OCRCli() {
		initOpts();

	}

	/**
	 * Parses the ocr format.
	 * 
	 * @param str
	 *            the str
	 * @return the list
	 */
	protected static List<OCRFormat> parseOCRFormat(String str) {
		List<OCRFormat> ocrFormats = new ArrayList<OCRFormat>();
		if (str.contains(",")) {
			for (String ocrFormat : Arrays.asList(str.split(","))) {
				ocrFormats
						.add(OCRFormat.parseOCRFormat(ocrFormat.toUpperCase()));
			}
		} else {

			ocrFormats.add(OCRFormat.parseOCRFormat(str.toUpperCase()));
		}
		return ocrFormats;
	}

	/**
	 * Default opts.
	 * 
	 * @param args
	 *            the args
	 * @return the list
	 */
	protected List<String> defaultOpts(String[] args) {

		String cmdName = "OCRRunner [opts] files";
		CommandLine cmd = null;

		CommandLineParser parser = new GnuParser();

		try {
			cmd = parser.parse(opts, args);

		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(3);
		}

		if (cmd.getArgList().isEmpty()) {
			logger.trace("No Input Files!");
			System.out.println("No Input Files!");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(cmdName, opts);
			System.exit(1);
		}

		// Hilfe
		if (cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(cmdName, opts);
			System.exit(0);
		}

		// Extension
		if (cmd.hasOption("e")) {
			if (cmd.getOptionValue("e") != null) {
				extension = cmd.getOptionValue("e");
			}
		}

		// Version
		if (cmd.hasOption("v")) {
			System.out.println("Version " + version);
			System.exit(0);
		}

		if (cmd.hasOption("f")) {
			f = parseOCRFormat(cmd.getOptionValue("f"));

		}
		// Debug
		if (cmd.hasOption("d")) {
			// logger.setLevel(Level.toLevel(cmd.getOptionValue("d")));
			logger.debug(cmd.getOptionValue("d"));
			logger.trace("Debuglevel: " + cmd.getOptionValue("d"));
		}

		// Sprache
		if (cmd.hasOption("l")) {
			langs = OCRUtil.parseLang(cmd.getOptionValue("l"));
		} else {
			langs = new HashSet<Locale>();
			langs.add(new Locale("de"));
		}
		for (Locale lang : langs) {
			logger.trace("Language: " + lang.getLanguage());
		}

		logger.trace("Parsing Options");

		if (cmd.hasOption("r")) {
			recursiveMode = true;
		}

		// OCRTextTyp
		if (cmd.hasOption("t")) {
			if (cmd.getOptionValue("t") != null
					&& !cmd.getOptionValue("t").equals("")) {
				ocrTextTyp = cmd.getOptionValue("t");
				try {
					OCRProcess.OCRTextTyp.valueOf(ocrTextTyp).ordinal();
					logger.trace("ocrTextTyp: " + ocrTextTyp);
				} catch (IllegalArgumentException e) {
					logger.error("the process ended, This ocrTextTyp < "
							+ ocrTextTyp + " > is not supported");
					System.exit(0);

				}
			}
		}
		
		//Priority
		if (cmd.hasOption("p")) {
			if (cmd.getOptionValue("p") != null
					&& !cmd.getOptionValue("p").equals("")) {
				ocrPriority = cmd.getOptionValue("p");
				try {
					OCRProcess.OCRPriority.valueOf(ocrPriority).ordinal();
					logger.trace("ocrPriority: " + ocrPriority);
				} catch (IllegalArgumentException e) {
					logger.error("the process ended, This ocrPriority < "
							+ ocrPriority + " > is not supported");
					System.exit(0);

				}
			}else ocrPriority = "NORMAL";
		}
		// Output foler
		if (cmd.hasOption("o")) {
			if (cmd.getOptionValue("o") != null
					&& !cmd.getOptionValue("o").equals("")) {
				localOutputDir = cmd.getOptionValue("o");
			} else {
				logger.error("the process ended, This localOutputDir < "
						+ localOutputDir + " > is null");
				System.exit(0);
			}
		}
		// List of the directory wich are images
		return cmd.getArgList();
	}

}