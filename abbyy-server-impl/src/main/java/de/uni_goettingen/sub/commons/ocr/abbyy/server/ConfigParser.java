package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.util.Arrays;

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
	protected Boolean debugAuth = false;
	protected static String DEFAULT_CONFIG = "abbyyServer.properties";

	final static Logger logger = LoggerFactory.getLogger(ConfigParser.class);
	
	static {
		
			
	}

	public ConfigParser() {
		loadConfig();
	}

	//TODO: Try to remove this method
	public ConfigParser(Configuration config) {
		this.config = config;
		loadConfig();
	}

	/**
	 * Load config.
	 * 
	 * @param config
	 *            the config
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	public void loadConfig () {
		if (Boolean.parseBoolean(System.getProperty("ocr.finereader.server.debug.auth"))) {
			debugAuth = true;
		}
		// do something with config
		try {
			config = new PropertiesConfiguration(DEFAULT_CONFIG);
		} catch (ConfigurationException e) {
			logger.error("Error reading configuration", e);
			throw new RuntimeException(e);
		}

		webdavURL = config.getString("remoteURL");
		webdavURL = webdavURL.endsWith("/") ? webdavURL : webdavURL + "/";
		if (webdavURL != null) {
			webdavURL = parseString(webdavURL);

		}

		webdavUsername = config.getString("username");
		webdavPassword = config.getString("password");
		inputFolder = config.getString("input");
		outputFolder = config.getString("output");
		errorFolder = config.getString("error");

		if (config.getString("checkServerState") != null && !config.getString("checkServerState").equals("")) {
			checkServerState = Boolean.parseBoolean(config.getString("checkServerState"));
		}

		if (config.getString("maxThreads") != null && !config.getString("maxThreads").equals("")) {
			maxThreads = Integer.parseInt(config.getString("maxThreads"));
		}

		if (config.getString("maxSize") != null && !config.getString("maxSize").equals("")) {
			maxSize = Long.parseLong(config.getString("maxSize"));
		}

		if (config.getString("maxFiles") != null && !config.getString("maxFiles").equals("")) {
			maxFiles = Long.parseLong(config.getString("maxFiles"));
		}

		// Add a preconfigred local output folder
		
		if (debugAuth) {
			logger.debug("URL: " + webdavURL);
			logger.debug("User: " + webdavUsername);
			logger.debug("Password: " + webdavPassword);
		} else {
			logger.debug("URL: " + "*hidden* - enable debugAuth to log login data");
			logger.debug("User: " + "*hidden* - enable debugAuth to log login data");
			logger.debug("Password: " + "*hidden* - enable debugAuth to log login data");
		}

		logger.debug("Input folder: " + inputFolder);
		logger.debug("Output Folder: " + outputFolder);
		logger.debug("Error Folder: " + errorFolder);

		logger.debug("Max size: " + maxSize);
		logger.debug("Max files: " + maxFiles);
		logger.debug("Max treads: " + maxThreads);

		logger.debug("Check server state: " + checkServerState);

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

	public String getWebdavURL () {
		return webdavURL;
	}

	public void setWebdavURL (String webdavURL) {
		ConfigParser.webdavURL = webdavURL;
	}
	
	public Boolean getDebugAuth() {
		return debugAuth;
	}
}
