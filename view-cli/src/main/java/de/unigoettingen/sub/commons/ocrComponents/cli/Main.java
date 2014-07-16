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

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.unigoettingen.sub.ocr.controller.OcrEngineStarter;
import de.unigoettingen.sub.ocr.controller.OcrParameters;
import de.unigoettingen.sub.ocr.controller.Validator;

public class Main {

	private PrintStream out = System.out;
	private Options options = new Options();
	private CommandLine parsedOptions;
	private boolean terminated = false;
	private Validator paramValidator = new Validator();
	private OcrEngineStarter engineStarter = new OcrEngineStarter();

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
			out.println("Starting OCR...");
			engineStarter.startOcrWithParams(params);
			out.println("Finished OCR.");
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
	
}