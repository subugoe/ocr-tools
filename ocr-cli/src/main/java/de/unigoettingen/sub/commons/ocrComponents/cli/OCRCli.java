/**
 * @author Sven Thomas
 * @author Christian Mahnke
 * @version 1.0
 */
package de.unigoettingen.sub.commons.ocrComponents.cli;


import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import java.io.File;
import java.net.URL;
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
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





public class OCRCli {
	//TODO: Test if languages are handled correctly
	public final static String version = "0.0.4";
/*
	//TODO: Check if this could be static
	protected HttpClient client;*/
	protected static Logger logger = LoggerFactory.getLogger(de.unigoettingen.sub.commons.ocrComponents.cli.OCRCli.class);

	private static Options opts = new Options();
	

	protected static String localOutputDir = null;
	protected static String extension = "tif";

	protected List<File> directories = new ArrayList<File>();
	protected List<Locale> langs;
	protected HierarchicalConfiguration config;
	public String defaultConfig = "server-config.xml";
	//Settings for Ticket creation
	protected Boolean recursiveMode = true;
	//TODO: Try to move this stuff to the OCRProcess
	protected static Boolean writeRemotePrefix = true;

	
	//Options
	//TODO: Merge with OCR.java (make OCR.jave the base class for this stuff)
	//TODO: Check if needed (mostly the output file stuff)
	//TODO make this static or add a metod for ading this to a aprocess
	/*protected HashMap<OCRFormat, String> of = new HashMap<OCRFormat, String>();
	protected HashMap<OCRExportFormat, List<String>> ofo = new HashMap<OCRExportFormat, List<String>>();*/
	List<OCRFormat> of = new ArrayList<OCRFormat>();
	

	

	protected static void initOpts() {
		// Parameters
		opts.addOption("r", false, "Recursive - scan for subdirectories");
		opts.addOption("ofn", true, "Output filename / directory");
		opts.addOption("of", true, "Output format");

		/*Option ofo = OptionBuilder.withArgName("format:option").hasArg().withValueSeparator().withDescription("Output format options").create("ofo");

		opts.addOption(ofo);*/
		opts.addOption("l", true, "Languages - seperated by \",\"");
		opts.addOption("h", false, "Help");
		opts.addOption("v", false, "Version");
		//opts.addOption("lc", true, "Logger Configuration");
		opts.addOption("d", true, "Debuglevel");
		opts.addOption("c", true, "Configuration file (optional)");
		opts.addOption("e", true, "File extension (default \"tif\")");
		opts.addOption("o", true, "Output folder");
	}

	private OCRCli() {
		initOpts();
	}

	public static List<Locale> parseLangs (String str) {
		List<Locale> langs = new ArrayList<Locale>();
		if (str.contains(",")) {
			for (String lang: Arrays.asList(str.split(",")))	{
				langs.add(new Locale(lang));
			}
		} else {
			langs.add(new Locale(str));
		}
		return langs;
	}
	///////////////////////////////////////////////////////////////
	/*public static List<OCRFormat> parseOpts(String opt) {
		List<OCRFormat> hm = new ArrayList<OCRFormat>();
		String[] opts = StringUtils.split(opt, ",");
		for(int i=0; i<opts.length; i++ ) {
			
			hm.add(OCRFormat.(opts[i]).toString());
		}
		return hm;
	}*/

	protected List<String> defaultOpts(String[] args) {
		//TODO OutputDir konfigurierbar (Kommandozeile)
		String cmdName = "OCRRunner [opts] files";
		CommandLine cmd = null;
		// Parameter interpretieren
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
		//////////////////////////////////////
		// Logger Configuration
		/*if (cmd.hasOption("lc")) {
			if (cmd.getOptionValue("lc") != null && !cmd.getOptionValue("lc").equals("")) {
				PropertyConfigurator.configure(cmd.getOptionValue("lc"));
				
			}
		}*/

		// Debug
		if (cmd.hasOption("d")) {
			//logger.setLevel(Level.toLevel(cmd.getOptionValue("d")));
			logger.debug(cmd.getOptionValue("d"));
			logger.trace("Debuglevel: " + cmd.getOptionValue("d"));
		}

		// Configuration
		if (cmd.hasOption("c") && cmd.getOptionValue("c") != null) {
			try {
				config = new XMLConfiguration(cmd.getOptionValue("c"));
			} catch (ConfigurationException e) {
				logger.error("Could not load configuration", e);
			}
		} else {
			URL cfile;
			try {
				cfile = getClass().getResource(defaultConfig);
				if (cfile != null) {
					config = new XMLConfiguration(cfile);

				}
			} catch (ConfigurationException e) {
				logger.error("Could not load configuration", e);
				throw new RuntimeException("Could not load configuration");
			}
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
		
		/////////////////////////////////////////////
		/*if (cmd.hasOption("of")) {
			of = parseOpts(cmd.getOptionValue("of"));
		}
		if (cmd.hasOption("ofn")) {
			of = parseOpts(cmd.getOptionValue("ofn"));
		}*/

		/*if (cmd.hasOption("ofo")) {
			ofo = OCR.parseOptsList(cmd.getOptionValue("ofo"));
		}*/

		//Directories
		//recursive mode
		if (cmd.hasOption("r")) {
			recursiveMode = true;
		}
		
		// Output foler
		if (cmd.hasOption("o")) {
			if (cmd.getOptionValue("o") != null && !cmd.getOptionValue("o").equals("")) {
				localOutputDir = cmd.getOptionValue("o");
			}
		}

		return cmd.getArgList();
	}

	

	public Long getFileCount() {
		throw new NotImplementedException();
	}

	
	public void addDirectory (File dir) {
		this.directories.add(dir);
	}

	public List<File> getDirectories() {
		return directories;
	}

	public void setDirectories(List<File> directories) {
		this.directories = directories;
	}

	

	
}