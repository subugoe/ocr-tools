/**
 * 
 */
package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

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
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.vfs.FileSystemException;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cmahnke
 * 
 */
public class JackrabbitHotfolderImpl implements Hotfolder {
	// The Constant logger.
	final static Logger logger = LoggerFactory.getLogger(JackrabbitHotfolderImpl.class);
	private long mkColWait = 300l;
	protected static HttpClient client = null;
	private static Hotfolder _instance;

	protected Map<String, File> tmpfiles = new HashMap<String, File>();

	/**
	 * 
	 */
	private JackrabbitHotfolderImpl(ConfigParser config) {
		try {
			client = initConnection(config.serverURL, config.username, config.password);
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#checkServerState()
	 */
	@Override
	public void checkServerState () throws IOException, URISyntaxException {
		throw new NotImplementedException();
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#copyFile(java.lang.String, java.lang.String)
	 */
	@Override
	public void copyFile (String from, String to) throws FileSystemException {
		throw new NotImplementedException();
		//Wehave two methods that must be called here, one for local to remote and the other way arround

	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#copyTmpFile(java.lang.String, java.net.URI)
	 */
	@Override
	public void copyTmpFile (String tmpFile, URI to) throws IOException {
		if (tmpfiles.containsKey(tmpFile)) {
			copyFile(tmpfiles.get(tmpFile).toURI().toString(), to.toString());
		} else {
			throw new IOException("Tmp file not registred at hotfolder");
		}

	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#createTmpFile(java.lang.String)
	 */
	@Override
	public OutputStream createTmpFile (String name) throws IOException, URISyntaxException {
		File tmpFile = File.createTempFile(name, null);
		tmpfiles.put(name, tmpFile);
		return new FileOutputStream(tmpFile);
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#delete(java.net.URI)
	 */
	@Override
	public void delete (URI uri) throws IOException {
		DavMethod delete = new DeleteMethod(uri.toString());
		executeMethod(client, delete);
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#deleteIfExists(java.net.URI)
	 */
	@Override
	public void deleteIfExists (URI uri) throws IOException {
		if (head(uri) == 200) {
			delete(uri);
			logger.debug("Deleted " + uri);
		}

	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#exists(java.net.URI)
	 */
	@Override
	public Boolean exists (URI uri) throws IOException {
		if (head(uri) == 200) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#mkDir(java.net.URI)
	 */
	@Override
	public void mkDir (URI uri) throws IOException {
		DavMethod mkCol = new MkColMethod(uri.toString());
		executeMethod(client, mkCol);

		//Since we use the multithreaded Connection manager we have to wait until the directory is created
		//The problem doesn't accoure in debug mode since the main thread is slower there
		//You get a 403 if you try to PUT something in an non existing COLection
		while (true) {
			try {
				Thread.sleep(mkColWait);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Integer status = head(uri);
			if (status == HttpStatus.SC_OK) {
				break;
			}
			if (status == HttpStatus.SC_FORBIDDEN) {
				throw new IllegalStateException("Got HTTP Code " + status);
			}
		}

	}

	protected void put (String uri, File file) throws HttpException, IOException {
		if (!file.exists()) {
			throw new IllegalArgumentException("File " + file + " doesn't exist.");
		}
		PutMethod put = new PutMethod(uri);
		String fileName = file.getPath();
		String mimeType = URLConnection.guessContentTypeFromName(fileName);
		put.setRequestEntity(new FileRequestEntity(file, mimeType));
		executeMethod(client, put);
	}

	protected int head (URI uri) throws HttpException, IOException {
		HeadMethod head = new HeadMethod(uri.toString());
		Integer status = client.executeMethod(head);
		head.releaseConnection();
		return status;
	}

	protected static void executeMethod (HttpClient client, DavMethod method) throws HttpException, IOException {
		Integer responseCode = client.executeMethod(method);
		method.releaseConnection();
		logger.trace("Response code: " + responseCode);
		if (responseCode >= HttpStatus.SC_UNAUTHORIZED) {
			throw new IllegalStateException("Got HTTP Code " + responseCode + " for " + method.getURI());
		}
	}

	public static HttpClient initConnection (String webdavURL, String webdavUsername, String webdavPassword) throws GeneralSecurityException, IOException {
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

	protected void getWebdavFile (String url, String outdir, String localfilename) {
		outdir = outdir.endsWith(File.separator) ? outdir : outdir + File.separator;
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

			//TODO: Use the stream helper class from util here
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
	
	protected Boolean isLocal (URI uri) {
		return false;
	}
	
	public static Hotfolder newInstace (ConfigParser config) {
		if (_instance == null) {
			_instance = new JackrabbitHotfolderImpl(config);
		}
		return _instance;
	}


}
