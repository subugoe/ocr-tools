package de.unigoettingen.sub.commons.ocr.engine.abbyy;

/*

© 2009, 2010, SUB Goettingen. All rights reserved.
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.AbstractImage;
import de.uni_goettingen.sub.commons.ocr.api.AbstractProcess;
import de.uni_goettingen.sub.commons.ocr.api.AbstractOutput;
import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrImage;
import de.uni_goettingen.sub.commons.ocr.api.OcrOutput;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;
import de.unigoettingen.sub.commons.ocr.util.abbyy.ToAbbyyMapper;

public class AbbyyCLIOCRProcess extends AbstractProcess implements OcrProcess, Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7402569872261354815L;

	final static Logger logger = LoggerFactory.getLogger(AbbyyCLIOCRProcess.class);

	private List<String> cmd;

	//Internal configuration
	private Boolean setOrientation = false;
	private Boolean traceEngine = true;

	//Internal state variables
	private Integer progress;

	@Override
	public void addImage(URI localUri) {
		OcrImage image = new AbstractImage() {};
		image.setLocalUri(localUri);
		ocrImages.add(image);
	}
	
	@Override
	public void addOutput(OcrFormat format) {
		OcrOutput output = new AbstractOutput() {};
		output.setLocalUri(constructLocalUri(format));
		output.setFormat(format);
		ocrOutputs.add(output);
	}

	private List<String> buildInputFileList (String param) throws URISyntaxException {
		ArrayList<String> arglist = new ArrayList<String>();
		for (OcrImage image : ocrImages) {
			File file = new File(image.getLocalUri());
			arglist.add(param);
			arglist.add(file.getAbsolutePath());
			if (setOrientation) {
				arglist.add("-ir");
				String orientation = "NoRotation";
				arglist.add(orientation);
			}

			logger.trace("Datei " + file + "hinzugefügt");
		}
		logger.trace("Argumentlist: " + arglist);
		return arglist;
	}

	private ProcessBuilder buildCmd () throws URISyntaxException {
		ProcessBuilder pb = new ProcessBuilder();
		pb.redirectErrorStream(true);
		if (cmd == null) {
			throw new IllegalStateException("No commmand set!");
		}
		List<String> arglist = new ArrayList<String>();
		//Command
		arglist.addAll(cmd);

		arglist.addAll(AbbyyCLIOCREngine.engineSettings);
		//TODO: Add settings from the configuration file.
		/*
		if (AbstractAbbyyOCREngine.loadSettings(AbstractAbbyyOCREngine.configFile) != null) {
			arglist.addAll(AbstractAbbyyOCREngine.loadSettings(AbstractAbbyyOCREngine.configFile));
		}
		*/
		//Language
		arglist.add("-rl");
		//TODO: add default languages to config
		for (Locale l : getLanguages()) {

			if (ToAbbyyMapper.getLanguage(l) == null) {
				throw new IllegalStateException();
			}
			arglist.add(ToAbbyyMapper.getLanguage(l));
		}

		//Input files
		arglist.addAll(buildInputFileList("-if"));

		//Output format
		//options | format | file
		//-xca -xeca | -f XML | -of

		for (OcrFormat ef : getAllOutputFormats()) {

			arglist.add("-f");
			arglist.add(AbbyyCLIOCREngine.FORMAT_MAPPING.get(ef));
			if (AbbyyCLIOCREngine.FORMAT_SETTINGS.get(ef) != null) {
				arglist.addAll(AbbyyCLIOCREngine.FORMAT_SETTINGS.get(ef));
			}
			if (AbbyyCLIOCREngine.FORMAT_MAPPING.get(ef) == null) {
				throw new IllegalStateException();
			}

			arglist.add("-of");
			String outFile = new File(getOutputUriForFormat(ef)).getAbsolutePath();
			arglist.add(outFile);

		}

		StringBuilder cmd = new StringBuilder();
		for (String param : arglist) {
			logger.trace(param);
			cmd.append(param).append(" ");
		}
		logger.debug(cmd.toString());

		//arglist.add("-of");
		//logger.trace(arglist);
		pb.command(arglist);

		return pb;
	}

	@Override
	public void run () {
		ProcessBuilder pb = null;
		try {
			pb = buildCmd();
		} catch (URISyntaxException e1) {
			logger.error("Can't create process builder, probaly there is a problem with the locations of result files");
			throw new IllegalStateException("Can't create external process!", e1);
		}
		pb.redirectErrorStream(true);
		try {
			Process p = pb.start();
			InputStreamReader tempReader = new InputStreamReader(new BufferedInputStream(p.getInputStream()));
			BufferedReader reader = new BufferedReader(tempReader);
			int i = 0;
			while (true) {
				String line = reader.readLine();
				if (traceEngine && line != null) {
					logger.trace(line);
				}
				try {
					p.exitValue();
					break;
				} catch (IllegalThreadStateException e) {
					if (line == null) {
						break;
					}
				}
				if (!line.equals("\n") && line.length() > 5) {
					if (i < 100) {
						logger.trace(line);
					}
					if ((i % 50) == 0 && line.matches(".*?page (\\d*)\\..*")) {
						calculateProgress(line);
					}
				}
				i++;
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void calculateProgress (String output) {
		Pattern p = Pattern.compile(".*?page (\\d*)\\..*");
		Matcher m = p.matcher(output);
		try {
			if (m.matches()) {
				Integer page = Integer.decode(m.group(1));
				if (page > 0) {
					//This just calculates the progress for the current segment.
					progress = (ocrImages.size() / 100) * page;
				}
				logger.trace("Recognized " + progress + "%");
			}
		} catch (IllegalStateException e) {
			logger.warn("Failed to get progress: ", e);
		}
	}

	protected int getProgress () {
		return progress;
	}
}
