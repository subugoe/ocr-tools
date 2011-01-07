/**
 * @author Sven Thomas
 * @author Christian Mahnke
 * @version 1.0
 */
package de.unigoettingen.sub.commons.ocrComponents.cli;

import java.io.File;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.swing.plaf.metal.OceanTheme;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.helpers.Loader;
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

import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;
import de.unigoettingen.sub.commons.ocr.util.OCRUtil;
import de.unigoettingen.sub.commons.util.file.FileExtensionsFilter;



/**
 * The Class OCRCli. command line is special for the input parametres which the
 * API abbyy-server-impl needs. these parametres are language, format ,
 * directories and outputlocation
 */
public class OCRCli {

	/** The Constant version. */
	public final static String version = "0.0.4";

	/** The logger. */
	protected static Logger logger = LoggerFactory.getLogger(de.unigoettingen.sub.commons.ocrComponents.cli.OCRCli.class);

	/** The opts. */
	protected static Options opts = new Options();

	/** The local output dir. */
	protected static String localOutputDir = null;

	/** The extension. */
	protected static String extension = "tif";

	/** The directories wich are images */
	protected List<File> directories = new ArrayList<File>();

	/** The language. */
	protected static Set<Locale> langs;
	
	/** The config. */
	protected HierarchicalConfiguration config;
	//public String defaultConfig = "server-config.xml";
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
	
	private static String ocrTextTyp = null;

	/** The inputfiles. list of the images */
	private static List<File> inputFiles = new ArrayList<File>();

	/** The engine. */
	protected static OCREngine engine;
	
	protected static OCRProcess ocrProcess;

	/** The process. */
	protected static List<OCRProcess> processes = new ArrayList<OCRProcess>();
	protected static HashMap<OCRFormat, OCROutput> OUTPUT_DEFINITIONS;
	/**
	 * Inits the opts.
	 */
	protected static void initOpts () {
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
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws Exception
	 *             the exception
	 */
	public static void main (String[] args) throws IOException, URISyntaxException {
		logger.debug("Creating OCRCli instance");
		OCRCli ocr = OCRCli.getInstance();
		logger.debug("Creating OCREngineFactory instance");
		
		XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("context.xml")); 		
		OCREngineFactory ocrEngineFactory = (OCREngineFactory)factory.getBean("OCREngineFactory");		

		engine = ocrEngineFactory.newOcrEngine();
		
		List<String> files = ocr.defaultOpts(args);
		if(files.size() > 1){
			starten(files);
		}
		if(files.size() == 1){
			for (String id : files) {
				File directory = new File(getBaseFolderAsFile() + "/" + id);
				List<File> folder = OCRUtil.getTargetDirectories(directory, extension);
				List<String> idfiles = new ArrayList<String>();
				for(File idfolder : folder){
					idfiles.add(idfolder.getName());
				}
				starten(idfiles);
			}
			
		}
		
	}

	public static void starten(List<String> files) throws URISyntaxException{
		for (String book : files){
			File directory = new File(getBaseFolderAsFile() + "/" + book);
			logger.debug("Creating AbbyyOCRProcess for " + directory.toString());
			OCRProcess aop =  engine.newOcrProcessforCLI();
			List<File> imageDirs = OCRUtil.getTargetDirectories(directory, extension);
			for (File id : imageDirs) {
				if (imageDirs.size() > 1) {
					logger.error("Directory " + id.getAbsolutePath() + " contains more then one image directories");
					throw new OCRException("can currently create only one AbbyyOCRProcess!");
				}
				List<OCRImage> imgs = new ArrayList<OCRImage>();
				String jobName = id.getName();
				for (File imageFile : OCRUtil.makeFileList(id, extension)) {
					aop.setName(jobName);
					OCRImage aoi = engine.newOcrImageforCLI(imageFile.toURI());
					aoi.setSize(imageFile.length());
			//		aop.addImage(aoi);
					String[] urlParts = imageFile.toURI().toString().split("/");
					if (jobName == null) {
						logger.error("Name for process not set, to avoid errors if your using parallel processes, we generate one.");
						aop.setName(UUID.randomUUID().toString());
					}
					aoi.setRemoteFileName(jobName + "-" + urlParts[urlParts.length - 1]);
					imgs.add(aoi);
				}
				aop.setOcrImages(imgs);
				
			}
			//add format
			OUTPUT_DEFINITIONS = new HashMap<OCRFormat, OCROutput>();
			
			for (OCRFormat ocrformat: f){
				
				OCROutput aoo = engine.newOcrOutput();
				URI uri = new URI(getBaseFolderAsFile().toURI() + localOutputDir + "/" + book + "." + ocrformat.toString().toLowerCase());
				aoo.setUri(uri);				
				OUTPUT_DEFINITIONS.put(ocrformat, aoo);
				aop.addOutput(ocrformat, aoo);
			}
			//add language
			aop.setLanguages(langs);	
			engine.addOcrProcess(aop);
		}
		engine.recognize();
	}
	/**
	 * Configure from args.
	 * 
	 * @param args
	 *            the arguments
	 * @throws IOException
	 */
	public List<String> configureFromArgs (String[] args) {
		//list of the directory
		List<String> files = defaultOpts(args);
		return files;
		
	}

	/**
	 * Gets the image directories wich are in File dir
	 * 
	 * @param dir
	 *            the File wich are images
	 * @return the image directories
	 */
	public static List<File> getImageDirectories (File dir) {
		List<File> files = new ArrayList<File>();
		if (dir.isDirectory()) {
			//get all files which in the topical list, have "extension"  as ending
			files = makeFileList(dir, extension);

		} else {
			throw new IllegalStateException(dir.getAbsolutePath() + " is not a directory");
		}

		return files;
	}

	/**
	 * Make file list, wich have "filter"  as ending.
	 *
	 * @param File, wich are the images
	 * @param filter as ending 
	 * @return the list File
	 */
	public static List<File> makeFileList (File dir, String filter) {
		List<File> fileList;
		if (dir.isDirectory()) {
			// OCR.logger.trace(inputFile + " is a directory");

			File files[] = dir.listFiles(new FileExtensionsFilter(filter));
			fileList = Arrays.asList(files);
			Collections.sort(fileList);

		} else {
			fileList = new ArrayList<File>();
			fileList.add(dir);
			// OCR.logger.trace("Input file: " + inputFile);
		}
		return fileList;
	}

	/**
	 * Gets the single instance of OCRCli.
	 * 
	 * @return single instance of OCRCli
	 */
	public static OCRCli getInstance () {
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
	 * getordinal. checks whether format already gives in enum OCRFormat Class
	 * 
	 * @param name
	 *            is the format from arguments, big converted withUpperCase
	 * @param format
	 *            from arguments
	 * @return the ordinal
	 */
	static int getOrdinal (String name, String format) {
		try {
			return OCRFormat.valueOf(name).ordinal();
		} catch (IllegalArgumentException e) {
			logger.error("the process ended, This Format < " + format + " > is not supported");
			System.exit(0);
			return -1;
		}
	}

	/**
	 * Parse the format from the arguments.
	 * 
	 * @param str
	 *            is the format
	 * @return the list of the OCRFormat
	 */
	public static List<OCRFormat> parseOCRTextTyp (String str) {
		List<OCRFormat> ocrFormats = new ArrayList<OCRFormat>();
		//TODO: Add a test for this
		//TODO: remove the else clause
		if (str.contains(",")) {
			for (String ocrTextTyp : Arrays.asList(str.split(","))) {

				//getOrdinal( ocrFormat.toUpperCase(), ocrFormat );
				ocrFormats.add(OCRFormat.parseOCRFormat(ocrTextTyp.toUpperCase()));

				//process.addOCRFormat(OCRFormat.parseOCRFormat(ocrFormat.toUpperCase()));
			}
		} else {
			//getOrdinal( str.toUpperCase() , str );
			ocrFormats.add(OCRFormat.parseOCRFormat(str.toUpperCase()));
			//process.addOCRFormat(OCRFormat.parseOCRFormat(str.toUpperCase()));
		}
		return ocrFormats;
	}

	
	public static List<OCRFormat> parseOCRFormat (String str) {
		List<OCRFormat> ocrFormats = new ArrayList<OCRFormat>();
		//TODO: Add a test for this
		//TODO: remove the else clause
		if (str.contains(",")) {
			for (String ocrFormat : Arrays.asList(str.split(","))) {

				//getOrdinal( ocrFormat.toUpperCase(), ocrFormat );
				ocrFormats.add(OCRFormat.parseOCRFormat(ocrFormat.toUpperCase()));

				//process.addOCRFormat(OCRFormat.parseOCRFormat(ocrFormat.toUpperCase()));
			}
		} else {
			//getOrdinal( str.toUpperCase() , str );
			ocrFormats.add(OCRFormat.parseOCRFormat(str.toUpperCase()));
			//process.addOCRFormat(OCRFormat.parseOCRFormat(str.toUpperCase()));
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
	protected List<String> defaultOpts (String[] args) {

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

		//TODO OCRTextTyp
		if(cmd.hasOption("t")){
			if (cmd.getOptionValue("t") != null && !cmd.getOptionValue("t").equals("")) {
				ocrTextTyp = cmd.getOptionValue("o");
			}
		}
		// Output foler
		if (cmd.hasOption("o")) {
			if (cmd.getOptionValue("o") != null && !cmd.getOptionValue("o").equals("")) {
				localOutputDir = cmd.getOptionValue("o");
			}
		}
		//List of the directory wich are images
		return cmd.getArgList();
	}

	public static File getBaseFolderAsFile () {
		File basefolder;
		// TODO: GDZ: Do wee really need to depend on Log4J here? I don't think so...
		URL url = Loader.getResource("");
		try {
			basefolder = new File(url.toURI());
		} catch (URISyntaxException ue) {
			basefolder = new File(url.getPath());
		}
		return basefolder;
	}
	
	/**
	 * Gets the file count.
	 * 
	 * @return the file count
	 */
	public Long getFileCount () {
		throw new NotImplementedException();
	}

	/**
	 * Adds the directory.
	 * 
	 * @param dir
	 *            the dir
	 */
	public void addDirectory (File dir) {
		this.directories.add(dir);
	}

	/**
	 * Gets the directories.
	 * 
	 * @return the directories
	 */
	public List<File> getDirectories () {
		return directories;
	}

	/**
	 * Sets the directories.
	 * 
	 * @param directories
	 *            the new directories
	 */
	public void setDirectories (List<File> directories) {
		this.directories = directories;
	}

	/**
	 * Gets the input files.
	 * 
	 * @return the input files
	 */
	public static List<File> getInputFiles () {
		return inputFiles;
	}

}