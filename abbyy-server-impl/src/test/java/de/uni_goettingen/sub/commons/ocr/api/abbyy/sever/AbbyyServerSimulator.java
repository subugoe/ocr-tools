package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;

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
	protected static String INPUT_NAME = "input";
	protected static String OUTPUT_NAME = "output";
	protected static String ERROR_NAME = "error";

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
		outputExpected = new File(expactations.getAbsolutePath() + File.separator + ERROR_NAME);

		for (File f : Arrays.asList(errorExpected.listFiles())) {
			if (f.isDirectory()) {
				resultsError.put(f.getName(), f);
			}
		}

		for (File f : Arrays.asList(outputExpected.listFiles())) {
			if (f.isDirectory()) {
				resultsError.put(f.getName(), f);
			}
		}

	}

	@Override
	public void run () {
		while (!isInterrupted()) {

			if (input.listFiles().length == 0) {
				finish = true;
			}

			try {
				sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (finish == true) {
				clean();
				interrupt();
			}
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
