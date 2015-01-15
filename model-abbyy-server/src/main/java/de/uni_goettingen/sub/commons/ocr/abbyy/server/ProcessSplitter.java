package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrImage;
import de.uni_goettingen.sub.commons.ocr.api.OcrOutput;

public class ProcessSplitter {

	private ProcessMergingObserver mergingObserver = new ProcessMergingObserver();
	
	// for unit tests
	void setProcessMergingObserver(ProcessMergingObserver newObserver) {
		mergingObserver = newObserver;
	}
		
	public List<AbbyyProcess> split(AbbyyProcess process, int splitSize) {
		mergingObserver.setParentProcess(process);
		if (process.getNumberOfImages() <= splitSize) {
			List<AbbyyProcess> sp = new ArrayList<AbbyyProcess>();
			sp.add(process);
			return sp;
		} else {
			List<AbbyyProcess> subProcesses = createSubProcesses(process, splitSize);
			for(AbbyyProcess subProcess : subProcesses){	
				subProcess.setMerger(mergingObserver);
				mergingObserver.addSubProcess(subProcess);		
			}
			return subProcesses;
		}
	}

	
	private List<AbbyyProcess> createSubProcesses(AbbyyProcess process, int splitSize) {
		List<AbbyyProcess> subProcesses = new ArrayList<AbbyyProcess>();

		List<List<OcrImage>> imageChunks = splitImages(process.getImages(), splitSize);
		int chunkIndex = 1;
		int numberOfChunks = imageChunks.size();
		for(List<OcrImage> chunk : imageChunks){				
			AbbyyProcess subProcess = process.createSubProcess();

			for (OcrImage imageFromChunk : chunk) {
				subProcess.addImage(imageFromChunk.getLocalUri());
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

	private List<List<OcrImage>> splitImages(List<OcrImage> allImages, int chunkSize){
		List<List<OcrImage>> allChunks = new ArrayList<List<OcrImage>>();	
		
		for (int from = 0; from < allImages.size(); from += chunkSize) {
			int to = Math.min(from + chunkSize, allImages.size());
			List<OcrImage> chunk = new ArrayList<OcrImage>(allImages.subList(from, to));
			allChunks.add(chunk);
		}
		
		return allChunks;		
	}

	private void addOutputsToSubProcess(AbbyyProcess subProcess, AbbyyProcess process) {
		for (OcrFormat outputFormat : process.getAllOutputFormats()) {
			subProcess.addOutput(outputFormat);
		}
	}
	
}
