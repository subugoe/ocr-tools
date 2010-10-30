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
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.vfs.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;
import de.unigoettingen.sub.commons.ocr.util.FileMerger;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.AbstractHotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
/**
 * The Class AbbyyOCRProcess.
 */
public class AbbyyOCRProcess extends AbbyyTicket implements OCRProcess, Runnable {

	//TODO: Make sure that the Executor reads the size and count of the remote server
	//TODO: Save the stats of the remote system in a hidden file there, use the SharedHotfolder interface for this
	//TODO: check if orientation is handled properly
	//TODO: add a locking method to the hotfolder, use the SharedHotfolder interface for this
	//TODO: move hotfolder stuff into a seperate directory, partly done
	//TODO: make the priority configurable
	//TODO: Remove the expectedResults stuff

	// The Constant logger.
	public final static Logger logger = LoggerFactory.getLogger(AbbyyOCRProcess.class);

	//TODO: Use static fields from the engine class here.
	// The server url.
	protected static URI serverUri;

	// The folder URLs.
	protected URI inputUri, outputUri, errorUri;

	// The fix remote path.
	protected Boolean fixRemotePath = false;
	
	//TODO: Use these to hold informations about the ticketing stuff
	protected URI errorTicketUri;
	protected URI ticketUri;

	// State variables.
	// Set if process is failed
	protected Boolean failed = false;

	// Set if process is done
	protected Boolean done = true;

	// The done date.
	protected Long startTime = null;

	// The done date.
	protected Long endTime = null;

	protected Hotfolder hotfolder;

	
	protected XmlParser xmlParser;

	//TODO: Add calculation of timeout, set it in the ticket.
	protected Long maxOCRTimeout;

	protected Long maxSize;

	protected Long maxFiles;

	private Long totalFileCount;

	private Long totalFileSize;

	/**
	 * Instantiates a new process.
	 * 
	 * @param process
	 *            a OCR Process
	 */

	/*
	public AbbyyOCRProcess(OCRProcess p) {
		super(p);
		hotfolder = ApacheVFSHotfolderImpl.getInstance(config);
	}
	*/

	protected AbbyyOCRProcess(ConfigParser config, Hotfolder hotfolder) {
		super();
		this.config = config;
		this.hotfolder = hotfolder;
		init();
	}

	protected AbbyyOCRProcess(ConfigParser config) {
		super();
		this.config = config;
		hotfolder = AbstractHotfolder.getHotfolder(config.hotfolderClass, config);
		init();
	}
	
	private void init () {
		//config = new ConfigParser().parse();

		if (!config.isParsed()) {
			config = config.parse();
			//throw new IllegalStateException();
		}

		//Set constrains
		maxSize = config.getMaxSize();
		maxFiles = config.getMaxFiles();
		maxOCRTimeout = config.maxOCRTimeout;
		processTimeout = getOcrImages().size() * config.maxMillisPerFile;

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

	//TODO: Finish this constructor
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
		//TODO Break up this method
		startTime = System.currentTimeMillis();

		if (hotfolder == null) {
			throw new IllegalStateException("No Hotfolder set!");
		}

		//millisPerFile = config.minMilisPerFile;
		//String name = getName();

		//Create a List of files that should be copied
		List<AbbyyOCRImage> fileInfos = convertList(getOcrImages());

		//TODO: Try to get rid of this
		if (fixRemotePath) {
			fileInfos = fixRemotePath(fileInfos, name);
		}

		//TODO: calculate the server side timeout and add it to the ticket
		String tmpTicket = name + ".xml";
		//Create ticket, copy files and ticket

		List<URI> errorResults = new ArrayList<URI>();

		//If we use the static method to create a process some fields aren't set correctly (remoteUri, remoteFileName)
		//TODO: Check if addOcrImage() will do this for us as wll
		for (AbbyyOCRImage aoi : fileInfos) {
			String remoteFileName = aoi.getUri().toString();
			remoteFileName = name + "-" + remoteFileName.substring(remoteFileName.lastIndexOf("/") + 1, remoteFileName.length());
			if (aoi.getRemoteFileName() == null) {
				aoi.setRemoteFileName(remoteFileName);
			}
			URI remoteUri = null;
			try {
				errorResults.add(new URI(errorUri.toString() + remoteFileName));
				remoteUri = new URI(inputUri.toString() + remoteFileName);
			} catch (URISyntaxException e) {
				logger.error("Error contructing remote URL.");
				throw new OCRException(e);
			}
			if (aoi.getRemoteUri() == null) {
				aoi.setRemoteUri(remoteUri);
			}
			if (!config.dryRun) {
				try {
					if (hotfolder.exists(remoteUri)) {
						logger.warn("File alerady exists on server, deleting it");
						hotfolder.delete(remoteUri);
					}
				} catch (IOException e) {
					logger.error("Error while removing existing file.");
					throw new OCRException(e);
				}
			}
		}

		try {
			//Write ticket to temp file
			logger.debug("Creating AbbyyTicket");
			OutputStream os = hotfolder.createTmpFile(tmpTicket);
			write(os, name);
			os.close();

			if (!config.dryRun) {
				//Copy the ticket
				logger.debug("Copying tickt to server");
				hotfolder.copyTmpFile(tmpTicket, new URI(inputUri.toString() + tmpTicket));
				//Copy the files
				logger.debug("Coping imges to server.");
				copyFilesToServer(fileInfos);
			} else {
				return;
			}

			if (config.copyOnly) {
				logger.info("Process is in copy only mode, don't wait for results");
				return;
			}

			//Add result file to outputs
			addMetadataOutput();
			Map<OCRFormat, OCROutput> outputs = getOcrOutputs();

			//Wait for results if needed
			Long wait = fileInfos.size() * config.minMillisPerFile;
			logger.info("Waiting " + wait + " milli seconds for results");
			Thread.sleep(wait);
			//Create a list of the files to check for
			List<URI> expectedResults = new ArrayList<URI>();
			for (OCRFormat output : outputs.keySet()) {
			//	String remoteUri = ((AbbyyOCROutput) outputs.get(output)).getRemoteUri().toString();
				String remoteUri = outputUri + name + "." + output.name().toLowerCase();
				expectedResults.add(new URI(remoteUri));
			}
			Long timeout = getOcrImages().size() * config.maxMillisPerFile;
			try {
				xmlParser = new XmlParser();
				if (waitForResults(outputs, processTimeout)) {
					//Everything should be ok, get the files
					for (OCRFormat o : outputs.keySet()) {
						//TODO: Check for null here
						URI remoteUrl = ((AbbyyOCROutput) outputs.get(o)).getRemoteUri();
						URI localUrl = outputs.get(o).getUri();
						logger.debug("Copy from " + remoteUrl + " to " + localUrl);
						hotfolder.copyFile(remoteUrl, localUrl);
						//Merge from Mohamed, delete copied files
						logger.debug("Deleting remote file " + remoteUrl);
						hotfolder.deleteIfExists(remoteUrl);
					}
					URI from = new URI(outputUri + name + config.reportSuffix);
					URI to = new URI(config.localOutputLocation + name + config.reportSuffix);
					//TODO Erkennungsrat XMLParser
					logger.debug("Copy from " + from + " to " + to);
					hotfolder.copyFile(from, to);
					logger.debug("delete " + from + " from Remote URL" );
					hotfolder.deleteIfExists(from);
					//failed = true;
				}else{
					if (waitForResults(errorResults, timeout)) {
						for (URI errorUrl : errorResults) {
							logger.debug("delete " + errorUrl + " from NOT expectedResults" );
							hotfolder.deleteIfExists(errorUrl);
						}
						//Error Reports in Logfile
						InputStream is = new FileInputStream(new File(errorUri.toString() + name + config.reportSuffix));
						xmlParser.xmlresultErrorparse(is, name);
						hotfolder.deleteIfExists(new URI(errorUri.toString() + name + config.reportSuffix));
						failed = true;
					}
				}
			} catch (TimeoutExcetion e) {
				logger.error("Got an timeout while waiting for results", e);
				failed = true;
				//TODO: Handle errors here, look in the error folder
				//Delete failed processes
			} catch (XMLStreamException e) {
				logger.error("XML can not Parse, Missing Error Reports for "+ name + " : ", e);
			}

		} catch (IOException e) {
			failed = true;
			logger.error("Error writing files or ticket", e);
		} catch (InterruptedException e) {
			failed = true;
			logger.error("OCR Process was interrupted while coping files to server or waiting for result.", e);
		} catch (URISyntaxException e) {
			logger.error("Error seting tmp URI for ticket", e);
			failed = true;
		}

		//TODO: Remove this
		/*
		try {
			//TODO: failed isn't a shared state indicator
			while (!failed) {
				// for Output folder
				if (checkOutXmlResults(name)) {
					String resultOutURLPrefix = outputUri + name;
					File resultOutURLPrefixpath = new File(resultOutURLPrefix + "/" + name + config.reportSuffix);
					String resultOutURLPrefixAbsolutePath = resultOutURLPrefixpath.getAbsolutePath();
					// TODO Erkennungsrat muss noch ausgelesen werden(ich
					// wei das eigentlich nicht deswegen ist noch offen)
					ocrOutFormatFile = XmlParser.xmlresultOutputparse(new File(resultOutURLPrefixAbsolutePath));
					//File moveToLocalpath = new File(moveToLocal);
					//String moveToLocalAbsolutePath = moveToLocalpath.getAbsolutePath();

					// for Output folder
					for (int faktor = 1; faktor <= 2; faktor++) {
						if (checkIfAllFilesExists(ocrOutFormatFile, resultOutURLPrefix + "/")) {
							//copyAllFiles(ocrOutFormatFile, resultOutURLPrefix, moveToLocalAbsolutePath);
							deleteAllFiles(ocrOutFormatFile, resultOutURLPrefix);
							failed = true;
							//logger.info("Move Processing successfully to " + moveToLocalAbsolutePath);
						}

					}
				} else {
					//TODO: Delete images from failed processes
					// for Error folder
					if (checkErrorXmlResults(name)) {
						String resultErrorURLPrefix = errorUri.toString() + name;
						File resultErrorURLPrefixpath = new File(resultErrorURLPrefix + "/" + name + config.reportSuffix);
						String resultErrorURLPrefixAbsolutePath = resultErrorURLPrefixpath.getAbsolutePath();
						// TODO: Get the result report
						ocrErrorFormatFile = XmlParser.xmlresultErrorparse(new File(resultErrorURLPrefixAbsolutePath), name);
						for (int index = 1; index <= 2; index++) {
							if (checkIfAllFilesExists(ocrErrorFormatFile, resultErrorURLPrefix + "/")) {
								deleteAllFiles(ocrErrorFormatFile, resultErrorURLPrefix);
								failed = true;
								logger.info("delete All Files Processing is successfull ");
							}

						}
					}
				}

			}

		} catch (FileSystemException e) {
			logger.error("Processing failed (FileSystemException)", e);
			failed = true;
			throw new OCRException(e);
		} catch (FileNotFoundException e) {
			logger.error("Processing failed (FileNotFoundException)", e);
			failed = true;
			throw new OCRException(e);
		} catch (XMLStreamException e) {
			logger.error("Processing failed (XMLStreamException)", e);
			failed = true;
			throw new OCRException(e);
		} catch (URISyntaxException e) {
			logger.error("Processing failed (URISyntaxException)", e);
			failed = true;
			throw new OCRException(e);
		} finally {
			done = true;
			endTime = System.currentTimeMillis();
			logger.trace("AbbyyOCRProcess " + name + " ended ");
		}
		*/
	}

	/**
	 * Windows2unix file separator.
	 * 
	 * @param url
	 *            the url
	 * @return the string
	 */
	/*public static String windows2unixFileSeparator (String url) {
		return url.replace("\\", "/");
	}*/

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

	/**
	 * Fix remote path.
	 * 
	 * @param fileInfos
	 *            , is a List of AbbyyOCRImage
	 * @param name
	 *            for identifier
	 * @return the list of AbbyyOCRImage
	 */
	//TODO: Check if we need this method
	protected static List<AbbyyOCRImage> fixRemotePath (List<AbbyyOCRImage> fileInfos, String name) {
		LinkedList<AbbyyOCRImage> newList = new LinkedList<AbbyyOCRImage>();
		for (AbbyyOCRImage info : fileInfos) {
			//TODO: Check why remote URL is null here
			if (info.getRemoteUri().toString() != null) {
				try {
					// Rewrite remote name
					Pattern pr = Pattern.compile(".*\\\\(.*)");
					Matcher mr = pr.matcher(info.getRemoteUri().toString());
					mr.find();
					//TODO: this is a dirty hack
					if (mr.group(1) == null) {
						throw new IllegalStateException();
					}
					String newRemoteName = name + "-" + mr.group(1);
					logger.trace("Rewiting " + info.getRemoteUri().toString() + " to " + newRemoteName);

					try {
						info.setRemoteUri(new URI(newRemoteName));
					} catch (URISyntaxException e) {
						//TODO: Use a logger
						e.printStackTrace();
					}

					// rewrite webdav name
					Pattern pw = Pattern.compile("(.*/).*(/.*)");
					Matcher mw = pw.matcher(info.getRemoteFileName());
					mw.find();
					String newWebDavName = mw.group(1) + name + "-" + mw.group(2).substring(1);
					logger.trace("Rewriting " + info.getRemoteFileName() + " to " + newWebDavName);
					info.setRemoteFileName(newWebDavName);
				} catch (IllegalStateException e) {
					// No match found
					logger.trace("No match found", e);
				}
			}

			if (!info.getRemoteFileName().endsWith("/")) {
				newList.add(info);
			}
		}
		return newList;
	}

	/**
	 * Check xml results in output folder If exists.
	 * 
	 * @return the boolean, true If exists
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws MalformedURLException
	 */
	private Boolean checkOutXmlResults (String identifier) throws IOException {
		String resultURLPrefix = outputUri + identifier + "/" + identifier + config.reportSuffix;
		File resultURLPrefixpath = new File(resultURLPrefix);
		return hotfolder.exists(resultURLPrefixpath.toURI());
	}

	/**
	 * Check xml results in error folder If exists.
	 * 
	 * @return the boolean, true If exists.
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws MalformedURLException
	 */
	private Boolean checkErrorXmlResults (String identifier) throws IOException {
		String resultURLPrefix = errorUri.toString() + identifier + "/" + identifier + config.reportSuffix;
		File resultURLPrefixpath = new File(resultURLPrefix);
		return hotfolder.exists(resultURLPrefixpath.toURI());
	}

	/**
	 * Check if all files exists in url.
	 * 
	 * @param checkfile
	 *            is list of the name all images
	 * @param url
	 *            the url
	 * @return the boolean, true if all files exists
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws MalformedURLException
	 */
	private Boolean checkIfAllFilesExists (Set<String> checkfile, String url) throws IOException {

		for (String fileName : checkfile) {
			File urlpath = new File(url + "/" + fileName);
			if (hotfolder.exists(urlpath.toURI())) {
				logger.debug("File " + fileName + " exists already");
			} else {
				logger.debug("File " + fileName + " Not exists");
				return false;
			}
		}
		return true;
	}

	/**
	 * Result, number of all files not exists.
	 * 
	 * @param checkfile
	 *            is list of the name all images
	 * @param url
	 *            the url
	 * @return the int result
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	private int resultAllFilesNotExists (Set<String> checkfile, String url) throws IOException, URISyntaxException {
		Integer result = 0;

		for (String fileName : checkfile) {
			if (hotfolder.exists(new URI(new File(url).getAbsolutePath() + "/" + fileName))) {
				logger.debug("File " + fileName + " exists already");
			} else {
				logger.debug("File " + fileName + " Not exists");
				++result;
			}
		}
		return result;
	}

	/**
	 * Copy a files from url+fileName to localfile. Assumes overwrite.
	 * 
	 * @param checkfile
	 *            is list of the name all images
	 * @param url
	 *            the url, wich are all images
	 * @param localfile
	 *            the localfile
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws URISyntaxException
	 */
	//TODO: Remove this
	private void copyAllFiles (Set<String> checkfile, String url, String localfile) throws IOException, URISyntaxException {
		File urlpath = new File(url);
		hotfolder.mkDir(new URI(localfile + "/" + getName()));
		URI reportFrom = new URI(urlpath.getAbsolutePath() + "/" + getName() + config.reportSuffix);
		URI reportTo = new URI(localfile + "/" + getName() + "/" + getName() + config.reportSuffix);
		hotfolder.copyFile(reportFrom, reportTo);
		for (String fileName : checkfile) {
			URI fileFrom = new URI(urlpath.getAbsolutePath() + "/" + fileName);
			URI fileTo = new URI(localfile + "/" + getName() + "/" + fileName);
			hotfolder.copyFile(fileFrom, fileTo);
			logger.debug("Copy File From " + urlpath.getAbsolutePath() + "/" + fileName + " To" + localfile);
		}
	}

	/**
	 * Delete all files.
	 * 
	 * @param checkfile
	 *            is list of the name all images
	 * @param url
	 *            the url, wich are all images
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws URISyntaxException
	 */
	//TODO: Remove this, it works with diretories
	private void deleteAllFiles (Set<String> checkfile, String url) throws IOException, URISyntaxException {
		//TODO: Remove file from here
		String base = new File(url).getAbsolutePath();
		hotfolder.deleteIfExists(new URI(base + "/" + getName() + config.reportSuffix));
		for (String fileName : checkfile) {
			hotfolder.deleteIfExists(new URI(base + "/" + fileName));
		}
		hotfolder.deleteIfExists(new URI(base));
	}

	private static List<AbbyyOCRImage> convertList (List<OCRImage> ocrImages) {
		List<AbbyyOCRImage> images = new LinkedList<AbbyyOCRImage>();
		for (OCRImage i : ocrImages) {
			images.add((AbbyyOCRImage) i);
		}
		return images;
	}

	public Boolean isFailed () {
		return failed;
	}

	public Boolean isDone () {
		return done;
	}

	private Boolean waitForResults (Map<OCRFormat, OCROutput> results, Long timeout) throws TimeoutExcetion, InterruptedException, IOException {
		Boolean check = true;
		while (check) {
			Integer successCounter = 0;
			for (OCRFormat of : results.keySet()) {
				URI u = results.get(of).getUri();
				if (hotfolder.exists(u)) {
					successCounter++;
					logger.trace(u.toString() + " is available");
				} else {
					logger.trace(u.toString() + " is not available");
				}
			}
			if (successCounter == results.size()) {
				logger.trace("Got all " + successCounter + " files.");
				break;
			}
			if (System.currentTimeMillis() > timeout) {
				check = false;
				logger.warn("Waited to long - fail");
				throw new TimeoutExcetion();
			}
			logger.trace("Waiting for " + config.checkInterval + " milli seconds");
			Thread.sleep(config.checkInterval);
		}
		return true;
	}

	private Boolean waitForResults (List<URI> expectedFiles, Long timeout) throws TimeoutExcetion, InterruptedException, IOException {
		Boolean check = true;
		while (check) {
			Integer successCounter = 0;
			for (URI u : expectedFiles) {
				if (hotfolder.exists(u)) {
					successCounter++;
					logger.trace(u.toString() + " is available");
				} else {
					logger.trace(u.toString() + " is not available");
				}
			}
			if (successCounter == expectedFiles.size()) {
				logger.trace("Got all " + successCounter + " files.");
				break;
			}
			if (System.currentTimeMillis() > timeout) {
				check = false;
				logger.warn("Waited to long - fail");
				throw new TimeoutExcetion();
			}
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
	private void copyFilesToServer (List<AbbyyOCRImage> fileInfos) throws InterruptedException, IOException, URISyntaxException {
		// iterate over all Files and put them to Abbyy-server inputFolder:
		for (AbbyyOCRImage info : fileInfos) {
			if (info.toString().endsWith("/")) {
				logger.trace("Creating new directory " + info.getRemoteUri().toString() + "!");
				// Create the directory
				hotfolder.mkDir(info.getRemoteUri());
			} else {
				String to = info.getRemoteUri().toString().replace(config.password, "***");
				logger.trace("Copy from " + info.getUri().toString() + " to " + to);
				hotfolder.copyFile(info.getUri(), info.getRemoteUri());
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
	protected void checkServerState () throws IOException, URISyntaxException {
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
	protected void mergeResultStreams (Map<OCRFormat, AbbyyOCROutput> outputs) throws IOException {
		Map<OCRFormat, Exception> exceptions = new HashMap<OCRFormat, Exception>();
		for (OCRFormat f : outputs.keySet()) {
			if (FileMerger.isSegmentable(f)) {
				throw new OCRException("Format " + f.toString() + " isn't mergable!");
			}
			final AbbyyOCROutput o = outputs.get(f);
			OutputStream os = new FileOutputStream(new File(o.getUri()));
			//Convert URI list to File list, the hardly readable way ;-)
			List<InputStream> inputFiles = new ArrayList<InputStream>() {
				{
					for (URI u : o.getResultFragments()) {
						add(hotfolder.openInputStream(u));
					}
				}
			};
			try {
				FileMerger.mergeStreams(f, inputFiles, os);
			} catch (IllegalArgumentException e) {
				exceptions.put(f, e);
			} catch (IllegalAccessException e) {
				exceptions.put(f, e);
			} catch (InvocationTargetException e) {
				exceptions.put(f, e);
			}
		}
		if (!exceptions.isEmpty()) {
			throw new OCRException("Error while merging files.");
		}
	}

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
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		//TODO: check why this is called more than once
		Map<OCRFormat, OCROutput> outputs = getOcrOutputs();

		OCRFormat lastKey = getLastKey(outputs);
		AbbyyOCROutput out = (AbbyyOCROutput) outputs.get(lastKey);

		AbbyyOCROutput metadata = new AbbyyOCROutput(out);
		
		try {
			//The remote file name
			metadata.setRemoteUri(new URI(out.getRemoteUri() + config.reportSuffix));
			//The local file name
			//TODO: make this configurable
			metadata.setUri(new URI(out.getUri() + config.reportSuffix));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		addOutput(OCRFormat.METADATA, metadata);
	}

	@SuppressWarnings("unchecked")
	private static <K> K getLastKey (Map<K, ?> map) {
		//A stupid hack to get the last key, maybe there is something better in commons-lang 
		if (!(map instanceof LinkedHashMap)) {
			throw new IllegalArgumentException("Map needs to be of type LinkedHashMap, otherwise the order isn't predictable");
		}
		K lastOutput = null;
		for (K k : map.keySet()) {
			lastOutput = k;
		}
		return lastOutput;
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

		public TimeoutExcetion(Throwable t) {
			super(t);
		}
	}

}
