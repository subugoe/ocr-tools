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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;


import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.HotfolderProvider;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocr.util.FileMerger;
import de.unigoettingen.sub.commons.ocr.util.FileMerger.MergeException;

/**
 * The Class AbbyyOCRProcess.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public class AbbyyOCRProcess extends AbstractOCRProcess implements Observer,OCRProcess,Serializable,Cloneable,
		Runnable {

	private static final long serialVersionUID = -402196937662439454L;
	private final static Logger logger = LoggerFactory
			.getLogger(AbbyyOCRProcess.class);
	public static final String NAMESPACE = "http://www.abbyy.com/RecognitionServer1.0_xml/XmlResult-schema-v1.xsd";
	protected URI inputDavUri, outputDavUri, errorDavUri;

	private URI errorResultUri;
	private URI outputResultUri;

	private Boolean failed = false;
	private String errorDescription = null;

	private Boolean isResult = false;
	
	private Long startTime = 0L;
	
	private List<AbbyyOCRProcess> subProcesses = new ArrayList<AbbyyOCRProcess>();
	
	private List<String> subProcessNames = new ArrayList<String>();
	
	private Set<OCRFormat> formatForSubProcess = new HashSet<OCRFormat>();
	
	protected Map<File, List<File>> resultfilesForAllSubProcess = new HashMap<File, List<File>>();
	private String outResultUri = null;
	private Observer obs;
	private boolean finished = false;
	protected int splitNumberForSubProcess;
	
	private Long endTime = 0L;
	private Long processTimeResult = 0L;

	protected Hotfolder hotfolder;
	
	private Long maxSize;

	private String processId ;
	static Object monitor = new Object();

	private boolean alreadyBeenHere = false;
	
	//transient protected ConfigParser config;
	protected static String encoding = "UTF8";
	private URI inputTicketUri;
	private URI errorTicketUri;

	transient private HotfolderProvider hotfolderProvider = new HotfolderProvider();
	transient private AbbyyTicket abbyyTicket;
	transient private FileAccess fileAccess = new FileAccess();
	private Properties fileProps;

	// for unit tests
	void setHotfolderProvider(HotfolderProvider newProvider) {
		hotfolderProvider = newProvider;
	}
	void setAbbyyTicket(AbbyyTicket newTicket) {
		abbyyTicket = newTicket;
	}
	void setFileAccess(FileAccess newAccess) {
		fileAccess = newAccess;
	}
	
	public AbbyyOCRProcess() {
		
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
		
		hotfolder = hotfolderProvider.createHotfolder(fileProps.getProperty("serverUrl"), fileProps.getProperty("username"), fileProps.getProperty("password"));
		abbyyTicket = new AbbyyTicket(this);

		processId = java.util.UUID.randomUUID().toString();
		maxSize = Long.parseLong(fileProps.getProperty("maxSize"));

		try {
			URI serverUri = new URI(fileProps.getProperty("serverUrl"));
			inputDavUri = new URI(serverUri + fileProps.getProperty("input") + "/");
			outputDavUri = new URI(serverUri + fileProps.getProperty("output") + "/");
			errorDavUri = new URI(serverUri + fileProps.getProperty("error") + "/");
		} catch (URISyntaxException e) {
			logger.error("Can't setup server uris (" + getName() + ")", e);
			throw new OCRException(e);
		}
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
		
	private void enrichImages() {
		// If we use the static method to create a process some fields aren't
		// set (remoteUri, remoteFileName)
		// TODO: move this into addOcrImage()
		for (OCRImage image : getOcrImages()) {
			AbbyyOCRImage aoi = (AbbyyOCRImage) image;
			String remoteFileName = aoi.getUri().toString();
			remoteFileName = name
					+ "-"
					+ remoteFileName.substring(
							remoteFileName.lastIndexOf("/") + 1,
							remoteFileName.length());
			if (aoi.getRemoteFileName() == null) {
				aoi.setRemoteFileName(remoteFileName);
			}
			URI remoteImageUri, errorImageUri = null;
			try {
				errorImageUri = new URI(errorDavUri.toString() + remoteFileName);
				remoteImageUri = new URI(inputDavUri.toString() + remoteFileName);
			} catch (URISyntaxException e) {
				logger.error("Error contructing remote URL. (" + getName() + ")", e);
				throw new OCRException(e);
			}
			if (aoi.getRemoteUri() == null) {
				aoi.setRemoteUri(remoteImageUri);
				aoi.setErrorUri(errorImageUri);
			}
		}
	}
	
	private void createAndSendTicket() throws URISyntaxException, IOException {
		String ticketFileName = name + ".xml";
		inputTicketUri = new URI(inputDavUri.toString() + ticketFileName);
		errorTicketUri = new URI(errorDavUri.toString() + ticketFileName);
		
		synchronized (monitor) {
			logger.info("Creating AbbyyTicket (" + getName() + ")");
			OutputStream os = hotfolder.createTmpFile(ticketFileName);
			abbyyTicket.write(os, name);
			os.close();
		}
		
		//TODO: remove
//		URI ticketLogPath = new File("/home/dennis/temp/tickets/" + ticketFileName).toURI();
//		hotfolder.copyTmpFile(ticketFileName, ticketLogPath);

		logger.info("Copying ticket to server (" + getName() + ")");
		hotfolder.copyTmpFile(ticketFileName, inputTicketUri);
		
		logger.debug("Delete ticket tmp  (" + getName() + ")");
		hotfolder.deleteTmpFile(ticketFileName);
	}
	
	private void copyResultsFromServer() throws MergeException, IOException {
		for (Map.Entry<OCRFormat, OCROutput> entry : ocrOutputs.entrySet()) {
			final AbbyyOCROutput o = (AbbyyOCROutput) entry.getValue();
			if (o.isSingleFile()) {
				URI remoteUri = o.getRemoteUri();
				URI localUri = o.getUri();
				try {
					logger.debug("Copy from " + remoteUri + " to "
							+ localUri +  "(" + getName() + ")");
					hotfolder.copyFile(remoteUri, localUri);
				} catch (Exception e) {
					logger.warn("Can NOT Copy from " + remoteUri
							+ " to " + localUri + " (" + getName() + ")");
				}
				try {
					if (!new File(localUri).exists()) {
						logger.debug("another try Copy from "
								+ remoteUri + " to " + localUri);
						Thread.sleep(100);
						hotfolder.copyFile(remoteUri, localUri);
					}
				} catch (Exception e) {
					logger.error("Can NOT Copy from " + remoteUri
							+ " to " + localUri + " (" + getName() + ")", e);
					throw new OCRException("Can NOT Copy from "
							+ remoteUri + " to " + localUri, e);
				}
				if (new File(localUri).exists()) {
					logger.debug("Deleting remote file "
							+ remoteUri + " (" + getName() + ")");
					hotfolder.deleteIfExists(remoteUri);
				}
			} else {
				// The results are fragmented, merge them
				mergeResult(entry.getKey(), o);
			}
		}

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		startTime = System.currentTimeMillis();
		
		enrichImages();
		addResultXmlOutput();

		try {
			String resultxmlFileName = name + ".xml.result.xml";
			errorResultUri = new URI(errorDavUri.toString() + resultxmlFileName);
			
			logger.info("Cleaning Server (" + getName() + ")");

			// TODO: ServerCleaner class
			cleanOutputs(getOcrOutputs());
			cleanImages(convertList(getOcrImages()));
			createAndSendTicket();
			
			logger.info("Copying images to server. (" + getName() + ")");
			copyImagesToServer(getOcrImages());

			// Wait for results if needed
			long minWait = getOcrImages().size() * Long.parseLong(fileProps.getProperty("minMillisPerFile"));
			long maxWait = getOcrImages().size() * Long.parseLong(fileProps.getProperty("maxMillisPerFile"));
			logger.info("Waiting " + minWait + " milli seconds for results (" + minWait/1000/60 + " minutes) (" + getName() + ")");
			// TODO: make a collaborator
			Thread.sleep(minWait);

			try {
				
				long restTime = maxWait - minWait;
				logger.info("Waking up, waiting another "
						+ restTime + " milli seconds for results (" + restTime/1000/60 + " minutes) (" + getName() + ")");
				
				waitForResults(restTime);
				
				copyResultsFromServer();
				
				endTime = System.currentTimeMillis();
				processTimeResult = getDuration();
				logger.info("OCR Output file has been created successfully after "+  getDuration() + " milliseconds (" + getName() + ")");
					
			} catch (TimeoutExcetion e) {
				logger.error("Got an timeout while waiting for results (" + getName() + ")", e);
				failed = true;
				
				logger.debug("Trying to delete files of the failed process (" + getName() + ")");
				// Clean server, to reclaim storage
				cleanImages(convertList(getOcrImages()));
				cleanOutputs(getOcrOutputs());
				// Delete the error metadata
				hotfolder.deleteIfExists(inputTicketUri);
				hotfolder.deleteIfExists(errorTicketUri);
				
				// Error Reports
				logger.debug("Trying to parse file" + errorResultUri + " (" + getName() + ")");
				if (hotfolder.exists(errorResultUri)) {
					XmlParser xmlParser = new XmlParser();
					logger.debug("Trying to parse EXISTS file" + errorResultUri + " (" + getName() + ")");
					InputStream is = new FileInputStream(new File(
							errorResultUri.toString()));
					errorDescription = xmlParser.xmlresultErrorparse(is, name);
					//hotfolder.deleteIfExists(errorResultUri);
				}
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
				cleanImages(convertList(getOcrImages()));
				if (!isResult) {
					cleanOutputs(getOcrOutputs());
				}
				//hotfolder.deleteIfExists(errorResultUri);
				hotfolder.deleteIfExists(inputTicketUri);
				if (outputResultUri != null || !isResult) {
					hotfolder.deleteIfExists(outputResultUri);
				}
				if(obs != null && getSegmentation()) {
					setIsFinished();
					obs.update(this, this);
				}		
				logger.info("Process finished  (" + getName() + ")");
			} catch (IOException e) {
				logger.error("Unable to clean up! (" + getName() + ")", e);
			}
		}

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
		for (OCRImage i : getOcrImages()) {
			AbbyyOCRImage aoi = (AbbyyOCRImage) i;
			size += aoi.getSize();
		}
		return size;
	}

	private static List<AbbyyOCRImage> convertList(List<OCRImage> ocrImages) {
		List<AbbyyOCRImage> images = new LinkedList<AbbyyOCRImage>();
		for (OCRImage i : ocrImages) {
			images.add((AbbyyOCRImage) i);
		}
		return images;
	}

	public Boolean isFailed() {
		return failed;
	}

	private void waitForResults(long timeout) throws TimeoutExcetion, InterruptedException,
			IOException, URISyntaxException {
		long start = System.currentTimeMillis();
		
		List<URI> mustBeThereUris = extractFromOutputs();
		
		outerLoop:
		while (true) {

			checkIfError(start, timeout);

			for (URI expectedUri : mustBeThereUris) {
				if (!hotfolder.exists(expectedUri)) {
					logger.debug(expectedUri.toString() + " is not yet available (" + getName() + ")");
					waitALittle();
					continue outerLoop;
				}
			}
			logger.debug("Got all files. (" + getName() + ")");
			break;
		}
		
	}

	private List<URI> extractFromOutputs() {
		List<URI> mustBeThereUris = new ArrayList<URI>();
		for (Map.Entry<OCRFormat, OCROutput> entry : getOcrOutputs().entrySet()) {
			final AbbyyOCROutput output = (AbbyyOCROutput) entry.getValue();
			if (output.isSingleFile()) {
				URI uri = output.getRemoteUri();
				mustBeThereUris.add(uri);
			} else {
				for (URI uri : output.getResultFragments()) {
					mustBeThereUris.add(uri);
				}
			}
		}
		return mustBeThereUris;
	}
	
	private void checkIfError(long start, long timeout) throws TimeoutExcetion, IOException {
		if (System.currentTimeMillis() > start + timeout) {
			isResult = true;
			logger.error("Waited too long - fail (" + getName() + ")");
			throw new TimeoutExcetion();
		}
		if (hotfolder.exists(errorResultUri)) {
			logger.error("Server reported an error in file: " + errorResultUri + " (" + getName() + ")");
			throw new TimeoutExcetion();
		}
	}
	
	private void waitALittle() throws InterruptedException {
		long waitInterval = getOcrImages().size() * Long.parseLong(fileProps.getProperty("checkInterval"));
		logger.debug("Waiting for " + waitInterval
				+ " milli seconds (" + waitInterval/1000/60 + " minutes) (" + getName() + ")");
		Thread.sleep(waitInterval);
	}
	
	private void copyImagesToServer(final List<OCRImage> fileInfos)
			throws InterruptedException, IOException, URISyntaxException {
		for (OCRImage info : fileInfos) {
			AbbyyOCRImage image = (AbbyyOCRImage) info;

			URI fromUri = image.getUri();
			URI toUri = image.getRemoteUri();
			String toUriWothoutPassword = toUri.toString()
					.replace(fileProps.getProperty("password"), "***");
			logger.trace("Copy from " + fromUri.toString() + " to "
					+ toUriWothoutPassword + " (" + getName() + ")");
			try {
				hotfolder.copyFile(fromUri, toUri);
			} catch (IOException e) {
				logger.debug("can not Copy from "
						+ fromUri.toString() + " to " + toUriWothoutPassword + " (" + getName() + ")");
				logger.debug("another try Copy from "
						+ fromUri.toString() + " to " + toUriWothoutPassword + " (" + getName() + ")");
				hotfolder.copyFile(fromUri, toUri);
			}

		}
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
	public void checkServerState() throws URISyntaxException, IOException, IllegalStateException {
		if (maxSize != 0) {

			final URI configServerUri = new URI(fileProps.getProperty("serverUrl"));
			URI inUri = new URI(configServerUri.toString() + fileProps.getProperty("input") + "/");
			URI outUri = new URI(configServerUri.toString() + fileProps.getProperty("output") + "/");
			URI errUri = new URI(configServerUri.toString() + fileProps.getProperty("error") + "/");				
			long totalFileSize = hotfolder.getTotalSize(inUri) 
					+ hotfolder.getTotalSize(outUri) 
					+ hotfolder.getTotalSize(errUri);

			logger.debug("TotalFileSize = " + totalFileSize);
			if (totalFileSize > maxSize) {
				logger.error("Size of files is too much files. Max size of all files is "
						+ maxSize
						+ ". Size of files on server: "
						+ totalFileSize + ".\nExit program. (" + getName() + ")");
				throw new IllegalStateException("Max size of files exceeded");
			}
		} else {
			logger.warn("Server state checking is disabled. (" + getName() + ")");
		}
	}

	private void mergeResult(final OCRFormat format, final AbbyyOCROutput output)
			throws IOException, MergeException {
		if (!FileMerger.isSegmentable(format)) {
			throw new OCRException("Format " + format.toString()
					+ " isn't mergable!");
		}
		// TODO: Use the hotfolder stuff here, since we can redirect IO in this
		// layer
		OutputStream os = new FileOutputStream(new File(output.getUri()));
		
		List<InputStream> inputFiles = new ArrayList<InputStream>();
			
		for (URI u : output.getResultFragments()) {
			inputFiles.add(hotfolder.openInputStream(u));
		}			
		
		FileMerger.mergeStreams(format, inputFiles, os);
	}

	/**
	 * Adds the output for the given format
	 * 
	 * @param format
	 *            the format to add
	 * @param output
	 *            the output, the output settings for the given format
	 * 
	 */
	@Override
	public void addOutput(OCRFormat format, OCROutput output) {
		// Make sure we only add values, not replace existing ones
		if (ocrOutputs == null || ocrOutputs.size() == 0) {
			// We use a LinkedHashMap to get the order of the elements
			// predictable
			ocrOutputs = new LinkedHashMap<OCRFormat, OCROutput>();
		}
		AbbyyOCROutput aoo = new AbbyyOCROutput(output);
		String[] urlParts = output.getUri().toString().split("/");
		if (aoo.getRemoteUri() == null) {
			try {
				aoo.setRemoteUri(new URI(outputDavUri.toString()
						+ urlParts[urlParts.length - 1]));
			} catch (URISyntaxException e) {
				logger.error("Error while setting up URIs (" + getName() + ")");
				throw new OCRException(e);
			}
		}
		long maxMillis = Long.parseLong(fileProps.getProperty("maxMillisPerFile"));
		abbyyTicket.setProcessTimeout((long) getOcrImages().size() * maxMillis);
		aoo.setRemoteLocation(fileProps.getProperty("serverOutputLocation"));
		if (aoo.getRemoteFilename() == null) {
			aoo.setRemoteFilename(urlParts[urlParts.length - 1]);
		}
		ocrOutputs.put(format, aoo);
	}

	// TODO: relevant?
	//@Override
	public void setOcrOutputs(Map<OCRFormat, OCROutput> outputs) {
		for (Map.Entry<OCRFormat, OCROutput> entry : outputs.entrySet()) {
			OCRFormat format = entry.getKey();
			addOutput(format, entry.getValue());
		}
	}

	private synchronized void addResultXmlOutput() {
		Map<OCRFormat, OCROutput> outputs = getOcrOutputs();
		OCRFormat lastKey = getLastKey(outputs);
		AbbyyOCROutput out = (AbbyyOCROutput) outputs.get(lastKey);
		AbbyyOCROutput metadata = new AbbyyOCROutput(out);

		try {
			String resultXmlFolder = fileProps.getProperty("resultXmlFolder");
			String outputFolder = fileProps.getProperty("output");
			// The remote file name
			outputResultUri = new URI(out
					.getRemoteUri()
					.toString()
					.replace(outputFolder, resultXmlFolder)
					.replaceAll(lastKey.toString().toLowerCase(),
							"xml.result.xml"));
			metadata.setRemoteUri(outputResultUri);

			// The local file name
			metadata.setUri(new URI(out
					.getUri()
					.toString()
					.replaceAll(lastKey.toString().toLowerCase(),
							"xml.result.xml")));
		} catch (URISyntaxException e) {
			logger.error("Error while setting up URIs (" + getName() + ")");
			throw new OCRException(e);
		}
		
		addOutput(OCRFormat.METADATA, metadata);
	}

	@SuppressWarnings("unchecked")
	private static <K> K getLastKey(final Map<K, ?> map) {
		// A stupid hack to get the last key, maybe there is something better in
		// commons-lang
		if (!(map instanceof LinkedHashMap)) {
			throw new IllegalArgumentException(
					"Map needs to be of type LinkedHashMap, otherwise the order isn't predictable");
		}
		// We could use the keySet().toArray() method as well, but this isn't
		// type save.
		K lastOutput = null;
		for (K k : map.keySet()) {
			lastOutput = k;
		}
		return lastOutput;
	}

	/**
	 * Removes all outputs of this process from the server, this includes the
	 * output and the error folders. Use this method to clean up after errors
	 * and before sending new ones to avoid GUIDs as output name, since they
	 * aren't predictable.
	 * 
	 * @param outputs
	 *            Map of the outputs to remove
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void cleanOutputs(final Map<OCRFormat, OCROutput> outputs)
			throws IOException {
		for (Map.Entry<OCRFormat, OCROutput> entry : outputs.entrySet()) {
			AbbyyOCROutput out = (AbbyyOCROutput) entry.getValue();
			URI remoteUri = out.getRemoteUri();
			logger.trace("Trying to remove output from output folder: "
					+ remoteUri.toString() + " (" + getName() + ")");
			hotfolder.deleteIfExists(remoteUri);
			// Also remove result fragments if they exist.
			if (!out.isSingleFile()) {
				for (URI u : out.getResultFragments()) {
					logger.trace("Trying to remove output fragment from output folder: "
							+ remoteUri.toString() + " (" + getName() + ")");
					hotfolder.deleteIfExists(u);
				}
			}
		}
	}

	/**
	 * Removes all images of this process from the server, this includes the
	 * input and the error folders. Use this method to clean up after errors and
	 * before sending new ones.
	 * 
	 * @param images
	 *            List of images to remove
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void cleanImages(final List<AbbyyOCRImage> images)
			throws IOException {
		for (AbbyyOCRImage image : images) {
			URI remoteUri = image.getRemoteUri();
			logger.trace("Trying to remove image from input folder: "
					+ remoteUri.toString() + " (" + getName() + ")");
			hotfolder.deleteIfExists(remoteUri);
			URI errorImageUri = image.getErrorUri();
			logger.trace("Trying to remove image from error folder: "
					+ errorImageUri.toString() + " (" + getName() + ")");
			hotfolder.deleteIfExists(errorImageUri);
		}
	}
	
	/**
	 * The Class TimeoutExcetion.
	 */
	public static class TimeoutExcetion extends Exception {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -3002142265497735648L;
		/**
		 * Instantiates a new timeout excetion.
		 */
		public TimeoutExcetion() {
			super();
		}
	}

	protected List<AbbyyOCRProcess> split(){
		int imagesNumber = Integer.parseInt(fileProps.getProperty("imagesNumberForSubprocess"));
		if(getOcrImages().size() <= imagesNumber){
			List<AbbyyOCRProcess> sp = new ArrayList<AbbyyOCRProcess>();
			sp.add(this);
			return sp;
		}else{		
			//rename subProcess ID
			for(AbbyyOCRProcess subProcess : cloneProcess()){	
				subProcess.setProcessId(getProcessId()+ subProcess.getName());
				subProcesses.add(subProcess);		
			}
			return subProcesses;
		}
	}
	
	protected List<AbbyyOCRProcess> cloneProcess(){
		List<AbbyyOCRProcess> cloneProcesses = new ArrayList<AbbyyOCRProcess>();
		Map<OCRFormat, OCROutput> outs = new HashMap<OCRFormat, OCROutput>();
		for (OCRFormat f : getOcrOutputs().keySet()) {
			OCROutput aoo = new AbbyyOCROutput();
			aoo.setUri(getOcrOutputs().get(f).getUri());
			outResultUri = getOcrOutputs().get(f).getlocalOutput();
			aoo.setlocalOutput(outResultUri);
			outs.put(f, aoo);
		}
		int listNumber = 1;
		setSegmentation(true);
		int imagesNumber = Integer.parseInt(fileProps.getProperty("imagesNumberForSubprocess"));
		List<List<OCRImage>> imageChunks = splitingImages(getOcrImages(), imagesNumber);
		for(List<OCRImage> imgs : imageChunks){				
				AbbyyOCRProcess sP = null;
				try {
					sP = (AbbyyOCRProcess) this.clone();
					sP.setObs((Observer)this);
					sP.ocrImages.clear();
					sP.ocrOutputs.clear();
				} catch (CloneNotSupportedException e1) {
					logger.error("Clone Not Supported Exception:  (" + getName() + ")", e1);
					return null;
				}
				sP.setOcrImages(imgs);
				sP.setName(name + "_" + listNumber + "oF" + splitNumberForSubProcess);			
				subProcessNames.add(name + "_" + listNumber + "oF" + splitNumberForSubProcess);
				String localuri = null;
				for (Map.Entry<OCRFormat, OCROutput> entry : outs.entrySet()) {
					OCROutput aoo = new AbbyyOCROutput();
					URI localUri = entry.getValue().getUri();
					localuri = localUri.toString().replace(name, sP.getName());
					try {
						localUri = new URI(localuri);	
					} catch (URISyntaxException e) {
						logger.error("Error contructing localUri URL: "+ localuri + " (" + getName() + ")", e);
					}
					aoo.setUri(localUri);	
					OCRFormat f = entry.getKey();
					sP.addOutput(f, aoo);
					formatForSubProcess.add(f);
				}	
				sP.setTime(new Date().getTime());
			    listNumber++;
			    sP.abbyyTicket = new AbbyyTicket(sP);
				cloneProcesses.add(sP);
		}
		return cloneProcesses;
	}
	
	/** 
	 * The list of images to be shared here imagesNumberForSubprocess (properties 
	 * from config). Here a list of lists will be created.
	 * For example: 
	 * 
	 * + The process is between (250 and 274) images in the list and 
	 * imagesNumberForSubprocess = 50, here are 5 lists created, the final list has 
	 * (50 or 74) images. because the restnumber <50/2 
	 * 
	 * + The process is between (276 and 299) images in the list and imagesNumberForSubprocess = 50, here 6
	 * lists are created, the final list has (26 or 49) images. because the restnumber> = 50/2
	 * 
	 * Advantage in Abbyy with Error tolerance
	 * */
	protected List<List<OCRImage>> splitingImages(List<OCRImage> allImages, int chunkSize){
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
		
		splitNumberForSubProcess = allChunks.size();
		return allChunks;		
	}
	
	
	/**
	 * Observer can respond via its update method on changes an observable. 
	 * This happens only when registering Observer in Observable.
	 * 
	 * In our sample implementation is in the update method only checks a list of 
	 * observers, if all successfully completed. then all Results should be merged
	 * 
	 */
	public void update(Observable o, Object arg) {
		synchronized (monitor) {	   
			for (AbbyyOCRProcess sub : subProcesses) {
				boolean currentFinished = sub.getIsFinished();
				if (!currentFinished){
					processTimeResult = 0L;
					return;
				}
				processTimeResult = processTimeResult + sub.processTimeResult;
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
			startTime = System.currentTimeMillis();
			String uriTextMD = merge(!oneFailed);
			endTime = System.currentTimeMillis();
			if(!uriTextMD.equals("FAILED")){
				//serializerTextMD(ocrProcessMetadata, uriTextMD + "-textMD.xml");		   				
				removeSubProcessResults(resultfilesForAllSubProcess);
			}
				 
		}		
	}

	private String merge(Boolean noSubProcessfailed) {	
		String uriTextMD = null;
		File abbyyMergedResult = null;
		int i = 0, j =0;
		List<File> fileResults = new ArrayList<File>(); 
		for (OCRFormat f : formatForSubProcess){
			if (!FileMerger.isSegmentable(f)) {
				throw new OCRException("Format " + f.toString()
						+ " isn't mergable!");
			}
			List<File> files = new ArrayList<File>(); 
			for(String sn : subProcessNames){				
				File fileResult,file = new File(outResultUri + "/" + sn + "." + f.toString().toLowerCase());
				//parse only once enough for ProcessMetadata
				if ((f.toString().toLowerCase()).equals("xml") && j == 0) {
					InputStream isDoc = null;
					j++;
					if(noSubProcessfailed){
						try {
							isDoc = new FileInputStream(file);		
						} catch (FileNotFoundException e) {
							logger.error("Error contructing FileInputStream for: "+file.toString() + " (" + getName() + ")", e);
						} finally {
							try {
								if (isDoc != null) {
									isDoc.close();
								}
							} catch (IOException e) {
								logger.error("Could not close Stream. (" + getName() + ")", e);
							}
						}
						
					}
					
				}
				files.add(file); 
				if(i == 0){
					fileResult = new File(outResultUri + "/" + sn + ".xml.result.xml");
					InputStream resultStream = null;
					if(noSubProcessfailed){
						try {
							resultStream = new FileInputStream(fileResult);
						} catch (FileNotFoundException e) {
							logger.error("Error contructing FileInputStream for: "+fileResult.toString() + " (" + getName() + ")", e);
						}
					}					
					fileResults.add(fileResult);			
				}		
			}
			i++;
			if(noSubProcessfailed){
				logger.debug("Waiting... for Merge Proccessing (" + getName() + ")");
				//mergeFiles for input format if Supported
				abbyyMergedResult = new File(outResultUri + "/" + name + "." + f.toString().toLowerCase());
				FileMerger.abbyyVersionNumber = fileProps.getProperty("abbyyVersionNumber");
				FileMerger.mergeFiles(f, files, abbyyMergedResult);
				logger.debug(name + "." + f.toString().toLowerCase()+ " MERGED (" + getName() + ")");
				resultfilesForAllSubProcess.put(abbyyMergedResult, files);	
			}
					
		}
		try {
			if(noSubProcessfailed){
				logger.debug("Waiting... for Merge Proccessing (" + getName() + ")");
				//mergeFiles for Abbyy Result xml.result.xml
				abbyyMergedResult = new File(outResultUri + "/" + name + ".xml.result.xml");
				FileMerger.mergeAbbyyXMLResults(fileResults , abbyyMergedResult);
				resultfilesForAllSubProcess.put(abbyyMergedResult, fileResults);
				logger.debug(name + ".xml.result.xml" + " MERGED (" + getName() + ")");			
				uriTextMD = outResultUri + "/" + name;
			}else {
				uriTextMD = "FAILED";
			}
			
		} catch (IOException e) {
			logger.error("ERROR contructing :" +new File(outResultUri + "/" + name + ".xml.result.xml").toString() + " (" + getName() + ")", e);
		} catch (XMLStreamException e) {
			logger.error("ERROR in mergeAbbyyXML : (" + getName() + ")", e);
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
		

	public Observer getObs() {
		return obs;
	}

	public void setObs(Observer obs) {
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
	
}