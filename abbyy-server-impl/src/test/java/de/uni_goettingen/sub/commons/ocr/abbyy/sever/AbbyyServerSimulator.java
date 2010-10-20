package de.uni_goettingen.sub.commons.ocr.abbyy.sever;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbbyyServerSimulator extends Thread {
	protected File hotfolder, output, error, expected, errorExpected, outputExpected;
	public static String HOTFOLDER_NAME = "hotfolder";
	public static String OUTPUT_NAME = "output";
	public static String ERROR_NAME = "error";

	protected Map<String, File> resultsError = new HashMap<String, File>();
	protected Map<String, File> resultsOutput = new HashMap<String, File>();

	protected List<Thread> processes = new ArrayList<Thread>();

	protected static Long wait = 2000l;

	final static Logger logger = LoggerFactory.getLogger(AbbyyServerSimulator.class);

	protected Boolean finish = false;

	public AbbyyServerSimulator(File hotfolder, File output, File expactations) {
		//Hotfolder is the input directory
		this.hotfolder = hotfolder;
		this.output = output;
		this.error = new File(hotfolder.getAbsolutePath() + File.separator + ERROR_NAME);

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

			try {
				checkDirectory(hotfolder);
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				clean();
				interrupt();
			}
			logger.trace("Reached end of loop.");
		}
	}

	protected void removeJob (String name) throws XmlException, IOException {
		List<String> files = TicketTest.parseFilesFromTicket(new File(hotfolder.getAbsolutePath() + File.separator + name + ".xml"));
		for (String str : files) {
			new File(hotfolder.getAbsolutePath() + File.separator + str).delete();
		}
	}

	protected void checkDirectory (File dir) throws XmlException, IOException {
		logger.debug("Checking directory: " + dir.getAbsolutePath());

		List<File> inputContents = Arrays.asList(dir.listFiles());
		if (inputContents.size() < 1) {
			logger.info("No files in input folder.");
			return;
		}

		for (File f : inputContents) {
			if (f.getAbsolutePath().endsWith("xml")) {

				String ticket = f.getName();
				logger.debug("Found XML: " + ticket);
				String name = ticket.substring(ticket.indexOf(".xml"));

				//TODO: Parse ticket here;
				Long wait = calculateWait(name);
				//TODO: Create  new Thread which waits and copies the files afterwards
				Thread serverProcess = createCopyThread(wait, name);
				serverProcess.run();
				processes.add(serverProcess);
			}
		}

		/*
		if (containsTicket(dir)) {

			//Extract the job name
			String name = null;

			//calculate wait time

		}
		*/
	}

	protected Long calculateWait (String name) throws XmlException, IOException {
		List<String> files = TicketTest.parseFilesFromTicket(new File(hotfolder.getAbsolutePath() + File.separator + name + ".xml"));
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
		cleandir(hotfolder);
		cleandir(output);
		cleandir(error);

	}

	protected void cleandir (File dir) {
		for (File f : Arrays.asList(dir.listFiles())) {
			if (f.isDirectory()) {
				cleandir(f);
			} else {
				f.delete();
			}
		}
	}

	private Thread createCopyThread (final Long wait, final String name) {
		return new Thread() {
			@Override
			public void run () {
				try {
					logger.info("Waiting " + wait + " mili seconds");
					sleep(wait);
					//TODO: copy the files to the right location
					//FileUtils.
					if (resultsOutput.containsKey(name)) {

					}
					if (resultsError.containsKey(name)) {

					}

					//Remove the files
					removeJob(name);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
	}

}
