package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;

public class ProcessSplitter {

	private ProcessMergingObserver mergingObserver = new ProcessMergingObserver();
	
	// for unit tests
	void setProcessMergingObserver(ProcessMergingObserver newObserver) {
		mergingObserver = newObserver;
	}
		
	public List<AbbyyOCRProcess> split(AbbyyOCRProcess process, int splitSize) {
		mergingObserver.setParentProcess(process);
		if (process.getNumberOfImages() <= splitSize) {
			List<AbbyyOCRProcess> sp = new ArrayList<AbbyyOCRProcess>();
			sp.add(process);
			return sp;
		} else {
			process.setSegmentation(true);
			List<AbbyyOCRProcess> subProcesses = createSubProcesses(process, splitSize);
			for(AbbyyOCRProcess subProcess : subProcesses){	
				subProcess.setMerger(mergingObserver);
				mergingObserver.addSubProcess(subProcess);		
			}
			return subProcesses;
		}
	}

	
	private List<AbbyyOCRProcess> createSubProcesses(AbbyyOCRProcess process, int splitSize) {
		List<AbbyyOCRProcess> subProcesses = new ArrayList<AbbyyOCRProcess>();

		List<List<OCRImage>> imageChunks = splitImages(process.getImages(), splitSize);
		int chunkIndex = 1;
		int numberOfChunks = imageChunks.size();
		for(List<OCRImage> chunk : imageChunks){				
			AbbyyOCRProcess subProcess = process.createSubProcess();

			for (OCRImage imageFromChunk : chunk) {
				subProcess.addImage(imageFromChunk.getLocalUri(), imageFromChunk.getFileSize());
			}
			
			String subProcessName = process.getName() + "_" + chunkIndex + "of" + numberOfChunks;
			subProcess.setName(subProcessName);
			subProcess.setProcessId(process.getProcessId() + subProcessName);
			
			addOutputsToSubProcess(subProcess, process);
			
			subProcess.setStartedAt(new Date().getTime());
			subProcesses.add(subProcess);
			
			chunkIndex++;
		}
		return subProcesses;
	}

	private List<List<OCRImage>> splitImages(List<OCRImage> allImages, int chunkSize){
		List<List<OCRImage>> allChunks = new ArrayList<List<OCRImage>>();	
		
		for (int from = 0; from < allImages.size(); from += chunkSize) {
			int to = Math.min(from + chunkSize, allImages.size());
			List<OCRImage> chunk = new ArrayList<OCRImage>(allImages.subList(from, to));
			allChunks.add(chunk);
		}
		
		return allChunks;		
	}

	private void addOutputsToSubProcess(AbbyyOCRProcess subProcess, AbbyyOCRProcess process) {
		for (OCROutput entry : process.getOcrOutputs()) {
			// TODO: metadata should not be a special case
			if (entry.getFormat() == OCRFormat.METADATA) {
				continue;
			}
			OCRFormat outputFormat = entry.getFormat();
			subProcess.addOutput(outputFormat);
		}
	}
	
}
