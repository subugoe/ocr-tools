package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.ConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.vfs.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.unigoettingen.sub.commons.util.file.FileExtensionsFilter;

/**
 * The Class AbbyyProcess.
 */
public class AbbyyProcess extends Ticket implements OCRProcess, Runnable {

	/** The Constant logger. */
	final static Logger logger = LoggerFactory.getLogger(AbbyyProcess.class);

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

	/** The list of the inputfiles. */
	private static List<File> inputFiles = null;

	/** The list of the language. */
	protected static List<Locale> langs;

	/** The write remote prefix. */
	protected static Boolean writeRemotePrefix = true;

	/** The copy only. */
	protected Boolean copyOnly = true;

	/** The dry run. */
	protected Boolean dryRun = false;

	/** The fix remote path. */
	protected Boolean fixRemotePath = true;

	/** The failed. */
	protected Boolean failed = false;

	/** The done. */
	protected Boolean done = true;

	/** The done date. */
	Date doneDate = null;

	/** The report suffix. */
	protected String reportSuffix = ".xml.result.xml";

	protected String reportSuffixforXml = ".xml";
	/** The extension. */
	protected static String extension = "tif";

	/** The hotfolder. */
	protected Hotfolder hotfolder;

	/** The image directory. */
	protected String imageDirectory;

	/** The identifier. */
	protected String identifier;

	/** The ocr error format file. */
	Set<String> ocrErrorFormatFile = new LinkedHashSet<String>();

	/** The ocr out format file. */
	Set<String> ocrOutFormatFile = new LinkedHashSet<String>();

	/** The file infos. */
	protected List<AbbyyOCRImage> fileInfos = null;
	protected List<AbbyyOCRImage> fileInfosreplacement = null;
	/** The config. */
	PropertiesConfiguration config;

	
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
	public AbbyyProcess(File dir) throws FileSystemException {
		super();
		hotfolder = new Hotfolder();
		this.imageDirectory = dir.getAbsolutePath();
		this.identifier = dir.getName();

	}

	public AbbyyProcess(OCRProcess p) {
		super(p);
	}

	protected AbbyyProcess() {
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run () {

		try {
			loadConfig(config);
			//TODO: Don't catch raw Exceptions
		} catch (Exception e) {
			logger.error(e.toString());
		}
		try {
			hotfolder = new Hotfolder();
		} catch (FileSystemException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		identifier = getName();
		//imageDirectory = getImageDirectory();

		//TODO: This is a bad hack.
		if (imageDirectory == null) {
			throw new RuntimeException("No directory given!");
		}
		try {
			fileInfos = getFileList(imageDirectory);
		} catch (FileSystemException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Integer wait;
		try {
			// engineConfig = createConfig(identifier);

			if (fixRemotePath) {
				fileInfos = fixRemotePath(fileInfos, identifier);

			}
			String inputDerectory = webdavURL + inputFolder + "/" + identifier;
			File inputDerectoryFile = new File(inputDerectory);
			if (!hotfolder.fileIfexists(inputDerectoryFile.getAbsolutePath())) {
				hotfolder.mkCol(hotfolder.stringToUrl(inputDerectoryFile.getAbsolutePath()));

			}

			//XMLTicket must be treated here
			while (done) {
				try {
					done = false;
					fileInfos = addTicketFile(new LinkedList<AbbyyOCRImage>(fileInfos), identifier);
				} catch (Exception e) {

					done = false;
					failed = true;
					copyOnly = false;
					hotfolder.deleteIfExists(inputDerectoryFile.getAbsolutePath());
					logger.trace(" Failed!! XMLTicket can not created for " + identifier);
					System.out.println(" Failed!! XMLTicket can not created for " + identifier);

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
							System.out.println("Second try!! copy images from " + identifier);
						}
						k++;
					} else {
						for (AbbyyOCRImage info : fileInfos) {
							hotfolder.deleteIfExists(info.getRemoteURL());
							logger.error("Second try!! copy images from " + identifier);
							System.out.println("images deleted from " + identifier);
						}
						File xmlTicket = new File(inputDerectoryFile.getAbsolutePath() + "/" + identifier + "/" + reportSuffixforXml);
						hotfolder.deleteIfExists(xmlTicket.getAbsolutePath());

						hotfolder.deleteIfExists(inputDerectoryFile.getAbsolutePath());
						logger.error("failed!!can not copy images from " + identifier);
						copyOnly = false;
						failed = true;

					}

				} catch (Exception e1) {
					logger.error("Got Exception", e1);
					throw new RuntimeException(e1);
				}
			}
			wait = fileInfos.size() * millisPerFile;
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
					ocrOutFormatFile = xmlresultOutputparse(new File(resultOutURLPrefixAbsolutePath));

					File moveToLocalpath = new File(moveToLocal);
					String moveToLocalAbsolutePath = moveToLocalpath.getAbsolutePath();

					// for Output folder
					if (checkIfAllFilesExists(ocrOutFormatFile, resultOutURLPrefix + "/")) {
						copyAllFiles(ocrOutFormatFile, resultOutURLPrefix, moveToLocalAbsolutePath);
						deleteAllFiles(ocrOutFormatFile, resultOutURLPrefix);
						failed = true;
						logger.info("Move Processing successfully to " + moveToLocalAbsolutePath);
					} else {

						wait = resultAllFilesNotExists(ocrOutFormatFile, resultOutURLPrefix) * millisPerFile + millisPerFile;
						Thread.sleep(wait);
						if (checkIfAllFilesExists(ocrOutFormatFile, resultOutURLPrefix + "/")) {
							copyAllFiles(ocrOutFormatFile, resultOutURLPrefix, moveToLocalAbsolutePath);
							deleteAllFiles(ocrOutFormatFile, resultOutURLPrefix);
							failed = true;
							logger.info("Move Processing is successfull to " + moveToLocalAbsolutePath);

						} else {
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
						// TODO bericht wird von hier abgeholt
						ocrErrorFormatFile = xmlresultErrorparse(new File(resultErrorURLPrefixAbsolutePath));
						if (checkIfAllFilesExists(ocrErrorFormatFile, resultErrorURLPrefix + "/")) {
							deleteAllFiles(ocrErrorFormatFile, resultErrorURLPrefix);
							failed = true;
							logger.info("delete All Files Processing is successfull ");
						} else {
							wait = resultAllFilesNotExists(ocrErrorFormatFile, resultErrorURLPrefix) * millisPerFile + millisPerFile;
							Thread.sleep(wait);
							if (checkIfAllFilesExists(ocrErrorFormatFile, resultErrorURLPrefix + "/")) {
								deleteAllFiles(ocrErrorFormatFile, resultErrorURLPrefix);
								failed = true;
								logger.info("delete All Files Processing is successfull ");
							} else {
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
			logger.error("Processing failed", e);
		} catch (XMLStreamException e) {
			logger.error("Processing failed", e);
		} catch (InterruptedException e) {
			logger.error("Processing failed", e);
		} finally {
			fileInfosreplacement = null;
			fileInfos = null;
			ocrErrorFormatFile = null;
			ocrOutFormatFile = null;
			inputFiles = null;
			logger.trace("AbbyyProcess " + identifier + " ended ");
			System.out.println("AbbyyProcess " + identifier + " ended ");
		}

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

	/**
	 * proof if number of files is in limit as defined in config-properties
	 * file, param maxFiles and proof overall filesize-limit as defined in
	 * config-properties file, param maxSize.
	 * 
	 * @param dir
	 *            the file system, where are all images
	 * @return the file list of the AbbyyOCRImage.
	 * @throws FileSystemException
	 */
	protected List<AbbyyOCRImage> getFileList (String imageDirectory) throws FileSystemException {
		//	List<File> files = makeFileList(dir, extension);

		Long size = 0l;
		fileInfos = new ArrayList<AbbyyOCRImage>();
		for (OCRImage i : getOcrImages()) {
			String imageName = i.getUrl().getPath();
			File imageNameFile = new File(imageName);
			size += imageNameFile.length();
			String remoteImageNamePath = webdavURL + inputFolder + "/" + identifier + "/" + imageNameFile.getName() + "/";
			File remoteFilepath = new File(remoteImageNamePath);
			URL remoteURL = hotfolder.stringToUrl(remoteFilepath.getAbsolutePath());

			AbbyyOCRImage aof = new AbbyyOCRImage(i.getUrl(), remoteURL, imageNameFile.getName());
			//hotfolder.urlToFile(i.getUrl());		
			//aof.setRemoteURL(null);
			fileInfos.add(aof);
		}

		/**
		 * proof if number of files is in limit as defined in config-properties
		 * file, param maxFiles
		 */

		if (maxFiles != 0 && fileInfos.size() > maxFiles) {
			logger.error("To much files (" + fileInfos.size() + "). The max amount of files is " + maxFiles + ". Stop processing!");
			throw new RuntimeException("To much files!");
		}

		/**
		 * proof overall filesize-limit as defined in config-properties, param
		 * maxSize
		 */

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
	public String windows2unixFileSeparator (String url) {
		return url.replace("\\", "/");
	}

	/**
	 * Calculate size.
	 * 
	 * @param files
	 *            is the List of files
	 * @return the long, size of all files
	 */
	public static Long calculateSize (List<File> files) {
		Long size = 0l;
		for (File file : files) {
			size += file.length();
		}
		return size;
	}

	/**
	 * very easy file filters, get all files which in the topical list, have
	 * "filter" as ending
	 * 
	 * @param inputFile
	 *            the input file, where are all images
	 * @param filter
	 *            or Extension
	 * @return the list of all files have "filter" as ending
	 */
	public static List<File> makeFileList (File inputFile, String filter) {
		List<File> fileList;
		if (inputFile.isDirectory()) {
			// OCR.logger.trace(inputFile + " is a directory");

			File files[] = inputFile.listFiles(new FileExtensionsFilter(filter));
			fileList = Arrays.asList(files);
			Collections.sort(fileList);

		} else {
			fileList = new ArrayList<File>();
			fileList.add(inputFile);
			// OCR.logger.trace("Input file: " + inputFile);
		}
		return fileList;
	}

	/**
	 * Fix remote path.
	 * 
	 * @param infoList
	 *            , is a List of AbbyyOCRImage
	 * @param name
	 *            for identifier
	 * @return the list of AbbyyOCRImage
	 */
	protected List<AbbyyOCRImage> fixRemotePath (List<AbbyyOCRImage> infoList, String name) {
		LinkedList<AbbyyOCRImage> newList = new LinkedList<AbbyyOCRImage>();
		for (AbbyyOCRImage info : infoList) {
			if (info.getRemoteURL().toString() != null) {
				try {
					// Rewrite remote name
					Pattern pr = Pattern.compile(".*\\\\(.*)");
					Matcher mr = pr.matcher(info.getRemoteURL().toString());
					mr.find();
					String newRemoteName = name + "-" + mr.group(1);
					logger.trace("Rewiting " + info.getRemoteURL().toString() + " to " + newRemoteName);

					try {
						info.setRemoteURL(new URL(newRemoteName));
					} catch (MalformedURLException e) {
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
	private LinkedList<AbbyyOCRImage> addTicketFile (LinkedList<AbbyyOCRImage> fileInfos, String ticketName) throws IOException {
		/* write Ticket-File over all Files: */

		String ticketFileName = ticketName + ".xml";
		String ticketTempDir = null;
		inputFiles = null;
		inputFiles = new ArrayList<File>();
		for (AbbyyOCRImage fileInfo : fileInfos) {
			if (fileInfo.getRemoteURL().toString() != null) {
				inputFiles.add(hotfolder.urlToFile(fileInfo.getRemoteURL()));
			}
			if (ticketTempDir == null) {
				ticketTempDir = webdavURL + inputFolder + "/" + identifier + "/" + ticketFileName;
			}
		}
		setInputFiles(inputFiles);
		// put XML-Ticket to webdav-server, inputFolder
		ticketFile = new File(ticketTempDir);
		// Writing Ticket
		// Ticket ticket = new Ticket(ticketFile);
		// ticket.setEngineConfig(engineConfig);
		// addLanguage(locale)
		addLanguage(Locale.GERMAN);
		addOCRFormat(OCRFormat.PDF);
		setOutPutLocation(webdavURL + outputFolder);

		//TODO: Commented these out, these methods aren't found
		//setMillisPerFile(millisPerFile);
		//setMaxOCRTimeout(maxOCRTimeout);
		write(ticketFile.getAbsoluteFile(), identifier);
		inputFiles = null;
		String outputFile = webdavURL + inputFolder + "/" + ticketFileName;
		/*fileInfos.addFirst(new AbbyyOCRImage(hotfolder.fileToURL(ticketFile),
				null, outputFile));*/
		logger.trace("Copy from " + ticketFile.getAbsolutePath() + " to " + ticketTempDir);
		return fileInfos;
	}

	/**
	 * Load config.
	 * 
	 * @param config
	 *            the config
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	public void loadConfig (PropertiesConfiguration config) throws ConfigurationException {
		// do something with config
		try {
			config = new PropertiesConfiguration("config-properties");
		} catch (org.apache.commons.configuration.ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		webdavURL = config.getString("remoteURL");
		webdavURL = webdavURL.endsWith("/") ? webdavURL : webdavURL + "/";
		if (webdavURL != null) {
			webdavURL = parseString(webdavURL);
		}
		moveToLocal = config.getString("moveToLocal");
		moveToLocal = moveToLocal.endsWith("/") ? moveToLocal : moveToLocal + "/";
		if (moveToLocal != null) {
			moveToLocal = parseString(moveToLocal);
		}

		inputFolder = config.getString("input");
		outputFolder = config.getString("output");
		errorFolder = config.getString("error");

		/*
		 * if (config.getString("//setting[@name='langs']/@value") != null &&
		 * !config.getString("//setting[@name='langs']/@value").equals("")) {
		 * langs =
		 * parseLangs(config.getString("//setting[@name='langs']/@value")); }
		 */
		if (config.getString("maxSize") != null && !config.getString("maxSize").equals("")) {
			maxSize = Long.parseLong(config.getString("maxSize"));
		}

		if (config.getString("maxFiles") != null && !config.getString("maxFiles").equals("")) {
			maxFiles = Long.parseLong(config.getString("maxFiles"));
		}

		// Add a preconfigred local output folder
		logger.debug("URL: " + webdavURL);

		logger.debug("Input folder: " + inputFolder);

		logger.debug("Max size: " + maxSize);
		logger.debug("Max files: " + maxFiles);

	}

	public static String parseString (String str) {
		String remoteFile = null;
		if (str.contains("/./")) {
			int i = 0;
			for (String lang : Arrays.asList(str.split("/./"))) {
				if (i == 0) {
					i++;
				} else {
					remoteFile = lang;
				}
			}
		}
		return remoteFile;

	}

	/**
	 * Check xml results in output folder If exists.
	 * 
	 * @return the boolean, true If exists
	 * @throws FileSystemException
	 *             the file system exception
	 */
	protected Boolean checkOutXmlResults () throws FileSystemException {
		String resultURLPrefix = webdavURL + outputFolder + "/" + identifier + "/" + identifier + reportSuffix;
		File resultURLPrefixpath = new File(resultURLPrefix);

		return hotfolder.fileIfexists(resultURLPrefixpath.getAbsolutePath());
	}

	/**
	 * Check xml results in error folder If exists.
	 * 
	 * @return the boolean, true If exists.
	 * @throws FileSystemException
	 *             the file system exception
	 */
	protected Boolean checkErrorXmlResults () throws FileSystemException {
		String resultURLPrefix = webdavURL + errorFolder + "/" + identifier + "/" + identifier + reportSuffix;
		File resultURLPrefixpath = new File(resultURLPrefix);
		return hotfolder.fileIfexists(resultURLPrefixpath.getAbsolutePath());
	}

	/**
	 * parse Xml result in output folder.
	 * 
	 * @param file
	 *            xml result in output folder
	 * @return the sets of all files Name, wich are in the xml file
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws XMLStreamException
	 *             the xML stream exception
	 */
	protected Set<String> xmlresultOutputparse (File file) throws FileNotFoundException, XMLStreamException {
		Set<String> ocrFormatFile = new LinkedHashSet<String>();
		String filename = null;
		final InputStream osmHamburgInStream = new FileInputStream(file);
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(osmHamburgInStream);
		try {
			while (xmlStreamReader.hasNext()) {
				int event = xmlStreamReader.next();
				if (event == XMLStreamConstants.START_ELEMENT) {
					// if the element 'NamingRule' found
					if (xmlStreamReader.getName().toString().equals("NamingRule")) {
						filename = xmlStreamReader.getElementText().toString();
						ocrFormatFile.add(filename);
					}
				}
			}
			logger.debug("the files which should be in output folder: " + ocrFormatFile);
		} finally {
			xmlStreamReader.close();
		}
		return ocrFormatFile;
	}

	/**
	 * parse Xml result in error folder and get Error description
	 * 
	 * @param file
	 *            xml result in error folder
	 * @return the sets of all files Name, wich are in the xml file
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws XMLStreamException
	 *             the xML stream exception
	 */
	protected Set<String> xmlresultErrorparse (File file) throws FileNotFoundException, XMLStreamException {
		Set<String> ocrErrorFile = new LinkedHashSet<String>();
		String error = null;

		final InputStream osmHamburgInStream = new FileInputStream(file);
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(osmHamburgInStream);
		try {
			while (xmlStreamReader.hasNext()) {
				int event = xmlStreamReader.next();
				if (event == XMLStreamConstants.START_ELEMENT) {
					// Error description
					if (xmlStreamReader.getName().toString().equals("Error")) {
						error = xmlStreamReader.getElementText();
					}
					// bilder die in verzeichnis befinden
					if (xmlStreamReader.getName().toString().equals("InputFile")) {
						// ber alle Attribute
						for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
							String attributeName = xmlStreamReader.getAttributeName(i).toString();
							// wenn version gefunden wurde
							if (attributeName.equals("Name")) {
								String str = xmlStreamReader.getAttributeValue(i);
								String[] results = str.split("}_");
								Boolean image = true;
								for (int j = 0; j < results.length; j++) {
									if (image) {
										image = false;
									} else {
										ocrErrorFile.add(results[j]);
										image = true;
									}
								}
							}
						}
					}
				}
			}
		} finally {
			xmlStreamReader.close();
		}
		logger.debug("Band Name " + identifier + " Error :" + error);
		return ocrErrorFile;
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
	 */
	protected Boolean checkIfAllFilesExists (Set<String> checkfile, String url) throws FileSystemException {

		for (String fileName : checkfile) {
			File urlpath = new File(url + "/" + fileName);
			if (hotfolder.fileIfexists(urlpath.getAbsolutePath())) {
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
	 */
	protected int resultAllFilesNotExists (Set<String> checkfile, String url) throws FileSystemException {
		int result = 0;
		File urlpath = new File(url);
		for (String fileName : checkfile) {
			if (hotfolder.fileIfexists(urlpath.getAbsolutePath() + "/" + fileName)) {
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
	 */
	protected void copyAllFiles (Set<String> checkfile, String url, String localfile) throws FileSystemException {
		File urlpath = new File(url);
		//URL folder = hotfolder.stringToUrl(urlpath.getAbsolutePath());
		URL localFolder = hotfolder.stringToUrl(localfile + "/" + identifier);
		hotfolder.mkCol(localFolder);
		hotfolder.copyAllFiles(urlpath.getAbsolutePath() + "/" + identifier + reportSuffix, localfile + "/" + identifier + "/" + identifier + reportSuffix);
		for (String fileName : checkfile) {
			//System.out.println(localfile + "/" + identifier + "/" + fileName);
			hotfolder.copyAllFiles(urlpath.getAbsolutePath() + "/" + fileName, localfile + "/" + identifier + "/" + fileName);
			logger.debug("Copy File From " + urlpath.getAbsolutePath() + "/" + fileName + " To" + localfile);
		}
		//hotfolder.deleteIfExists(folder);
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
	 */
	protected void deleteAllFiles (Set<String> checkfile, String url) throws FileSystemException {
		File urlpath = new File(url);
		hotfolder.deleteIfExists(urlpath.getAbsolutePath() + "/" + identifier + reportSuffix);
		for (String fileName : checkfile) {
			hotfolder.deleteIfExists(urlpath.getAbsolutePath() + "/" + fileName);
		}
		hotfolder.deleteIfExists(urlpath.getAbsolutePath());
	}

}
