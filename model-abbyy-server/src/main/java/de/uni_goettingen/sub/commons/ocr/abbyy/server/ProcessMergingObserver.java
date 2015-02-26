package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocr.util.merge.Merger;
import de.unigoettingen.sub.commons.ocr.util.merge.MergerProvider;

public class ProcessMergingObserver {

	private final static Logger logger = LoggerFactory.getLogger(ProcessMergingObserver.class);
	private Object monitor = new Object();
	private boolean alreadyBeenHere = false;
	private AbbyyProcess parentProcess;
	private List<AbbyyProcess> subProcesses = new ArrayList<AbbyyProcess>();
	private MergerProvider mergerProvider = new MergerProvider();
	private FileAccess fileAccess = new FileAccess();
	
	// for unit tests
	void setMergerProvider(MergerProvider newProvider) {
		mergerProvider = newProvider;
	}
	void setFileAccess(FileAccess newAccess) {
		fileAccess = newAccess;
	}

	public void setParentProcess(AbbyyProcess process) {
		parentProcess = process;
	}

	public void addSubProcess(AbbyyProcess subProcess) {
		subProcesses.add(subProcess);
	}

	public void update() {
		synchronized (monitor) {	   
			for (AbbyyProcess sub : subProcesses) {
				boolean currentFinished = sub.hasFinished();
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
			
			for (AbbyyProcess sub : subProcesses) {
				if(sub.hasFailed()) {
					logger.error("Could not merge process: " + parentProcess.getName());
					throw new IllegalStateException("Subprocess failed: " + sub.getName());
				}
			}
			mergeAllFormats();				 
		}		
	}

	private void mergeAllFormats() {
		try {
			for (OcrFormat format : parentProcess.getAllOutputFormats()) {
				Merger merger = mergerProvider.createMerger(format);
	
				OutputStream mergedStream = null;
				List<InputStream> streamsToMerge = new ArrayList<InputStream>();
				try {
					List<File> filesToMerge = new ArrayList<File>(); 
					for(AbbyyProcess subProcess : subProcesses) {
						File file = new File(subProcess.getOutputUriForFormat(format));
						filesToMerge.add(file);
						InputStream is = fileAccess.inputStreamForFile(file);
						streamsToMerge.add(is); 
					}
					
					File mergedFile = new File(parentProcess.getOutputUriForFormat(format));
					mergedStream = fileAccess.outputStreamForFile(mergedFile);
					logger.debug("Trying to merge into " + mergedFile + " (" + parentProcess.getName() + ")");
					merger.merge(streamsToMerge, mergedStream);
					logger.debug(mergedFile + " merged successfully (" + parentProcess.getName() + ")");
					
					removeSubProcessResults(filesToMerge);
				} finally {
					closeStreams(streamsToMerge, mergedStream);
				}
			}
		} catch (IOException e) {
			logger.error("Failed to merge all files correctly. (" + parentProcess.getName() + ")", e);
		}
	}
	
	private void removeSubProcessResults(List<File> resultFiles) throws IOException{
		for(File file : resultFiles) {
			fileAccess.deleteFile(file);
		}
	}

	private void closeStreams(List<InputStream> streamsToMerge, OutputStream mergedStream) throws IOException {
			for (InputStream is : streamsToMerge) {
				if (is != null)
					is.close();
			}
			if (mergedStream != null)
				mergedStream.close();
	}

}