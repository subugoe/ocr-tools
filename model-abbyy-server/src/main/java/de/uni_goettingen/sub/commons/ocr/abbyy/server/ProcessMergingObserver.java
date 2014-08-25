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
				if (!currentFinished){
					parentProcess.processTimeResult = 0L;
					return;
				}
				parentProcess.processTimeResult = parentProcess.processTimeResult + sub.processTimeResult;
			}
			// only get here when all processes are finished

			// it might happen that a subprocess (not the last one) must wait too 
			// long in the monitor and gets here after the last one, because it 
			// also finds out that all subprocesses have finished
			if (alreadyBeenHere) {
				return;
			}
			alreadyBeenHere = true;
			
			boolean oneFailed = false;
			for (AbbyyOCRProcess sub : subProcesses) {
				oneFailed = sub.failed;
				if (oneFailed) {
					break;		
				}
			}
			parentProcess.startTime = System.currentTimeMillis();
			merge(!oneFailed);
			parentProcess.endTime = System.currentTimeMillis();
				 
		}		
	}

	private void merge(Boolean noSubProcessfailed) {	
		File abbyyMergedResult = null;
		int i = 0;
		List<File> fileResults = new ArrayList<File>();

		Set<OCRFormat> formatsWithoutResultXml = new HashSet<OCRFormat>(subProcesses.get(0).getOcrOutputs().keySet());
		formatsWithoutResultXml.remove(OCRFormat.METADATA);
		
		for (OCRFormat f : formatsWithoutResultXml){
			if (!FileMerger.isSegmentable(f)) {
				throw new OCRException("Format " + f.toString()
						+ " isn't mergable!");
			}
			List<File> files = new ArrayList<File>(); 
			for(AbbyyOCRProcess subProcess : subProcesses) {
				File file = new File(subProcess.getOcrOutputs().get(f).getUri());
				files.add(file); 
				if(i == 0){
					File fileResult = new File(subProcess.getOcrOutputs().get(OCRFormat.METADATA).getUri());
					fileResults.add(fileResult);			
				}		
			}
			i++;
			if(noSubProcessfailed){
				logger.debug("Waiting... for Merge Proccessing (" + parentProcess.getName() + ")");
				//mergeFiles for input format if Supported
				abbyyMergedResult = new File(parentProcess.outResultUri + "/" + parentProcess.getName() + "." + f.toString().toLowerCase());
				FileMerger.abbyyVersionNumber = "v10";
				FileMerger.mergeFiles(f, files, abbyyMergedResult);
				logger.debug(parentProcess.getName() + "." + f.toString().toLowerCase()+ " MERGED (" + parentProcess.getName() + ")");
				removeSubProcessResults(files);
			}
					
		}
		try {
			if(noSubProcessfailed){
				logger.debug("Waiting... for Merge Proccessing (" + parentProcess.getName() + ")");
				//mergeFiles for Abbyy Result xml.result.xml
				abbyyMergedResult = new File(parentProcess.outResultUri + "/" + parentProcess.getName() + ".xml.result.xml");
				FileMerger.mergeAbbyyXMLResults(fileResults , abbyyMergedResult);
				logger.debug(parentProcess.getName() + ".xml.result.xml" + " MERGED (" + parentProcess.getName() + ")");			
				removeSubProcessResults(fileResults);
			}
			
		} catch (IOException e) {
			logger.error("ERROR contructing :" +new File(parentProcess.outResultUri + "/" + parentProcess.getName() + ".xml.result.xml").toString() + " (" + parentProcess.getName() + ")", e);
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
