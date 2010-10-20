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
	final static Logger logger = LoggerFactory.getLogger(ConfigParser.class);
	protected Configuration config;
	public final static String DEFAULT_CONFIG = "/abbyyServer.properties";
	public final static String DEBUG_PROPERTY = "ocr.finereader.server.debug.auth";
	
	//Default is 100 MB of storage
	public final static Long DEFAULT_MAXSIZE = 100l * 1024l * 1024l;
	public final static String PARAMETER_MAXSIZE = "maxSize";
	//Default is 10000 files
	public final static Long DEFAULT_MAXFILES = 10000l;
	public final static String PARAMETER_MAXFILES = "maxFiles";
	//
	public final static Integer DEFAULT_MAXTHREADS = 10;
	public final static String PARAMETER_MAXTHREADS = "maxThreads";
	public final static Boolean DEFAULT_CHECKSERVERSTATE = true;
	public final static String PARAMETER_CHECKSERVERSTATE = "checkServerState";
	public final static Boolean DEFAULT_DEBUGAUTH = false;
	public final static String PARAMETER_DEBUGAUTH = "debugAuth";
	
	public final static String PARAMETER_USERNAME = "username";
	public final static String PARAMETER_PASSWORD = "password";
	

	protected String username, password;

	public final static String PARAMETER_SERVERURL = "serverUrl";
	
	protected String serverURL, inputFolder, outputFolder, errorFolder;

	protected Long maxSize, maxFiles;
	
	protected Long minMilisPerFile, maxMilisPerFile;   
	
	protected Integer maxThreads;
	protected Boolean checkServerState;
	protected Boolean debugAuth = false;

	//Ticket specific settings
	public final static String DEFAULT_TICKETTMPSTORE = "tmp://";
	public final static String PARAMETER_TICKETTMPSTORE = "ticketTmpStore"; 
	protected String ticketTmpStore = "tmp://";
	public final static Boolean DEFAULT_VALIDATETICKET  = false;
	public final static String PARAMETER_VALIDATETICKET = "validateTicket";
	protected Boolean validateTicket = false;
	public final static Long DEFAULT_CHECKINTERVAL = 20000l;
	public final static String PARAMETER_CHECKINTERVAL = "checkInterval";
	protected Long checkInterval;
	//public final static String DEFAULT_OUTPUTLOCATION 
	public final static String PARAMETER_OUTPUTLOCATION = "outputLocation";
	protected String outputLocation;
	
	//Process specific settings
	public final static Boolean DEFAULT_COPYONLY  = false;
	public final static String PARAMETER_COPYONLY = "copyOnly";
	protected Boolean copyOnly;

	public final static Boolean DEFAULT_DRYRUN  = false;
	public final static String PARAMETER_DRYRUN = "dryRun";
	protected Boolean dryRun = false;
	

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
		// load configuration
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

		username = config.getString(PARAMETER_USERNAME);
		password = config.getString(PARAMETER_PASSWORD);
		inputFolder = config.getString("input");
		outputFolder = config.getString("output");
		errorFolder = config.getString("error");

		if (config.getString("checkServerState") != null && !config.getString("checkServerState").equals("")) {
			checkServerState = Boolean.parseBoolean(config.getString("checkServerState"));
		}

		maxThreads = config.getInteger(PARAMETER_MAXTHREADS, DEFAULT_MAXTHREADS);
		maxSize = config.getLong(PARAMETER_MAXSIZE, DEFAULT_MAXSIZE);
		maxFiles = config.getLong(PARAMETER_MAXFILES, DEFAULT_MAXFILES);

		if (config.getString("maxFiles") != null && !config.getString("maxFiles").equals("")) {
			maxFiles = Long.parseLong(config.getString("maxFiles"));
		}

		//TODO: Add a preconfigured local output folder

		debugAuth = config.getBoolean(PARAMETER_DEBUGAUTH, DEFAULT_DEBUGAUTH);
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
