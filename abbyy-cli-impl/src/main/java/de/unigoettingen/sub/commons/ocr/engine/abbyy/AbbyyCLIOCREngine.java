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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;
import de.unigoettingen.sub.commons.ocr.util.FileMerger;

public class AbbyyCLIOCREngine extends AbstractAbbyyOCREngine implements OCREngine {
	final static Logger logger = LoggerFactory.getLogger(AbbyyCLIOCREngine.class);

	protected final String name = "Abby Finereader Engine";
	protected final String version = "8.1";
	public final static Map<OCRFormat, String> FORMAT_MAPPING;
	public static Map<OCRFormat, List<String>> FORMAT_SETTINGS;
	protected final static List<String> engineSettings;
	public final static Map<Integer, String> ORIENTATION_MAPPINGS;

	public final static Integer SEGSIZE = 300;

	//This needs to be a list to be able to use preconfigured parameters
	protected static List<String> cmd;

	private static AbbyyCLIOCREngine _instance;

	private Boolean deleteTmp = false;

	private Integer segCount = 1;
	private Integer curSeg = 1;

	private Queue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();

	static {
		engineSettings = new ArrayList<String>();
		engineSettings.add("-pi");
		//engineSettings.add("-ido");
		engineSettings.add("-ics");
		engineSettings.add("-skb");

		FORMAT_MAPPING = new HashMap<OCRFormat, String>();
		FORMAT_MAPPING.put(OCRFormat.DOC, "DOC");
		FORMAT_MAPPING.put(OCRFormat.HTML, "HTML");
		FORMAT_MAPPING.put(OCRFormat.XHTML, "HTML");
		FORMAT_MAPPING.put(OCRFormat.PDF, "PDF");
		FORMAT_MAPPING.put(OCRFormat.XML, "XML");
		FORMAT_MAPPING.put(OCRFormat.TXT, "Text");

		FORMAT_SETTINGS = new HashMap<OCRFormat, List<String>>();
		FORMAT_SETTINGS.put(OCRFormat.PDF, new ArrayList<String>());
		FORMAT_SETTINGS.get(OCRFormat.PDF).add("-pem");
		FORMAT_SETTINGS.get(OCRFormat.PDF).add("ImageOnText");
		FORMAT_SETTINGS.get(OCRFormat.PDF).add("-pku");
		FORMAT_SETTINGS.get(OCRFormat.PDF).add("-pfpf");
		FORMAT_SETTINGS.get(OCRFormat.PDF).add("Automatic");
		FORMAT_SETTINGS.get(OCRFormat.PDF).add("-pfpr");
		FORMAT_SETTINGS.get(OCRFormat.PDF).add("300");
		FORMAT_SETTINGS.get(OCRFormat.PDF).add("-pfq");
		FORMAT_SETTINGS.get(OCRFormat.PDF).add("50");

		FORMAT_SETTINGS.put(OCRFormat.XML, new ArrayList<String>());
		FORMAT_SETTINGS.get(OCRFormat.XML).add("-xca");
		FORMAT_SETTINGS.get(OCRFormat.XML).add("-xeca");

		FORMAT_SETTINGS.put(OCRFormat.TXT, new ArrayList<String>());
		FORMAT_SETTINGS.get(OCRFormat.TXT).add("-tpb");
		FORMAT_SETTINGS.get(OCRFormat.TXT).add("-tet");
		FORMAT_SETTINGS.get(OCRFormat.TXT).add("UTF8");

		ORIENTATION_MAPPINGS = new HashMap<Integer, String>();
		ORIENTATION_MAPPINGS.put(OCRImage.Orientation.PORTRAIT.getDegree(), "NoRotation");
		ORIENTATION_MAPPINGS.put(OCRImage.Orientation.LANDSCAPE.getDegree(), "Clockwise");

	}

	private AbbyyCLIOCREngine() {
		super();
		config = (AbstractConfiguration) loadConfig(this.getClass().getSimpleName() + ".properties");
		//config.setListDelimiter(' ');
		cmd = Arrays.asList(config.getString(this.getClass().getSimpleName() + ".cmd").split(" "));
	}

	/**
	 * Gets the single instance of AbbyyCLIOCREngine.
	 * 
	 * @return single instance of AbbyyCLIOCREngine
	 * 
	 */

	public static AbbyyCLIOCREngine getInstance () {
		if (_instance == null) {
			_instance = new AbbyyCLIOCREngine();
		}
		return _instance;
	}

	@Override
	public String getName () {
		return name;
	}

	@Override
	public String getVersion () {
		return version;
	}

	@Override
	public Observable recognize (OCRProcess p) throws OCRException {
		AbbyyCLIOCRProcess process = (AbbyyCLIOCRProcess) p;

		if (process.getOcrImages().size() < SEGSIZE) {
			//TODO: Start Thread here
			queue.add(new Thread(process));
			logger.trace("No segmentation needed for " + String.valueOf(process.getOcrImages()) + " images.");
		} else {
			//TODO: create seperate processes for segments

			logger.info("Segmentation needed for " + String.valueOf(process.getOcrImages()) + " images.");
			//Segment into multiple processes
			segCount = new Double(Math.floor((double) process.getOcrImages().size() / SEGSIZE)).intValue();
			logger.info("Number of segments:" + String.valueOf(segCount));
			List<AbbyyCLIOCRProcess> segments = new ArrayList<AbbyyCLIOCRProcess>();
			//TODO: This should be connected to OCROutput
			Map<OCRFormat, ArrayList<String>> outputSegments = new HashMap<OCRFormat, ArrayList<String>>();

			for (int i = 0; i <= segCount; i++) {
				/*
				//Clone ORCResult here, rewrite the result file based on a pattern (suffix)
				OCROutput segOutput = new AbbyyOCROutput((AbbyyOCROutput) process.getOcrOutputs());
				OCRProcess segProcess = new AbbyyCLIOCRProcess(process);
				//TODO: Include just the images for the segment
				Integer count = 0;
				if (process.getOcrImages().size() >= i * SEGSIZE + SEGSIZE) {
					count = i * SEGSIZE + SEGSIZE;
				} else {
					count = process.getOcrImages().size();
				}
				for (int j = i * SEGSIZE; j < count; j++) {

					//TODO: Rewrite the OCROutput to just represent a segment
					OCROutput oo = new OCROutputParameters(configuration.getInputFiles().get(j));
					//OCROutputParameters params = (OCROutputParameters) configuration.getInputFiles().get(j).clone();
					//Map<OCRFormat, String> segExports = new HashMap<OCRFormat, String>(); 
					for (OCRFormat outputFormat : process.getOcrOutputs().keySet()) {
						if (!FileMerger.isSegmentable(outputFormat)) {
							throw new OCRException("Segmentation for outputformat " + outputFormat.toString() + " not possible");
						}
						String outFile = oo.getUri().toString();
						outFile = outFile + "-" + i + "." + FileUtils.getExtension(outFile);
						oo.addExportFormat(outputFormat, outFile);
						//segExports.put(outputFormat, outFile);

						if (!outputSegments.containsKey(outputFormat)) {
							outputSegments.put(outputFormat, new ArrayList<String>());
							outputSegments.get(outputFormat).add(outFile);
						} else {
							if (!outputSegments.get(outputFormat).contains(outFile)) {
								outputSegments.get(outputFormat).add(outFile);
							}
						}

						//segConf.getDefaultExportFormats().get(outputFormat)
					}
					//params.setFormats(segExports);
					//segConf.addDefaultExportFormat(segExports);
					segConf.addDefaultExportFormats(oo);
					segConf.addFile(oo.getFile(), oo);
				}
				segments.add(segConf);
				*/
			}
			//Recognize the segments 
			for (AbbyyCLIOCRProcess segment : segments) {
				//TODO: Add segment to queue here
				curSeg++;
			}
			//TODO: Merge using the new merge method
		

			//delete tmp files
			if (deleteTmp) {
				for (OCRFormat oef : outputSegments.keySet()) {
					for (String s : outputSegments.get(oef)) {
						new File(s).delete();
					}
				}
			}
		}
		return null;
	}

	/*
	//TODO: Make this work with multiple sub processes
	private void calculateProgress (String output) {
		Pattern p = Pattern.compile(".*?page (\\d*)\\..*");
		Matcher m = p.matcher(output);
		try {
			if (m.matches()) {
				Integer page = Integer.decode(m.group(1));
				if (page > 0) {
					//TODO: take segmentation into account here
					if (getOcrImages().size() > SEGSIZE) {
						page = curSeg * SEGSIZE + page;
					}
					progress = (getOcrImages().size() / 100) * page;
				}
				logger.trace("Recognized " + progress + "%");
			}
		} catch (IllegalStateException e) {
			logger.warn("Failed to get progress: ", e);
		}
	}
	*/

	@Override
	public Observable addOcrProcess (OCRProcess ocrp) {
		return null;
		// TODO Auto-generated method stub

	}

	@Override
	public List<OCRProcess> getOcrProcess () {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public OCRProcess newOcrProcess () {
		return new AbbyyCLIOCRProcess(cmd);
	}


	protected class SegmentedFinereaderCLIOCRProcess implements Runnable {
		protected List<AbbyyCLIOCRProcess> processes = null;

		SegmentedFinereaderCLIOCRProcess(List<AbbyyCLIOCRProcess> processes) {
			this.processes = processes;
		}

		@Override
		public void run () {
			if (processes == null) {
				throw new IllegalStateException("No sub processes given");
			}
			for (AbbyyCLIOCRProcess fcop : processes) {
				//We don't start the threads in parallel mode her to be more predictable in terms of resource usage
				Thread thread = new Thread(fcop);
				thread.start();
				try {
					thread.join();
					//TODO: Merge resutls here
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	@Override
	public Boolean init () {
		// TODO: Check if engine is licencesed
		return true;
	}

	@Override
	public Observable recognize () {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean stop () {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("serial")
	protected static void mergeResultFiles (Map<OCRFormat, AbbyyOCROutput> outputs) {
		Map<OCRFormat, Exception> exceptions = new HashMap<OCRFormat, Exception>();
		for (OCRFormat f : outputs.keySet()) {
			if (FileMerger.isSegmentable(f)) {
				throw new OCRException("Format " + f.toString() + " isn't mergable!");
			}
			final AbbyyOCROutput o = outputs.get(f);
			File file = new File(o.getUri());
			//Convert URI list to File list, the hardly readable way ;-)
			List<File> inputFiles = new ArrayList<File>() {
				{
					for (URI u : o.getResultFragments()) {
						add(new File(u));
					}
				}
			};
			try {
				FileMerger.mergeFiles(f, inputFiles, file);
			} catch (IllegalArgumentException e) {
				exceptions.put(f, e);
			}
		}
		if (!exceptions.isEmpty()) {
			throw new OCRException("Error while merging files.");
		}
	}

}
