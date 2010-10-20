package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

public class ConfigParser {
	protected Configuration config;
	protected String serverURL;
	protected String username, password;
	protected String inputFolder, outputFolder, errorFolder;

	protected Long maxSize, maxFiles;
	protected Integer maxThreads;
	protected Boolean checkServerState;
	protected Boolean debugAuth = false;
	public final static String DEFAULT_CONFIG = "/abbyyServer.properties";
	public final static String DEBUG_PROPERTY = "ocr.finereader.server.debug.auth";

	final static Logger logger = LoggerFactory.getLogger(ConfigParser.class);

	protected URL configUrl;

	public ConfigParser() {
		this.configUrl = getClass().getResource(DEFAULT_CONFIG);
	}

	public ConfigParser(URL url) throws ConfigurationException {
		this.configUrl = url;
	}

	public ConfigParser loadConfig () {
		return loadConfig(this.configUrl);
	}

	/**
	 * Load config.
	 * 
	 * @param config
	 *            the config
	 */
	public ConfigParser loadConfig (URL configLocation) {
		if (Boolean.parseBoolean(System.getProperty(DEBUG_PROPERTY))) {
			debugAuth = true;
		}
		// do something with config
		try {
			config = new PropertiesConfiguration(configLocation);
		} catch (ConfigurationException e) {
			logger.error("Error reading configuration", e);
			throw new OCRException(e);
		}

		serverURL = config.getString("remoteURL");

		try {
			if (!serverURL.contains("./") && serverURL.startsWith("file")) {
				//This is true if we have an absolute local (file) uri;
				serverURL = new URI(serverURL).toString();
			} else if (serverURL.startsWith("file") && serverURL.contains("./")) {
				//An relative URI with file prefix, just remove the prefix
				serverURL = serverURL.replace("file:", "");
			}
			if (!new URI(serverURL).isAbsolute()) {
				//got an relative URI
				URI context = new File(".").toURI();
				//Resolve it against the base path of the current process
				serverURL = context.resolve(new URI(serverURL)).toString();
			}
		} catch (URISyntaxException e) {
			logger.error("URI is malformed", e);
		}
		serverURL = serverURL.endsWith("/") ? serverURL : serverURL + "/";

		username = config.getString("username");
		password = config.getString("password");
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

		//TODO: Add a preconfigured local output folder

		if (debugAuth) {
			logger.debug("URL: " + serverURL);
			logger.debug("User: " + username);
			logger.debug("Password: " + password);
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

		return this;
	}

	public String getServerURL () {
		return serverURL;
	}

	public void setServerURL (String webdavURL) {
		this.serverURL = webdavURL;
	}

	public Boolean getDebugAuth () {
		return debugAuth;
	}

	/**
	 * @return the inputFolder
	 */
	public String getInputFolder () {
		return inputFolder;
	}

	/**
	 * @param inputFolder
	 *            the inputFolder to set
	 */
	public void setInputFolder (String inputFolder) {
		this.inputFolder = inputFolder;
	}

	/**
	 * @return the errorFolder
	 */
	public String getErrorFolder () {
		return errorFolder;
	}

	/**
	 * @param errorFolder
	 *            the errorFolder to set
	 */
	public void setErrorFolder (String errorFolder) {
		this.errorFolder = errorFolder;
	}

	/**
	 * @return the maxSize
	 */
	public Long getMaxSize () {
		return maxSize;
	}

	/**
	 * @param maxSize
	 *            the maxSize to set
	 */
	public void setMaxSize (Long maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * @return the maxFiles
	 */
	public Long getMaxFiles () {
		return maxFiles;
	}

	/**
	 * @param maxFiles
	 *            the maxFiles to set
	 */
	public void setMaxFiles (Long maxFiles) {
		this.maxFiles = maxFiles;
	}

	/**
	 * @return the maxThreads
	 */
	public Integer getMaxThreads () {
		return maxThreads;
	}

	/**
	 * @param maxThreads
	 *            the maxThreads to set
	 */
	public void setMaxThreads (Integer maxThreads) {
		this.maxThreads = maxThreads;
	}

	/**
	 * @return the checkServerState
	 */
	public Boolean getCheckServerState () {
		return checkServerState;
	}

	/**
	 * @param checkServerState
	 *            the checkServerState to set
	 */
	public void setCheckServerState (Boolean checkServerState) {
		this.checkServerState = checkServerState;
	}

	/**
	 * @return the config
	 */
	public Configuration getConfig () {
		return config;
	}

	public String getOutoutFolder () {
		return outputFolder;
	}

}
