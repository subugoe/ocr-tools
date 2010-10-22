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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

/**
 * The Class AbbyyOCRProcess.
 */
public class AbbyyOCRProcess extends AbbyyTicket implements OCRProcess, Runnable {

	//TODO: Add this stuff: <OutputLocation>D:\Recognition\GDZ\output</OutputLocation>, it's now part of the ticket
	//TODO: Make sure that the Executor reads the size and count of the remote server
	//TODO: Save the stats of the remote system in a hidden file there
	//TODO: check if the OCRResult stuff is used correctly
	//TODO: check if orientatio is handled properly

	// The Constant logger.
	public final static Logger logger = LoggerFactory.getLogger(AbbyyOCRProcess.class);

	//TODO: Use static fields from the engine class here.
	// The server url.
	protected static URI serverUri;

	// The folder URLs.
	protected URI inputUri, outputUri, errorUri;

	// The fix remote path.
	protected Boolean fixRemotePath = false;

	// State variables.
	// Set if process is failed
	protected Boolean failed = false;

	// Set if process is done
	protected Boolean done = true;

	// The done date.
	protected Long startTime = null;

	// The done date.
	protected Long endTime = null;

	// The apacheVFSHotfolderImpl.
	protected Hotfolder hotfolder;

	//TODO: Remove this
	// The ocr error format file.
	Set<String> ocrErrorFormatFile = new LinkedHashSet<String>();

	//TODO: Remove this
	// The ocr out format file.
	Set<String> ocrOutFormatFile = new LinkedHashSet<String>();

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

	public AbbyyOCRProcess(OCRProcess p) {
		super(p);
		hotfolder = ApacheVFSHotfolderImpl.newInstance(config);
	}

	protected AbbyyOCRProcess(ConfigParser config, Hotfolder hotfolder) {
		super();
		this.hotfolder = hotfolder;
	}

	protected AbbyyOCRProcess(ConfigParser config) {
		super();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run () {
		//TODO Break up this method
		startTime = System.currentTimeMillis();

		//TODO: move this into an init method
		config = new ConfigParser().parse();
		maxSize = config.getMaxSize();
		maxFiles = config.getMaxFiles();

		if (hotfolder == null) {
			hotfolder = ApacheVFSHotfolderImpl.newInstance(config);
		}

		try {
			serverUri = new URI(config.getServerURL());
		} catch (URISyntaxException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		maxOCRTimeout = config.maxOCRTimeout;
		oCRTimeOut = getOcrImages().size() * config.maxMillisPerFile;
		//millisPerFile = config.minMilisPerFile;
		try {
			inputUri = new URI(serverUri + config.getInput() + "/");
			outputUri = new URI(serverUri + config.getOutput() + "/");
			errorUri = new URI(serverUri + config.getError() + "/");
		} catch (URISyntaxException e1) {
			logger.error("Can't setup server uris", e1);
			throw new OCRException(e1);
		}

		String name = getName();

		//Create a List of files that should be copied
		List<AbbyyOCRImage> fileInfos = convertList(getOcrImages());

		//TODO: Try to get rid of this
		if (fixRemotePath) {
			fileInfos = fixRemotePath(fileInfos, name);
		}

		//TODO: calculate the server side timeout and add it to the ticket
		String tmpTicket = name + ".xml";
		//Create ticket, copy files and ticket

		//If we use the static method to create a process some fields aren't set correctly (remoteUrl, remoteFileName)
		for (AbbyyOCRImage aoi : fileInfos) {
			String remoteFileName = aoi.getUrl().toString();
			remoteFileName = name + "-" + remoteFileName.substring(remoteFileName.lastIndexOf("/") + 1, remoteFileName.length());
			if (aoi.getRemoteFileName() == null) {
				aoi.setRemoteFileName(remoteFileName);
			}
			URI remoteUri = null;
			try {
				remoteUri = new URI(inputUri.toString() + remoteFileName);
			} catch (URISyntaxException e) {
				logger.error("Error contructing remote URL.");
				throw new OCRException(e);
			}
			if (aoi.getRemoteURI() == null) {
				aoi.setRemoteURI(remoteUri);
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

			logger.debug("Coping files to server.");

			if (!config.dryRun) {
				//Copy the ticket
				hotfolder.copyTmpFile(tmpTicket, new URI(inputUri.toString() + tmpTicket));
				//Copy the files
				copyFilesToServer(fileInfos);
			} else {
				return;
			}

			if (config.copyOnly) {
				logger.info("Process is in copy only mode, don't wait for results");
				return;
			}
			//Wait for results if needed
			Long wait = fileInfos.size() * config.minMillisPerFile;
			logger.info("Waiting " + wait + " milli seconds for results");
			Thread.sleep(wait);
			//Create a list of the files to check for
			List<URI> expectedResults = new ArrayList<URI>();
			Map<OCRFormat, OCROutput> outputs = getOcrOutput();
			for (OCRFormat output : outputs.keySet()) {
				String remoteUrl = ((AbbyyOCROutput) outputs.get(output)).getRemoteUrl().toString();
				expectedResults.add(new URI(remoteUrl));
				expectedResults.add(new URI(remoteUrl + config.reportSuffix));
			}
			Long timeout = getOcrImages().size() * config.maxMillisPerFile;
			try {
				if (waitForResults(expectedResults, timeout)) {
					//Everything should be ok, get the files
					for (OCRFormat output : outputs.keySet()) {
						URI remoteUrl = ((AbbyyOCROutput) outputs.get(output)).getRemoteUrl().toURI();
						URI localUrl = outputs.get(output).getUrl().toURI();
						logger.debug("Copy from " + remoteUrl + " to " + localUrl);
						hotfolder.copyFile(remoteUrl, localUrl);
						logger.debug("Getting result descriptor");
						//TODO: Use AbbyyOCRResult here
						URI from = new URI(remoteUrl + config.reportSuffix);
						URI to = new URI(localUrl + config.reportSuffix);
						hotfolder.copyFile(from, to);
					}
				}
			} catch (TimeoutExcetion e) {
				logger.error("Got an timeout while waiting for results", e);
				failed = true;
				//TODO: Handle errors here
				//Delete failed processes
			}

		} catch (FileSystemException e) {
			failed = true;
			logger.error("Couldn't write files to server URL", e);
		} catch (IOException e) {
			failed = true;
			logger.error("Error writing ticket", e);
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
			if (info.getRemoteURI().toString() != null) {
				try {
					// Rewrite remote name
					Pattern pr = Pattern.compile(".*\\\\(.*)");
					Matcher mr = pr.matcher(info.getRemoteURI().toString());
					mr.find();
					//TODO: this is a dirty hack
					if (mr.group(1) == null) {
						throw new IllegalStateException();
					}
					String newRemoteName = name + "-" + mr.group(1);
					logger.trace("Rewiting " + info.getRemoteURI().toString() + " to " + newRemoteName);

					try {
						info.setRemoteURI(new URI(newRemoteName));
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
	protected Boolean checkOutXmlResults (String identifier) throws IOException {
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
	protected Boolean checkErrorXmlResults (String identifier) throws IOException {
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
	//TODO: Use OCROutput here
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

	protected static List<AbbyyOCRImage> convertList (List<OCRImage> ocrImages) {
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

	protected Boolean waitForResults (List<URI> expectedFiles, Long timeout) throws TimeoutExcetion, InterruptedException, IOException {
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
	public void copyFilesToServer (List<AbbyyOCRImage> fileInfos) throws InterruptedException, IOException, URISyntaxException {
		// iterate over all Files and put them to Abbyy-server inputFolder:
		for (AbbyyOCRImage info : fileInfos) {
			if (info.toString().endsWith("/")) {
				logger.trace("Creating new directory " + info.getRemoteURI().toString() + "!");
				// Create the directory
				hotfolder.mkDir(info.getRemoteURI());
			} else {
				String to = info.getRemoteURI().toString().replace(config.password, "***");
				logger.trace("Copy from " + info.getUrl().toString() + " to " + to);
				hotfolder.copyFile(info.getUrl().toURI(), info.getRemoteURI());
			}
		}
	}

	public Long getDuration () {
		if (done) {
			return endTime - startTime;
		}
		return null;
	}

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
