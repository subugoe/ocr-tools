package de.uni_goettingen.sub.commons.ocr.abbyy.server;

/*

 © 2010, SUB Göttingen. All rights reserved.
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
import java.net.URL;
import java.util.ArrayList;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;

import java.util.List;

import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.vfs.FileSystemException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.OCRExecuter;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;

import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

public class AbbyyServerEngine implements OCREngine {

	protected static Integer maxThreads = 5;
	// protected ExecutorService pool = new OCRExecuter(maxThreads);
	final static Logger logger = LoggerFactory
			.getLogger(AbbyyServerEngine.class);

	PropertiesConfiguration config;

	protected Process process;
	protected Hotfolder hotfolder;

	private static AbbyyServerEngine _instance;
	protected static String extension = "tif";
	// Server information
	protected static String webdavURL = null;
	protected static String webdavUsername = null;
	protected static String webdavPassword = null;
	// public String defaultConfig = "config-properties";
	// State variables
	protected static Long totalFileCount = 0l;
	protected static Long totalFileSize = 0l;
	// Folders
	protected static String inputFolder = null;
	protected static String outputFolder = null;
	protected static String errorFolder = null;

	// internal tweaking variables
	// Variables used for process management
	protected static Long maxSize = 5368709120l;
	protected static Long maxFiles = 5000l;

	protected static Boolean checkServerState = true;

	protected static String localOutputDir = null;

	protected List<File> directories = new ArrayList<File>();

	// OCR Processes
	Queue<Process> processes = new ConcurrentLinkedQueue<Process>();

	public AbbyyServerEngine() throws FileSystemException,
			ConfigurationException {
		hotfolder = new Hotfolder();
		loadConfig(config);
		Thread thread = new Thread(process);
		thread.start();
	}

	@Override
	public void recognize() {
		try {
			AbbyyServerEngine.getInstance();
		} catch (FileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.apache.commons.configuration.ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void start() throws RuntimeException, FileSystemException {
		if (checkServerState) {
			try {
				checkServerState();
			} catch (Exception e) {
				logger.error(e.toString());
			}
		}

		ExecutorService pool = new OCRExecuter(maxThreads);

		for (File dir : directories) {
			if (localOutputDir == null) {
				localOutputDir = dir.getParent();
			}

			process = new Process(dir);

			process.setOutputLocation(localOutputDir);

			// process.addOCRFormat(enums);

		}

		for (Process process : processes) {
			pool.execute(process);
		}

		pool.shutdown();
		try {

			pool.awaitTermination(3600, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Got a problem with thread pool: ", e);
		}

	}

	public static AbbyyServerEngine getInstance() throws FileSystemException,
			ConfigurationException,
			org.apache.commons.configuration.ConfigurationException {
		if (_instance == null) {
			_instance = new AbbyyServerEngine();
		}
		return _instance;
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
		webdavUsername = config.getString("username");
		webdavPassword = config.getString("password");
		inputFolder = config.getString("input");
		outputFolder = config.getString("output");
		errorFolder = config.getString("error");

		if (config.getString("checkServerState") != null
				&& !config.getString("checkServerState").equals("")) {
			checkServerState = Boolean.parseBoolean(config
					.getString("checkServerState"));
		}

		if (config.getString("maxTreads") != null
				&& !config.getString("maxTreads").equals("")) {
			maxThreads = Integer.parseInt(config.getString("maxTreads"));
		}

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
		logger.debug("User: " + webdavUsername);
		logger.debug("Password: " + webdavPassword);

		logger.debug("Input folder: " + inputFolder);
		logger.debug("Output Folder: " + outputFolder);
		logger.debug("Error Folder: " + errorFolder);

		logger.debug("Max size: " + maxSize);
		logger.debug("Max files: " + maxFiles);
		logger.debug("Max treads: " + maxThreads);

		logger.debug("Check server state: " + checkServerState);

	}

	public void checkServerState() throws IOException {
		if (maxSize != 0 && maxFiles != 0) {
			List<URL> urls = new ArrayList<URL>();
			// check if a slash is already appended
			String input_uri = webdavURL + inputFolder + "/";
			String output_uri = webdavURL + outputFolder + "/";
			String error_uri = webdavURL + errorFolder + "/";
			// We need to check all three folders since the limits are for the
			// whole system, not just input
			urls.add(hotfolder.stringToUrl(input_uri));
			urls.add(hotfolder.stringToUrl(output_uri));
			urls.add(hotfolder.stringToUrl(error_uri));

			Map<URL, Long> infoMap = new LinkedHashMap<URL, Long>();
			for (URL uri : urls) {
				infoMap.put(uri, hotfolder.getTotalSize(uri));
			}
			totalFileCount = new Integer(infoMap.size()).longValue();
			for (Long size : infoMap.values()) {
				if (size != null) {
					totalFileSize += size;
				}
			}
			System.out.print("TotalFileSize = " + totalFileSize);
			logger.trace("TotalFileSize = " + totalFileSize);
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

	@Override
	public void setOCRProcess(OCRProcess ocrp) {
		// TODO Auto-generated method stub
		// this.ocrp = ocrp;
	}

	@Override
	public OCRProcess getOCRProcess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OCROutput getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setObserver(Observer observer) {
		// TODO Auto-generated method stub

	}

}
