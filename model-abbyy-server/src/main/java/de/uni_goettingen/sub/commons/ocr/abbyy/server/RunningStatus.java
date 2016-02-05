package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;

public class RunningStatus {

	private final static Logger logger = LoggerFactory.getLogger(RunningStatus.class);

	private BeanProvider beanProvider = new BeanProvider();
	private FileAccess fileAccess = beanProvider.getFileAccess();
	private String prefix = "ocr_running_";
	
	public boolean isOn(String processName) {
		return fileAccess.fileExists(statusFileFor(processName));
	}

	public void switchOn(String processName) {
		try {
			fileAccess.createEmptyFile(statusFileFor(processName));
		} catch (IOException e) {
			logger.error("Could not create status file for process " + processName, e);
		}
		
	}

	public void switchOff(String processName) {
		try {
			fileAccess.deleteFile(statusFileFor(processName));
		} catch (IOException e) {
			logger.error("Could not delete status file for process " + processName, e);
		}
		
	}
	
	private File statusFileFor(String processName) {
		File tempFolder = new File(System.getProperty("java.io.tmpdir"));
		return new File(tempFolder, prefix + processName);
	}

}
