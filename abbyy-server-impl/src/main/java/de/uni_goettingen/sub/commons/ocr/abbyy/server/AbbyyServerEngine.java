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


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.List;
import java.util.Locale;

import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.vfs.FileSystemException;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import de.uni_goettingen.sub.commons.ocr.abbyy.server.OCRExecuter;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;


public class AbbyyServerEngine implements OCREngine{
	
	protected static Integer maxThreads = 5;
	protected ExecutorService pool = new OCRExecuter(maxThreads);
	final static Logger logger = LoggerFactory.getLogger(AbbyyServerEngine.class);

	
	protected HierarchicalConfiguration config;
	protected Hotfolder hotfolder;
	//Server information
	protected static String webdavURL = null;
	protected static String webdavUsername = null;
	protected static String webdavPassword = null;

	protected static List<Locale> langs;
	
	//State variables 
	protected static Long totalFileCount = 0l;
	protected static Long totalFileSize = 0l;
	//Folders
	protected static String inputFolder = null;
	protected static String outputFolder = null;
	protected static String errorFolder = null;

	//public String defaultConfig = "config-properties";
	
	//internal tweaking variables
	//Variables used for process management
	protected static Long maxSize = 5368709120l;
	protected static Long maxFiles = 5000l;
	
	protected static Boolean checkServerState = true;
	
	//TODO: Try to move this stuff to the OCRProcess
	protected static Boolean writeRemotePrefix = true;
	
	
	
	
	
	public AbbyyServerEngine() throws FileSystemException{		
		hotfolder = new Hotfolder();
	}
	
	@Override
	public void recognize() throws OCRException {
		// TODO Auto-generated method stub
		
	}
	

	public void loadConfig(PropertiesConfiguration config) throws ConfigurationException  {
		// do something with config
		config = new PropertiesConfiguration("config-properties");

		webdavURL = config.getString("base");
		webdavURL = webdavURL.endsWith("/") ? webdavURL : webdavURL + "/";
		webdavUsername = config.getString("username");
		webdavPassword = config.getString("password");
		inputFolder = config.getString("input");
		outputFolder = config.getString("output");
		errorFolder = config.getString("error");

		//TODO: Simplify this stuff, maybe use a Map for the variables

		if (config.getString("langs") != null && !config.getString("langs").equals("")) {
			langs = parseLangs(config.getString("langs"));
		}

		if (config.getString("checkServerState") != null && !config.getString("checkServerState").equals("")) {
			checkServerState = Boolean.parseBoolean(config.getString("checkServerState"));
		}

		if (config.getString("maxTreads") != null && !config.getString("maxTreads").equals("")) {
			maxThreads = Integer.parseInt(config.getString("maxTreads"));
		}


		if (config.getString("writeRemotePrefix") != null && !config.getString("writeRemotePrefix").equals("")) {
			writeRemotePrefix = Boolean.parseBoolean(config.getString("writeRemotePrefix"));
		}

		if (config.getString("maxSize") != null && !config.getString("maxSize").equals("")) {
			maxSize = Long.parseLong(config.getString("maxSize"));
		}

		if (config.getString("maxFiles") != null && !config.getString("maxFiles").equals("")) {
			maxFiles = Long.parseLong(config.getString("maxFiles"));
		}

		//Add a preconfigured local output folder
		logger.debug("URL: " + webdavURL);
		logger.debug("User: " + webdavUsername);
		logger.debug("Password: " + webdavPassword);
		
		logger.debug("Input folder: " + inputFolder);
		logger.debug("Output Folder: " + outputFolder);
		logger.debug("Error Folder: " + errorFolder);

		logger.debug("Max size: " + maxSize);
		logger.debug("Max files: " + maxFiles);
		logger.debug("Max treads: " + maxThreads);
	
		logger.debug("Write Remoe Prefix: " + writeRemotePrefix);
		logger.debug("Check server state: " + checkServerState);

	}
	
	public void checkServerState() throws IOException {
		if (maxSize != 0 && maxFiles != 0) {
			List<URL> urls = new ArrayList<URL>();
			//TODO: check if a slash is already appended
			String input_uri = webdavURL + inputFolder + "/";
			String output_uri = webdavURL + outputFolder + "/";
			String error_uri = webdavURL + errorFolder + "/";
			//We need to check all three folders since the limits are for the whole system, not just input
			urls.add(hotfolder.stringToUrl(input_uri));
			urls.add(hotfolder.stringToUrl(output_uri));
			urls.add(hotfolder.stringToUrl(error_uri));
			
			Map<URL, Long> infoMap = new LinkedHashMap<URL, Long>();
			for (URL uri : urls) {
				infoMap.put(uri , hotfolder.getTotalSize(uri));
			}
			totalFileCount = new Integer(infoMap.size()).longValue();
			for (Long size : infoMap.values()) {
				if (size != null) {
					totalFileSize += size;
				}
			}
			System.out.print("TotalFileSize = " + totalFileSize);
			logger.trace("TotalFileSize = " + totalFileSize );
			if (maxFiles != 0 && totalFileCount > maxFiles) {
				logger.error("Too much files. Max number of files is " + maxFiles + ". Number of files on server: " + totalFileCount + ".\nExit program.");
				throw new IllegalStateException("Max number of files exeded");
			}
			if (maxSize != 0 && totalFileSize > maxSize) {
				logger.error("Size of files is too much files. Max size of all files is " + maxSize + ". Size of files on server: " + totalFileSize + ".\nExit program.");
				throw new IllegalStateException("Max size of files exeded");
			}
		} else {
			logger.warn("Server state checking is disabled.");
		}
	}
		
	
	public static List<Locale> parseLangs(String str) {
		List<Locale> langs = new ArrayList<Locale>();
		if (str.contains(",")) {
			for (String lang : Arrays.asList(str.split(","))) {
				langs.add(new Locale(lang));			
			}
		} else {
			langs.add(new Locale(str));			
		}
		return langs;
	}
	
	
	@Override
	public void setOCRProcess(OCRProcess ocrp) {
		// TODO Auto-generated method stub
		//this.ocrp = ocrp; 
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

	protected void finalize () {
		pool.shutdown();
		try {
			//TODO: Calculate the right expected timeout
			pool.awaitTermination(3600, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Got a problem with thread pool: ", e);
		}
	}

}
