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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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

	/** The Constant logger. */
	public final static Logger logger = LoggerFactory.getLogger(AbbyyProcess.class);

	/** The local path separator. */
	protected static String localPathSeparator = File.separator;
	// Variables used for process management
	/** The max size. */
	protected static Long maxSize = 5368709120l;

	/** The max files. */
	protected static Long maxFiles = 5000l;

	/** The file count. */
	protected Integer fileCount = 0;

	/** The file size. */
	protected Long fileSize = 0l;

	/** The webdav url. */
	protected static String webdavURL = null;

	/** local Url wich are moved a result */
	protected static String moveToLocal = null;

	/** The input folder. */
	protected static String inputFolder = null;

	/** The output folder. */
	protected static String outputFolder = null;

	/** The error folder. */
	protected static String errorFolder = null;

	/** The list of the language. */
	protected static List<Locale> langs;

	/** The write remote prefix. */
	protected static Boolean writeRemotePrefix = true;

	/** The copy only. */
	protected Boolean copyOnly = false;

	/** The dry run. */
	protected Boolean dryRun = false;

	/** The fix remote path. */
	protected Boolean fixRemotePath = false;

	/** The failed. */
	protected Boolean failed = false;

	/** The done. */
	protected Boolean done = true;

	// The done date.
	protected Date doneDate = null;

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
	// protected Integer secondsPerImage = 5;
	protected Integer millisPerFile = 1200;

	/**
	 * Instantiates a new process.
	 * 
	 * @param dir
	 *            the file system
	 * @throws FileSystemException
	 *             the file system exception
	 */
	/*
	public AbbyyProcess(File dir) throws FileSystemException {
		super();
		hotfolder = new Hotfolder();
		//this.imageDirectory = dir.getAbsolutePath();
		this.identifier = dir.getName();

	}*/

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

		try {
			config = new ConfigParser().loadConfig();
			//hotfolder = new Hotfolder();
		} catch (OCRException e) {
			logger.error("Can't create Hotfolder", e);
		}
		identifier = getName();

		List<AbbyyOCRImage> fileInfos = convertList(getOcrImages());

		Long wait;
		try {

			if (fixRemotePath) {
				fileInfos = fixRemotePath(fileInfos, identifier);

			}
			String inputDerectory = webdavURL + inputFolder + "/" + identifier;
			File inputDirectoryFile = new File(inputDerectory);
			if (!hotfolder.exists(inputDirectoryFile.toURI().toURL())) {
				hotfolder.mkDir(inputDirectoryFile.toURI().toURL());
			}

			//XMLTicket must be treated here
			while (done) {
				try {
					done = false;
					fileInfos = addTicketFile(new LinkedList<AbbyyOCRImage>(fileInfos), identifier);
				} catch (FileSystemException e) {
					done = false;
					failed = true;
					copyOnly = false;
					hotfolder.deleteIfExists(inputDirectoryFile.toURI().toURL());
					logger.error(" Failed!! XMLTicket can not created for " + identifier, e);
				} catch (IOException e) {
					done = false;
					failed = true;
					copyOnly = false;
					hotfolder.deleteIfExists(inputDirectoryFile.toURI().toURL());
					logger.error(" Failed!! XMLTicket can not created for " + identifier, e);
				}
			}
			//copy must be treated here
			int k = 0;
			while (copyOnly) {
				try {
					copyOnly = false;
					hotfolder.copyFilesToServer(fileInfos);
				} catch (FileSystemException e) {
					logger.error("Got FileSystemException ", e);
					if (k == 0 || k == 1) {
						copyOnly = true;
						for (AbbyyOCRImage info : fileInfos) {
							hotfolder.deleteIfExists(info.getRemoteURL());
							logger.error("Second try!! copy images from " + identifier);
						}
						if (k == 1) {
							File xmlTicket = new File(inputDirectoryFile.getAbsolutePath() + "/" + identifier + "/" + reportSuffixforXml);
							hotfolder.deleteIfExists(xmlTicket.toURI().toURL());
							hotfolder.deleteIfExists(inputDirectoryFile.toURI().toURL());
							logger.error("failed!!can not copy images from " + identifier);
							copyOnly = false;
							failed = true;
						}
						k++;
					}

					/*
					} catch (FileSystemException e) {
					logger.error("Got Exception", e);
					throw new OCRException(e);
					*/
				}
			}
			wait = fileInfos.size() * new Long(millisPerFile);
			logger.info("Waiting " + wait + " milli seconds");

			Thread.sleep(wait);

			while (!failed) {
				int firstwait = 0;
				// for Output folder
				if (checkOutXmlResults()) {
					String resultOutURLPrefix = webdavURL + outputFolder + "/" + identifier;
					File resultOutURLPrefixpath = new File(resultOutURLPrefix + "/" + identifier + reportSuffix);
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
						if (faktor == 1 && !failed) {
							wait = resultAllFilesNotExists(ocrOutFormatFile, resultOutURLPrefix) * new Long(millisPerFile) + millisPerFile;
							Thread.sleep(wait);
						}
						if (faktor == 2 && !failed) {
							failed = true;
							logger.error("failed!!TimeoutExcetion for Move Processing, All files Not exists in " + resultOutURLPrefix);
						}
					}
				} else {
					// for Error folder
					if (checkErrorXmlResults()) {
						String resultErrorURLPrefix = webdavURL + errorFolder + "/" + identifier;
						File resultErrorURLPrefixpath = new File(resultErrorURLPrefix + "/" + identifier + reportSuffix);
						String resultErrorURLPrefixAbsolutePath = resultErrorURLPrefixpath.getAbsolutePath();
						// TODO: Get the result report
						ocrErrorFormatFile = XmlParser.xmlresultErrorparse(new File(resultErrorURLPrefixAbsolutePath), identifier);
						for (int index = 1; index <= 2; index++) {
							if (checkIfAllFilesExists(ocrErrorFormatFile, resultErrorURLPrefix + "/")) {
								deleteAllFiles(ocrErrorFormatFile, resultErrorURLPrefix);
								failed = true;
								logger.info("delete All Files Processing is successfull ");
							}
							if (index == 1 && !failed) {
								wait = resultAllFilesNotExists(ocrErrorFormatFile, resultErrorURLPrefix) * new Long(millisPerFile) + millisPerFile;
								Thread.sleep(wait);
							}
							if (index == 2 && !failed) {
								failed = true;
								logger.error("failed!! TimeoutExcetion for delete All Files Processing, All files Not exists in!! " + resultErrorURLPrefix);
							}
						}
					}
				}
				if (firstwait == 0) {
					Thread.sleep(wait / 2);
					firstwait++;
				} else {
					failed = true;
				}
			}

		} catch (FileSystemException e) {
			logger.error("Processing failed", e);
			failed = true;
			throw new RuntimeException(e);

		} catch (FileNotFoundException e) {
			logger.error("Processing failed (FileNotFoundException)", e);
		} catch (XMLStreamException e) {
			logger.error("Processing failed (XMLStreamException)", e);
		} catch (InterruptedException e) {
			logger.error("Processing failed (InterruptedException)", e);
		} catch (MalformedURLException e) {
			logger.error("Processing failed (MalformedURLException)", e);
		} finally {
			//fileInfosreplacement = null;
			fileInfos = null;
			ocrErrorFormatFile = null;
			ocrOutFormatFile = null;
			//inputFiles = null;
			logger.trace("AbbyyProcess " + identifier + " ended ");
		}

	}

	/**
	 * proof if number of files is in limit as defined in config-properties
	 * file, param maxFiles and proof overall filesize-limit as defined in
	 * config-properties file, param maxSize.
	 * 
	 * @param dir
	 *            the file system, where are all images
	 * @return the file list of the AbbyyOCRImage.
	 * @throws FileSystemException
	 * @throws MalformedURLException
	 */
	//TODO: Remove this.
	//TODO: remove size calculation
	private List<AbbyyOCRImage> getFileList (String imageDirectory) throws FileSystemException, MalformedURLException {
		Long size = 0l;
		List<AbbyyOCRImage> fileInfos = new ArrayList<AbbyyOCRImage>();
		for (OCRImage i : getOcrImages()) {
			String imageName = i.getUrl().getPath();
			File imageNameFile = new File(imageName);
			size += imageNameFile.length();
			String remoteImageNamePath = webdavURL + inputFolder + "/" + identifier + "/" + imageNameFile.getName() + "/";
			File remoteFilepath = new File(remoteImageNamePath);
			URL remoteURL = remoteFilepath.toURI().toURL();

			AbbyyOCRImage aof = new AbbyyOCRImage(i.getUrl(), remoteURL, imageNameFile.getName());
			fileInfos.add(aof);
		}

		// proof if number of files is in limit as defined in config-properties file, param maxFiles

		if (maxFiles != 0 && fileInfos.size() > maxFiles) {
			logger.error("To much files (" + fileInfos.size() + "). The max amount of files is " + maxFiles + ". Stop processing!");
			throw new RuntimeException("To much files!");
		}

		// proof overall filesize-limit as defined in config-properties, param maxSize

		if (maxSize != 0 && size > maxSize) {
			logger.error("Filesize to much (" + size + "Byte). The max size of all files is " + maxSize + "Byte. Stop processing!");
			throw new RuntimeException("Filesize to much!");
		}

		this.fileCount = getOcrImages().size();
		this.fileSize = size;

		return fileInfos;
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
	 * Adds the ticket file.
	 * 
	 * @param fileInfos
	 *            the list of the AbbyyOCRImage
	 * @param ticketName
	 *            the ticket name
	 * @return the list of the AbbyyOCRImage
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	//TODO: Keep the ticket in ram or tmp and add it on demand
	private LinkedList<AbbyyOCRImage> addTicketFile (LinkedList<AbbyyOCRImage> fileInfos, String ticketName) throws IOException {

		String ticketFileName = ticketName + ".xml";
		String ticketTempDir = null;

		if (ticketTempDir == null) {
			ticketTempDir = webdavURL + inputFolder + "/" + identifier + "/" + ticketFileName;
		}

		File ticketFile = new File(ticketTempDir);
		write(ticketFile.getAbsoluteFile(), identifier);
		logger.trace("Copy from " + ticketFile.getAbsolutePath() + " to " + ticketTempDir);
		return fileInfos;
	}

	/**
	 * Check xml results in output folder If exists.
	 * 
	 * @return the boolean, true If exists
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws MalformedURLException
	 */
	protected Boolean checkOutXmlResults () throws FileSystemException, MalformedURLException {
		String resultURLPrefix = webdavURL + outputFolder + "/" + identifier + "/" + identifier + reportSuffix;
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
	protected Boolean checkErrorXmlResults () throws FileSystemException, MalformedURLException {
		String resultURLPrefix = webdavURL + errorFolder + "/" + identifier + "/" + identifier + reportSuffix;
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

	public static AbbyyProcess createProcessFromDir (File directory, String extension) throws MalformedURLException {
		AbbyyProcess ap = new AbbyyProcess();
		List<File> imageDirs = getImageDirectories(directory, extension);

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

	//TODO: Move this
	public static List<File> getImageDirectories (File dir, String extension) {
		List<File> dirs = new ArrayList<File>();

		if (AbstractOCRProcess.makeFileList(dir, extension).size() > 0) {
			dirs.add(dir);
		}

		List<File> fileList;
		if (dir.isDirectory()) {
			fileList = Arrays.asList(dir.listFiles());
			for (File file : fileList) {
				if (file.isDirectory()) {
					List<File> files = AbstractOCRProcess.makeFileList(dir, extension);
					for (File f : files) {
						logger.debug("File: " + f.getAbsolutePath());
					}
					if (files.size() > 0) {
						dirs.addAll(files);
					} else {
						dirs.addAll(getImageDirectories(file, extension));
					}
				}
			}
		} else {
			throw new IllegalStateException(dir.getAbsolutePath() + " is not a directory");
		}
		return dirs;
	}

	protected static List<AbbyyOCRImage> convertList (List<OCRImage> ocrImages) {
		List<AbbyyOCRImage> images = new LinkedList<AbbyyOCRImage>();
		for (OCRImage i : ocrImages) {
			images.add((AbbyyOCRImage) i);
		}
		return images;
	}

	/**
	 * The Class TimeoutExcetion.
	 */
	public class TimeoutExcetion extends Exception {

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
