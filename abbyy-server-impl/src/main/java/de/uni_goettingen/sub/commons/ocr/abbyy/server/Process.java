package de.uni_goettingen.sub.commons.ocr.abbyy.server;





import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.ConfigurationException;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.vfs.FileObject;
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

	private static List<File> inputFiles = new ArrayList<File>();

	protected static List<Locale> langs;

	protected static Boolean writeRemotePrefix = true;
	protected Boolean copyOnly = true;
	protected Boolean dryRun = false;
	protected Boolean fixRemotePath = true;
	protected Boolean failed = false;
	protected Boolean done = false;
	Date doneDate = null;


	
	protected static String extension = "tif";
	protected Hotfolder hotfolder;
	protected String imageDirectory;
	protected String identifier;

	protected List<AbbyyOCRFile> fileInfos = null;
	protected Map<String, String> remoteFiles = null;

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

			if (!dryRun) {
				hotfolder.copyFilesToServer(fileInfos);
				// everything uploaded, release the locks

				//lockManager.unlockGroup(identifier);

				/*if (!copyOnly) {
					wait = fileInfos.size() * millisPerFile;
					logger.info("Waiting " + wait + " milli seconds");

					Thread.sleep(wait);

					remoteFiles = getResults();
					try {
						if (waitForResults(remoteFiles)) {
							copyServerFiles(remoteFiles);
						}
					} catch (TimeoutExcetion e) {
						failed = true;
					}
				} else {
					logger.info("We don't wait for the Server, copy mode.");
				}*/

			}
		} catch (Exception e) {
			logger.error("Processing failed", e);
			failed = true;
			throw new RuntimeException(e);

		} /*
		 * finally { //clean localCleanUp(); if (!dryRun) {
		 * 
		 * //Set state vars done = true; doneDate = new Date(); try { //This
		 * methos should remove any remote file if (!copyOnly) {
		 * removeRemoteFiles(); } else {
		 * logger.info("Don't delete anything, copy mode."); } } catch
		 * (IOException e) { logger.error("Unable to clean up!", e); } //Safe
		 * this stuff
		 * 
		 * //TODO: We have a memory leak here //Dirty hack - empty a few
		 * collections fileInfos = null; remoteFiles = null; of = null;
		 * engineConfig = null; } }
		 */

		// TODO: deal with errors, remove error files
		// TODO: Parse result statistics or pass them to caller
		// TODO: delete empty folder (if exists), delete error files

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
			AbbyyOCRFile fileInfo = new AbbyyOCRFile(imageUrlNew,
					remoteURL, outputFile);
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
		setMillisPerFile(millisPerFile);
		setMaxOCRTimeout(maxOCRTimeout);
		write(ticketFile);

		String outputFile = webdavURL + inputFolder + "/" + ticketFileName;
		fileInfos.addFirst(new AbbyyOCRFile(hotfolder.fileToURL(ticketFile), null, outputFile));
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
		// errorFolder = config.getString("error");

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

	//copy files back
	/*public Map<String, String> getResults() {
		Map<String, String> copyFiles = new HashMap<String, String>();
		String resultURLPrefix = webdavURL + outputFolder + "/";
		
		if (!imageDirectory.endsWith(localPathSeparator)) {
			imageDirectory = imageDirectory + localPathSeparator;
		}
		
		OCRExportFormat f = null;
		for (OCRExportFormat ef : engineConfig.getDefaultParams().getFormats().keySet()) {
			String fileName = TicketHelper.getName(engineConfig.getDefaultParams().getFormats().get(ef));
			String resultFileURL = resultURLPrefix + fileName;
			//results.put("reportFileURL", results.get("resultFileURL") + reportSuffix);
			copyFiles.put(resultFileURL, imageDirectory + fileName);
			f = ef;
		}
		//Get the last Format 

		if (getReport) {
			String fileName = TicketHelper.getName(engineConfig.getDefaultParams().getFormats().get(f));
			// Change result to .xml.result.xml
			Pattern pattern = Pattern.compile("(.+)\\..+$");
			Matcher matcher = pattern.matcher(fileName);
			// rename file to xml, because of OCR malfunction:
			fileName = matcher.replaceAll("$1" + ".xml");

			String reportFileURL = resultURLPrefix + fileName + reportSuffix;
			copyFiles.put(reportFileURL, imageDirectory + fileName + reportSuffix);
		}

		return copyFiles;
	}*/
	

}
