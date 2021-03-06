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
import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocr.util.merge.Merger;
import de.unigoettingen.sub.commons.ocr.util.merge.MergerProvider;

public class ProcessMergingObserver {

	private final static Logger logger = LoggerFactory.getLogger(ProcessMergingObserver.class);
	private AbbyyProcess parentProcess;
	private List<AbbyyProcess> subProcesses = new ArrayList<AbbyyProcess>();
	private MergerProvider mergerProvider = new MergerProvider();
	private BeanProvider beanProvider = new BeanProvider();
	private FileAccess fileAccess;

	// for unit tests
	void setBeanProvider(BeanProvider newProvider) {
		beanProvider = newProvider;
	}
	void setMergerProvider(MergerProvider newProvider) {
		mergerProvider = newProvider;
	}

	public void setParentProcess(AbbyyProcess process) {
		parentProcess = process;
	}

	public void addSubProcess(AbbyyProcess subProcess) {
		subProcesses.add(subProcess);
	}

	public synchronized void update(AbbyyProcess finishedSubProcess) {
			finishedSubProcess.setFinished();
			for (AbbyyProcess sub : subProcesses) {
				boolean currentFinished = sub.hasFinished();
				if (!currentFinished) {
					return;
				}
			}
			// only get here when all subprocesses are finished
			
			for (AbbyyProcess sub : subProcesses) {
				if(sub.hasFailed()) {
					logger.error("Could not merge process: " + parentProcess.getName());
					throw new IllegalStateException("Subprocess failed: " + sub.getName());
				}
			}
			fileAccess = beanProvider.getFileAccess();
			mergeAllFormats();				 
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
					merger.mergeBuffered(streamsToMerge, mergedStream);
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