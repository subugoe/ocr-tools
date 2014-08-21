package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

	public void setParentProcess(AbbyyOCRProcess process) {
		parentProcess = process;
	}

	/**
	 * Observer can respond via its update method on changes an observable. 
	 * This happens only when registering Observer in Observable.
	 * 
	 * In our sample implementation is in the update method only checks a list of 
	 * observers, if all successfully completed. then all Results should be merged
	 * 
	 */
	public void update() {
		synchronized (monitor) {	   
			for (AbbyyOCRProcess sub : parentProcess.subProcesses) {
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
			for (AbbyyOCRProcess sub : parentProcess.subProcesses) {
				oneFailed = sub.failed;
				if (oneFailed) {
					break;		
				}
			}
			parentProcess.startTime = System.currentTimeMillis();
			String uriTextMD = merge(!oneFailed);
			parentProcess.endTime = System.currentTimeMillis();
			if(!uriTextMD.equals("FAILED")){
				//serializerTextMD(ocrProcessMetadata, uriTextMD + "-textMD.xml");		   				
				removeSubProcessResults(parentProcess.resultfilesForAllSubProcess);
			}
				 
		}		
	}

	private String merge(Boolean noSubProcessfailed) {	
		String uriTextMD = null;
		File abbyyMergedResult = null;
		int i = 0, j =0;
		List<File> fileResults = new ArrayList<File>(); 
		for (OCRFormat f : parentProcess.formatForSubProcess){
			if (!FileMerger.isSegmentable(f)) {
				throw new OCRException("Format " + f.toString()
						+ " isn't mergable!");
			}
			List<File> files = new ArrayList<File>(); 
			for(String sn : parentProcess.subProcessNames){				
				File fileResult,file = new File(parentProcess.outResultUri + "/" + sn + "." + f.toString().toLowerCase());
				//parse only once enough for ProcessMetadata
				if ((f.toString().toLowerCase()).equals("xml") && j == 0) {
					InputStream isDoc = null;
					j++;
					if(noSubProcessfailed){
						try {
							isDoc = new FileInputStream(file);		
						} catch (FileNotFoundException e) {
							logger.error("Error contructing FileInputStream for: "+file.toString() + " (" + parentProcess.getName() + ")", e);
						} finally {
							try {
								if (isDoc != null) {
									isDoc.close();
								}
							} catch (IOException e) {
								logger.error("Could not close Stream. (" + parentProcess.getName() + ")", e);
							}
						}
						
					}
					
				}
				files.add(file); 
				if(i == 0){
					fileResult = new File(parentProcess.outResultUri + "/" + sn + ".xml.result.xml");
					InputStream resultStream = null;
					if(noSubProcessfailed){
						try {
							resultStream = new FileInputStream(fileResult);
						} catch (FileNotFoundException e) {
							logger.error("Error contructing FileInputStream for: "+fileResult.toString() + " (" + parentProcess.getName() + ")", e);
						}
					}					
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
				parentProcess.resultfilesForAllSubProcess.put(abbyyMergedResult, files);	
			}
					
		}
		try {
			if(noSubProcessfailed){
				logger.debug("Waiting... for Merge Proccessing (" + parentProcess.getName() + ")");
				//mergeFiles for Abbyy Result xml.result.xml
				abbyyMergedResult = new File(parentProcess.outResultUri + "/" + parentProcess.getName() + ".xml.result.xml");
				FileMerger.mergeAbbyyXMLResults(fileResults , abbyyMergedResult);
				parentProcess.resultfilesForAllSubProcess.put(abbyyMergedResult, fileResults);
				logger.debug(parentProcess.getName() + ".xml.result.xml" + " MERGED (" + parentProcess.getName() + ")");			
				uriTextMD = parentProcess.outResultUri + "/" + parentProcess.getName();
			}else {
				uriTextMD = "FAILED";
			}
			
		} catch (IOException e) {
			logger.error("ERROR contructing :" +new File(parentProcess.outResultUri + "/" + parentProcess.getName() + ".xml.result.xml").toString() + " (" + parentProcess.getName() + ")", e);
		} catch (XMLStreamException e) {
			logger.error("ERROR in mergeAbbyyXML : (" + parentProcess.getName() + ")", e);
		}		
		return uriTextMD;
	}
	
	protected void removeSubProcessResults(Map<File, List<File>> resultFiles){
		for(List<File> files : resultFiles.values()) {
			for(File file : files) {
				file.delete();
			}
		}
	}


	
}
