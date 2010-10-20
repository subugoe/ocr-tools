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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.vfs.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

/**
 * The Class AbbyyProcess.
 */
public class AbbyyProcess extends Ticket implements OCRProcess, Runnable {

	//TODO: Add this stuff: <OutputLocation>D:\Recognition\GDZ\output</OutputLocation>
	//TODO: Check if timeout is written, add a test for this
	//TODO: User ram or tmp file system for ticket file

	// The Constant logger.
	public final static Logger logger = LoggerFactory.getLogger(AbbyyProcess.class);

	// The local path separator.
	protected final static String localPathSeparator = File.separator;

	//TODO: Use static fields from the engine class here.
	// The server url.
	protected final static String serverURL = null;

	// The output folder.
	protected final static String outputFolder = null;

	// The error folder.
	protected final static String errorFolder = null;

	//TODO: Try to get rid of this
	// local Url wich are moved a result 
	protected final static String moveToLocal = null;
	
	// The write remote prefix.
	//TODO: check if we need this
	protected final static Boolean writeRemotePrefix = true;

	// The copy only.
	protected Boolean copyOnly = false;

	// The dry run.
	protected Boolean dryRun = false;

	/// The fix remote path.
	protected Boolean fixRemotePath = false;

	// The failed.
	protected Boolean failed = false;

	// The done.
	protected Boolean done = true;

	// The done date.
	protected Long startTime = null;
	
	// The done date.
	protected Long doneTime = null;

	// The report suffix.
	protected String reportSuffix = ".xml.result.xml";

	protected String reportSuffixforXml = ".xml";

	// The hotfolder.
	protected Hotfolder hotfolder;

	// The identifier.
	//TODO: reuse name of AbstractOCRProcess
	protected String identifier;

	// The ocr error format file.
	Set<String> ocrErrorFormatFile = new LinkedHashSet<String>();

	// The ocr out format file.
	Set<String> ocrOutFormatFile = new LinkedHashSet<String>();

	//protected List<AbbyyOCRImage> fileInfosreplacement = null;
	// The configuration.
	protected ConfigParser config;

	//TODO: Add calculation of timeout, set it in the ticket.
	// Two hours by default
	protected Long maxOCRTimeout = 3600000l * 2;
	protected Integer millisPerFile = 1200;

	/**
	 * Instantiates a new process.
	 * 
	 * @param process
	 *            a OCR Process
	 */

	public AbbyyProcess(OCRProcess p) {
		super(p);
		hotfolder = new Hotfolder();
	}

	protected AbbyyProcess() {
		super();
		hotfolder = new Hotfolder();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run () {
		//TODO Break up this method
		startTime = System.currentTimeMillis();

		config = new ConfigParser().loadConfig();
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
		try {
			//Write ticket to temp file
			logger.debug("Creating Ticket");
			OutputStream os = hotfolder.createTmpFile(tmpTicket);
			write(os, name);
			os.close();

			logger.debug("Coping files to server.");
			//Copy the files
			if (!dryRun) {
				for (AbbyyOCRImage aoi: fileInfos) {
					//TODO: Fail if remoteUrl is null
					//Here is an error somewhere
					URL remoteUrl = aoi.getRemoteURL();
					if (hotfolder.exists(remoteUrl)) {
						logger.warn("File alerady exists on server, deleting it");
						hotfolder.delete(remoteUrl);
					}
				}
				hotfolder.copyFilesToServer(fileInfos);
			}
			//Copy the ticket
			hotfolder.copyTmpFile(tmpTicket, new URL(hotfolder  + tmpTicket));

		} catch (FileSystemException e) {
			failed = true;
			logger.error("Couldn't write files to server URL", e);
		} catch (IOException e) {
			failed = true;
			logger.error("Error writing ticket", e);
		} catch (InterruptedException e) {
			failed = true;
			logger.error("OCR Process was interrupted while coping files to server.", e);
		} catch (URISyntaxException e) {
			logger.error("Error seting tmp URI for ticket", e);
			failed = true;
		}

		//Wait for results if needed
		if (!failed && !copyOnly) {
			Long wait = fileInfos.size() * new Long(millisPerFile);
			logger.info("Waiting " + wait + " milli seconds for results");

			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
				failed = true;
				logger.error("OCR Process was interrupted while waiting for results.", e);
			}
			//Get files here
			
			/*
			if (checkOutXmlResults(name)) {
				
			}
			*/
			
			
		}

		try {


			//TODO: failed isn't a shared state indicator
			while (!failed) {
				//int firstwait = 0;
				// for Output folder
				if (checkOutXmlResults(name)) {
					String resultOutURLPrefix = serverURL + outputFolder + "/" + name;
					File resultOutURLPrefixpath = new File(resultOutURLPrefix + "/" + name + reportSuffix);
					String resultOutURLPrefixAbsolutePath = resultOutURLPrefixpath.getAbsolutePath();
					// TODO Erkennungsrat muss noch ausgelesen werden(ich
					// wei das eigentlich nicht deswegen ist noch offen)
					ocrOutFormatFile = XmlParser.xmlresultOutputparse(new File(resultOutURLPrefixAbsolutePath));
					File moveToLocalpath = new File(moveToLocal);
					String moveToLocalAbsolutePath = moveToLocalpath.getAbsolutePath();

					// for Output folder
					for (int faktor = 1; faktor <= 2; faktor++) {
						if (checkIfAllFilesExists(ocrOutFormatFile, resultOutURLPrefix + "/")) {
							copyAllFiles(ocrOutFormatFile, resultOutURLPrefix, moveToLocalAbsolutePath);
							deleteAllFiles(ocrOutFormatFile, resultOutURLPrefix);
							failed = true;
							logger.info("Move Processing successfully to " + moveToLocalAbsolutePath);
						}
						/*
						if (faktor == 1 && !failed) {
							//TODO: Don't wait again
							//wait = resultAllFilesNotExists(ocrOutFormatFile, resultOutURLPrefix) * new Long(millisPerFile) + millisPerFile;
							//Thread.sleep(wait);
						}
						
						if (faktor == 2 && !failed) {
							failed = true;
							logger.error("failed!!TimeoutExcetion for Move Processing, All files Not exists in " + resultOutURLPrefix);
						}
						*/
					}
				} else {
					// for Error folder
					if (checkErrorXmlResults(name)) {
						String resultErrorURLPrefix = serverURL + errorFolder + "/" + name;
						File resultErrorURLPrefixpath = new File(resultErrorURLPrefix + "/" + name + reportSuffix);
						String resultErrorURLPrefixAbsolutePath = resultErrorURLPrefixpath.getAbsolutePath();
						// TODO: Get the result report
						ocrErrorFormatFile = XmlParser.xmlresultErrorparse(new File(resultErrorURLPrefixAbsolutePath), name);
						for (int index = 1; index <= 2; index++) {
							if (checkIfAllFilesExists(ocrErrorFormatFile, resultErrorURLPrefix + "/")) {
								deleteAllFiles(ocrErrorFormatFile, resultErrorURLPrefix);
								failed = true;
								logger.info("delete All Files Processing is successfull ");
							}
							/*
							if (index == 1 && !failed) {
								//wait = resultAllFilesNotExists(ocrErrorFormatFile, resultErrorURLPrefix) * new Long(millisPerFile) + millisPerFile;
								//Thread.sleep(wait);
							}
							
							if (index == 2 && !failed) {
								failed = true;
								logger.error("failed!! TimeoutExcetion for delete All Files Processing, All files Not exists in!! " + resultErrorURLPrefix);
							}
							*/
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
			logger.trace("AbbyyProcess " + name + " ended ");
		}
		doneTime = System.currentTimeMillis();
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
		String resultURLPrefix = serverURL + outputFolder + "/" + identifier + "/" + identifier + reportSuffix;
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
		String resultURLPrefix = serverURL + errorFolder + "/" + identifier + "/" + identifier + reportSuffix;
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
	protected Boolean checkIfAllFilesExists (Set<String> checkfile, String url) throws FileSystemException, MalformedURLException {

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
	protected int resultAllFilesNotExists (Set<String> checkfile, String url) throws FileSystemException, MalformedURLException {
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
	protected void copyAllFiles (Set<String> checkfile, String url, String localfile) throws FileSystemException, MalformedURLException {
		File urlpath = new File(url);
		hotfolder.mkDir(new URL(localfile + "/" + identifier));
		hotfolder.copyFile(urlpath.getAbsolutePath() + "/" + identifier + reportSuffix, localfile + "/" + identifier + "/" + identifier + reportSuffix);
		for (String fileName : checkfile) {
			hotfolder.copyFile(urlpath.getAbsolutePath() + "/" + fileName, localfile + "/" + identifier + "/" + fileName);
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
	protected void deleteAllFiles (Set<String> checkfile, String url) throws FileSystemException, MalformedURLException {
		//TODO: Remove file from here
		String base = new File(url).getAbsolutePath();
		hotfolder.deleteIfExists(new URL(base + "/" + identifier + reportSuffix));
		for (String fileName : checkfile) {
			hotfolder.deleteIfExists(new URL(base + "/" + fileName));
		}
		hotfolder.deleteIfExists(new URL(base));
	}

	//TODO: check if remoteURL is set correctly
	public static AbbyyProcess createProcessFromDir (File directory, String extension) throws MalformedURLException {
		AbbyyProcess ap = new AbbyyProcess();
		List<File> imageDirs = AbstractOCRProcess.getImageDirectories(directory, extension);

		for (File id : imageDirs) {
			if (imageDirs.size() > 1) {
				logger.error("Directory " + directory.getAbsolutePath() + " contains more then one image directories");
				throw new OCRException("createProcessFromDir can currently create only one AbbyyProcess!");
			}
			String jobName = id.getName();
			for (File imageFile : AbstractOCRProcess.makeFileList(id, extension)) {
				ap.setName(jobName);
				AbbyyOCRImage aoi = new AbbyyOCRImage(imageFile.toURI().toURL());
				aoi.setSize(imageFile.length());
				ap.addImage(aoi);
			}
		}

		return ap;
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
