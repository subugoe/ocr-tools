package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;
import de.unigoettingen.sub.commons.ocr.util.OCRUtil;

public class ConfigParser {
	final static Logger logger = LoggerFactory.getLogger(ConfigParser.class);
	protected Configuration config;
	public final static String DEFAULT_CONFIG = "/AbbyyServerOCREngine.properties";
	public final static String DEBUG_PROPERTY = "ocr.finereader.server.debug.auth";

	//Default is 100 MB of storage
	public final static Long DEFAULT_MAXSIZE = 100l * 1024l * 1024l;
	public final static String PARAMETER_MAXSIZE = "maxSize";
	protected Long maxSize;
	//Default is 10000 files
	public final static Long DEFAULT_MAXFILES = 10000l;
	public final static String PARAMETER_MAXFILES = "maxFiles";
	protected Long maxFiles;
	//
	public final static Integer DEFAULT_MAXTHREADS = 10;
	public final static String PARAMETER_MAXTHREADS = "maxThreads";
	protected Integer maxThreads;
	public final static Boolean DEFAULT_CHECKSERVERSTATE = true;
	public final static String PARAMETER_CHECKSERVERSTATE = "checkServerState";
	protected Boolean checkServerState;
	public final static Boolean DEFAULT_DEBUGAUTH = false;
	public final static String PARAMETER_DEBUGAUTH = "debugAuth";
	protected Boolean debugAuth;

	//User and password
	public final static String PARAMETER_USERNAME = "username";
	public final static String PARAMETER_PASSWORD = "password";
	protected String username, password;

	public final static String PARAMETER_SERVERURL = "serverUrl";
	public final static String PARAMETER_INPUT = "input";
	public final static String DEFAULT_INPUT = "input";
	public final static String PARAMETER_OUTPUT = "output";
	public final static String DEFAULT_OUTPUT = "output";
	public final static String PARAMETER_ERROR = "error";
	public final static String DEFAULT_ERROR = "error";
	protected String serverURL, input, output, error;
	
	public final static String PARAMETER_HOTFOLDERCLASS = "hotfolderClass";
	protected String hotfolderClass;

	//The different timeouts:
	//Assume at least 1 second per file
	public final static Long DEFAULT_MINMILLISPERFILE = 1000l;
	public final static String PARAMETER_MINMILLISPERFILE = "minMillisPerFile";
	//Assume 10 seconds per file as maximum
	public final static Long DEFAULT_MAXMILLISPERFILE = 1000l * 10l;
	public final static String PARAMETER_MAXMILLISPERFILE = "maxMillisPerFile";
	//Process should be done in three hours, this is good for 6 * 60 * 3 images = 1080 at 10 seconds per image
	public final static Long DEFAULT_MAXOCRTIMEOUT = 1000l * 60l * 60l * 3;
	public final static String PARAMETER_MAXOCRTIMEOUT = "maxOCRTimeout";
	protected Long minMillisPerFile, maxMillisPerFile, maxOCRTimeout;

	//AbbyyTicket specific settings
	public final static String DEFAULT_TICKETTMPSTORE = "tmp://";
	public final static String PARAMETER_TICKETTMPSTORE = "ticketTmpStore";
	protected String ticketTmpStore;
	public final static Boolean DEFAULT_VALIDATETICKET = false;
	public final static String PARAMETER_VALIDATETICKET = "validateTicket";
	protected Boolean validateTicket;
	public final Boolean DEFAULT_SINGLEFILE = false;
	public final String PARAMETER_SINGLEFILE = "singleFile";
	protected Boolean singleFile;

	//The output location on the server, needed to generate tickets
	public final static String PARAMETER_SERVEROUTPUTLOCATION = "serverOutputLocation";
	public final static String PARAMETER_LOCALOUTPUTLOCATION = "localOutputLocation";
	public final static String DEFAULT_LOCALOUTPUTLOCATION = ".";
	protected String serverOutputLocation, localOutputLocation;

	//Process specific settings
	public final static Boolean DEFAULT_COPYONLY = false;
	public final static String PARAMETER_COPYONLY = "copyOnly";
	protected Boolean copyOnly;
	public final static Boolean DEFAULT_DRYRUN = false;
	public final static String PARAMETER_DRYRUN = "dryRun";
	protected Boolean dryRun;
	public final static Long DEFAULT_CHECKINTERVAL = 20000l;
	public final static String PARAMETER_CHECKINTERVAL = "checkInterval";
	protected Long checkInterval;
	public final static String DEFAULT_REPORTSUFFIX = ".xml.result.xml";
	public final static String PARAMETER_REPORTSUFFIX = "reportSuffix";
	protected String reportSuffix;
	public final static String PARAMETER_DEFAULTLANGS = "defaultLangs";
	protected List<Locale> defaultLangs;

	protected URL configUrl;

	public ConfigParser() {
		this.configUrl = getClass().getResource(DEFAULT_CONFIG);
	}

	public ConfigParser(URL url) throws ConfigurationException {
		this.configUrl = url;
	}

	public ConfigParser parse () {
		return parse(this.configUrl);
	}

	/**
	 * Load config.
	 * 
	 * @param config
	 *            the config
	 */
	public ConfigParser parse (URL configLocation) {
		// load configuration
		try {
			config = new PropertiesConfiguration(configLocation);
		} catch (ConfigurationException e) {
			logger.error("Error reading configuration", e);
			throw new OCRException(e);
		}

		serverURL = config.getString(PARAMETER_SERVERURL);

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

		if (System.getProperty(DEBUG_PROPERTY) != null) {
			debugAuth = Boolean.parseBoolean(System.getProperty(DEBUG_PROPERTY));
		} else {
			debugAuth = config.getBoolean(PARAMETER_DEBUGAUTH, DEFAULT_DEBUGAUTH);
		}

		username = config.getString(PARAMETER_USERNAME, null);
		password = config.getString(PARAMETER_PASSWORD, null);

		input = config.getString(PARAMETER_INPUT, DEFAULT_INPUT);
		output = config.getString(PARAMETER_OUTPUT, DEFAULT_OUTPUT);
		error = config.getString(PARAMETER_ERROR, DEFAULT_ERROR);

		hotfolderClass = config.getString(PARAMETER_HOTFOLDERCLASS, null);
		
		checkServerState = config.getBoolean(PARAMETER_CHECKSERVERSTATE, DEFAULT_CHECKSERVERSTATE);

		maxThreads = config.getInteger(PARAMETER_MAXTHREADS, DEFAULT_MAXTHREADS);
		maxSize = config.getLong(PARAMETER_MAXSIZE, DEFAULT_MAXSIZE);
		maxFiles = config.getLong(PARAMETER_MAXFILES, DEFAULT_MAXFILES);

		minMillisPerFile = config.getLong(PARAMETER_MINMILLISPERFILE, DEFAULT_MINMILLISPERFILE);
		maxMillisPerFile = config.getLong(PARAMETER_MAXMILLISPERFILE, DEFAULT_MAXMILLISPERFILE);
		maxOCRTimeout = config.getLong(PARAMETER_MAXOCRTIMEOUT, DEFAULT_MAXOCRTIMEOUT);

		copyOnly = config.getBoolean(PARAMETER_COPYONLY, DEFAULT_COPYONLY);
		dryRun = config.getBoolean(PARAMETER_DRYRUN, DEFAULT_DRYRUN);
		checkInterval = config.getLong(PARAMETER_CHECKINTERVAL, DEFAULT_CHECKINTERVAL);

		ticketTmpStore = config.getString(PARAMETER_TICKETTMPSTORE, DEFAULT_TICKETTMPSTORE);
		validateTicket = config.getBoolean(PARAMETER_VALIDATETICKET, DEFAULT_VALIDATETICKET);
		singleFile = config.getBoolean(PARAMETER_SINGLEFILE, DEFAULT_SINGLEFILE);
		config.getString(PARAMETER_REPORTSUFFIX, DEFAULT_REPORTSUFFIX);
		if (config.getString(PARAMETER_DEFAULTLANGS, null) != null) {
			defaultLangs = OCRUtil.parseLangs(config.getString(PARAMETER_DEFAULTLANGS, null));
		}

		//Local and remote output locations
		serverOutputLocation = config.getString(PARAMETER_SERVEROUTPUTLOCATION);
		localOutputLocation = config.getString(PARAMETER_LOCALOUTPUTLOCATION, DEFAULT_LOCALOUTPUTLOCATION);

		if (debugAuth) {
			logger.trace("URL: " + serverURL);
			logger.trace("User: " + username);
			logger.trace("Password: " + password);
		} else {
			logger.trace("URL: " + "*hidden* - enable debugAuth to log login data");
			logger.trace("User: " + "*hidden* - enable debugAuth to log login data");
			logger.trace("Password: " + "*hidden* - enable debugAuth to log login data");
		}

		logger.trace("Input folder: " + input);
		logger.trace("Output Folder: " + output);
		logger.trace("Error Folder: " + error);

		logger.trace("Max size: " + maxSize);
		logger.trace("Max files: " + maxFiles);
		logger.trace("Max treads: " + maxThreads);

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
	public String getInput () {
		return input;
	}

	/**
	 * @param inputFolder
	 *            the inputFolder to set
	 */
	public void setInputFolder (String input) {
		this.input = input;
	}

	/**
	 * @return the errorFolder
	 */
	public String getError () {
		return error;
	}

	/**
	 * @param errorFolder
	 *            the errorFolder to set
	 */
	public void setError (String error) {
		this.error = error;
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

	public String getOutput () {
		return output;
	}

	/**
	 * @return the username
	 */
	public String getUsername () {
		return username;
	}

	/**
	 * @return the password
	 */
	public String getPassword () {
		return password;
	}

	
	
}
