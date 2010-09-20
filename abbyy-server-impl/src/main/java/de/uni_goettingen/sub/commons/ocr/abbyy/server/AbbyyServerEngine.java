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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import de.uni_goettingen.sub.commons.ocr.abbyy.server.OCRExecuter;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;


public class AbbyyServerEngine implements OCREngine{
	
	protected Integer maxThreads = 5;
	protected ExecutorService pool = new OCRExecuter(maxThreads);
	final Logger logger = LoggerFactory.getLogger(AbbyyServerEngine.class);

	
	protected HierarchicalConfiguration config;

	//Server information
	protected static String webdavURL = null;
	protected static String webdavUsername = null;
	protected static String webdavPassword = null;

	//PaAth related stuff
	protected static String localPathSeparator = File.separator;
	protected static String remotePathSeperator = null;
	protected static String remoteBasePath = null;
	protected static String localBasePath = null;
	protected static String localOutputDir = null;

	protected static List<Locale> langs;
	
	//Folders
	protected static String inputFolder = null;
	protected static String outputFolder = null;
	protected static String errorFolder = null;

	public String defaultConfig = "server-config.xml";
	
	//internal tweaking variables
	//Variables used for process management
	protected static Long maxSize = 5368709120l;
	protected static Long maxFiles = 5000l;
	
	protected Boolean checkServerState = true;
	
	//TODO: Try to move this stuff to the OCRProcess
	protected static Boolean writeRemotePrefix = true;
	
	//Settings for Ticket creation
	protected Boolean recursiveMode = true;
	
	
	
	
	public AbbyyServerEngine(){		
	}
	
	@Override
	public void recognize() throws OCRException {
		// TODO Auto-generated method stub
		
	}

	
	
	public void loadConfig(HierarchicalConfiguration config) throws ConfigurationException {
		// do something with config
		config.setExpressionEngine(new XPathExpressionEngine());
		webdavURL = config.getString("//webdavURL/@base");
		webdavURL = webdavURL.endsWith("/") ? webdavURL : webdavURL + "/";
		webdavUsername = config.getString("//webdavURL/@username");
		webdavPassword = config.getString("//webdavURL/@password");
		remotePathSeperator = config.getString("//remotePaths/@seperator");
		remoteBasePath = config.getString("//remotePaths/@base");

		if (config.getString("//localPaths/@base") != null && !config.getString("//localPaths/@base").equals("")) {
			localBasePath = config.getString("//localPaths/@base");
		} else {
			localBasePath = System.getProperty("user.dir");
		}

		if (!remoteBasePath.endsWith(remotePathSeperator)) {
			remoteBasePath = remoteBasePath + remotePathSeperator;
		}

		inputFolder = config.getString("//paths/input");
		outputFolder = config.getString("//paths/output");
		errorFolder = config.getString("//paths/error");

		//TODO: Simplify this stuff, maybe use a Map for the variables

		if (config.getString("//setting[@name='langs']/@value") != null && !config.getString("//setting[@name='langs']/@value").equals("")) {
			langs = parseLangs(config.getString("//setting[@name='langs']/@value"));
		}

		if (config.getString("//setting[@name='checkServerState']/@value") != null && !config.getString("//setting[@name='checkServerState']/@value").equals("")) {
			checkServerState = Boolean.parseBoolean(config.getString("//setting[@name='checkServerState']/@value"));
		}

		if (config.getString("//setting[@name='maxTreads']/@value") != null && !config.getString("//setting[@name='maxTreads']/@value").equals("")) {
			maxThreads = Integer.parseInt(config.getString("//setting[@name='maxTreads']/@value"));
		}


		if (config.getString("//setting[@name='writeRemotePrefix']/@value") != null && !config.getString("//setting[@name='writeRemotePrefix']/@value").equals("")) {
			writeRemotePrefix = Boolean.parseBoolean(config.getString("//setting[@name='writeRemotePrefix']/@value"));
		}

		if (config.getString("//setting[@name='maxSize']/@value") != null && !config.getString("//setting[@name='maxSize']/@value").equals("")) {
			maxSize = Long.parseLong(config.getString("//setting[@name='maxSize']/@value"));
		}

		if (config.getString("//setting[@name='maxFiles']/@value") != null && !config.getString("//setting[@name='maxFiles']/@value").equals("")) {
			maxFiles = Long.parseLong(config.getString("//setting[@name='maxFiles']/@value"));
		}

		if (config.getString("//setting[@name='recursiveMode']/@value") != null && !config.getString("//setting[@name='recursiveMode']/@value").equals("")) {
			recursiveMode = Boolean.parseBoolean(config.getString("//setting[@name='recursiveMode']/@value"));
		}

		//Add a preconfigured local output folder
		
		logger.debug("URL: " + webdavURL);
		logger.debug("User: " + webdavUsername);
		logger.debug("Password: " + webdavPassword);
		logger.debug("Remote Path Seperator: " + remotePathSeperator);
		logger.debug("Remote base path: " + remoteBasePath);
		logger.debug("Local base path: " + localBasePath);
		logger.debug("Input folder: " + inputFolder);
		logger.debug("Output Folder: " + outputFolder);
		logger.debug("Error Folder: " + errorFolder);

		logger.debug("Max size: " + maxSize);
		logger.debug("Max files: " + maxFiles);
		logger.debug("Max treads: " + maxThreads);

	
		logger.debug("Write Remoe Prefix: " + writeRemotePrefix);
		logger.debug("Check server state: " + checkServerState);

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
