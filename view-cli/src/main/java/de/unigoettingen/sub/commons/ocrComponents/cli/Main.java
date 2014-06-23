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
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextType;
import de.unigoettingen.sub.commons.ocr.util.OCRUtil;
import de.unigoettingen.sub.ocr.controller.OcrEngineStarter;
import de.unigoettingen.sub.ocr.controller.Validator;
import de.unigoettingen.sub.ocr.controller.OcrParameters;

public class Main {

	private PrintStream out = System.out;
	private Options options = new Options();
	private CommandLine parsedOptions;
	private boolean terminated = false;
	private Validator paramValidator = new Validator();
	private OcrEngineStarter engineStarter = new OcrEngineStarter();
	
	private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private Options opts = new Options();

	String localOutputDir = null;

	private String extension = "tif";

	Set<Locale> langs;

	List<OCRFormat> f = new ArrayList<OCRFormat>();

	private String ocrTextTyp = null;
	
	private String ocrPriority = null;

	private String ocrEngineToUse = "abbyy";
	
	private boolean splitProcess = false;
	
	private OCREngine engine;

	private List<File> dirs = new ArrayList<File>();

	private Map<String, String> extraOptions;

	// for unit tests
	void redirectSystemOutputTo(PrintStream stream) {
		out = stream;
	}
	// for unit tests
	void setValidator(Validator newValidator) {
		paramValidator = newValidator;
	}
	// for unit tests
	void setOcrEngineStarter(OcrEngineStarter newStarter) {
		engineStarter = newStarter;
	}

	public static void main(String[] args) throws URISyntaxException, UnsupportedEncodingException {
		new Main().execute(args);
	}

	void execute(String[] args) throws UnsupportedEncodingException {
		initOptions(args);
		if (terminated) {
			return;
		}
		OcrParameters params = transformOptions();
		if (terminated) {
			return;
		}
		String validationMessage = paramValidator.validateParameters(params);
		if ("OK".equals(validationMessage)) {
			engineStarter.startOcrWithParams(params);
		} else {
			out.println("Illegal options: " + validationMessage);
		}
		
	}
	
	private void initOptions(String[] args) throws UnsupportedEncodingException {
		options.addOption("help", false, "Print help");
		options.addOption("indir", true, "Input directory - required");
		options.addOption("informats", true, "File extensions, e.g. tif,jpg (default: all images)");
		options.addOption("texttype", true, "E.g. normal or gothic - required");
		options.addOption("langs", true, "Languages, e.g. de,en,fr - required");
		options.addOption("outdir", true, "Output directory - required");
		options.addOption("outformats", true, "Output formats, e.g. pdf,xml - required");
		options.addOption("prio", true, "Priority: -2, -1, 0, 1, or 2. default is 0");
		options.addOption("engine", true, "OCR engine, e.g. abbyy, abbyy-multiuser, ocrsdk, tesseract (default is abbyy)");
		options.addOption("props", true, "Further properties, comma-separated. E.g. -props lock.overwrite=true,user=hans,filesegments=true");
		CommandLineParser parser = new GnuParser();

		try {
			parsedOptions = parser.parse(options, args);
		} catch (ParseException e) {
			out.println("Illegal arguments. Use -help.");
			terminated = true;
		}

	}
	
	private void printHelp() throws UnsupportedEncodingException {
		OutputStreamWriter osw = new OutputStreamWriter(out, "UTF8");
		PrintWriter pw = new PrintWriter(osw);
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH, "java -jar ocr.jar <options>", "", options,
				HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, "");
		pw.close();
	}

	private OcrParameters transformOptions() throws UnsupportedEncodingException {
		OcrParameters params = new OcrParameters();
		if (parsedOptions.hasOption("help")) {
			terminated = true;
			printHelp();
			return params;
		}
		
		if (requiredOptionsArePresent()) {
			params.inputFolder = parsedOptions.getOptionValue("indir");
			params.inputTextType = parsedOptions.getOptionValue("texttype");
			params.inputLanguages = parsedOptions.getOptionValue("langs").split(",");
			params.outputFolder = parsedOptions.getOptionValue("outdir");
			params.outputFormats = parsedOptions.getOptionValue("outformats").split(",");
		} else {
			out.println("Required options are missing. Use -help.");
			terminated = true;
			return params;
		}
		
		initDefaultParams(params);
		
		if (parsedOptions.hasOption("informats")) {
			params.inputFormats = parsedOptions.getOptionValue("informats").split(",");
		}
		
		if (parsedOptions.hasOption("prio")) {
			params.priority = parsedOptions.getOptionValue("prio");
		}
		
		if (parsedOptions.hasOption("engine")) {
			params.ocrEngine = parsedOptions.getOptionValue("engine");
		}
		
		if (parsedOptions.hasOption("props")) {
			params.props = convertExtraProperties(parsedOptions.getOptionValue("props"));
		}
		
		return params;
	}
	
	private boolean requiredOptionsArePresent() {
		boolean allPresent = true;
		allPresent &= parsedOptions.hasOption("indir");
		allPresent &= parsedOptions.hasOption("texttype");
		allPresent &= parsedOptions.hasOption("langs");
		allPresent &= parsedOptions.hasOption("outdir");
		allPresent &= parsedOptions.hasOption("outformats");
		return allPresent;
	}

	private void initDefaultParams(OcrParameters params) {
		params.inputFormats = new String[]{"tif", "jpg", "gif", "tiff", "png", "jpeg"};
		params.priority = "0";
		params.ocrEngine = "abbyy";
		params.props = new Properties();
	}

	private Properties convertExtraProperties(String extras) {
		Properties extraProperties = new Properties();
		String[] extrasArray = extras.split(","); // opt1=a,opt2=b
		for (String extraProp : extrasArray) {
			String[] keyAndValue = extraProp.split("=");
			String key = keyAndValue[0];
			String value = keyAndValue[1];
			extraProperties.setProperty(key, value);
		}
		return extraProperties;
	}

	void executeOld(String[] args) throws URISyntaxException {
		initOpts();

		List<String> files = defaultOpts(args);
		ApplicationContext ac = new ClassPathXmlApplicationContext(
				ocrEngineToUse + "-context.xml");

		engine = (OCREngine) ac.getBean("ocrEngine");

		if (extraOptions != null) {
			engine.setOptions(extraOptions);
		}
		if (files.size() > 1) {
			LOGGER.error("there are more folders, should be only one folder as Input");
			System.exit(0);
		}
		if (files.size() == 1) {
			startRecognize(files);
		}
	}

	protected void initOpts() {
		opts.addOption("f", true, "Output format");
		opts.addOption("l", true, "Languages - separated by \",\"");
		opts.addOption("h", false, "Help");
		opts.addOption("d", true, "Debuglevel");
		opts.addOption("e", true, "File extension (default \"tif\")");
		opts.addOption("t", true, "OCRTextTyp");
		opts.addOption("o", true, "Output folder");
		opts.addOption("p", true, "Priority");
		opts.addOption("s", false, "Segmentation");
		opts.addOption("E", true, "OCR Engine, e.g. abbyy, abbyy-multiuser, ocrsdk, tesseract (default is abbyy)");
		opts.addOption("O", true, "Further options, comma-separated. E.g. -O lock.overwrite=true,opt2=value2");
	}

	public void startRecognize(List<String> files)
			throws URISyntaxException {
		for (String book : files) {
			File directory = new File(book);
			
			getDirectories(directory);
			if (dirs.size() == 0) {
				LOGGER.error("Directories is Empty : " + book);
				System.exit(0);
			}
			for (File id : dirs) {
				if (OCRUtil.makeFileList(id, extension).size() != 0) {

					LOGGER.debug("Creating Process for " + id.toString());
					OCRProcess aop = engine.newOcrProcess();
					String jobName = id.getName();
					aop.setName(jobName);
					if (jobName.equals("")) {
						LOGGER.error("Name for process not set, to avoid errors if your using parallel processes, we generate one.");
						aop.setName(UUID.randomUUID().toString());
					}
					List<OCRImage> imgs = new ArrayList<OCRImage>();
					for (File imageFile : OCRUtil.makeFileList(id, extension)) {
						OCRImage aoi = engine.newOcrImage(imageFile.toURI());
						aoi.setSize(imageFile.length());
						imgs.add(aoi);
					}
					aop.setOcrImages(imgs);

					for (OCRFormat ocrformat : f) {
						OCROutput aoo = engine.newOcrOutput();						
						URI uri = new URI(new File(localOutputDir).toURI()
								+ id.getName()
								+ "." + ocrformat.toString().toLowerCase());
						LOGGER.debug("output Location " + uri.toString());
						aoo.setUri(uri);
						aoo.setlocalOutput(new File(localOutputDir).getAbsolutePath());
						aop.addOutput(ocrformat, aoo);
					}
					// add language
					aop.setLanguages(langs);
					aop.setSplitProcess(splitProcess);
					if(ocrPriority != null){
						aop.setPriority(OCRPriority.valueOf(ocrPriority));
					}else{
						aop.setPriority(OCRPriority.NORMAL);
					}
					aop.setTextType(OCRTextType.valueOf(ocrTextTyp));
					engine.addOcrProcess(aop);
				}
			}
		}
		LOGGER.info("Starting recognize method");
		engine.recognize();
		LOGGER.debug("recognize Finished");
	}

	void getDirectories(File aFile) {
		if (aFile.isDirectory()) {
			dirs.add(aFile);
			File[] listOfFiles = aFile.listFiles();
			if (listOfFiles != null) {
				for (int i = 0; i < listOfFiles.length; i++) {
					getDirectories(listOfFiles[i]);
				}
			} else {
				LOGGER.error(" [ACCESS DENIED]");
			}
		}
	}

	protected List<OCRFormat> parseOCRFormat(String str) {
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

	protected List<String> defaultOpts(String[] args) {

		String cmdName = "OCRRunner [opts] files";
		CommandLine cmd = null;

		CommandLineParser parser = new GnuParser();

		try {
			cmd = parser.parse(opts, args);

		} catch (ParseException e) {
			LOGGER.error("Illegal options", e);
			System.exit(3);
		}

		if (cmd.getArgList().isEmpty()) {
			LOGGER.trace("No Input Files!");
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

		if (cmd.hasOption("E")) {
			if (cmd.getOptionValue("E") != null) {
				ocrEngineToUse = cmd.getOptionValue("E");
			}
		}

		if (cmd.hasOption("f")) {
			f = parseOCRFormat(cmd.getOptionValue("f"));

		}
		// Debug
		if (cmd.hasOption("d")) {
			// logger.setLevel(Level.toLevel(cmd.getOptionValue("d")));
			LOGGER.debug(cmd.getOptionValue("d"));
			LOGGER.trace("Debuglevel: " + cmd.getOptionValue("d"));
		}

		// Sprache
		if (cmd.hasOption("l")) {
			langs = OCRUtil.parseLang(cmd.getOptionValue("l"));
		} else {
			langs = new HashSet<Locale>();
			langs.add(new Locale("de"));
		}
		for (Locale lang : langs) {
			LOGGER.trace("Language: " + lang.getLanguage());
		}

		LOGGER.trace("Parsing Options");

		//Segmentation
		if(cmd.hasOption("s")){
			splitProcess = true;
		}
		// OCRTextTyp
		if (cmd.hasOption("t")) {
			if (cmd.getOptionValue("t") != null
					&& !cmd.getOptionValue("t").equals("")) {
				ocrTextTyp = cmd.getOptionValue("t");
				try {
					OCRProcess.OCRTextType.valueOf(ocrTextTyp).ordinal();
					LOGGER.trace("ocrTextTyp: " + ocrTextTyp);
				} catch (IllegalArgumentException e) {
					LOGGER.error("the process ended, This ocrTextTyp < "
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
					LOGGER.trace("ocrPriority: " + ocrPriority);
				} catch (IllegalArgumentException e) {
					LOGGER.error("the process ended, This ocrPriority < "
							+ ocrPriority + " > is not supported");
					System.exit(0);

				}
			}else {ocrPriority = "NORMAL";}
		}
		// Output foler
		if (cmd.hasOption("o")) {
			if (cmd.getOptionValue("o") != null
					&& !cmd.getOptionValue("o").equals("")) {
				localOutputDir = cmd.getOptionValue("o");
			} else {
				LOGGER.error("the process ended, This localOutputDir < "
						+ localOutputDir + " > is null");
				System.exit(0);
			}
		}
		if (cmd.hasOption("O")) {
			String extraOptionsString = cmd.getOptionValue("O");
			if (extraOptionsString != null	&& !extraOptionsString.equals("")) {
				extraOptions = new HashMap<String, String>();
				parseExtraOptions(extraOptionsString);
			}
		}
		// List of the directory wich are images
		return cmd.getArgList();
	}

	private void parseExtraOptions(String extras) {
		String[] extrasArray = extras.split(","); // opt1=a,opt2=b
		for (String extraOpt : extrasArray) {
			String[] keyAndValue = extraOpt.split("=");
			String key = keyAndValue[0];
			String value = keyAndValue[1];
			extraOptions.put(key, value);
		}
		
	}
	
}