package de.uni_goettingen.sub.commons.ocr.abbyy.server;

/*

Copyright 2010 SUB Goettingen. All rights reserved.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AbbyyServerSimulator extends Thread {
	protected File hotfolder, inputHotfolder, outputHotfolder, errorHotfolder, expected, errorExpected, outputExpected;
	public static String INPUT_NAME = "input";
	public static String OUTPUT_NAME = "output";
	public static String ERROR_NAME = "error";

	protected Map<String, File> resultsError = new HashMap<String, File>();
	protected Map<String, File> resultsOutput = new HashMap<String, File>();

	protected List<Thread> processes = new ArrayList<Thread>();

	protected static Long wait = 1000l;

	final static Logger logger = LoggerFactory.getLogger(AbbyyServerSimulator.class);

	protected Boolean finish = false;

	protected static Long startTime = System.currentTimeMillis();
	//Wait 15 minutes
	protected static Long maxWait = 1000l * 60l * 15l;

	public AbbyyServerSimulator(File hotfolder, File expactations) {
		
		this.hotfolder = hotfolder;
		this.expected = expactations;

		this.inputHotfolder = new File(hotfolder.getAbsolutePath() + File.separator + INPUT_NAME);
		this.outputHotfolder = new File(hotfolder.getAbsolutePath() + File.separator + OUTPUT_NAME);
		this.errorHotfolder = new File(hotfolder.getAbsolutePath() + File.separator + ERROR_NAME);

		errorExpected = new File(expactations.getAbsolutePath() + File.separator + ERROR_NAME);
		outputExpected = new File(expactations.getAbsolutePath() + File.separator + OUTPUT_NAME);

		for (File f : Arrays.asList(errorExpected.listFiles())) {
			if (f.isDirectory()) {
				resultsError.put(f.getName(), f);
				logger.debug("Adding " + f.getName() + " as error result.");
			}
		}

		for (File f : Arrays.asList(outputExpected.listFiles())) {
			if (f.isDirectory()) {
				resultsOutput.put(f.getName(), f);
				logger.debug("Adding " + f.getName() + " as expected result.");
			}
		}
		logger.debug("Simulator set up.");
	}

	@Override
	public void run () {
		while (!isInterrupted()) {
			if (System.currentTimeMillis() > startTime + maxWait) {
				interrupt();
			}

			try {
				checkDirectory(inputHotfolder);
				sleep(500);
			} catch (InterruptedException e) {
				interrupt();
			} catch (XmlException e) {
				logger.error("Got an error parsing XML.", e);
			} catch (IOException e) {
				logger.error("Got an error reading file.", e);
			}

			if (finish == true) {
				for (Thread t : processes) {
					try {
						t.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				interrupt();
			}
			logger.trace("Reached end of loop.");
		}
	}

	protected void removeJob (File ticket) throws XmlException, IOException {
		List<String> files = getFileNamesFromTicket(ticket);
		for (String str : files) {
			new File(inputHotfolder.getAbsolutePath() + File.separator + str).delete();
		}
		
		logger.info("Deleted referenced files, now deleting ticket");
		ticket.delete();
	}

	protected void checkDirectory (File dir) throws XmlException, IOException {
		logger.trace("Checking directory: " + dir.getAbsolutePath());

		List<File> inputContents = Arrays.asList(dir.listFiles());
		if (inputContents.size() < 1) {
			//logger.info("No files in input folder.");
			return;
		}

		for (File f : inputContents) {
			if (f.getAbsolutePath().endsWith("xml")) {

				String ticket = f.getName();
				logger.debug("Found XML: " + ticket);
				Long wait = calculateWait(f);
				Thread serverProcess = createCopyThread(wait, f);
				serverProcess.start();
				processes.add(serverProcess);
			}
		}
	}

	protected String getResultFileType(File ticket) throws IOException {
		
		Map<String, String> typeMapping = new HashMap<String, String>();
		typeMapping.put("Text", "txt");
		typeMapping.put("XML", "xml");
		typeMapping.put("PDF", "pdf");
		typeMapping.put("HTML", "html");
		typeMapping.put("MSWord", "doc");
		
		String ticketString = IOUtils.toString(new FileInputStream(ticket));
		Pattern pattern = Pattern.compile("OutputFileFormat=\"(.+?)\"");
		Matcher matcher = pattern.matcher(ticketString);
		matcher.find();
		String abbyyType = matcher.group(1);
		
		return typeMapping.get(abbyyType);
	}
	
	protected List<String> getFileNamesFromTicket(File ticket) throws IOException {
		String ticketString = IOUtils.toString(new FileInputStream(ticket));
		Pattern pattern = Pattern.compile("<InputFile Name=\"(.+?)\"");
		Matcher matcher = pattern.matcher(ticketString);
		
		List<String> fileNames = new ArrayList<String>();
		while (matcher.find()) {
			fileNames.add(matcher.group(1));
		}
		
		return fileNames;
	}
	
	protected Long calculateWait (File file) throws XmlException, IOException {
		List<String> files = getFileNamesFromTicket(file);
		
		return files.size() * wait;
	}

	protected static Boolean containsTicket (File dir) {
		for (File f : Arrays.asList(dir.listFiles())) {
			if (f.getAbsolutePath().endsWith("xml")) {
				return true;
			}
		}
		return false;
	}

	protected void clean () {
		de.unigoettingen.sub.commons.util.file.FileUtils.deleteInDir(inputHotfolder);
		de.unigoettingen.sub.commons.util.file.FileUtils.deleteInDir(outputHotfolder);
		de.unigoettingen.sub.commons.util.file.FileUtils.deleteInDir(errorHotfolder);
	}

	public void finish() {
		finish = true;
	}
	
	private Thread createCopyThread (final Long wait, final File ticket) {
		return new Thread() {
			@Override
			public void run () {
				try {
					String jobName = ticket.getName();
					jobName = jobName.substring(0, jobName.indexOf(".xml"));
					String resultExtension = getResultFileType(ticket);
					//Remove the files
					removeJob(ticket);
					logger.info("Removed files for " + jobName + ", waiting " + wait + " mili seconds");
					sleep(wait);
					//Check if this Thread waits to long
					if (System.currentTimeMillis() > startTime + AbbyyServerSimulator.maxWait) {
						logger.error("Waited a least" + (AbbyyServerSimulator.maxWait / 1000) + " seconds, exiting");
						this.interrupt();
					}
					//Copy the files to the right location
					Boolean foundResult = false;
					if (resultsOutput.containsKey(jobName)) {
						de.unigoettingen.sub.commons.util.file.FileUtils.copyDirectory(resultsOutput.get(jobName), outputHotfolder);
						foundResult = true;
					}
					if (resultsError.containsKey(jobName)) {
						de.unigoettingen.sub.commons.util.file.FileUtils.copyDirectory(resultsError.get(jobName), errorHotfolder);
						foundResult = true;
					}
					if (!foundResult) {
						logger.info("Found no prepared result for " + jobName + ", using generic samples instead");
						File samples = new File(expected, "samples");
						
						File ocrResultSample = new File(samples, "sample." + resultExtension);
						File ocrResult = new File(outputHotfolder, jobName + "." + resultExtension);
						FileUtils.copyFile(ocrResultSample, ocrResult);

						File xmlResultSample = new File(samples, "sample.xml.result.xml");
						File xmlResult = new File(outputHotfolder, jobName + ".xml.result.xml");
						FileUtils.copyFile(xmlResultSample, xmlResult);
												
					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (XmlException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}

}
