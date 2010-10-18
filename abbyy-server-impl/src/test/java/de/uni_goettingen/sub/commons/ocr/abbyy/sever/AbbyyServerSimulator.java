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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbbyyServerSimulator extends Thread {
	protected File hotfolder, input, output, error, expected, errorExpected, outputExpected;
	public static String INPUT_NAME = "input";
	public static String OUTPUT_NAME = "output";
	public static String ERROR_NAME = "error";

	protected Map<String, File> resultsError = new HashMap<String, File>();
	protected Map<String, File> resultsOutput = new HashMap<String, File>();

	protected static Long wait = 2000l;

	final static Logger logger = LoggerFactory.getLogger(AbbyyServerSimulator.class);

	protected Boolean finish = false;

	public AbbyyServerSimulator(File hotfolder, File expactations) {
		input = new File(hotfolder.getAbsolutePath() + File.separator + INPUT_NAME);
		output = new File(hotfolder.getAbsolutePath() + File.separator + OUTPUT_NAME);
		error = new File(hotfolder.getAbsolutePath() + File.separator + ERROR_NAME);

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
				resultsError.put(f.getName(), f);
				logger.debug("Adding " + f.getName() + " as expected result.");
			}
		}

	}

	@Override
	public void run () {
		while (!isInterrupted()) {
			try {
				sleep(500);
			} catch (InterruptedException e) {
				interrupt();
			}

			if (finish == true) {
				clean();
				interrupt();
			}
			logger.trace("Reached end of loop.");
		}

	}

	protected void removeJob (String name) throws XmlException, IOException {
		List<String> files = TicketTest.parseFilesFromTicket(new File(input.getAbsolutePath() + File.separator + name + ".xml"));
		for (String str : files) {
			new File(input.getAbsolutePath() + File.separator + str).delete();
		}
	}

	protected void checkDirectory (File dir) throws XmlException, IOException {
		for (File f : Arrays.asList(dir.listFiles())) {
			if (f.getAbsolutePath().endsWith("xml")) {

				String name = f.getName();
				name = name.substring(name.indexOf(".xml"));
			}
		}

		if (containsTicket(dir)) {

			//Extract the job name
			String name = null;

			//calculate wait time
			Long wait = calculateWait(name);

			//Remove the files
			removeJob(name);

		}
	}

	protected Long calculateWait (String name) throws XmlException, IOException {
		List<String> files = TicketTest.parseFilesFromTicket(new File(input.getAbsolutePath() + File.separator + name + ".xml"));
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
		cleandir(input);
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

}
