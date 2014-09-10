package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;
import de.unigoettingen.sub.commons.ocr.util.FileMerger;

public class ProcessMergingObserver {

	private final static Logger logger = LoggerFactory
			.getLogger(ProcessMergingObserver.class);
	static Object monitor = new Object();
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
			merge();				 
		}		
	}

	private void merge() {	
		File abbyyMergedResult = null;
		int i = 0;
		List<File> fileResults = new ArrayList<File>();

		// TODO: metadata should not be a special case
		Set<OCRFormat> formatsWithoutResultXml = new HashSet<OCRFormat>(subProcesses.get(0).getAllOutputFormats());
		formatsWithoutResultXml.remove(OCRFormat.METADATA);
		
		for (OCRFormat f : formatsWithoutResultXml){
			if (!FileMerger.isSegmentable(f)) {
				throw new OCRException("Format " + f.toString()
						+ " isn't mergable!");
			}
			List<File> files = new ArrayList<File>(); 
			for(AbbyyOCRProcess subProcess : subProcesses) {
				File file = new File(subProcess.getOutputUriForFormat(f));
				files.add(file); 
				if(i == 0){
					File fileResult = new File(subProcess.getOutputUriForFormat(OCRFormat.METADATA));
					fileResults.add(fileResult);			
				}		
			}
			i++;
			logger.debug("Waiting... for Merge Proccessing (" + parentProcess.getName() + ")");
			
			abbyyMergedResult = new File(parentProcess.getOutputUriForFormat(f));
			FileMerger.abbyyVersionNumber = "v10";
			FileMerger.mergeFiles(f, files, abbyyMergedResult);
			logger.debug(parentProcess.getName() + "." + f.toString().toLowerCase()+ " MERGED (" + parentProcess.getName() + ")");
			removeSubProcessResults(files);
					
		}
		try {
			logger.debug("Waiting... for Merge Proccessing (" + parentProcess.getName() + ")");
			//mergeFiles for Abbyy Result xml.result.xml
			abbyyMergedResult = new File(parentProcess.getOutputUriForFormat(OCRFormat.METADATA));
			FileMerger.mergeAbbyyXMLResults(fileResults , abbyyMergedResult);
			logger.debug(parentProcess.getName() + ".xml.result.xml" + " MERGED (" + parentProcess.getName() + ")");			
			removeSubProcessResults(fileResults);
			
		} catch (IOException e) {
			logger.error("ERROR contructing result xml file (" + parentProcess.getName() + ")", e);
		} catch (XMLStreamException e) {
			logger.error("ERROR in mergeAbbyyXML : (" + parentProcess.getName() + ")", e);
		}		
	}
	
	protected void removeSubProcessResults(List<File> resultFiles){
		for(File file : resultFiles) {
			file.delete();
		}
	}


	
}
