/**
 * @author Sven Thomas
 * @author Christian Mahnke
 * @version 1.0
 */
package de.unigoettingen.sub.commons.ocrComponents.cli;


import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.unigoettingen.sub.commons.util.file.FileExtensionsFilter;
import de.unigoettingen.sub.commons.util.file.FileUtils;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.HierarchicalConfiguration;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class OCRCli.
 * command line is special for the input parametres 
 * which the API abbyy-server-impl needs. these parametres are
 * language, format , directories and outputlocation
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

	/** The directories wich are images */
	protected List<File> directories = new ArrayList<File>();
	
	/** The language. */
	protected static List<Locale> langs;
	
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
	
	/** The inputfiles. list of the images */
	private static List<File> inputFiles = new ArrayList<File>();
	
	
	/** The engine. */
	protected static OCREngine engine;
	
	/** The process. */
	protected static OCRProcess process;

	
	
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
		opts.addOption("o", true, "Output folder");
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		logger.info("Creating OCRRunner instance");
		OCRCli ocr = OCRCli.getInstance();	
		ocr.configureFromArgs(args);
		engine.recognize();
	}

	/**
	 * Configure from args.
	 *
	 * @param args the arguments
	 * @throws IOException 
	 */
	public void configureFromArgs(String[] args) {
		//list of the directory
		List<String> files = defaultOpts(args);
		
		if (recursiveMode) {
			
			
			for (String dir : files) {
				List<File> newFiles = new ArrayList<File>();
				newFiles.addAll(getImageDirectories(new File(dir)));
				//TODO
			//	OCRProcess p = engine.newProcess(new File(dir));
				OCRImage img = null;
				for (File file : newFiles) {
					img = engine.newImage();
					System.out.println(engine.newImage().getClass());
					System.out.println(img.toString());
					System.out.print(file.toString());
					img.setUrl(new URL ("file://" + file.toString()));
					//img.setUrl(new URL(file.getAbsolutePath().toString()));
				//	p.addImage(img);
				}
				//list of the directory as process
			//	engine.addOcrProcess(p);
			}	
		}
	}
	
	/**
	 * Gets the image directories wich are in File dir
	 * 
	 * @param dir the File wich are images
	 * @return the image directories
	 */
	public static List<File> getImageDirectories(File dir) {
		List<File> files = new ArrayList<File>();
		if (dir.isDirectory()) {				
			//get all files which in the topical list, have "extension"  as ending
			files = makeFileList(dir, extension);
			
		} else {
			throw new IllegalStateException(dir.getAbsolutePath() + " is not a directory");
		}
		
		return files;
	}
	
	
	public static List<File> makeFileList(File dir, String filter) {
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
	 * Parses the language.
	 *
	 * @param str the str
	 * @return the list of language
	 */
	public static List<Locale> parseLangs(String str) {
		List<Locale> langs = new ArrayList<Locale>();
		//TODO: Test this, remove the if
		if (str.contains(",")) {
			for (String lang : Arrays.asList(str.split(","))) {
				langs.add(new Locale(lang));
			    process.addLanguage(new Locale(lang));
			}
		} else {
			langs.add(new Locale(str));
			process.addLanguage(new Locale(str));
		}
		return langs;
	}
	
	/**
	 * getordinal. checks whether format already gives in enum OCRFormat Class
	 *
	 * @param name is the format from arguments, big converted withUpperCase
	 * @param format from arguments
	 * @return the ordinal
	 */
	static int getOrdinal( String name , String format ) 
	{ 
	  try { 
	    return OCRFormat.valueOf( name ).ordinal(); 
	  } 
	  catch ( IllegalArgumentException e ) { 
		  logger.error("the process ended, This Format < "+ format +" > is not supported"  );
		  System.exit(0);
	    return -1; 
	  } 
	}

	/**
	 * Parses the format from the arguments.
	 *
	 * @param str is the format
	 * @return the list of the OCRFormat
	 */
	public static List<OCRFormat> parseOCRFormat(String str) {
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
	 * @param args the args
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
			langs = parseLangs(cmd.getOptionValue("l"));
		} else {
			langs = new ArrayList<Locale>();
			langs.add(new Locale("de"));
		}
		for (Locale lang : langs) {
			logger.trace("Language: " + lang.getLanguage());
		}

		logger.trace("Parsing Options");

		
		if (cmd.hasOption("r")) {
			recursiveMode = true;
		}
		
		// Output foler
		if (cmd.hasOption("o")) {
			if (cmd.getOptionValue("o") != null
					&& !cmd.getOptionValue("o").equals("")) {
				localOutputDir = cmd.getOptionValue("o");
				for (OCRFormat of: f) {
					//TODO: Finish this
					//OCRResult 
				}
				
				//process.setOutputLocation(localOutputDir);
			}
		}
		//List of the directory wich are images
		return cmd.getArgList();
	}

		
	/**
	 * Gets the file count.
	 *
	 * @return the file count
	 */
	public Long getFileCount() {
		throw new NotImplementedException();
	}

	/**
	 * Adds the directory.
	 *
	 * @param dir the dir
	 */
	public void addDirectory(File dir) {
		this.directories.add(dir);
	}

	/**
	 * Gets the directories.
	 *
	 * @return the directories
	 */
	public List<File> getDirectories() {
		return directories;
	}

	/**
	 * Sets the directories.
	 *
	 * @param directories the new directories
	 */
	public void setDirectories(List<File> directories) {
		this.directories = directories;
	}

	/**
	 * Gets the input files.
	 *
	 * @return the input files
	 */
	public static List<File> getInputFiles() {
		return inputFiles;
	}

	
	

}