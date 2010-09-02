/**
 * @author Sven Thomas
 * @author Christian Mahnke
 * @version 1.0
 */
package de.unigoettingen.sub.commons.ocrComponents.cli;


import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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

public class OCRCli {
	
	public final static String version = "0.0.4";
	
	protected static Logger logger = LoggerFactory
			.getLogger(de.unigoettingen.sub.commons.ocrComponents.cli.OCRCli.class);

	private static Options opts = new Options();

	protected static String localOutputDir = null;
	protected static String extension = "tif";

	protected List<File> directories = new ArrayList<File>();
	protected List<Locale> langs;
	protected HierarchicalConfiguration config;
	//public String defaultConfig = "server-config.xml";
	// Settings for Ticket creation
	protected Boolean recursiveMode = true;
	String[] args;
	protected static Boolean writeRemotePrefix = true;

	private static OCRCli _instance;

	List<OCRFormat> of = new ArrayList<OCRFormat>();

	protected static void initOpts() {
		// Parameters
		opts.addOption("r", false, "Recursive - scan for subdirectories");
		opts.addOption("ofn", true, "Output filename / directory");
		opts.addOption("of", true, "Output format");
		
		
		opts.addOption("l", true, "Languages - seperated by \",\"");
		opts.addOption("h", false, "Help");
		opts.addOption("v", false, "Version");
		// opts.addOption("lc", true, "Logger Configuration");
		opts.addOption("d", true, "Debuglevel");
		//opts.addOption("c", true, "Configuration file (optional)");
		opts.addOption("e", true, "File extension (default \"tif\")");
		opts.addOption("o", true, "Output folder");
	}

	public static void main(String[] args) throws Exception {
		logger.info("Creating OCRRunner instance");
		OCRCli ocr = OCRCli.getInstance();
		ocr.configureFromArgs(args);

		//System.exit(0);
	}

	public void configureFromArgs(String[] args) {
		List<String> files = defaultOpts(args);
		//loadConfig(config);
		if (recursiveMode) {
			List<File> newFiles = new ArrayList<File>();
			
			for (String dir : files) {
				newFiles.addAll(getImageDirectories(new File(dir)));
			}
			files = new ArrayList<String>();
			for (File dir : newFiles) {
				files.add(dir.getAbsolutePath());
			}
		}

		for (String path : files) {
			File file = new File(path);
			//System.out.println(path + "?????!" );
			if (file.isDirectory()) {
				directories.add(file);


			} else {
				logger.error(path + " is not a directory!");
				//System.out.println(path + " is not a directory!" );
			}
			
		}
		//System.out.println(directories + "directories directory!" );
		
	}
	public static List<File> getImageDirectories(File dir) {
		List<File> dirs = new ArrayList<File>();

		if (OCRUtils.makeFileList(dir, extension).size() > 0) {
			dirs.add(dir);
		}

		List<File> fileList;
		if (dir.isDirectory()) {
			fileList = Arrays.asList(dir.listFiles());
			for (File file : fileList) {
				if (file.isDirectory()) {
					List<File> files = OCRUtils.makeFileList(dir, extension);
					if (files.size() > 0) {
						dirs.addAll(files);
					} else {
						dirs.addAll(getImageDirectories(file));
					}
				}
			}
		} else {
			throw new IllegalStateException(dir.getAbsolutePath() + " is not a directory");
		}
		return dirs;
	}
	public static OCRCli getInstance() {
		if (_instance == null) {
			_instance = new OCRCli();
		}
		return _instance;
	}
	
	public OCRCli() {
		initOpts();
		
	}

	public static List<Locale> parseLangs(String str) {
		List<Locale> langs = new ArrayList<Locale>();
		if (str.contains(",")) {
			for (String lang : Arrays.asList(str.split(","))) {
				langs.add(new Locale(lang));
			}
		} else {
			langs.add(new Locale(str));
		}
		return langs;
	}
	
	//TODO
	public static List<OCRFormat> parseOCRFormat(String str) {
		List<OCRFormat> ocrFormats = new ArrayList<OCRFormat>();
		if (str.contains(",")) {
			for (String ocrFormat : Arrays.asList(str.split(","))) {
				
				if (OCRFormat.TXT.toString().equals(ocrFormat.toUpperCase())){
					ocrFormats.add(OCRFormat.TXT);
					//System.out.println("OCRFOrmat string" + OCRFormat.TXT.toString() );
				}
				if (OCRFormat.PDF.toString().equals(ocrFormat.toUpperCase())){
					ocrFormats.add(OCRFormat.PDF);
					//System.out.println("OCRFOrmat string" + OCRFormat.PDF.toString() );
				}
				if (OCRFormat.DOC.toString().equals(ocrFormat.toUpperCase())){
					ocrFormats.add(OCRFormat.DOC);
					//System.out.println("OCRFOrmat string" + OCRFormat.DOC.toString() );
				}
				if (OCRFormat.HTML.toString().equals(ocrFormat.toUpperCase())){
					ocrFormats.add(OCRFormat.HTML);
					//System.out.println("OCRFOrmat string" + OCRFormat.HTML.toString() );
				}	
				if (OCRFormat.PDFA.toString().equals(ocrFormat.toUpperCase())){
					ocrFormats.add(OCRFormat.PDFA);
					//System.out.println("OCRFOrmat string" + OCRFormat.PDFA.toString() );
				}
				if (OCRFormat.XHTML.toString().equals(ocrFormat.toUpperCase())){
					ocrFormats.add(OCRFormat.XHTML);
					//System.out.println("OCRFOrmat string" + OCRFormat.XHTML.toString() );	
				}
				if (OCRFormat.XML.toString().equals(ocrFormat.toUpperCase())){
					ocrFormats.add(OCRFormat.XML);
					//System.out.println("OCRFOrmat string" + OCRFormat.XML.toString() );
				}
			}
		} else {
			if (OCRFormat.TXT.toString() == str)
			ocrFormats.add(OCRFormat.TXT);
			System.out.println("OCRFOrmat" + ocrFormats );
		}
		return ocrFormats;
	}

	protected List<String> defaultOpts(String[] args) {
		
		// TODO OutputDir konfigurierbar (Kommandozeile)
		String cmdName = "OCRRunner [opts] files";
		CommandLine cmd = null;
		// Parameter interpretieren
		CommandLineParser parser = new GnuParser();
		
		try {
			cmd = parser.parse(opts, args);
			System.out.println("Language        :" + cmd.getOptionValue("l"));
	        System.out.println("hilfe        :" + cmd.getOptionValue("h"));
	        System.out.println("Output Format:" + cmd.getOptionValue("of"));
	        System.out.println("Output File name :" + cmd.getOptionValue("ofn"));
	        System.out.println("Output folder:" + cmd.getOptionValue("o"));
	        System.out.println("Version   :" + cmd.getOptionValue("v"));	        
	        System.out.println(cmd.getArgList());
	        
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
			System.out.println("h ist gegeben");
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
		
		if (cmd.hasOption("ofn")) {
			of = parseOCRFormat(cmd.getOptionValue("ofn"));
		}
		if (cmd.hasOption("of")) {
			of = parseOCRFormat(cmd.getOptionValue("of"));
		}
		// Debug
		if (cmd.hasOption("d")) {
			// logger.setLevel(Level.toLevel(cmd.getOptionValue("d")));
			logger.debug(cmd.getOptionValue("d"));
			logger.trace("Debuglevel: " + cmd.getOptionValue("d"));
		}

		
		// Sprache
		if (cmd.hasOption("l")) {
			//System.out.println("l ist gegeben " );
			langs = parseLangs(cmd.getOptionValue("l"));
		} else {
			langs = new ArrayList<Locale>();
			langs.add(new Locale("de"));
		}
		for (Locale lang : langs) {
			logger.trace("Language: " + lang.getLanguage());
			System.out.println("Language: " + lang.getLanguage());
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
			}
		}

				
		return (List<String>)cmd.getArgList();
	}

		
	public Long getFileCount() {
		throw new NotImplementedException();
	}

	public void addDirectory(File dir) {
		this.directories.add(dir);
	}

	public List<File> getDirectories() {
		return directories;
	}

	public void setDirectories(List<File> directories) {
		this.directories = directories;
	}

}