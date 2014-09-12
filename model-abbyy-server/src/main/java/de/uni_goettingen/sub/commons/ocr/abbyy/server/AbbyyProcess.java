package de.uni_goettingen.sub.commons.ocr.abbyy.server;

/*

 Copyright 2010 SUB Goettingen. All rights reserved.
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.

 */

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.AbstractProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrImage;
import de.uni_goettingen.sub.commons.ocr.api.OcrOutput;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OcrException;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocr.util.Pause;

public class AbbyyProcess extends AbstractProcess implements OcrProcess,Serializable,Cloneable,
		Runnable {

	private static final long serialVersionUID = -402196937662439454L;
	private final static Logger logger = LoggerFactory.getLogger(AbbyyProcess.class);
	protected URI inputDavUri, outputDavUri, errorDavUri, resultXmlDavUri;

	private boolean failed = false;
	
	private transient ProcessMergingObserver processMerger;
	private boolean finished = false;
	
	private long startedAtTimestamp;
	
	private String processId;

	transient private AbbyyTicket abbyyTicket;
	transient private FileAccess fileAccess = new FileAccess();
	private Properties fileProps;
	transient private HotfolderManager hotfolderManager;
	private String windowsPathForServer;
	private transient Pause pause = new Pause();

	// for unit tests
	void setAbbyyTicket(AbbyyTicket newTicket) {
		abbyyTicket = newTicket;
	}
	void setFileAccess(FileAccess newAccess) {
		fileAccess = newAccess;
	}
	void setHotfolderManager(HotfolderManager newManager) {
		hotfolderManager = newManager;
	}
	void setPause(Pause newPause) {
		pause = newPause;
	}
		
	public void initialize(Properties userProps) {

		String propertiesFile = userProps.getProperty("abbyy.config", "gbv-antiqua.properties");
		fileProps = fileAccess.getPropertiesFromFile(propertiesFile);

		String user = userProps.getProperty("user");
		String password = userProps.getProperty("password");
		if (user != null) {
			fileProps.setProperty("username", user);
		}
		if (password != null) {
			fileProps.setProperty("password", password);
		}
		
		hotfolderManager = new HotfolderManager(fileProps.getProperty("serverUrl"), fileProps.getProperty("username"), fileProps.getProperty("password"));
		abbyyTicket = new AbbyyTicket(this);

		processId = java.util.UUID.randomUUID().toString();
		windowsPathForServer = fileProps.getProperty("serverOutputLocation");
		
		try {
			URI serverUri = new URI(fileProps.getProperty("serverUrl"));
			inputDavUri = new URI(serverUri + fileProps.getProperty("input") + "/");
			outputDavUri = new URI(serverUri + fileProps.getProperty("output") + "/");
			errorDavUri = new URI(serverUri + fileProps.getProperty("error") + "/");
			resultXmlDavUri = new URI(serverUri + fileProps.getProperty("resultXmlFolder") + "/");

			abbyyTicket.setRemoteInputFolder(inputDavUri);
			abbyyTicket.setRemoteErrorFolder(errorDavUri);
		} catch (URISyntaxException e) {
			logger.error("Can't setup server uris (" + getName() + ")", e);
			throw new IllegalArgumentException(e);
		}
	}
	
	public String getWindowsPathForServer() {
		return windowsPathForServer;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();

		long maxMillis = Long.parseLong(fileProps.getProperty("maxMillisPerFile"));
		abbyyTicket.setProcessTimeout((long) ocrImages.size() * maxMillis);

		
		try {			
			logger.info("Cleaning Server (" + getName() + ")");
			hotfolderManager.deleteOutputs(ocrOutputs);
			hotfolderManager.deleteImages(ocrImages);
			
			logger.info("Creating and sending Abbyy ticket (" + getName() + ")");
			hotfolderManager.createAndSendTicket(abbyyTicket, name);

			logger.info("Copying images to server. (" + getName() + ")");
			hotfolderManager.copyImagesToHotfolder(ocrImages);

			long minWait = ocrImages.size() * Long.parseLong(fileProps.getProperty("minMillisPerFile"));
			long maxWait = ocrImages.size() * Long.parseLong(fileProps.getProperty("maxMillisPerFile"));
			logger.info("Waiting " + minWait + " milli seconds for results (" + minWait/1000/60 + " minutes) (" + getName() + ")");
			
			pause.forMilliseconds(minWait);

			String resultXmlFileName = name + ".xml.result.xml";
			URI errorResultXmlUri = new URI(errorDavUri.toString() + resultXmlFileName);
				
			long restTime = maxWait - minWait;
			logger.info("Waking up, waiting another "
					+ restTime + " milli seconds for results (" + restTime/1000/60 + " minutes) (" + getName() + ")");
			
			long waitInterval = ocrImages.size() * Long.parseLong(fileProps.getProperty("checkInterval"));
			hotfolderManager.waitForResults(restTime, waitInterval, ocrOutputs, errorResultXmlUri);
			
			hotfolderManager.retrieveResults(ocrOutputs);
			
			long endTime = System.currentTimeMillis();
			logger.info("OCR Output file has been created successfully after " + (endTime - startTime) + " milliseconds (" + getName() + ")");
					
		} catch (TimeoutException e) {
			logger.error("Got an timeout while waiting for results (" + getName() + ")", e);
			failed = true;
			
		} catch (IOException e) {
			failed = true;
			logger.error("Error writing files or ticket (" + getName() + ")", e);
		} catch (URISyntaxException e) {
			logger.error("Error seting tmp URI for ticket (" + getName() + ")", e);
			failed = true;
		} catch (OcrException e) {
			logger.error("Error during OCR Process (" + getName() + ")", e);
			failed = true;
		} finally {
			try {	
				hotfolderManager.deleteImages(ocrImages);
				hotfolderManager.deleteOutputs(ocrOutputs);
				//hotfolder.deleteIfExists(errorResultUri);
				hotfolderManager.deleteTicket(abbyyTicket);
				if(processMerger != null) {
					setIsFinished();
					processMerger.update();
				}		
				logger.info("Process finished  (" + getName() + ")");
			} catch (IOException e) {
				logger.error("Unable to clean up! (" + getName() + ")", e);
			} catch (URISyntaxException e) {
				logger.warn("Could not delete abbyy ticket (" + getName() + ")", e);
			}
		}

	}

	@Override
	public void addImage(URI localUri, long fileSize) {
		AbbyyImage image = new AbbyyImage();
		image.setLocalUri(localUri);
		image.setFileSize(fileSize);
		String localUriString = localUri.toString();
		String remoteFileName = name + "-" + localUriString.substring(
						localUriString.lastIndexOf("/") + 1,
						localUriString.length());
		image.setRemoteFileName(remoteFileName);
		try {
			URI errorImageUri = new URI(errorDavUri.toString() + remoteFileName);
			URI remoteImageUri = new URI(inputDavUri.toString() + remoteFileName);
			image.setRemoteUri(remoteImageUri);
			image.setErrorUri(errorImageUri);
		} catch (URISyntaxException e) {
			logger.error("Error contructing remote URL. (" + getName() + ")", e);
			throw new IllegalArgumentException(e);
		}
		ocrImages.add(image);
	}
	
	
	/**
	 * Calculate size of the OCRImages representing this process
	 * 
	 * @return the long, size of all files
	 */
	public Long calculateSize() {
		Long size = 0l;
		for (OcrImage i : ocrImages) {
			AbbyyImage aoi = (AbbyyImage) i;
			size += aoi.getFileSize();
		}
		return size;
	}

	public boolean hasFailed() {
		return failed;
	}
	
	/**
	 * Check server state. check all three folders since the limits are for the
	 * whole system.
	 * 
	 */
	public void checkHotfolderState() throws IOException, IllegalStateException {
		long maxSize = Long.parseLong(fileProps.getProperty("maxSize"));
		hotfolderManager.checkIfEnoughSpace(maxSize, inputDavUri, outputDavUri, errorDavUri);
	}

	@Override
	public void addOutput(OcrFormat format) {
		AbbyyOutput aoo = new AbbyyOutput();
		aoo.setLocalUri(constructLocalUri(format));
		aoo.setFormat(format);
		try {
			String fileName = name + "." + format.toString().toLowerCase();
			aoo.setRemoteUri(new URI(outputDavUri.toString() + fileName));
		} catch (URISyntaxException e) {
			logger.error("Error while setting up URIs (" + getName() + ")");
			throw new IllegalArgumentException(e);
		}
		ocrOutputs.add(aoo);
		
		// TODO: metadata should not be a special case
		if (getOutputUriForFormat(OcrFormat.METADATA) == null) {
			addResultXmlOutput();
		}
	}

	private void addResultXmlOutput() {
		AbbyyOutput metadata = new AbbyyOutput();
		metadata.setLocalUri(new File(outputDir, name + ".xml.result.xml").toURI());
		try {
			URI outputResultUri = new URI(resultXmlDavUri.toString() + name + ".xml.result.xml");
			metadata.setRemoteUri(outputResultUri);
		} catch (URISyntaxException e) {
			logger.error("Error while setting up URIs (" + getName() + ")");
			throw new OcrException(e);
		}
		metadata.setFormat(OcrFormat.METADATA);
		ocrOutputs.add(metadata);
	}
	
	public void setMerger(ProcessMergingObserver newMerger) {
		processMerger = newMerger;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	
	public boolean getIsFinished() {
		return finished;
	}

	public void setIsFinished() {
		this.finished = true;
	}
	
	public List<String> getRemoteImageNames() {
		List<String> imageNames = new ArrayList<String>();
		for (OcrImage image : ocrImages) {
			imageNames.add(((AbbyyImage)image).getRemoteFileName());
		}
		return imageNames;
	}
	
	public AbbyyProcess createSubProcess() {
		try {
			AbbyyProcess subProcess = (AbbyyProcess)super.clone();
			subProcess.ocrImages = new ArrayList<OcrImage>();
			subProcess.ocrOutputs =  new ArrayList<OcrOutput>();
			subProcess.abbyyTicket = new AbbyyTicket(subProcess);
			subProcess.abbyyTicket.setRemoteInputFolder(inputDavUri);
			subProcess.abbyyTicket.setRemoteErrorFolder(errorDavUri);
			return subProcess;
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Could not clone " + AbbyyProcess.class);
		}
	}
	
	public Long getStartedAt() {
		return startedAtTimestamp;
	}

	public void setStartedAt(long time) {
		this.startedAtTimestamp = time;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbbyyProcess) {
			AbbyyProcess process = (AbbyyProcess) obj;
			return this.getProcessId().equals(process.getProcessId());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.getProcessId().hashCode();
	}

}