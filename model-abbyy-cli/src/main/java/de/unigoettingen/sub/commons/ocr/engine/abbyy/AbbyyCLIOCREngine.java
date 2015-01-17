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

import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;

public final class AbbyyCLIOCREngine extends AbstractAbbyyOCREngine implements OcrEngine {
	final static Logger logger = LoggerFactory.getLogger(AbbyyCLIOCREngine.class);

	public static final Map<OcrFormat, String> FORMAT_MAPPING;
	public static final Map<OcrFormat, List<String>> FORMAT_SETTINGS;
	protected static final List<String> engineSettings;

	public static final Integer SEGSIZE = 300;

	//This needs to be a list to be able to use preconfigured parameters
	protected static List<String> cmd;

	private static AbbyyCLIOCREngine instance;

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

		FORMAT_MAPPING = new HashMap<OcrFormat, String>();
		FORMAT_MAPPING.put(OcrFormat.DOC, "DOC");
		FORMAT_MAPPING.put(OcrFormat.HTML, "HTML");
		FORMAT_MAPPING.put(OcrFormat.XHTML, "HTML");
		FORMAT_MAPPING.put(OcrFormat.PDF, "PDF");
		FORMAT_MAPPING.put(OcrFormat.XML, "XML");
		FORMAT_MAPPING.put(OcrFormat.TXT, "Text");

		FORMAT_SETTINGS = new HashMap<OcrFormat, List<String>>();
		FORMAT_SETTINGS.put(OcrFormat.PDF, new ArrayList<String>());
		FORMAT_SETTINGS.get(OcrFormat.PDF).add("-pem");
		FORMAT_SETTINGS.get(OcrFormat.PDF).add("ImageOnText");
		FORMAT_SETTINGS.get(OcrFormat.PDF).add("-pku");
		FORMAT_SETTINGS.get(OcrFormat.PDF).add("-pfpf");
		FORMAT_SETTINGS.get(OcrFormat.PDF).add("Automatic");
		FORMAT_SETTINGS.get(OcrFormat.PDF).add("-pfpr");
		FORMAT_SETTINGS.get(OcrFormat.PDF).add("300");
		FORMAT_SETTINGS.get(OcrFormat.PDF).add("-pfq");
		FORMAT_SETTINGS.get(OcrFormat.PDF).add("50");

		FORMAT_SETTINGS.put(OcrFormat.XML, new ArrayList<String>());
		FORMAT_SETTINGS.get(OcrFormat.XML).add("-xca");
		FORMAT_SETTINGS.get(OcrFormat.XML).add("-xeca");

		FORMAT_SETTINGS.put(OcrFormat.TXT, new ArrayList<String>());
		FORMAT_SETTINGS.get(OcrFormat.TXT).add("-tpb");
		FORMAT_SETTINGS.get(OcrFormat.TXT).add("-tet");
		FORMAT_SETTINGS.get(OcrFormat.TXT).add("UTF8");


	}

	public AbbyyCLIOCREngine() {
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

	public static synchronized AbbyyCLIOCREngine getInstance () {
		if (instance == null) {
			instance = new AbbyyCLIOCREngine();
		}
		return instance;
	}

	
	public Observable recognize (OcrProcess p) {
		AbbyyCLIOCRProcess process = (AbbyyCLIOCRProcess) p;

		if (process.getNumberOfImages() < SEGSIZE) {
			//TODO: Start Thread here
			queue.add(new Thread(process));
			logger.trace("No segmentation needed for images.");
		} else {
			//TODO: create seperate processes for segments

			logger.info("Segmentation needed for images.");
			//Segment into multiple processes
			segCount = new Double(Math.floor((double) process.getNumberOfImages() / SEGSIZE)).intValue();
			logger.info("Number of segments:" + String.valueOf(segCount));
			List<AbbyyCLIOCRProcess> segments = new ArrayList<AbbyyCLIOCRProcess>();
			//TODO: This should be connected to OcrOutput
			Map<OcrFormat, ArrayList<String>> outputSegments = new HashMap<OcrFormat, ArrayList<String>>();

			for (int i = 0; i <= segCount; i++) {
				/*
				//Clone ORCResult here, rewrite the result file based on a pattern (suffix)
				OcrOutput segOutput = new AbbyyOutput((AbbyyOutput) process.getOcrOutputs());
				OcrProcess segProcess = new AbbyyCLIOCRProcess(process);
				//TODO: Include just the images for the segment
				Integer count = 0;
				if (process.getOcrImages().size() >= i * SEGSIZE + SEGSIZE) {
					count = i * SEGSIZE + SEGSIZE;
				} else {
					count = process.getOcrImages().size();
				}
				for (int j = i * SEGSIZE; j < count; j++) {

					//TODO: Rewrite the OcrOutput to just represent a segment
					OcrOutput oo = new OCROutputParameters(configuration.getInputFiles().get(j));
					//OCROutputParameters params = (OCROutputParameters) configuration.getInputFiles().get(j).clone();
					//Map<OcrFormat, String> segExports = new HashMap<OcrFormat, String>(); 
					for (OcrFormat outputFormat : process.getOcrOutputs().keySet()) {
						if (!FileMerger.isSegmentable(outputFormat)) {
							throw new OcrException("Segmentation for outputformat " + outputFormat.toString() + " not possible");
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
				for (Map.Entry<OcrFormat, ArrayList<String>> entry : outputSegments.entrySet()) {
					for (String s : entry.getValue()) {
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
	public void addOcrProcess (OcrProcess ocrp) {
		ocrProcesses.add(ocrp);
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
					logger.error("Merge failed: ", e);
				}
			}

		}

	}

	@Override
	public void recognize () {
		recognize(ocrProcesses.get(0));
	}


	@Override
	public int getEstimatedDurationInSeconds() {
		return 0;
	}

}
