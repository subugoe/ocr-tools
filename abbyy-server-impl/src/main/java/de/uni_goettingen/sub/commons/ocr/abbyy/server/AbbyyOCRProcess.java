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

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.NotImplementedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.abbyy.fineReaderXml.fineReader6SchemaV1.DocumentDocument;
import com.abbyy.fineReaderXml.fineReader6SchemaV1.DocumentDocument.Document;
import com.abbyy.recognitionServer10Xml.xmlResultSchemaV1.XmlResultDocument;
import com.abbyy.recognitionServer10Xml.xmlResultSchemaV1.XmlResultDocument.XmlResult;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.AbstractHotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;

import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;
import de.unigoettingen.sub.commons.ocr.util.FileMerger;
import de.unigoettingen.sub.commons.ocr.util.FileMerger.MergeException;

/**
 * The Class AbbyyOCRProcess.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public class AbbyyOCRProcess extends AbbyyTicket implements Observer,OCRProcess,Serializable,Cloneable,
		Runnable {

	
	private static final long serialVersionUID = -402196937662439454L;
	// The Constant logger.
	private final static Logger logger = LoggerFactory
			.getLogger(AbbyyOCRProcess.class);
	public static final String NAMESPACE = "http://www.abbyy.com/RecognitionServer1.0_xml/XmlResult-schema-v1.xsd";
	// TODO: Use static fields from the engine class here.
	
	protected URI serverUri;
	
	// The folder URLs.
	protected URI inputUri, outputUri, errorUri;

	private URI errorTicketUri;
	private URI errorResultUri;
	private URI outputResultUri;
	private URI ticketUri;

	// State variables.
	// Set if process is failed
	private Boolean failed = false;
	protected String errorDescription = null;
	// Set if process is done
	private Boolean done = true;

	private Boolean isResult = false;
	
	private Long startTime = 0L;
	
	private List<AbbyyOCRProcess> subProcesses = new ArrayList<AbbyyOCRProcess>();
	
	private List<String> subProcessNames = new ArrayList<String>();
	
	private Set<OCRFormat> formatForSubProcess = new HashSet<OCRFormat>();
	
	protected Map<File, List<File>> resultfilesForAllSubProcess = new HashMap<File, List<File>>();
	//localOutput from CLI 
	private String outResultUri = null;
	private Observer obs;
	private boolean finished = false;
	//Number of SubProcesses
	protected int splitNumberForSubProcess;
	
	private Long endTime = 0L;
	private Long processTimeResult = 0L;

	protected Hotfolder hotfolder;
	transient protected XmlParser xmlParser;
	transient protected AbbyySerializerTextMD abbyySerializerTextMD;
	
	protected XmlResultDocument xmlResultDocument;
	protected DocumentDocument xmlExportDocument;
	protected XmlResult xmlResultEngine;
	protected Document xmlExport;
	private Long maxSize;

	private Long maxFiles;
	private Long totalFileCount;
	//ID Number for AbbyyOCRProcess
	private String processId ;
	private Long totalFileSize = 0l;
	static Object monitor = new Object();


	protected AbbyyOCRProcess(ConfigParser config) {
		super();
		this.config = config;
		ocrProcessMetadata = new AbbyyOCRProcessMetadata();
		hotfolder = AbstractHotfolder.getHotfolder(config.hotfolderClass,
				config);
		init();
	}
	
	protected AbbyyOCRProcess(OCRProcess process, ConfigParser config) {
		super(process);
		this.config = config;
		hotfolder = AbstractHotfolder.getHotfolder(config.hotfolderClass,
				config);
		init();
		throw new NotImplementedException("This constructor isn't finished");
	}

	private void init() {
		if (!config.isParsed()) {
			throw new IllegalStateException();
		}
		processId = java.util.UUID.randomUUID().toString();
		//processId = config.getId_Process();
		// Set constraints
		maxSize = config.getMaxSize();
		maxFiles = config.getMaxFiles();

		try {
			serverUri = new URI(config.getServerURL());
			inputUri = new URI(serverUri + config.getInput() + "/");
			outputUri = new URI(serverUri + config.getOutput() + "/");
			errorUri = new URI(serverUri + config.getError() + "/");
		} catch (URISyntaxException e) {
			logger.error("Can't setup server uris", e);
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		startTime = System.currentTimeMillis();

		if (hotfolder == null) {
			throw new IllegalStateException("No Hotfolder set!");
		}
		
		if (encoding.equals("UTF8")) {
			ocrProcessMetadata.setEncoding("UTF-8");
		} else {
			ocrProcessMetadata.setEncoding(encoding);
		}
		if (langs != null) {
			setLanguageforMetadata(langs);
		}

		// If we use the static method to create a process some fields aren't
		// set correctly (remoteUri, remoteFileName)
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
				errorImageUri = new URI(this.errorUri.toString() + remoteFileName);
				remoteImageUri = new URI(inputUri.toString() + remoteFileName);
			} catch (URISyntaxException e) {
				logger.error("Error contructing remote URL.");
				throw new OCRException(e);
			}
			if (aoi.getRemoteUri() == null) {
				aoi.setRemoteUri(remoteImageUri);
				aoi.setErrorUri(errorImageUri);
			}

		}

		// Add the metadata descriptor (result file) to the outputs
		addMetadataOutput();

		try {
			// Set the file names and URIs
			String tmpTicket = name + ".xml";
			ticketUri = new URI(inputUri.toString() + tmpTicket);
			errorTicketUri = new URI(errorUri.toString() + tmpTicket);
			errorResultUri = new URI(errorUri.toString() + tmpTicket
					+ config.reportSuffix);

			if (config.dryRun) {
				// No interaction with the server and IO wanted, just return
				// here
				logger.info("Process is in dry run mode, don't write anything");
				return;
			}
			// Create ticket, copy files and ticket
			// Write ticket to temp file
			synchronized (monitor) {
				logger.debug("Creating AbbyyTicket for " + name);
				OutputStream os = hotfolder.createTmpFile(tmpTicket);
				write(os, name);
				os.close();
			}

			logger.debug("Cleaning Server");

			// Clean the server here to avoid GUIDs as filenames
			cleanOutputs(getOcrOutputs());
			// Remove all files that are part of this process, they
			// shouldn't
			// exist yet.
			cleanImages(convertList(getOcrImages()));


			// Copy the ticket
			logger.debug("Copying tickt to server");
			hotfolder.copyTmpFile(tmpTicket, ticketUri);
			// delete ticket tmp
			logger.debug("Delete ticket tmp ");
			hotfolder.deleteTmpFile(tmpTicket);
			// Copy the files
			logger.debug("Coping imges to server.");
			copyFilesToServer(getOcrImages());

			if (config.copyOnly) {
				logger.info("Process is in copy only mode, don't wait for results");
				return;
			}

			// Wait for results if needed
			Long minWait = getOcrImages().size() * config.minMillisPerFile;
			Long maxWait = getOcrImages().size() * config.maxMillisPerFile;
			logger.info("Waiting " + minWait + " milli seconds for results");
			Thread.sleep(minWait);

			try {
				Map<OCRFormat, OCROutput> outputs = getOcrOutputs();
				logger.debug("Waking up, waiting another "
						+ (maxWait - minWait) + " milli seconds for results");
				if (waitForResults(outputs, maxWait)) {
					//for Serializer
					List<URI> listOfLocalURI = new ArrayList<URI>();
					// Everything should be ok, get the files
					for (Map.Entry<OCRFormat, OCROutput> entry : outputs.entrySet()) {
						final AbbyyOCROutput o = (AbbyyOCROutput) entry.getValue();
						if (o.isSingleFile()) {
							URI remoteUri = o.getRemoteUri();
							URI localUri = o.getUri();
							//for Serializer
							listOfLocalURI.add(localUri);
							try {
								logger.debug("Copy from " + remoteUri + " to "
										+ localUri);
								hotfolder.copyFile(remoteUri, localUri);
							} catch (Exception e) {
								logger.debug("Can NOT Copy from " + remoteUri
										+ " to " + localUri);
							}
							try {
								if (!new File(localUri).exists()) {
									logger.debug("another try Copy from "
											+ remoteUri + " to " + localUri);
									Thread.sleep(2000);
									hotfolder.copyFile(remoteUri, localUri);
								}
							} catch (Exception e) {
								logger.debug("Can NOT Copy from " + remoteUri
										+ " to " + localUri, e);
								throw new OCRException("Can NOT Copy from "
										+ remoteUri + " to " + localUri, e);
							}
							if (new File(localUri).exists()) {
								logger.debug("Deleting remote file "
										+ remoteUri);
								hotfolder.deleteIfExists(remoteUri);
							}
						} else {
							// The results are fragmented, merge them
							mergeResult(entry.getKey(), o);
						}
					}
					endTime = System.currentTimeMillis();
					processTimeResult = getDuration();
					ocrProcessMetadata.setDuration(processTimeResult);
					logger.debug("OCR Output file for " +name + " has been created successfully after "+  getDuration() + " milliseconds");
					setOcrProcessMetadata(ocrProcessMetadata);
					//Serializer
					if(!getSegmentation()){
						for (URI l : listOfLocalURI) {
							if ((l.toString())
									.endsWith("xml" + config.reportSuffix)) {
								InputStream resultStream = new FileInputStream(new File(l));
								((AbbyyOCRProcessMetadata) ocrProcessMetadata)
										.parseXmlResult(resultStream);
							}
							if ((l.toString()).endsWith(name + ".xml")) {
								InputStream isDoc = new FileInputStream(new File(l));
								((AbbyyOCRProcessMetadata) ocrProcessMetadata)
										.parseXmlExport(isDoc);
							}
						}
						serializerTextMD(ocrProcessMetadata, name);
					}
						
					// end of serializer
				} else {
					failed = true;
				}
			} catch (TimeoutExcetion e) {
				logger.error("Got an timeout while waiting for results", e);
				failed = true;
				
				logger.debug("Trying to delete files of the failed process");
				// Clean server, to reclaim storage
				cleanImages(convertList(getOcrImages()));
				cleanOutputs(getOcrOutputs());
				// Delete the error metadata
				hotfolder.deleteIfExists(ticketUri);
				hotfolder.deleteIfExists(errorTicketUri);
				
				// Error Reports
				logger.debug("Trying to parse file" + errorResultUri);
				if (hotfolder.exists(errorResultUri)) {
					xmlParser = new XmlParser();
					logger.debug("Trying to parse EXISTS file" + errorResultUri);
					InputStream is = new FileInputStream(new File(
							errorResultUri.toString()));
					errorDescription = xmlParser.xmlresultErrorparse(is, name);
					hotfolder.deleteIfExists(errorResultUri);
				}
			}
		} catch (XMLStreamException e) {
			// Set failed here since the results isn't worth much without
			// metadata
			failed = true;
			logger.error("XML can not Parse, Missing Error Reports for " + name
					+ " : ", e);
		} catch (IOException e) {
			failed = true;
			logger.error("Error writing files or ticket", e);
		} catch (InterruptedException e) {
			failed = true;
			logger.error(
					"OCR Process was interrupted while coping files to server or waiting for result.",
					e);
		} catch (URISyntaxException e) {
			logger.error("Error seting tmp URI for ticket", e);
			failed = true;
		} catch (OCRException e) {
			logger.error("Error during OCR Process", e);
			failed = true;
		} finally {
			xmlParser = null;
			try {	
				cleanImages(convertList(getOcrImages()));
				if (!isResult) {
					cleanOutputs(getOcrOutputs());
				}
				hotfolder.deleteIfExists(errorResultUri);
				hotfolder.deleteIfExists(ticketUri);
				if (outputResultUri != null || !isResult) {
					hotfolder.deleteIfExists(outputResultUri);
				}
				if(obs != null && getSegmentation()) {
					setIsFinished();
					obs.update(this, this);
				}		
				logger.debug("Process " +name + " finished ");
			} catch (IOException e) {
				logger.error("Unable to clean up!", e);
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

	private void serializerTextMD(OCRProcessMetadata ocrProcessMetadata,
			String textMD) {
		abbyySerializerTextMD = new AbbyySerializerTextMD(ocrProcessMetadata);
		logger.debug("Creating " + name + "-textMD.xml");
		if(getSegmentation()){
			abbyySerializerTextMD.write(new File(textMD));
			logger.debug("TextMD Created " + textMD);
		}else{
			Map<OCRFormat, OCROutput> outputs = getOcrOutputs();
			OCRFormat lastKey = getLastKey(outputs);
			AbbyyOCROutput out = (AbbyyOCROutput) outputs.get(lastKey);
			URI urii;
			try {
				String localUrl = out.getUri().toString().replaceAll(lastKey.toString().toLowerCase(),
						"xml" + config.reportSuffix);
				urii = new URI(localUrl.replace(".xml" + config.reportSuffix, "-textMD.xml"));
				abbyySerializerTextMD.write(new File(urii));
				logger.debug("TextMD Created " + urii.toString());
			} catch (URISyntaxException e) {
				logger.error("CAN NOT Copying Serializer textMD to local " + name
						+ "-textMD.xml", e);
			}
		}
		
	}

	/**
	 * Checks if is failed.
	 * 
	 * @return the boolean
	 */
	public Boolean isFailed() {
		return failed;
	}

	/**
	 * Checks if is Done.
	 * 
	 * @return the boolean
	 */
	public Boolean isDone() {
		return done;
	}

	/**
	 * Waits for results, returns true if they are available in the given time.
	 * This method never returns false!
	 * 
	 * @param results
	 *            the results
	 * @param timeout
	 *            the timeout
	 * @return true, if the results are available.
	 * @throws TimeoutExcetion
	 *             the timeout excetion
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws URISyntaxException
	 */

	private Boolean waitForResults(final Map<OCRFormat, OCROutput> results,
			Long timeout) throws TimeoutExcetion, InterruptedException,
			IOException, URISyntaxException {
		Long start = System.currentTimeMillis();
		Boolean check = true;
		Boolean putfalse = false;
		Map<URI, Boolean> expectedUris = new HashMap<URI, Boolean>();
		for (Map.Entry<OCRFormat, OCROutput> entry : results.entrySet()) {
			final AbbyyOCROutput o = (AbbyyOCROutput) entry.getValue();
			if (o.isSingleFile()) {
				URI u = o.getRemoteUri();
				expectedUris.put(u, false);
			} else {
				for (URI u : o.getResultFragments()) {
					expectedUris.put(u, false);
				}
			}
		}
		while (check) {
			for (Map.Entry<URI, Boolean> entry : expectedUris.entrySet()) {
				URI u = entry.getKey();
				Boolean bool = entry.getValue();
				if (!bool && hotfolder.exists(u)) {
					logger.debug(u.toString() + " is available");
					expectedUris.put(u, true);
				} else {
					logger.debug(u.toString() + " is not available");
					putfalse = true;
				}
			}
			if (putfalse) {
				for (URI uri : expectedUris.keySet()) {
					expectedUris.put(uri, false);
				}
			}
			if (!expectedUris.containsValue(false)) {
				logger.debug("Got all files.");
				break;
			}
			if (System.currentTimeMillis() > start + timeout) {
				//check = false;
				isResult = true;
				logger.debug("Waited to long - fail");
				throw new TimeoutExcetion();
			}
			putfalse = false;			
			logger.debug("Waiting for " + getOcrImages().size() * config.checkInterval
					+ " milli seconds");
			Thread.sleep(getOcrImages().size()* config.checkInterval);
		}
		return true;
	}

	/**
	 * Copy a url from source to destination. Assumes overwrite.
	 * 
	 * @param fileInfos
	 *            is a List of The Class AbbyyOCRImage. Is a representation of
	 *            an OCRImage suitable for holding references to remote files as
	 *            used by the Abbyy Recognition Server
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws URISyntaxException
	 * @throws FileSystemException
	 */
	private void copyFilesToServer(final List<OCRImage> fileInfos)
			throws InterruptedException, IOException, URISyntaxException {
		// iterate over all Files and put them to Abbyy-server inputFolder:
		for (OCRImage info : fileInfos) {
			AbbyyOCRImage image = (AbbyyOCRImage) info;
			if (image.toString().endsWith("/")) {
				logger.trace("Creating new directory "
						+ image.getRemoteUri().toString() + "!");
				// Create the directory
				hotfolder.mkDir(image.getRemoteUri());
			} else {
				String to = image.getRemoteUri().toString()
						.replace(config.password, "***");
				logger.trace("Copy from " + image.getUri().toString() + " to "
						+ to);
				try {
					hotfolder.copyFile(image.getUri(), image.getRemoteUri());
				} catch (IOException e) {
					logger.debug("can not Copy from "
							+ image.getUri().toString() + " to " + to);
					logger.debug("another try Copy from "
							+ image.getUri().toString() + " to " + to);
					hotfolder.copyFile(image.getUri(), image.getRemoteUri());
				}

			}
		}
	}

	public Long getDuration() {
		if (done) {
			return endTime - startTime;
		}
		return null;
	}

	/**
	 * Check server state. check all three folders since the limits are for the
	 * whole system.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void checkServerState() throws URISyntaxException, IOException, IllegalStateException {
		if (maxSize != 0 && maxFiles != 0) {

			// check if a slash is already appended
			final URI configServerUri = new URI(config.getServerURL());
			Map<URI, Long> sizeMap = new HashMap<URI, Long>(); 
			sizeMap.put(new URI(configServerUri.toString() + config.getInput()
					+ "/"), 0l);
			sizeMap.put(new URI(configServerUri.toString() + config.getOutput()
					+ "/"), 0l);
			sizeMap.put(new URI(configServerUri.toString() + config.getError()
					+ "/"), 0l);				
			

			for (URI uri : sizeMap.keySet()) {
				sizeMap.put(uri, hotfolder.getTotalSize(uri));
			}
			totalFileCount = Integer.valueOf(sizeMap.size()).longValue();
			for (Long size : sizeMap.values()) {
				if (size != null && size != 0) {
					totalFileSize += size;
				}
			}
			logger.debug("TotalFileSize = " + totalFileSize);
			if (maxFiles != 0 && totalFileCount > maxFiles) {
				logger.error("Too much files. Max number of files is "
						+ maxFiles + ". Number of files on server: "
						+ totalFileCount + ".\nExit program.");
				throw new IllegalStateException("Max number of files exeded");
			}
			if (maxSize != 0 && totalFileSize > maxSize) {
				logger.error("Size of files is too much files. Max size of all files is "
						+ maxSize
						+ ". Size of files on server: "
						+ totalFileSize + ".\nExit program.");
				throw new IllegalStateException("Max size of files exeded");
			}
		} else {
			logger.warn("Server state checking is disabled.");
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
		// Convert URI list to File list, the hardly readable way ;-)
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
				aoo.setRemoteUri(new URI(outputUri.toString()
						+ urlParts[urlParts.length - 1]));
			} catch (URISyntaxException e) {
				logger.error("Error while setting up URIs");
				throw new OCRException(e);
			}
		}
		processTimeout = (long) getOcrImages().size() * config.maxMillisPerFile;
		aoo.setRemoteLocation(config.serverOutputLocation);
		if (aoo.getRemoteFilename() == null) {
			aoo.setRemoteFilename(urlParts[urlParts.length - 1]);
		}
		ocrOutputs.put(format, aoo);
	}

	@Override
	public void setOcrOutputs(Map<OCRFormat, OCROutput> outputs) {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<OCRFormat, OCROutput> entry : outputs.entrySet()) {
			OCRFormat format = entry.getKey();
			addOutput(format, entry.getValue());
			// set Format for ocrProcessMetadata
			sb.append(format.toString());
			sb.append(" ");
		}
		ocrProcessMetadata.setFormat(sb.toString());
	}

	private synchronized void addMetadataOutput() {
		Map<OCRFormat, OCROutput> outputs = getOcrOutputs();
		OCRFormat lastKey = getLastKey(outputs);
		AbbyyOCROutput out = (AbbyyOCROutput) outputs.get(lastKey);
		AbbyyOCROutput metadata = new AbbyyOCROutput(out);

		try {
			// The remote file name
			outputResultUri = new URI(out
					.getRemoteUri()
					.toString()
					.replaceAll(lastKey.toString().toLowerCase(),
							"xml" + config.reportSuffix));
			metadata.setRemoteUri(outputResultUri);

			// The local file name
			metadata.setUri(new URI(out
					.getUri()
					.toString()
					.replaceAll(lastKey.toString().toLowerCase(),
							"xml" + config.reportSuffix)));
		} catch (URISyntaxException e) {
			logger.error("Error while setting up URIs");
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
					+ remoteUri.toString());
			hotfolder.deleteIfExists(remoteUri);
			// Also remove result fragments if they exist.
			if (!out.isSingleFile()) {
				for (URI u : out.getResultFragments()) {
					logger.trace("Trying to remove output fragment from output folder: "
							+ remoteUri.toString());
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
					+ remoteUri.toString());
			hotfolder.deleteIfExists(remoteUri);
			URI errorImageUri = image.getErrorUri();
			logger.trace("Trying to remove image from error folder: "
					+ errorImageUri.toString());
			hotfolder.deleteIfExists(errorImageUri);
		}
	}
	//Language for MetaData
	private void setLanguageforMetadata(Set<Locale> language) {
		List<Locale> lang = new ArrayList<Locale>();
		for (Locale l : language) {
			lang.add(l);
		}
		ocrProcessMetadata.setLanguages(lang);
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

	//Split in SubProcess
	protected List<AbbyyOCRProcess> split(){		
		//Example: getOcrImages().size()=234 and  imagesNumberForSubprocess=50, 234 < 75=(50+25)
		//so is optimal for Abbyy
		if(getOcrImages().size() <= config.imagesNumberForSubprocess){
			List<AbbyyOCRProcess> sp = new ArrayList<AbbyyOCRProcess>();
			sp.add(this);
			return sp;
		}else{		
			// setencoding for ocrProcessMetadata
			if (encoding.equals("UTF8")) {
				ocrProcessMetadata.setEncoding("UTF-8");
			} else {
				ocrProcessMetadata.setEncoding(encoding);
			}
			// set Language for ocrProcessMetadata
			if (langs != null) {
				setLanguageforMetadata(langs);
			}
			//rename subProcess ID
			for(AbbyyOCRProcess subProcess : cloneProcess()){	
				subProcess.setProcessId(getProcessId()+ subProcess.getName());
				subProcesses.add(subProcess);		
			}
			return subProcesses;
		}
	}
	//The subprocess will be here cloned from the Process
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
		List<List<OCRImage>> imageChunks = splitingImages(getOcrImages(), config.imagesNumberForSubprocess);
		for(List<OCRImage> imgs : imageChunks){				
				AbbyyOCRProcess sP = null;
				try {
					sP = (AbbyyOCRProcess) this.clone();
					sP.setObs((Observer)this);
					sP.ocrImages.clear();
					sP.ocrOutputs.clear();					
				} catch (CloneNotSupportedException e1) {
					logger.error("Clone Not Supported Exception: ", e1);
					return null;
				}
				sP.setOcrImages(imgs);
				sP.setName(name + "_" + listNumber + "oF" + splitNumberForSubProcess);			
				subProcessNames.add(name + "_" + listNumber + "oF" + splitNumberForSubProcess);
				String localuri = null;
				for (Map.Entry<OCRFormat, OCROutput> entry : outs.entrySet()) {
					OCROutput aoo = new AbbyyOCROutput();
					URI localUri = entry.getValue().getUri();
//					if(localUri.isAbsolute())
					localuri = localUri.toString().replace(name, sP.getName());
					try {
						localUri = new URI(localuri);	
					} catch (URISyntaxException e) {
						logger.error("Error contructing localUri URL: "+ localuri , e);
					}
					aoo.setUri(localUri);	
					OCRFormat f = entry.getKey();
					if(sP.ocrOutputs.size() == 0) {
						sP.addOutput(f, aoo);
					}
					formatForSubProcess.add(f);
				}	
				sP.setTime(new Date().getTime());
			    listNumber++;
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
			ocrProcessMetadata.setDuration(getDuration() + processTimeResult);
			if(!uriTextMD.equals("FAILED")){
				serializerTextMD(ocrProcessMetadata, uriTextMD + "-textMD.xml");		   				
				removeSubProcessResults(resultfilesForAllSubProcess);
			}
				 
		}		
	}

	// merge if nosubProcessfailed is true 
	//merge Results and ProcessMetaData
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
							logger.error("Error contructing FileInputStream for: "+file.toString() , e);
						} finally {
							try {
								if (isDoc != null) {
									isDoc.close();
								}
							} catch (IOException e) {
								logger.error("Could not close Stream.", e);
							}
						}
						
						// TODO this causes an exception because the namespaces do not match anymore
//						((AbbyyOCRProcessMetadata) ocrProcessMetadata)
//								.parseXmlExport(isDoc);
					}
					
				}
				files.add(file); 
				if(i == 0){
					fileResult = new File(outResultUri + "/" + sn + ".xml"+ config.reportSuffix);
					InputStream resultStream = null;
					if(noSubProcessfailed){
						try {
							resultStream = new FileInputStream(fileResult);
						} catch (FileNotFoundException e) {
							logger.error("Error contructing FileInputStream for: "+fileResult.toString() , e);
						}
						((AbbyyOCRProcessMetadata) ocrProcessMetadata)
								.parseXmlResult(resultStream);
					}					
					fileResults.add(fileResult);			
				}		
			}
			i++;
			if(noSubProcessfailed){
				logger.debug("Waiting... for Merge Proccessing");
				//mergeFiles for input format if Supported
				abbyyMergedResult = new File(outResultUri + "/" + name + "." + f.toString().toLowerCase());
				FileMerger.abbyyVersionNumber = config.abbyyVersionNumber;
				FileMerger.mergeFiles(f, files, abbyyMergedResult);
				logger.debug(name + "." + f.toString().toLowerCase()+ " MERGED");
				resultfilesForAllSubProcess.put(abbyyMergedResult, files);	
			}
					
		}
		try {
			if(noSubProcessfailed){
				logger.debug("Waiting... for Merge Proccessing");
				//mergeFiles for Abbyy Result xml.result.xml
				abbyyMergedResult = new File(outResultUri + "/" + name + ".xml" + config.reportSuffix);
				FileMerger.mergeAbbyyXMLResults(fileResults , abbyyMergedResult);
				resultfilesForAllSubProcess.put(abbyyMergedResult, fileResults);
				logger.debug(name + ".xml" + config.reportSuffix+ " MERGED");			
				uriTextMD = outResultUri + "/" + name;
			}else {
				uriTextMD = "FAILED";
			}
			
		} catch (IOException e) {
			logger.error("ERROR contructing :" +new File(outResultUri + "/" + name + ".xml" + config.reportSuffix).toString(), e);
		} catch (XMLStreamException e) {
			logger.error("ERROR in mergeAbbyyXML :", e);
		}		
		return uriTextMD;
	}
	
	//remove local files from the list after merge if the mergeResults Exists
	protected void removeSubProcessResults(Map<File, List<File>> resultFiles){
		@SuppressWarnings("rawtypes")
		Iterator k = resultFiles.keySet().iterator();
		File abbyyMergedResult = (File) k.next();
		if(abbyyMergedResult.exists()){
			List<File> files =  (List<File>) resultFiles.get(abbyyMergedResult);
			for(File file : files){
				file.delete(); 
				//second delete if still exists 
				if(file.exists()) {file.delete();}
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
