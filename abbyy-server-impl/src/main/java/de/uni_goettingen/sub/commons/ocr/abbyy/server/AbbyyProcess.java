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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.vfs.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

/**
 * The Class AbbyyProcess.
 */
public class AbbyyProcess extends Ticket implements OCRProcess, Runnable {

	//TODO: Add this stuff: <OutputLocation>D:\Recognition\GDZ\output</OutputLocation>, it's now part of the ticket
	//TODO: Make sure that the Executor reads the size and count of the remote server
	//TODO: Save the stats of the remote system in a hidden file there
	//TODO: check if the OCRResult stuff is used correctly

	// The Constant logger.
	public final static Logger logger = LoggerFactory.getLogger(AbbyyProcess.class);

	//TODO: Use static fields from the engine class here.
	// The server url.
	protected static String serverURL;

	// The folder URLs.
	protected URL inputUrl, outputUrl, errorUrl;

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

	// The hotfolder.
	protected Hotfolder hotfolder;

	//TODO: Remove this
	// The ocr error format file.
	Set<String> ocrErrorFormatFile = new LinkedHashSet<String>();

	//TODO: Remove this
	// The ocr out format file.
	Set<String> ocrOutFormatFile = new LinkedHashSet<String>();

	//TODO: Add calculation of timeout, set it in the ticket.

	protected Long maxOCRTimeout;

	/**
	 * Instantiates a new process.
	 * 
	 * @param process
	 *            a OCR Process
	 */

	public AbbyyProcess(OCRProcess p) {
		super(p);
		hotfolder = new Hotfolder(config);
	}

	protected AbbyyProcess(ConfigParser config) {
		super();
		hotfolder = new Hotfolder(config);
	}

	private AbbyyProcess() {
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
		config = new ConfigParser().loadConfig();
		serverURL = config.getServerURL();
		maxOCRTimeout = config.maxOCRTimeout;
		oCRTimeOut = getOcrImages().size() * config.maxMillisPerFile;
		//millisPerFile = config.minMilisPerFile;
		try {
			inputUrl = new URL(serverURL + "/" + config.getInput() + "/");
			outputUrl = new URL(serverURL + "/" + config.getOutput() + "/");
			errorUrl = new URL(serverURL + "/" + config.getError() + "/");
		} catch (MalformedURLException e1) {
			logger.error("Can't setup server urls");
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
			URL remoteUrl = null;
			try {
				remoteUrl = new URL(inputUrl.toString() + remoteFileName);
			} catch (MalformedURLException e) {
				logger.error("Error contructing remote URL.");
				throw new OCRException(e);
			}
			if (aoi.getRemoteURL() == null) {
				aoi.setRemoteURL(remoteUrl);
			}
			if (!config.dryRun) {
				try {
					if (hotfolder.exists(remoteUrl)) {
						logger.warn("File alerady exists on server, deleting it");
						hotfolder.delete(remoteUrl);
					}
				} catch (FileSystemException e) {
					logger.error("Error while removing existing file.");
					throw new OCRException(e);
				}
			}
		}

		try {
			//Write ticket to temp file
			logger.debug("Creating Ticket");
			OutputStream os = hotfolder.createTmpFile(tmpTicket);
			write(os, name);
			os.close();

			logger.debug("Coping files to server.");
			//Copy the files
			if (!config.dryRun) {
				hotfolder.copyFilesToServer(fileInfos);
			}
			//Copy the ticket
			hotfolder.copyTmpFile(tmpTicket, new URL(inputUrl.toString() + tmpTicket));
			if (config.copyOnly) {
				logger.info("Process is in copy only mode, don't wait for results");
				return;
			}
			//Wait for results if needed
			Long wait = fileInfos.size() * config.minMillisPerFile;
			logger.info("Waiting " + wait + " milli seconds for results");
			Thread.sleep(wait);
			//Create a list of the files to check for
			List<URL> expectedResults = new ArrayList<URL>();
			Map<OCRFormat, OCROutput> outputs = getOcrOutput();
			for (OCRFormat output : outputs.keySet()) {
				String remoteUrl = ((AbbyyOCROutput) outputs.get(output)).getRemoteUrl().toString();
				expectedResults.add(new URL(remoteUrl));
				expectedResults.add(new URL(remoteUrl + config.reportSuffix));
			}
			Long timeout = getOcrImages().size() * config.maxMillisPerFile;
			try {
				if (waitForResults(expectedResults, timeout)) {
					//Everything should be ok, get the files
					for (OCRFormat output : outputs.keySet()) {
						String remoteUrl = ((AbbyyOCROutput) outputs.get(output)).getRemoteUrl().toString();
						String localUrl = outputs.get(output).getUrl().toString();
						logger.debug("Copy from " + remoteUrl + " to " + localUrl);
						hotfolder.copyFile(remoteUrl, localUrl);
						logger.debug("Getting result descriptor");
						hotfolder.copyFile(remoteUrl + config.reportSuffix, localUrl + config.reportSuffix);
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
		try {
			//TODO: failed isn't a shared state indicator
			while (!failed) {
				// for Output folder
				if (checkOutXmlResults(name)) {
					String resultOutURLPrefix = outputUrl + name;
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
						String resultErrorURLPrefix = errorUrl.toString() + name;
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
		} catch (MalformedURLException e) {
			logger.error("Processing failed (MalformedURLException)", e);
			failed = true;
			throw new OCRException(e);
		} finally {
			done = true;
			endTime = System.currentTimeMillis();
			logger.trace("AbbyyProcess " + name + " ended ");
		}
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
			if (info.getRemoteURL().toString() != null) {
				try {
					// Rewrite remote name
					Pattern pr = Pattern.compile(".*\\\\(.*)");
					Matcher mr = pr.matcher(info.getRemoteURL().toString());
					mr.find();
					//TODO: this is a dirty hack
					if (mr.group(1) == null) {
						throw new IllegalStateException();
					}
					String newRemoteName = name + "-" + mr.group(1);
					logger.trace("Rewiting " + info.getRemoteURL().toString() + " to " + newRemoteName);

					try {
						info.setRemoteURL(new URL(newRemoteName));
					} catch (MalformedURLException e) {
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
	protected Boolean checkOutXmlResults (String identifier) throws FileSystemException, MalformedURLException {
		String resultURLPrefix = outputUrl + identifier + "/" + identifier + config.reportSuffix;
		File resultURLPrefixpath = new File(resultURLPrefix);
		return hotfolder.exists(resultURLPrefixpath.toURI().toURL());
	}

	/**
	 * Check xml results in error folder If exists.
	 * 
	 * @return the boolean, true If exists.
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws MalformedURLException
	 */
	protected Boolean checkErrorXmlResults (String identifier) throws FileSystemException, MalformedURLException {
		String resultURLPrefix = errorUrl.toString() + identifier + "/" + identifier + config.reportSuffix;
		File resultURLPrefixpath = new File(resultURLPrefix);
		return hotfolder.exists(resultURLPrefixpath.toURI().toURL());
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
	private Boolean checkIfAllFilesExists (Set<String> checkfile, String url) throws FileSystemException, MalformedURLException {

		for (String fileName : checkfile) {
			File urlpath = new File(url + "/" + fileName);
			if (hotfolder.exists(urlpath.toURI().toURL())) {
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
	 */
	private int resultAllFilesNotExists (Set<String> checkfile, String url) throws FileSystemException, MalformedURLException {
		Integer result = 0;

		for (String fileName : checkfile) {
			if (hotfolder.exists(new URL(new File(url).getAbsolutePath() + "/" + fileName))) {
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
	 * @throws MalformedURLException
	 */
	//TODO: Use OCROutput here
	protected void copyAllFiles (Set<String> checkfile, String url, String localfile) throws FileSystemException, MalformedURLException {
		File urlpath = new File(url);
		hotfolder.mkDir(new URL(localfile + "/" + getName()));
		hotfolder.copyFile(urlpath.getAbsolutePath() + "/" + getName() + config.reportSuffix, localfile + "/"
				+ getName()
				+ "/"
				+ getName()
				+ config.reportSuffix);
		for (String fileName : checkfile) {
			hotfolder.copyFile(urlpath.getAbsolutePath() + "/" + fileName, localfile + "/" + getName() + "/" + fileName);
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
	 * @throws MalformedURLException
	 */
	//TODO: Remove this, it works with diretories
	private void deleteAllFiles (Set<String> checkfile, String url) throws FileSystemException, MalformedURLException {
		//TODO: Remove file from here
		String base = new File(url).getAbsolutePath();
		hotfolder.deleteIfExists(new URL(base + "/" + getName() + config.reportSuffix));
		for (String fileName : checkfile) {
			hotfolder.deleteIfExists(new URL(base + "/" + fileName));
		}
		hotfolder.deleteIfExists(new URL(base));
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

	protected Boolean waitForResults (List<URL> expectedFiles, Long timeout) throws TimeoutExcetion, InterruptedException, FileSystemException {
		Boolean check = true;
		while (check) {
			Integer successCounter = 0;
			for (URL u : expectedFiles) {
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
	
	public Long getDuration () {
		if (done) {
			return endTime - startTime;
		}
		return null;
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
