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

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;

/**
 * The Class AbbyyOCRProcess.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public class AbbyyOCRProcess extends AbstractOCRProcess implements OCRProcess,Serializable,Cloneable,
		Runnable {

	private static final long serialVersionUID = -402196937662439454L;
	private final static Logger logger = LoggerFactory
			.getLogger(AbbyyOCRProcess.class);
	public static final String NAMESPACE = "http://www.abbyy.com/RecognitionServer1.0_xml/XmlResult-schema-v1.xsd";
	protected URI inputDavUri, outputDavUri, errorDavUri;

	private URI errorResultUri;

	Boolean failed = false;
	private String errorDescription = null;

	Long startTime = 0L;	
	
	private transient ProcessMergingObserver obs;
	private boolean finished = false;
	
	Long endTime = 0L;
	Long processTimeResult = 0L;

	private long time;
	private boolean segmentation = false;
	
	private String processId ;
	static Object monitor = new Object();

	protected static String encoding = "UTF8";

	transient AbbyyTicket abbyyTicket;
	transient private FileAccess fileAccess = new FileAccess();
	private Properties fileProps;
	transient private HotfolderManager hotfolderManager;
	private String windowsPathForServer;

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

		long maxMillis = Long.parseLong(fileProps.getProperty("maxMillisPerFile"));
		abbyyTicket.setProcessTimeout((long) ocrImages.size() * maxMillis);

		startTime = System.currentTimeMillis();
		
		try {
			String resultxmlFileName = name + ".xml.result.xml";
			errorResultUri = new URI(errorDavUri.toString() + resultxmlFileName);
			
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
			// TODO: make a collaborator
			Thread.sleep(minWait);

			try {
				
				long restTime = maxWait - minWait;
				logger.info("Waking up, waiting another "
						+ restTime + " milli seconds for results (" + restTime/1000/60 + " minutes) (" + getName() + ")");
				
				long waitInterval = ocrImages.size() * Long.parseLong(fileProps.getProperty("checkInterval"));
				hotfolderManager.waitForResults(restTime, waitInterval, ocrOutputs, errorResultUri);
				
				hotfolderManager.retrieveResults(ocrOutputs);
				
				endTime = System.currentTimeMillis();
				processTimeResult = getDuration();
				logger.info("OCR Output file has been created successfully after "+  getDuration() + " milliseconds (" + getName() + ")");
					
			} catch (TimeoutException e) {
				logger.error("Got an timeout while waiting for results (" + getName() + ")", e);
				failed = true;
				
				logger.debug("Trying to delete files of the failed process (" + getName() + ")");
				// Clean server, to reclaim storage
				hotfolderManager.deleteImages(ocrImages);
				hotfolderManager.deleteOutputs(ocrOutputs);
				// Delete the error metadata
				hotfolderManager.deleteTicket(abbyyTicket);
				
				errorDescription = hotfolderManager.readFromErrorFile(errorResultUri, name);
			}
		} catch (XMLStreamException e) {
			// Set failed here since the results isn't worth much without
			// metadata
			failed = true;
			logger.error("XML can not Parse, Missing Error Reports (" + getName() + "): ", e);
		} catch (IOException e) {
			failed = true;
			logger.error("Error writing files or ticket (" + getName() + ")", e);
		} catch (InterruptedException e) {
			failed = true;
			logger.error(
					"OCR Process was interrupted while coping files to server or waiting for result. (" + getName() + ")",
					e);
		} catch (URISyntaxException e) {
			logger.error("Error seting tmp URI for ticket (" + getName() + ")", e);
			failed = true;
		} catch (OCRException e) {
			logger.error("Error during OCR Process (" + getName() + ")", e);
			failed = true;
		} finally {
			try {	
				hotfolderManager.deleteImages(ocrImages);
				hotfolderManager.deleteOutputs(ocrOutputs);
				//hotfolder.deleteIfExists(errorResultUri);
				hotfolderManager.deleteTicket(abbyyTicket);
				if(obs != null && getSegmentation()) {
					setIsFinished();
					obs.update();
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
		AbbyyOCRImage image = new AbbyyOCRImage();
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
	 * Gets the error description from xmlError.
	 * 
	 * @return the error description
	 */
	public String getErrorDescription() {
		return errorDescription;
	}

	/**
	 * Calculate size of the OCRImages representing this process
	 * 
	 * @return the long, size of all files
	 */
	public Long calculateSize() {
		Long size = 0l;
		for (OCRImage i : ocrImages) {
			AbbyyOCRImage aoi = (AbbyyOCRImage) i;
			size += aoi.getFileSize();
		}
		return size;
	}

	public Boolean isFailed() {
		return failed;
	}
	
	public Long getDuration() {
		return endTime - startTime;
	}

	/**
	 * Check server state. check all three folders since the limits are for the
	 * whole system.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void checkHotfolderState() throws IOException, IllegalStateException {
		long maxSize = Long.parseLong(fileProps.getProperty("maxSize"));
		hotfolderManager.checkIfEnoughSpace(maxSize, inputDavUri, outputDavUri, errorDavUri);
	}

	@Override
	public void addOutput(URI localUri, OCRFormat format) {
		AbbyyOCROutput aoo = new AbbyyOCROutput();
		aoo.setLocalUri(localUri);
		aoo.setFormat(format);
		String[] urlParts = localUri.toString().split("/");
		try {
			aoo.setRemoteUri(new URI(outputDavUri.toString()
					+ urlParts[urlParts.length - 1]));
		} catch (URISyntaxException e) {
			logger.error("Error while setting up URIs (" + getName() + ")");
			throw new IllegalArgumentException(e);
		}
		ocrOutputs.add(aoo);
		
		// TODO: metadata should not be a special case
		if (getOutputUriForFormat(OCRFormat.METADATA) == null) {
			addResultXmlOutput();
		}
	}

	private synchronized void addResultXmlOutput() {
		AbbyyOCROutput out = (AbbyyOCROutput) ocrOutputs.get(0);
		AbbyyOCROutput metadata = new AbbyyOCROutput();

		OCRFormat firstFormatInList = ocrOutputs.get(0).getFormat();
		
		try {
			String resultXmlFolder = fileProps.getProperty("resultXmlFolder");
			String outputFolder = fileProps.getProperty("output");
			// The remote file name
			URI outputResultUri = new URI(out
					.getRemoteUri()
					.toString()
					.replace(outputFolder, resultXmlFolder)
					.replaceAll(firstFormatInList.toString().toLowerCase(),
							"xml.result.xml"));
			metadata.setRemoteUri(outputResultUri);

			// The local file name
			metadata.setLocalUri(new URI(out
					.getLocalUri()
					.toString()
					.replaceAll(firstFormatInList.toString().toLowerCase(),
							"xml.result.xml")));
		} catch (URISyntaxException e) {
			logger.error("Error while setting up URIs (" + getName() + ")");
			throw new OCRException(e);
		}
		metadata.setFormat(OCRFormat.METADATA);
		ocrOutputs.add(metadata);
	}
	
	public void setMerger(ProcessMergingObserver obs) {
		this.obs = obs;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	
	public Boolean getIsFinished() {
		return finished;
	}

	public void setIsFinished() {
		this.finished = true;
	}
	
	public List<String> getRemoteImageNames() {
		List<String> imageNames = new ArrayList<String>();
		for (OCRImage image : ocrImages) {
			imageNames.add(((AbbyyOCRImage)image).getRemoteFileName());
		}
		return imageNames;
	}
	
	public AbbyyOCRProcess createSubProcess() {
		try {
			AbbyyOCRProcess subProcess = (AbbyyOCRProcess)super.clone();
			subProcess.ocrImages = new ArrayList<OCRImage>();
			subProcess.ocrOutputs =  new ArrayList<OCROutput>();
			subProcess.abbyyTicket = new AbbyyTicket(subProcess);
			subProcess.abbyyTicket.setRemoteInputFolder(inputDavUri);
			subProcess.abbyyTicket.setRemoteErrorFolder(errorDavUri);
			return subProcess;
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Could not clone " + AbbyyOCRProcess.class);
		}
	}
	
	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Boolean getSegmentation() {
		return segmentation;
	}

	public void setSegmentation(Boolean segmentaion) {
		this.segmentation = segmentaion;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbbyyOCRProcess) {
			AbbyyOCRProcess process = (AbbyyOCRProcess) obj;
			return this.getProcessId().equals(process.getProcessId());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.getProcessId().hashCode();
	}

}