package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigParser {
	protected Configuration config;
	protected static String webdavURL;
	protected String webdavUsername;
	protected String webdavPassword;
	protected String inputFolder;
	protected String outputFolder;
	protected String errorFolder;
	
	protected Long maxSize;
	protected Long maxFiles;
	protected Integer maxThreads;
	protected Boolean checkServerState;
	
	final static Logger logger = LoggerFactory
	.getLogger(ConfigParser.class);
	
	public ConfigParser () {
		try {
			loadConfig();
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	
	public ConfigParser (Configuration config) {
		this.config = config;
		try {
			loadConfig();
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Load config.
	 *
	 * @param config the config
	 * @throws ConfigurationException the configuration exception
	 */
	public void loadConfig()
			throws ConfigurationException {
		// do something with config
		try {
			config = new PropertiesConfiguration("config-properties");
		} catch (org.apache.commons.configuration.ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		webdavURL = config.getString("remoteURL");
		webdavURL = webdavURL.endsWith("/") ? webdavURL : webdavURL + "/";
		if(webdavURL != null){
			webdavURL = parseString(webdavURL);

		}
		
		
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

		if (config.getString("maxThreads") != null
				&& !config.getString("maxThreads").equals("")) {
			maxThreads = Integer.parseInt(config.getString("maxThreads"));
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
	
	public static String parseString(String str){
		String remoteFile = null;
		if (str.contains("/./")) {
			int i = 0;
			for (String lang : Arrays.asList(str.split("/./"))) {
				if (i == 0){
					i++;
				}else{
					remoteFile = lang;
				}
			}
		}
		return remoteFile;
		
	}
	
	public String getWebdavURL() {
		return webdavURL;
	}

	public  void setWebdavURL(String webdavURL) {
		this.webdavURL = webdavURL;
	}
}
