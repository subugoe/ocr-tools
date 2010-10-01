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

public class Process extends Ticket implements Runnable {
	final static Logger logger = LoggerFactory.getLogger(Process.class);

	protected static String localPathSeparator = File.separator;
	// Variables used for process management
	protected static Long maxSize = 5368709120l;
	protected static Long maxFiles = 5000l;

	protected Integer fileCount = 0;
	protected Long fileSize = 0l;

	protected static String webdavURL = null;
	protected static String inputFolder = null;
	protected static String outputFolder = null;
	protected static String errorFolder = null;
	private static List<File> inputFiles = new ArrayList<File>();

	protected static List<Locale> langs;

	protected static Boolean writeRemotePrefix = true;
	protected Boolean copyOnly = true;
	protected Boolean dryRun = false;
	protected Boolean fixRemotePath = true;
	protected Boolean failed = false;
	protected Boolean done = false;
	Date doneDate = null;

	protected String reportSuffix = "xml.result.xml";
	protected static String extension = "tif";
	protected Hotfolder hotfolder;
	protected String imageDirectory;
	protected String identifier;

	Set<String> ocrErrorFormatFile = new LinkedHashSet<String>();
	Set<String> ocrOutFormatFile = new LinkedHashSet<String>();
	protected List<AbbyyOCRFile> fileInfos = null;

	PropertiesConfiguration config;

	public Process(File dir) throws FileSystemException {
		super();
		hotfolder = new Hotfolder();
		this.imageDirectory = dir.getAbsolutePath();
		this.identifier = dir.getName();

	}

	@Override
	public void run() {

		try {
			loadConfig(config);
		//TODO: Don't catch raw Exceptions
		} catch (Exception e) {
			logger.error(e.toString());
		}

		fileInfos = getFileList(new File(imageDirectory));

		Integer wait;
		try {
			// engineConfig = createConfig(identifier);

			if (fixRemotePath) {
				fileInfos = fixRemotePath(fileInfos, identifier);

			}
			fileInfos = addTicketFile(new LinkedList<AbbyyOCRFile>(fileInfos),
					identifier);

			hotfolder.copyFilesToServer(fileInfos);

			// lockManager.unlockGroup(identifier);
			wait = fileInfos.size() * millisPerFile;
			logger.info("Waiting " + wait + " milli seconds");

			Thread.sleep(wait);

			// remoteFiles = getResults();
			
				while (!failed) {
					// for Output folder
					if (checkOutXmlResults()) {
						String resultOutURLPrefix = webdavURL + outputFolder
								+ "/" + identifier;
						// TODO Erkennungsrat muss noch ausgelesen werden(ich
						// wei das eigentlich nicht deswegen ist noch offen)
						ocrOutFormatFile = xmlresultOutputparse(new File(
								resultOutURLPrefix + reportSuffix));
						// TODO muss ich diese locale URL wissen, wo die Dateien
						// veschieben werden sollen
						String local = null;
						// for Output folder
						if (checkIfAllFilssExists(ocrOutFormatFile,
								resultOutURLPrefix)) {
							copyAllFiles(ocrOutFormatFile, resultOutURLPrefix,
									local);
							deleteAllFiles(ocrOutFormatFile, resultOutURLPrefix);
							failed = true;
							logger.info("Move Processing successfully to "
									+ resultOutURLPrefix);
						} else {
							// es soll nochmal gewartet werden um zu prfen ob
							// alles da ist
							Thread.sleep(wait / 20);
							if (checkIfAllFilssExists(ocrOutFormatFile,
									resultOutURLPrefix)) {
								copyAllFiles(ocrOutFormatFile,
										resultOutURLPrefix, local);
								deleteAllFiles(ocrOutFormatFile,
										resultOutURLPrefix);
								failed = true;
								logger.info("Move Processing is successfull to "
										+ resultOutURLPrefix);

							} else {
								failed = true;
								logger.error("failed!!TimeoutExcetion for Move Processing, All files Not exists in "
										+ resultOutURLPrefix);
							}
						}
					} else {
						// for Error folder
						if (checkErrorXmlResults()) {
							String resultErrorURLPrefix = webdavURL
									+ errorFolder + "/" + identifier;
							// TODO bericht wird von hier angeholt
							ocrErrorFormatFile = xmlresultErrorparse(new File(
									resultErrorURLPrefix + reportSuffix));
							if (checkIfAllFilssExists(ocrErrorFormatFile,
									resultErrorURLPrefix)) {
								deleteAllFiles(ocrErrorFormatFile,
										resultErrorURLPrefix);
								failed = true;
								logger.info("delete All Files Processing is successfull ");
							} else {
								// es soll nochmal gewartet werden um zu prfen
								// ob alles da ist
								Thread.sleep(wait / 20);
								if (checkIfAllFilssExists(ocrErrorFormatFile,
										resultErrorURLPrefix)) {
									deleteAllFiles(ocrErrorFormatFile,
											resultErrorURLPrefix);
									failed = true;
									logger.info("delete All Files Processing is successfull ");
								} else {
									failed = true;
									logger.error("failed!! TimeoutExcetion for delete All Files Processing, All files Not exists in!! "
											+ resultErrorURLPrefix);
								}
							}
						}
					}
				}

			} catch (Exception e) {
				logger.error("Processing failed", e);
				failed = true;
				throw new RuntimeException(e);

			} finally {
				fileInfos = null;
				ocrErrorFormatFile = null;
				ocrOutFormatFile = null;
			}

	}

	public class TimeoutExcetion extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3002142265497735648L;

		public TimeoutExcetion() {
			super();
		}
	}
	protected LinkedList<AbbyyOCRFile> getFileList(File dir) {
		List<File> files = makeFileList(dir, extension);

		/*
		 * proof if number of files is in limit as defined in xml-configfile,
		 * param maxFiles
		 */

		if (maxFiles != 0 && files.size() > maxFiles) {
			logger.error("To much files (" + files.size()
					+ "). The max amount of files is " + maxFiles
					+ ". Stop processing!");
			throw new RuntimeException("To much files!");
		}

		/*
		 * proof overall filesize-limit as defined in xml-configfile, param
		 * maxSize
		 */
		Long size = calculateSize(files);

		if (maxSize != 0 && size > maxSize) {
			logger.error("Filesize to much (" + size
					+ "Byte). The max size of all files is " + maxSize
					+ "Byte. Stop processing!");
			throw new RuntimeException("Filesize to much!");
		}

		LinkedList<AbbyyOCRFile> fileInfoList = new LinkedList<AbbyyOCRFile>();

		this.fileCount = files.size();
		this.fileSize = size;

		String dirName = dir.getName();

		String uriPrefix = webdavURL + inputFolder + "/" + dirName + "/";
		URL imageUrl = hotfolder.fileToURL(dir);
		fileInfoList.add(new AbbyyOCRFile(imageUrl, null, uriPrefix));

		for (File localFile : files) {
			StringBuffer remoteFile = new StringBuffer("");
			if (writeRemotePrefix) {
				remoteFile.append(webdavURL);
				remoteFile.append(inputFolder).append("/");
			}
			remoteFile.append(dirName).append("/");
			remoteFile.append(localFile.getName());

			logger.trace(localFile.getAbsolutePath() + " -> "
					+ remoteFile.toString());

			String outputFile = uriPrefix
					+ windows2unixFileSeparator(localFile.getName());

			URL imageUrlNew = hotfolder.fileToURL(localFile);
			URL remoteURL = null;
			try {
				remoteURL = new URL(remoteFile.toString());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			AbbyyOCRFile fileInfo = new AbbyyOCRFile(imageUrlNew, remoteURL,
					outputFile);
			fileInfoList.add(fileInfo);
		}
		return fileInfoList;
	}

	public String windows2unixFileSeparator(String url) {
		return url.replace("\\", "/");
	}

	public static Long calculateSize(List<File> files) {
		Long size = 0l;
		for (File file : files) {
			size += file.length();
		}
		return size;
	}

	public static List<File> makeFileList(File inputFile, String filter) {
		List<File> fileList;
		if (inputFile.isDirectory()) {
			// OCR.logger.trace(inputFile + " is a directory");

			File files[] = inputFile.listFiles(new FileExtensionFilter(filter));
			fileList = Arrays.asList(files);
			Collections.sort(fileList);

		} else {
			fileList = new ArrayList<File>();
			fileList.add(inputFile);
			// OCR.logger.trace("Input file: " + inputFile);
		}
		return fileList;
	}

	protected List<AbbyyOCRFile> fixRemotePath(List<AbbyyOCRFile> infoList,
			String name) {
		LinkedList<AbbyyOCRFile> newList = new LinkedList<AbbyyOCRFile>();
		for (AbbyyOCRFile info : infoList) {
			if (info.getRemoteURL().toString() != null) {
				try {
					// Rewrite remote name
					Pattern pr = Pattern.compile(".*\\\\(.*)");
					Matcher mr = pr.matcher(info.getRemoteURL().toString());
					mr.find();
					String newRemoteName = name + "-" + mr.group(1);
					logger.trace("Rewiting " + info.getRemoteURL().toString()
							+ " to " + newRemoteName);

					try {
						info.setRemoteURL(new URL(newRemoteName));
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}

					// rewrite webdav name
					Pattern pw = Pattern.compile("(.*/).*(/.*)");
					Matcher mw = pw.matcher(info.getRemoteFileName());
					mw.find();
					String newWebDavName = mw.group(1) + name + "-"
							+ mw.group(2).substring(1);
					logger.trace("Rewriting " + info.getRemoteFileName()
							+ " to " + newWebDavName);
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

	private LinkedList<AbbyyOCRFile> addTicketFile(
			LinkedList<AbbyyOCRFile> fileInfos, String ticketName)
			throws IOException {
		/* write Ticket-File over all Files: */
		String ticketFileName = ticketName + ".xml";
		String ticketTempDir = null;
		for (AbbyyOCRFile fileInfo : fileInfos) {
			if (fileInfo.getRemoteURL().toString() != null) {
				// engineConfig.addFile(fileInfo.getRemoteFileName());
				inputFiles.add(hotfolder.urlToFile(fileInfo.getRemoteURL()));
			}
			if (ticketTempDir == null) {
				ticketTempDir = fileInfo.getRemoteFileName();
			}
		}
		setInputFiles(inputFiles);
		// put XML-Ticket to webdav-server, inputFolder
		ticketFile = new File(ticketTempDir + ticketFileName);
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
		write(ticketFile);

		String outputFile = webdavURL + inputFolder + "/" + ticketFileName;
		fileInfos.addFirst(new AbbyyOCRFile(hotfolder.fileToURL(ticketFile),
				null, outputFile));
		logger.trace("Copy from " + ticketFile.getAbsolutePath() + " to "
				+ outputFile);
		return fileInfos;
	}

	public void loadConfig(PropertiesConfiguration config)
			throws ConfigurationException {
		// do something with config
		try {
			config = new PropertiesConfiguration("config-properties");
		} catch (org.apache.commons.configuration.ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		webdavURL = config.getString("base");
		webdavURL = webdavURL.endsWith("/") ? webdavURL : webdavURL + "/";
		inputFolder = config.getString("input");
		outputFolder = config.getString("output");
		errorFolder = config.getString("error");

		/*
		 * if (config.getString("//setting[@name='langs']/@value") != null &&
		 * !config.getString("//setting[@name='langs']/@value").equals("")) {
		 * langs =
		 * parseLangs(config.getString("//setting[@name='langs']/@value")); }
		 */
		if (config.getString("maxSize") != null
				&& !config.getString("maxSize").equals("")) {
			maxSize = Long.parseLong(config.getString("maxSize"));
		}

		if (config.getString("maxFiles") != null
				&& !config.getString("maxFiles").equals("")) {
			maxFiles = Long.parseLong(config.getString("maxFiles"));
		}

		// Add a preconfigred local output folder
		logger.debug("URL: " + webdavURL);

		logger.debug("Input folder: " + inputFolder);

		logger.debug("Max size: " + maxSize);
		logger.debug("Max files: " + maxFiles);

	}

	// copy files back
	protected Boolean checkOutXmlResults() throws FileSystemException {
		String resultURLPrefix = webdavURL + outputFolder + "/" + identifier
				+ reportSuffix;
		return hotfolder.fileIfexists(resultURLPrefix);
	}

	protected Boolean checkErrorXmlResults() throws FileSystemException {
		String resultURLPrefix = webdavURL + errorFolder + "/" + identifier
				+ reportSuffix;
		return hotfolder.fileIfexists(resultURLPrefix);
	}

	protected Set<String> xmlresultOutputparse(File file)
			throws FileNotFoundException, XMLStreamException {
		Set<String> ocrFormatFile = new LinkedHashSet<String>();
		String filename = null;
		final InputStream osmHamburgInStream = new FileInputStream(file);
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader xmlStreamReader = factory
				.createXMLStreamReader(osmHamburgInStream);
		try {
			while (xmlStreamReader.hasNext()) {
				int event = xmlStreamReader.next();
				if (event == XMLStreamConstants.START_ELEMENT) {
					// wenn das Element 'osm' gefunden wurde
					if (xmlStreamReader.getName().toString()
							.equals("NamingRule")) {
						filename = xmlStreamReader.getElementText().toString();
						ocrFormatFile.add(filename);
					}
				}
			}
			logger.debug("the files which should be in output folder: "
					+ ocrFormatFile);
		} finally {
			xmlStreamReader.close();
		}
		return ocrFormatFile;
	}

	protected Set<String> xmlresultErrorparse(File file)
			throws FileNotFoundException, XMLStreamException {
		Set<String> ocrErrorFile = new LinkedHashSet<String>();
		String error = null;

		final InputStream osmHamburgInStream = new FileInputStream(file);
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader xmlStreamReader = factory
				.createXMLStreamReader(osmHamburgInStream);
		try {
			while (xmlStreamReader.hasNext()) {
				int event = xmlStreamReader.next();
				if (event == XMLStreamConstants.START_ELEMENT) {
					// Error Beschreibung
					if (xmlStreamReader.getName().toString().equals("Error")) {
						error = xmlStreamReader.getElementText();
					}
					// bilder die in verzeichnis befinden
					if (xmlStreamReader.getName().toString()
							.equals("InputFile")) {
						// ber alle Attribute
						for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
							String attributeName = xmlStreamReader
									.getAttributeName(i).toString();
							// wenn version gefunden wurde
							if (attributeName.equals("Name")) {
								String str = xmlStreamReader
										.getAttributeValue(i);
								String[] results = str.split("_");
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

	protected Boolean checkIfAllFilssExists(Set<String> checkfile, String url)
			throws FileSystemException {
		for (String fileName : checkfile) {
			if (hotfolder.fileIfexists(url + fileName))
				logger.debug("File " + fileName + " exists already");
			else {
				logger.debug("File " + fileName + " Not exists");
				return false;
			}
		}
		return true;
	}

	protected void copyAllFiles(Set<String> checkfile, String url,
			String localfile) throws FileSystemException {
		URL folder = hotfolder.stringToUrl(url);
		hotfolder.deleteIfExists(folder);
		hotfolder.mkCol(folder);
		for (String fileName : checkfile) {
			hotfolder.copyAllFiles(url + fileName, localfile);
			logger.debug("Copy File From " + url + fileName + " To" + localfile);
		}
	}

	protected void deleteAllFiles(Set<String> checkfile, String url)
			throws FileSystemException {
		hotfolder.deleteIfExists(url + reportSuffix);
		for (String fileName : checkfile) {
			hotfolder.deleteIfExists(url + fileName);
		}
		hotfolder.deleteIfExists(url);
	}

}
