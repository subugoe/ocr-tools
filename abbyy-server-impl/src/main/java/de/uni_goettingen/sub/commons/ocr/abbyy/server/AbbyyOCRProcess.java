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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.vfs.FileSystemException;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 */
public class AbbyyOCRProcess extends AbbyyTicket implements OCRProcess, Runnable {

	//TODO: Make sure that the Executor reads the size and count of the remote server
	//TODO: Save the stats of the remote system in a hidden file there, use the SharedHotfolder interface for this
	//TODO: add a locking method to the hotfolder, use the SharedHotfolder interface for this
	//TODO: make the priority configurable

	// The Constant logger.
	public final static Logger logger = LoggerFactory.getLogger(AbbyyOCRProcess.class);

	//TODO: Use static fields from the engine class here.
	// The server url.
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

	// Set if process is done
	private Boolean done = true;

	// The done date.
	private Long startTime = null;

	// The done date.
	private Long endTime = null;

	protected Hotfolder hotfolder;

	protected XmlParser xmlParser;
	
	protected OCRProcessMetadata ocrProcessMetadata;
	protected XmlResultDocument xmlResultDocument; 
	protected XmlResult xmlResultEngine ;

	private Long maxSize;

	private Long maxFiles;

	private Long totalFileCount;

	private Long totalFileSize;


	protected AbbyyOCRProcess(ConfigParser config, Hotfolder hotfolder) {
		super();
		this.config = config;
		this.hotfolder = hotfolder;
		init();
	}

	protected AbbyyOCRProcess(ConfigParser config) {
		super();
		this.config = config;
		ocrProcessMetadata = new OCRProcessMetadataImpl();
		hotfolder = AbstractHotfolder.getHotfolder(config.hotfolderClass, config);
		init();
	}

	private void init () {
		if (!config.isParsed()) {
			//config = config.parse();
			throw new IllegalStateException();
		}

		//Set constrains
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

	protected AbbyyOCRProcess(OCRProcess process, ConfigParser config) {
		super(process);
		this.config = config;
		hotfolder = AbstractHotfolder.getHotfolder(config.hotfolderClass, config);
		init();
		throw new NotImplementedException("This constructor isn't finished");
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run () {
		
		startTime = System.currentTimeMillis();

		if (hotfolder == null) {
			throw new IllegalStateException("No Hotfolder set!");
		}

		//If we use the static method to create a process some fields aren't set correctly (remoteUri, remoteFileName)
		for (OCRImage image : getOcrImages()) {
			AbbyyOCRImage aoi = (AbbyyOCRImage) image;
			String remoteFileName = aoi.getUri().toString();
			remoteFileName = name + "-" + remoteFileName.substring(remoteFileName.lastIndexOf("/") + 1, remoteFileName.length());
			if (aoi.getRemoteFileName() == null) {
				aoi.setRemoteFileName(remoteFileName);
			}
			URI remoteUri, errorUri = null;
			try {
				errorUri = new URI(this.errorUri.toString() + remoteFileName);
				remoteUri = new URI(inputUri.toString() + remoteFileName);
			} catch (URISyntaxException e) {
				logger.error("Error contructing remote URL.");
				ocrProcessMetadata.setDuration(0L);
				throw new OCRException(e);
			}
			if (aoi.getRemoteUri() == null) {
				aoi.setRemoteUri(remoteUri);
				aoi.setErrorUri(errorUri);
			}

		}

		//Add the metadata descriptor (result file) to the outputs
		addMetadataOutput();

		try {
			//Set the file names and URIs
			String tmpTicket = name + ".xml";
			ticketUri = new URI(inputUri.toString() + tmpTicket);
			errorTicketUri = new URI(errorUri.toURL() + tmpTicket);
			errorResultUri = new URI(errorUri.toURL() + tmpTicket + config.reportSuffix);

			if (config.dryRun) {
				//No interaction with the server and IO wanted, just return here
				logger.info("Process is in dry run mode, don't write anything");
				return;
			}
			
			//Create ticket, copy files and ticket
			//Write ticket to temp file
			logger.debug("Creating AbbyyTicket");
			OutputStream os = hotfolder.createTmpFile(tmpTicket);
			write(os, name, ocrProcessMetadata);
			os.close();
			
			
			logger.debug("Cleaning Server");
			//Clean the server here to avoid GUIDs as filenames
			cleanOutputs(getOcrOutputs());
			//Remove all files that are part of this process, they shouldn't exist yet.
			cleanImages(convertList(getOcrImages()));

			//Copy the ticket
			logger.debug("Copying tickt to server");
			hotfolder.copyTmpFile(tmpTicket, ticketUri);
			//Copy the files
			logger.debug("Coping imges to server.");
			copyFilesToServer(getOcrImages());

			if (config.copyOnly) {
				logger.info("Process is in copy only mode, don't wait for results");
				return;
			}

			//Wait for results if needed
			Long minWait = getOcrImages().size() * config.minMillisPerFile;
			Long maxWait = getOcrImages().size() * config.maxMillisPerFile;
			logger.info("Waiting " + minWait + " milli seconds for results");
			Thread.sleep(minWait);

			try {
				Map<OCRFormat, OCROutput> outputs = getOcrOutputs();
				logger.debug("Waking up, waiting another " + (maxWait - minWait) + " milli seconds for results");
				if (waitForResults(outputs, maxWait)) {
					//Everything should be ok, get the files
					for (OCRFormat f : outputs.keySet()) {
						final AbbyyOCROutput o = (AbbyyOCROutput) outputs.get(f);
						if (o.isSingleFile()) {
							URI remoteUri = o.getRemoteUri();
							URI localUri = o.getUri();
							//TODO Erkennungsrat XMLParser
							/*if((remoteUri.toString()).endsWith("xml"+ config.reportSuffix)){
								InputStream isResult = hotfolder.openInputStream(remoteUri);
								xmlResultDocument = XmlResultDocument.Factory.parse(isResult);
								xmlResultEngine = xmlResultDocument.getXmlResult();
								BigDecimal totalChar = new BigDecimal(xmlResultEngine.getStatistics().getTotalCharacters());
								BigDecimal totalUncerChar = new BigDecimal(xmlResultEngine.getStatistics().getUncertainCharacters());
							    BigDecimal prozent = (totalUncerChar.divide(totalChar, 4, BigDecimal.ROUND_UP)).multiply(new BigDecimal(100));
							    ocrProcessMetadata.setCharacterAccuracy(prozent);
							    System.out.println(prozent);
								
							}*/
							logger.debug("Copy from " + remoteUri + " to " + localUri);
							hotfolder.copyFile(remoteUri, localUri);
							logger.debug("Deleting remote file " + remoteUri);
							hotfolder.deleteIfExists(remoteUri);
						} else {
							//The results are fragmented, merge them
							mergeResult(f, o);
						}
					}
				} else {
					failed = true;
				}
			} catch (TimeoutExcetion e) {
				logger.error("Got an timeout while waiting for results", e);
				failed = true;

				logger.debug("Trying to delete files of the failed process");
				//Clean server, to reclaim storage
				cleanImages(convertList(getOcrImages()));
				cleanOutputs(getOcrOutputs());
				//Delete the error metadata
				hotfolder.deleteIfExists(ticketUri);
				hotfolder.deleteIfExists(errorTicketUri);
				
				//Error Reports 
				logger.debug("Trying to parse file" + errorResultUri);
				if(hotfolder.exists(errorResultUri)){
				xmlParser = new XmlParser();
				logger.debug("Trying to parse EXISTS file" + errorResultUri);
				InputStream is = new FileInputStream(new File(errorResultUri.toString()));
				xmlParser.xmlresultErrorparse(is, name);
				hotfolder.deleteIfExists(errorResultUri);
				}
				endTime = System.currentTimeMillis();
				ocrProcessMetadata.setDuration(getDuration());
			} /*catch (XmlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		} catch (XMLStreamException e) {
			//Set failed here since the results isn't worth much without metadata
			failed = true;
			logger.error("XML can not Parse, Missing Error Reports for " + name + " : ", e);
			endTime = System.currentTimeMillis();
			ocrProcessMetadata.setDuration(getDuration());
		} catch (IOException e) {
			failed = true;
			logger.error("Error writing files or ticket", e);
			endTime = System.currentTimeMillis();
			ocrProcessMetadata.setDuration(getDuration());
		} catch (InterruptedException e) {
			failed = true;
			logger.error("OCR Process was interrupted while coping files to server or waiting for result.", e);
			endTime = System.currentTimeMillis();
			ocrProcessMetadata.setDuration(getDuration());
		} catch (URISyntaxException e) {
			logger.error("Error seting tmp URI for ticket", e);
			failed = true;
			endTime = System.currentTimeMillis();
			ocrProcessMetadata.setDuration(getDuration());
		} catch (OCRException e) {
			logger.error("Error during OCR Process", e);
			failed = true;
			endTime = System.currentTimeMillis();
			ocrProcessMetadata.setDuration(getDuration());
		} finally {
			xmlParser = null;
			try {
				cleanImages(convertList(getOcrImages()));
				cleanOutputs(getOcrOutputs());
				hotfolder.deleteIfExists(errorResultUri);
				hotfolder.deleteIfExists(ticketUri);
				if (outputResultUri != null){
					hotfolder.deleteIfExists(outputResultUri);
				}
				endTime = System.currentTimeMillis();
				ocrProcessMetadata.setDuration(getDuration());
			} catch (IOException e) {
				failed = true;
				logger.error("Unable to clean up!", e);
				endTime = System.currentTimeMillis();
				ocrProcessMetadata.setDuration(getDuration());
			}		
		}
	}

	private void getErrorDescription () {
		
	}
	
	/**
	 * Calculate size of the OCRImages representing this process
	 * 
	 * @return the long, size of all files
	 */
	public Long calculateSize () {
		Long size = 0l;
		for (OCRImage i : getOcrImages()) {
			AbbyyOCRImage aoi = (AbbyyOCRImage) i;
			size += aoi.getSize();
		}
		return size;
	}

	private static List<AbbyyOCRImage> convertList (List<OCRImage> ocrImages) {
		List<AbbyyOCRImage> images = new LinkedList<AbbyyOCRImage>();
		for (OCRImage i : ocrImages) {
			images.add((AbbyyOCRImage) i);
		}
		return images;
	}

	/**
	 * Checks if is failed.
	 *
	 * @return the boolean
	 */
	public Boolean isFailed () {
		return failed;
	}
	/**
	 * Checks if is Done.
	 *
	 * @return the boolean
	 */
	public Boolean isDone () {
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

	private Boolean waitForResults (final Map<OCRFormat, OCROutput> results, Long timeout) throws TimeoutExcetion, InterruptedException, IOException, URISyntaxException {
		Long start = System.currentTimeMillis();
		Boolean check = true;
		Boolean putfalse = false;
		Map<URI,Boolean> expectedUris = new HashMap<URI, Boolean>();
		for (OCRFormat of : results.keySet()) {
		  final AbbyyOCROutput o = (AbbyyOCROutput) results.get(of);
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
			for (URI u: expectedUris.keySet()) {
				if (!expectedUris.get(u) && hotfolder.exists(u)) {
					logger.trace(u.toString() + " is available");
					expectedUris.put(u, true);
				} else {
					logger.trace(u.toString() + " is not available");
					putfalse = true;
				}			
			}
			if(putfalse){
				for (URI uri: expectedUris.keySet()){
					expectedUris.put(uri, false);
				}
			}
			if (!expectedUris.containsValue(false)) {
				logger.trace("Got all files.");
				break;
			}
			if (System.currentTimeMillis() > start + timeout) {
				check = false;
				logger.warn("Waited to long - fail");
				throw new TimeoutExcetion();
			}
			System.out.println("jetzt= " +System.currentTimeMillis()+ "Satrt = "+  start + "timeout = "+ timeout + "summe= "+start + timeout);
			putfalse = false;
			logger.trace("Waiting for " + config.checkInterval + " milli seconds");
			Thread.sleep(config.checkInterval);
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
	private void copyFilesToServer (final List<OCRImage> fileInfos) throws InterruptedException, IOException, URISyntaxException {
		// iterate over all Files and put them to Abbyy-server inputFolder:
		for (OCRImage info : fileInfos) {
			AbbyyOCRImage image = (AbbyyOCRImage) info;
			if (image.toString().endsWith("/")) {
				logger.trace("Creating new directory " + image.getRemoteUri().toString() + "!");
				// Create the directory
				hotfolder.mkDir(image.getRemoteUri());
			} else {
				String to = image.getRemoteUri().toString().replace(config.password, "***");
				logger.trace("Copy from " + image.getUri().toString() + " to " + to);
				hotfolder.copyFile(image.getUri(), image.getRemoteUri());
			}
		}
	}

	public Long getDuration () {
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
	@SuppressWarnings("serial")
	public void checkServerState () throws IOException, URISyntaxException {
		if (maxSize != 0 && maxFiles != 0) {

			// check if a slash is already appended
			final URI serverUri = new URI(config.getServerURL());
			Map<URI, Long> sizeMap = new HashMap<URI, Long>() {
				{
					put(new URI(serverUri.toString() + config.getInput() + "/"), 0l);
					put(new URI(serverUri.toString() + config.getOutput() + "/"), 0l);
					put(new URI(serverUri.toString() + config.getError() + "/"), 0l);
				}
			};

			for (URI uri : sizeMap.keySet()) {
				sizeMap.put(uri, hotfolder.getTotalSize(uri));
			}
			totalFileCount = Integer.valueOf(sizeMap.size()).longValue();
			for (Long size : sizeMap.values()) {
				if (size != null) {
					totalFileSize += size;
				}
			}
			logger.debug("TotalFileSize = " + totalFileSize);

			if (maxFiles != 0 && totalFileCount > maxFiles) {
				logger.error("Too much files. Max number of files is " + maxFiles + ". Number of files on server: " + totalFileCount + ".\nExit program.");
				throw new IllegalStateException("Max number of files exeded");
			}
			if (maxSize != 0 && totalFileSize > maxSize) {
				logger.error("Size of files is too much files. Max size of all files is " + maxSize
						+ ". Size of files on server: "
						+ totalFileSize
						+ ".\nExit program.");
				throw new IllegalStateException("Max size of files exeded");
			}
		} else {
			logger.warn("Server state checking is disabled.");
		}
	}

	@SuppressWarnings("serial")
	private void mergeResult (final OCRFormat format, final AbbyyOCROutput output) throws IOException, MergeException {
		if (!FileMerger.isSegmentable(format)) {
			throw new OCRException("Format " + format.toString() + " isn't mergable!");
		}
		//TODO: Use the hotfolder stuff here, since we can redirect IO in this layer
		OutputStream os = new FileOutputStream(new File(output.getUri()));
		//Convert URI list to File list, the hardly readable way ;-)
		List<InputStream> inputFiles = new ArrayList<InputStream>() {
			{
				for (URI u : output.getResultFragments()) {
					add(hotfolder.openInputStream(u));
				}
			}
		};
		FileMerger.mergeStreams(format, inputFiles, os);
	}

	/**
	 * Adds the output for the given format 
	 * @param format
	 *            the format to add
	 * @param output
	 *            the output, the output settings for the given format
	 * 
	 */
	@Override
	public void addOutput (OCRFormat format, OCROutput output) {
		//Make sure we only add values, not replace existing ones
		if (ocrOutputs == null) {
			//We use a LinkedHashMap to get the order of the elements predictable
			ocrOutputs = new LinkedHashMap<OCRFormat, OCROutput>();
		}
		AbbyyOCROutput aoo = new AbbyyOCROutput(output);
		String[] urlParts = output.getUri().toString().split("/");
		if (aoo.getRemoteUri() == null) {
			try {
				aoo.setRemoteUri(new URI(outputUri.toString() + urlParts[urlParts.length - 1]));
			} catch (URISyntaxException e) {
				logger.error("Error while setting up URIs");
				throw new OCRException(e);
			}
		}
		aoo.setRemoteLocation(config.serverOutputLocation);
		if (aoo.getRemoteFilename() == null) {
			aoo.setRemoteFilename(urlParts[urlParts.length - 1]);
		}
		ocrOutputs.put(format, aoo);
	}

	@Override
	public void setOcrOutputs (Map<OCRFormat, OCROutput> outputs) {
		for (OCRFormat format : outputs.keySet()) {
			addOutput(format, outputs.get(format));
		}
	}

	private void addMetadataOutput () {
		Map<OCRFormat, OCROutput> outputs = getOcrOutputs();
		OCRFormat lastKey = getLastKey(outputs);
		AbbyyOCROutput out = (AbbyyOCROutput) outputs.get(lastKey);
		AbbyyOCROutput metadata = new AbbyyOCROutput(out);

		try {
			//The remote file name
			outputResultUri = new URI(out.getRemoteUri().toString().replaceAll(lastKey.toString().toLowerCase(), "xml" + config.reportSuffix));
			metadata.setRemoteUri(outputResultUri);
			
			//The local file name
			metadata.setUri(new URI(out.getUri().toString().replaceAll(lastKey.toString().toLowerCase(), "xml" + config.reportSuffix)));
		} catch (URISyntaxException e) {
			logger.error("Error while setting up URIs");
			ocrProcessMetadata.setDuration(0L);
			throw new OCRException(e);
		}

		addOutput(OCRFormat.METADATA, metadata);
	}

	@SuppressWarnings("unchecked")
	private static <K> K getLastKey (final Map<K, ?> map) {
		//A stupid hack to get the last key, maybe there is something better in commons-lang 
		if (!(map instanceof LinkedHashMap)) {
			throw new IllegalArgumentException("Map needs to be of type LinkedHashMap, otherwise the order isn't predictable");
		}
		//We could use the keySet().toArray() method as well, but this isn't type save.
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
	private void cleanOutputs (final Map<OCRFormat, OCROutput> outputs) throws IOException {
		for (OCRFormat of : outputs.keySet()) {
			AbbyyOCROutput out = (AbbyyOCROutput) outputs.get(of);
			URI remoteUri = out.getRemoteUri();
			logger.trace("Trying to remove output from output folder: " + remoteUri.toString());
			hotfolder.deleteIfExists(remoteUri);
			//Also remove result fragments if they exist.
			if (!out.isSingleFile()) {
				for (URI u : out.getResultFragments()) {
					logger.trace("Trying to remove output fragment from output folder: " + remoteUri.toString());
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
	private void cleanImages (final List<AbbyyOCRImage> images) throws IOException {
		for (AbbyyOCRImage image : images) {
			URI remoteUri = image.getRemoteUri();
			logger.trace("Trying to remove image from input folder: " + remoteUri.toString());
			hotfolder.deleteIfExists(remoteUri);
			URI errorUri = image.getErrorUri();
			logger.trace("Trying to remove image from error folder: " + errorUri.toString());
			hotfolder.deleteIfExists(errorUri);
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

}
