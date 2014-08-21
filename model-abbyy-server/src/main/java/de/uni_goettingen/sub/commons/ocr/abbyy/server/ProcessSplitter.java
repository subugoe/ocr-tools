package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;

public class ProcessSplitter {

	//private List<AbbyyOCRProcess> subProcesses = new ArrayList<AbbyyOCRProcess>();
	private final static Logger logger = LoggerFactory
			.getLogger(ProcessSplitter.class);
	private ProcessMergingObserver mergingObserver = new ProcessMergingObserver();
	
//	public List<AbbyyOCRProcess> getSubProcesses() {
//		return subProcesses;
//	}
	
	public List<AbbyyOCRProcess> split(AbbyyOCRProcess process, int splitSize) {
		mergingObserver.setParentProcess(process);
		if(process.getOcrImages().size() <= splitSize){
			List<AbbyyOCRProcess> sp = new ArrayList<AbbyyOCRProcess>();
			sp.add(process);
			return sp;
		}else{		
			//rename subProcess ID
			for(AbbyyOCRProcess subProcess : cloneProcess(process, splitSize)){	
				subProcess.setProcessId(process.getProcessId()+ subProcess.getName());
				process.subProcesses.add(subProcess);		
			}
			return process.subProcesses;
		}
	}

	
	private List<AbbyyOCRProcess> cloneProcess(AbbyyOCRProcess process, int splitSize){
		List<AbbyyOCRProcess> cloneProcesses = new ArrayList<AbbyyOCRProcess>();
		Map<OCRFormat, OCROutput> outs = new HashMap<OCRFormat, OCROutput>();
		for (OCRFormat f : process.getOcrOutputs().keySet()) {
			OCROutput aoo = new AbbyyOCROutput();
			aoo.setUri(process.getOcrOutputs().get(f).getUri());
			process.outResultUri = process.getOcrOutputs().get(f).getlocalOutput();
			aoo.setlocalOutput(process.outResultUri);
			outs.put(f, aoo);
		}
		int listNumber = 1;
		process.setSegmentation(true);
		int imagesNumber = splitSize;
		List<List<OCRImage>> imageChunks = splitingImages(process.getOcrImages(), imagesNumber);
		int splitNumberForSubProcess = imageChunks.size();
		for(List<OCRImage> imgs : imageChunks){				
				AbbyyOCRProcess sP = null;
				try {
					sP = (AbbyyOCRProcess) process.clone();
					sP.setObs(mergingObserver);
					sP.getOcrImages().clear();
					sP.getOcrOutputs().clear();
				} catch (CloneNotSupportedException e1) {
					logger.error("Clone Not Supported Exception: ", e1);
					return null;
				}
				sP.setOcrImages(imgs);
				sP.setName(process.getName() + "_" + listNumber + "oF" + splitNumberForSubProcess);			
				process.subProcessNames.add(process.getName() + "_" + listNumber + "oF" + splitNumberForSubProcess);
				String localuri = null;
				for (Map.Entry<OCRFormat, OCROutput> entry : outs.entrySet()) {
					OCROutput aoo = new AbbyyOCROutput();
					URI localUri = entry.getValue().getUri();
					localuri = localUri.toString().replace(process.getName(), sP.getName());
					try {
						localUri = new URI(localuri);	
					} catch (URISyntaxException e) {
						logger.error("Error contructing localUri URL: "+ localuri + " (" + process.getName() + ")", e);
					}
					aoo.setUri(localUri);	
					OCRFormat f = entry.getKey();
					sP.addOutput(f, aoo);
					process.formatForSubProcess.add(f);
				}	
				sP.setTime(new Date().getTime());
			    listNumber++;
			    sP.abbyyTicket = new AbbyyTicket(sP);
			    sP.abbyyTicket.setRemoteInputFolder(process.inputDavUri);
			    sP.abbyyTicket.setRemoteErrorFolder(process.errorDavUri);
				cloneProcesses.add(sP);
		}
		return cloneProcesses;
	}
	
	private List<List<OCRImage>> splitingImages(List<OCRImage> allImages, int chunkSize){
		List<List<OCRImage>> allChunks = new ArrayList<List<OCRImage>>();		
		int fullChunks = allImages.size() / chunkSize;
		int restNumber = allImages.size() % chunkSize;
		
		int chunkCounter = 1;
		int imageCounter = 0;		
		List<OCRImage> oneChunk = new ArrayList<OCRImage>();
		for(OCRImage o : allImages){
			imageCounter++;
			if(imageCounter <= chunkSize  && chunkCounter <= fullChunks){					
				oneChunk.add(o);									
				if(chunkSize == imageCounter){
					allChunks.add(oneChunk);
					oneChunk = new ArrayList<OCRImage>();
					imageCounter = 0;
					chunkCounter++;
				}				
			}else{				
				oneChunk.add(o);				
				if(imageCounter == restNumber) {
					allChunks.add(oneChunk);
				}
			}							
		}
		
		return allChunks;		
	}

}
