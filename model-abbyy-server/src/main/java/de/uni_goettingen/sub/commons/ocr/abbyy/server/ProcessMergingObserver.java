package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OcrException;
import de.unigoettingen.sub.commons.ocr.util.FileMerger;

public class ProcessMergingObserver {

	private final static Logger logger = LoggerFactory.getLogger(ProcessMergingObserver.class);
	private Object monitor = new Object();
	private boolean alreadyBeenHere = false;
	private AbbyyOCRProcess parentProcess;
	private List<AbbyyOCRProcess> subProcesses = new ArrayList<AbbyyOCRProcess>();

	public void setParentProcess(AbbyyOCRProcess process) {
		parentProcess = process;
	}

	public void addSubProcess(AbbyyOCRProcess subProcess) {
		subProcesses.add(subProcess);
	}

	public void update() {
		synchronized (monitor) {	   
			for (AbbyyOCRProcess sub : subProcesses) {
				boolean currentFinished = sub.getIsFinished();
				if (!currentFinished) {
					return;
				}
			}
			// only get here when all processes are finished

			// it might happen that a subprocess (not the last one) must wait too 
			// long in the monitor and gets here after the last one, because it 
			// also finds out that all subprocesses have finished
			if (alreadyBeenHere) {
				return;
			}
			alreadyBeenHere = true;
			
			for (AbbyyOCRProcess sub : subProcesses) {
				if(sub.hasFailed()) {
					logger.error("Could not merge process: " + parentProcess.getName());
					throw new IllegalStateException("Subprocess failed: " + sub.getName());
				}
			}
			mergeAllFormats();				 
		}		
	}

	private void mergeAllFormats() {
		for (OcrFormat format : parentProcess.getAllOutputFormats()) {
			if (!FileMerger.isSegmentable(format)) {
				throw new OcrException("Format " + format.toString()
						+ " isn't mergable!");
			}
			List<File> filesToMerge = new ArrayList<File>(); 
			for(AbbyyOCRProcess subProcess : subProcesses) {
				File file = new File(subProcess.getOutputUriForFormat(format));
				filesToMerge.add(file); 
			}
			
			File mergedFile = new File(parentProcess.getOutputUriForFormat(format));
			logger.debug("Trying to merge into " + mergedFile + " (" + parentProcess.getName() + ")");
			FileMerger.abbyyVersionNumber = "v10";
			FileMerger.mergeFiles(format, filesToMerge, mergedFile);
			logger.debug(mergedFile + " merged successfully (" + parentProcess.getName() + ")");
			
			removeSubProcessResults(filesToMerge);
		}
	}
	
	protected void removeSubProcessResults(List<File> resultFiles){
		for(File file : resultFiles) {
			file.delete();
		}
	}
	
}