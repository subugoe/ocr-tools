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
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

public class Main {

	private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);
	private static PrintStream out = System.out;

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
	static void redirectSystemOutputTo(PrintStream stream) {
		out = stream;
	}

	public static void main(String[] args) throws URISyntaxException {
		new Main().execute(args);
	}

	void execute(String[] args) throws URISyntaxException {
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