/**
 * @author Sven Thomas
 * @author Christian Mahnke
 * @version 1.0
 */
package de.unigoettingen.sub.gdz.ocr.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.NotImplementedException;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import de.unigoettingen.sub.gdz.ocr.simple.OCR;
import de.unigoettingen.sub.gdz.ocr.simple.OCRExportFormat;
import de.unigoettingen.sub.gdz.ocr.simple.OCRUtils;

public class OCRRunner extends Thread implements OCRRunnerMBean {
	//TODO: Test if languages are handled correctly
	public final static String version = "0.0.4";

	//TODO: Check if this could be static
	protected HttpClient client;
	protected static Logger logger = Logger.getLogger(de.unigoettingen.sub.gdz.ocr.server.OCRRunner.class);

	private static Options opts = new Options();
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

	//Folders
	protected static String inputFolder = null;
	protected static String outputFolder = null;
	protected static String errorFolder = null;

	public String defaultConfig = "server-config.xml";

	protected static String extension = "tif";

	protected List<File> directories = new ArrayList<File>();
	protected List<Locale> langs;

	//internal tweaking variables
	//Variables used for process management
	protected static Long maxSize = 5368709120l;
	protected static Long maxFiles = 5000l;
	protected static Integer maxThreads = 5;
	protected static Boolean useJMX = false;
	protected static Boolean usePersistence = true;
	protected Boolean checkServerState = true;

	//Settings used for the lock manager
	protected Boolean persistLocks = false;
	protected static Long defaultLockTimeOut = 3600000l;
	protected static Boolean useLocks = false;

	//Settings for Ticket creation
	protected Boolean recursiveMode = true;
	//TODO: Try to move this stuff to the OCRProcess
	protected static Boolean writeRemotePrefix = true;

	//Instances
	private static OCRRunner _instance;
	private static LockManager lockManager;

	//Options
	//TODO: Merge with OCR.java (make OCR.jave the base class for this stuff)
	//TODO: Check if needed (mostly the output file stuff)
	//TODO make this static or add a metod for ading this to a aprocess
	protected HashMap<OCRExportFormat, String> of = new HashMap<OCRExportFormat, String>();
	protected HashMap<OCRExportFormat, List<String>> ofo = new HashMap<OCRExportFormat, List<String>>();

	//State variables 
	protected static Long totalFileCount = 0l;
	protected static Long totalFileSize = 0l;

	//OCR Processes
	Queue<OCRProcess> processes = new ConcurrentLinkedQueue<OCRProcess>();

	//Persistence related stuff
	protected static SessionFactory sessionFactory;
	protected Session session; 
	
	//Cleanup related stuff
	protected static Boolean emptyError = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		logger.info("Creating OCRRunner instance");
		OCRRunner ocrR = OCRRunner.getInstance();
		ocrR.configureFromArgs(args);
		ocrR.start();
		ocrR.join();
		//System.exit(0);
	}

	protected static void initOpts() {
		// Parameters
		opts.addOption("r", false, "Recursive - scan for subdirectories");
		opts.addOption("ofn", true, "Output filename / directory");
		opts.addOption("of", true, "Output format");

		Option ofo = OptionBuilder.withArgName("format:option").hasArg().withValueSeparator().withDescription("Output format options").create("ofo");

		opts.addOption(ofo);
		opts.addOption("l", true, "Languages - seperated by \",\"");
		opts.addOption("h", false, "Help");
		opts.addOption("v", false, "Version");
		opts.addOption("lc", true, "Logger Configuration");
		opts.addOption("d", true, "Debuglevel");
		opts.addOption("c", true, "Configuration file (optional)");
		opts.addOption("e", true, "File extension (default \"tif\")");
		opts.addOption("o", true, "Output folder");
	}

	private OCRRunner() {
		initOpts();
	}

	//TODO: Add backend for directories (for testing)
	@SuppressWarnings("unchecked")
	public void start() throws RuntimeException {
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
			OCRProcess process = new OCRProcess(dir);
			process.setLockManager(lockManager);
			process.setLocalOutputDir(localOutputDir);
			
			process.setOf(of);

			try {
				process.loadConfig(config);
			} catch (Exception e) {
				// TODO: Thread should fail if this happens
				logger.error(e.toString());
			}
			
			if (usePersistence) {
				Query query = session.createQuery("from OCRProcess ocrprocess where ocrprocess.imageDirectory = :imageDirectory");
				query.setString("imageDirectory", dir.getAbsolutePath());
				List<OCRProcess> results = (List<OCRProcess>) query.list();
				if (results.isEmpty()) {
					session.beginTransaction();
					session.save(process);
					session.getTransaction().commit();
				} else {
					logger.info("Process for directory " + process.identifier +" not added since is't already in the database.");
				}
			} else {
				this.processes.add(process);
			}
		}

		//TODO read everything from database and start
		if (usePersistence) {
			//Populate the queue from database
			Query query = session.createQuery("from OCRProcess ocrprocess");
			for (OCRProcess process: (List<OCRProcess>) query.list()) {
				if (process.getDone() == true) {
					logger.info("Process " + process.getIdentifier() + " already done.");
				} else {
					processes.add(process);
					logger.info("Process " + process.getIdentifier() + " added to queue.");
				}
			}
			//The short for of the sstuff above.
			//Query query = session.createQuery("from OCRProcess ocrprocess where ocrprocess.done = false");
			//processes.addAll((List<OCRProcess>) query.list());
		}
		
		
		for (OCRProcess process : processes) {
			//TODO: Keep in mind that we are using multiple Threads
			process.setClient(client);
			process.setSession(session);
			pool.execute(process);
		}
		
		pool.shutdown();
		try {
			//TODO: Calculate the right expected timeout
			pool.awaitTermination(3600, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Got a problem with thread pool: ", e);
		}
		
		
	}

	protected void checkServerState() throws HttpException, IOException, DavException {
		if (maxSize != 0 && maxFiles != 0) {
			List<String> urls = new ArrayList<String>();
			//TODO: check if a slash is already appended
			String input_uri = webdavURL + inputFolder + "/";
	
			//We need to check all three folders since the limits are for the whole system, not just input
			urls.add(input_uri);
			urls.add(webdavURL + errorFolder + "/");
			urls.add(webdavURL + outputFolder + "/");
			Map<String, Long> infoMap = new LinkedHashMap<String, Long>();
			for (String uri : urls) {
				infoMap.putAll(getRemoteSizes(uri));
			}
			totalFileCount = new Integer(infoMap.size()).longValue();
			for (Long size : infoMap.values()) {
				if (size != null) {
					totalFileSize += size;
				}
			}
	
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


	public void configureFromArgs(String[] args) throws ConfigurationException, HttpException, IOException, MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		List<String> files = defaultOpts(args);
		loadConfig(config);
		if (recursiveMode) {
			List<File> newFiles = new ArrayList<File>();
			for (String dir : files) {
				newFiles.addAll(getImageDirectories(new File(dir)));
			}
			files = new ArrayList<String>();
			for (File dir : newFiles) {
				files.add(dir.getAbsolutePath());
			}
		}

		for (String path : files) {
			File file = new File(path);
			if (file.isDirectory()) {
				directories.add(file);


			} else {
				logger.error(path + " is not a directory!");
			}
		}

		if (useJMX) {
			initJMX();
		}
		if (usePersistence) {
			try {
				sessionFactory = getSessionFactory();
				session = sessionFactory.openSession();
			} catch (Throwable t) {
				logger.error("Error while creating Hibernate session!");
				throw new RuntimeException();
			}
				
		}

		client = initConnection(webdavURL, webdavUsername, webdavPassword);
		lockManager = LockManager.getInstance(client, session);
		lockManager.setDefaultLockTimeOut(defaultLockTimeOut);
		lockManager.setPersistLocks(persistLocks);
		lockManager.setUseLocks(useLocks);
	}

	@SuppressWarnings("deprecation")
	public static HttpClient initConnection(String webdavURL, String webdavUsername, String webdavPassword) {
		Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
		Protocol.registerProtocol("https", easyhttps);
		if (webdavURL == null) {
			throw new IllegalStateException("no host given");
		}

		URL url;
		try {
			url = new URL(webdavURL);
		} catch (MalformedURLException e) {
			throw new IllegalStateException("no valid host given: " + e.toString());
		}

		HostConfiguration hostConfig = new HostConfiguration();
		hostConfig.setHost(url.getHost(), url.getDefaultPort(), url.getProtocol());

		HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		int maxHostConnections = 10;

		params.setMaxConnectionsPerHost(hostConfig, maxHostConnections);
		params.setStaleCheckingEnabled(true);
		params.setSoTimeout(1000);
		connectionManager.setParams(params);
		HttpClient client = new HttpClient(connectionManager);
		if (webdavUsername != null || webdavPassword != null) {
			Credentials creds = new UsernamePasswordCredentials(webdavUsername, webdavPassword);
			client.getParams().setAuthenticationPreemptive(true);
			client.getState().setCredentials(AuthScope.ANY, creds);
		}
		client.setHostConfiguration(hostConfig);
		return client;
	}

	protected void getWebdavFile(String url, String outdir, String localfilename) {
		outdir = outdir.endsWith(localPathSeparator) ? outdir : outdir + localPathSeparator;
		logger.info("URL:" + url);

		// Create a method instance.
		GetMethod method = new GetMethod(url);

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				logger.error("Method failed: " + method.getStatusLine());
			}

			InputStream is = method.getResponseBodyAsStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			// String name = getName(url).toString();

			FileOutputStream fos = new FileOutputStream(outdir + localfilename);
			byte[] bytes = new byte[8192];
			int count = bis.read(bytes);
			while (count != -1 && count <= 8192) {
				fos.write(bytes, 0, count);
				count = bis.read(bytes);
			}
			if (count != -1) {
				fos.write(bytes, 0, count);
			}
			fos.close();
			bis.close();

			// Deal with the response.
			// Use caution: ensure correct character encoding and is not binary
			// data
		} catch (HttpException e) {
			logger.error("Fatal protocol violation: ", e);

		} catch (IOException e) {
			logger.error("Fatal transport error: ", e);

		} finally {
			// Release the connection.
			method.releaseConnection();
		}
	}

	public static void executeMethod(HttpClient client, DavMethod method) throws HttpException, IOException {
		Integer responseCode = client.executeMethod(method);
		method.releaseConnection();
		logger.trace("Response code: " + responseCode);
		if (responseCode >= 400) {
			throw new IllegalStateException("Got HTTP Code " + responseCode + " for " + method.getURI());
		}
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
			langs = OCR.parseLangs(config.getString("//setting[@name='langs']/@value"));
		}

		if (config.getString("//setting[@name='useJMX']/@value") != null && !config.getString("//setting[@name='useJMX']/@value").equals("")) {
			useJMX = Boolean.parseBoolean(config.getString("//setting[@name='useJMX']/@value"));
		}

		if (config.getString("//setting[@name='checkServerState']/@value") != null && !config.getString("//setting[@name='checkServerState']/@value").equals("")) {
			checkServerState = Boolean.parseBoolean(config.getString("//setting[@name='checkServerState']/@value"));
		}

		if (config.getString("//setting[@name='maxTreads']/@value") != null && !config.getString("//setting[@name='maxTreads']/@value").equals("")) {
			maxThreads = Integer.parseInt(config.getString("//setting[@name='maxTreads']/@value"));
		}

		if (config.getString("//setting[@name='persistLocks']/@value") != null && !config.getString("//setting[@name='persistLocks']/@value").equals("")) {
			persistLocks = Boolean.parseBoolean(config.getString("//setting[@name='persistLocks']/@value"));
		}

		if (config.getString("//setting[@name='useLocks']/@value") != null && !config.getString("//setting[@name='useLocks']/@value").equals("")) {
			useLocks = Boolean.parseBoolean(config.getString("//setting[@name='useLocks']/@value"));
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
		logger.debug("Use locks: " + useLocks);

		logger.debug("Default lock timeout: " + defaultLockTimeOut);
		logger.debug("Write Remoe Prefix: " + writeRemotePrefix);
		logger.debug("Persist Locks: " + persistLocks);

		logger.debug("Use JMX: " + useJMX);
		logger.debug("Check server state: " + checkServerState);

	}

	@SuppressWarnings("unchecked")
	protected List<String> defaultOpts(String[] args) {
		//TODO OutputDir konfigurierbar (Kommandozeile)
		String cmdName = "OCRRunner [opts] files";
		CommandLine cmd = null;
		// Parameter interpretieren
		CommandLineParser parser = new GnuParser();
		try {
			cmd = parser.parse(opts, args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(3);
		}

		if (cmd.getArgList().isEmpty()) {
			logger.trace("No Input Files!");
			System.out.println("No Input Files!");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(cmdName, opts);
			System.exit(1);
		}

		// Hilfe
		if (cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(cmdName, opts);
			System.exit(0);
		}

		// Extension
		if (cmd.hasOption("e")) {
			if (cmd.getOptionValue("e") != null) {
				extension = cmd.getOptionValue("e");
			}
		}

		// Version
		if (cmd.hasOption("v")) {
			System.out.println("Version " + version);
			System.exit(0);
		}

		// Logger Configuration
		if (cmd.hasOption("lc")) {
			if (cmd.getOptionValue("lc") != null && !cmd.getOptionValue("lc").equals("")) {
				PropertyConfigurator.configure(cmd.getOptionValue("lc"));
			}
		}

		// Debug
		if (cmd.hasOption("d")) {
			logger.setLevel(Level.toLevel(cmd.getOptionValue("d")));
			logger.trace("Debuglevel: " + cmd.getOptionValue("d"));
		}

		// Configuration
		if (cmd.hasOption("c") && cmd.getOptionValue("c") != null) {
			try {
				config = new XMLConfiguration(cmd.getOptionValue("c"));
			} catch (ConfigurationException e) {
				logger.error("Could not load configuration", e);
			}
		} else {
			URL cfile;
			try {
				cfile = getClass().getResource(defaultConfig);
				if (cfile != null) {
					config = new XMLConfiguration(cfile);

				}
			} catch (ConfigurationException e) {
				logger.error("Could not load configuration", e);
				throw new RuntimeException("Could not load configuration");
			}
		}

		// Sprache
		if (cmd.hasOption("l")) {
			langs = OCR.parseLangs(cmd.getOptionValue("l"));
		} else {
			langs = new ArrayList<Locale>();
			langs.add(new Locale("de"));
		}
		for (Locale lang : langs) {
			logger.trace("Language: " + lang.getLanguage());
		}

		logger.trace("Parsing Options");

		if (cmd.hasOption("of")) {
			of = OCR.parseOpts(cmd.getOptionValue("of"));
		}
		if (cmd.hasOption("ofn")) {
			of = OCR.parseOpts(cmd.getOptionValue("ofn"));
		}

		if (cmd.hasOption("ofo")) {
			ofo = OCR.parseOptsList(cmd.getOptionValue("ofo"));
		}

		//Directories
		//recursive mode
		if (cmd.hasOption("r")) {
			recursiveMode = true;
		}
		
		// Output foler
		if (cmd.hasOption("o")) {
			if (cmd.getOptionValue("o") != null && !cmd.getOptionValue("o").equals("")) {
				localOutputDir = cmd.getOptionValue("o");
			}
		}

		return (List<String>) cmd.getArgList();
	}

	//	protected Integer getNumberOfRemoteFiles(String uri) throws HttpException,
	//			IOException, DavException {
	//		MultiStatus multiStatus = propFind(uri);
	//		List<MultiStatusResponse> responses = Arrays.asList(multiStatus
	//				.getResponses());
	//
	//		Integer number = new Integer(0);
	//		for (MultiStatusResponse response : responses) {
	//			String path = response.getHref();
	//
	//			DavPropertySet props = response.getProperties(200);
	//			if (props.contains(DavPropertyName.GETCONTENTLENGTH)
	//					&& props.get(DavPropertyName.GETCONTENTLENGTH).getValue() != null) {
	//				number += Long.parseLong((String) props.get(
	//						DavPropertyName.GETCONTENTLENGTH).getValue());
	//			} else {
	//				continue;
	//			}
	//		}
	//		return number;
	//	}
	
	
	protected Map<String, Long> getRemoteSizes(String uri) throws HttpException, IOException, DavException {
		Map<String, Long> infoMap = new LinkedHashMap<String, Long>();
		MultiStatus multiStatus = propFind(uri);
		List<MultiStatusResponse> responses = Arrays.asList(multiStatus.getResponses());

		for (MultiStatusResponse response : responses) {
			String path = response.getHref();
			DavPropertySet props = response.getProperties(200);
			if (props.contains(DavPropertyName.GETCONTENTLENGTH) && props.get(DavPropertyName.GETCONTENTLENGTH).getValue() != null) {
				infoMap.put(path, Long.parseLong((String) props.get(DavPropertyName.GETCONTENTLENGTH).getValue()));
			} else {
				infoMap.put(path, null);
			}
		}
		return infoMap;
	}

	private MultiStatus propFind(String uri) throws IOException, DavException {
		DavMethod probFind = new PropFindMethod(uri, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
		OCRRunner.executeMethod(client, probFind);
		//TODO: Check if this realy works since the connection is already closed if executed by the static methos
		return probFind.getResponseBodyAsMultiStatus();
	}



	@SuppressWarnings("unchecked")
	private static MBeanServer getServer() {
		MBeanServer mbserver = null;
		List<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
		if (mbservers.size() > 0) {
			mbserver = mbservers.get(0);
		}

		if (mbserver != null) {
			logger.info("Found our MBean server");
		} else {
			mbserver = MBeanServerFactory.createMBeanServer();
		}

		return mbserver;
	}

	private void initJMX() throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		MBeanServer server = getServer();
		ObjectName name = null;
		name = new ObjectName("OCRRunner:Name=OCRRunner,Type=Server");
		server.registerMBean(this, name);
	}

	private static SessionFactory getSessionFactory () {
		try {
			sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
		} catch (Throwable t) {
			logger.error("Problems creating session factory.", t);
			throw new RuntimeException(t); 
		}
		return sessionFactory;
	}

	public static OCRRunner getInstance() {
		if (_instance == null) {
			_instance = new OCRRunner();
		}
		return _instance;
	}

	public Long getFileCount() {
		throw new NotImplementedException();
	}

	public Long getTotalFileCount() {
		return totalFileCount;
	}

	public Long getTotalFileSize() {
		return totalFileSize;
	}

	public void addDirectory (File dir) {
		this.directories.add(dir);
	}

	public List<File> getDirectories() {
		return directories;
	}

	public void setDirectories(List<File> directories) {
		this.directories = directories;
	}

	public static List<File> getImageDirectories(File dir) {
		List<File> dirs = new ArrayList<File>();

		if (OCRUtils.makeFileList(dir, extension).size() > 0) {
			dirs.add(dir);
		}

		List<File> fileList;
		if (dir.isDirectory()) {
			fileList = Arrays.asList(dir.listFiles());
			for (File file : fileList) {
				if (file.isDirectory()) {
					List<File> files = OCRUtils.makeFileList(dir, extension);
					if (files.size() > 0) {
						dirs.addAll(files);
					} else {
						dirs.addAll(getImageDirectories(file));
					}
				}
			}
		} else {
			throw new IllegalStateException(dir.getAbsolutePath() + " is not a directory");
		}
		return dirs;
	}

	//TODO: Maybe needed for later use, to get the resultfiles up the stack
	/*
	public class OCRCallable extends OCRProcess implements Callable {
		protected Boolean getReport = true;

		public OCRCallable(File dir) {
			super(dir);
		}

		public Object call() throws Exception {
			run();
			return localResultfile;
		}
	}
	 */


	//Interface for stream handling to be able to plug in a XSLT Processor for example
	public interface OCRStreamHandler {
		public void setStream(InputStream is);

		public void handle() throws IOException;
	}


	//TODO: Test this executor
	//Executor that checks for Server constrains
	public class OCRExecuter extends ThreadPoolExecutor implements Executor {
		// TODO: Review:
		// http://www.jboss.org/file-access/default/members/netty/freezone
		// /api/3.0/org
		// /jboss/netty/handler/execution/MemoryAwareThreadPoolExecutor.html
		public Integer maxThreads;

		private boolean isPaused;
		private ReentrantLock pauseLock = new ReentrantLock();
		private Condition unpaused = pauseLock.newCondition();

		public OCRExecuter(Integer maxThreads) {
			super(maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			this.maxThreads = maxThreads;
		}

		@Override
		protected void beforeExecute(Thread t, Runnable r) {
			super.beforeExecute(t, r);
			if (r instanceof OCRProcess) {
				OCRProcess process = (OCRProcess) r;
				saveCheckServerState();	
				if (maxFiles != 0 && maxSize != 0) {
					if (process.getFileCount() + totalFileCount > maxFiles || process.getFileSize() + totalFileSize > maxSize) {
						pause();
					}
				}

			} else {
				throw new IllegalStateException("Not a OCRProcess object");
			}

			pauseLock.lock();
			try {
				while (isPaused) {
					unpaused.await();
				}
			} catch (InterruptedException ie) {
				t.interrupt();
			} finally {
				pauseLock.unlock();
			}
		}

		@Override
		protected void afterExecute(Runnable r, Throwable e) {
			super.afterExecute(r, e);
			if (r instanceof OCRProcess) {
				OCRProcess process = (OCRProcess) r;
				saveCheckServerState();
				if (maxFiles != 0 && maxSize != 0) {
					if (process.getFileCount() + totalFileCount < maxFiles || process.getFileSize() + totalFileSize < maxSize) {
						pause();
					}
				}

			} else {
				throw new IllegalStateException("Not a OCRProcess object");
			}
		}

		//TODO: Check if this stops only the processing of the pool or all threads containt in it
		public void pause() {
			pauseLock.lock();
			try {
				isPaused = true;
			} finally {
				pauseLock.unlock();
			}
		}

		public void resume() {
			pauseLock.lock();
			try {
				isPaused = false;
				unpaused.signalAll();
			} finally {
				pauseLock.unlock();
			}
		}

		//TODO: This should be removed
		protected synchronized void saveCheckServerState () {
			try {
				checkServerState();
			} catch (Exception e) {
				logger.warn("Exception checking server State", e);
				throw new RuntimeException(e);
			}
		}
	}


	public Long getDirectoryCount() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void finalize () {
		session.close();
		sessionFactory.close();
	}
	
}