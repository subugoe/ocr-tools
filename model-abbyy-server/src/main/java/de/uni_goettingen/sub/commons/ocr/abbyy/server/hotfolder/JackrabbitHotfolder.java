package de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder;

/*

 © 2009,2010, SUB Göttingen. All rights reserved.
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
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.NotImplementedException;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unigoettingen.sub.commons.util.file.FileUtils;

public class JackrabbitHotfolder extends ServerHotfolder implements
		Hotfolder, Serializable {

	private static final long serialVersionUID = 1L;

	private final static Logger log = LoggerFactory.getLogger(JackrabbitHotfolder.class);
	private long mkColWait = 300l;
	transient protected HttpClient client;

	JackrabbitHotfolder(String serverUrl, String username, String password) {
		configureConnection(serverUrl, username, password);
	}
	public JackrabbitHotfolder() {
	}
	
	protected void configureConnection(String serverUrl, String username, String password) {
		try {
			client = initConnection(serverUrl, username, password);
		} catch (IOException e) {
			log.error("Got an IOException while initilizing Jackrabbit Hotfolder implementation", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#copyFile(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	public void copyFile(URI from, URI to) throws IOException {
		// We have two methods that must be called here, one for local to remote
		// and the other way arround
		if (isLocal(from) && !isLocal(to)) {
			// This should be an upload
			put(to.toString(), new File(from));
		} else if (!isLocal(from) && isLocal(to)) {
			// This should be a download
			// outdir = outdir.endsWith(File.separator) ? outdir : outdir +
			// File.separator;
			// File localFile = new File(outdir + localfilename);
			getWebdavFile(from, new File(to));
		} else if (isLocal(from) && isLocal(to)) {
			// Just copy local files
			FileUtils.copyDirectory(new File(from), new File(to));
		} else {
			log.error("Copy from WebDAV URI to WebDAV URI isn't implemented!");
			throw new NotImplementedException(
					"Copy from WebDAV URI to WebDAV URI isn't implemented!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#delete(java.
	 * net.URI)
	 */
	@Override
	public void delete(URI uri) throws IOException {
		DavMethod delete = new DeleteMethod(uri.toString());
		executeMethod(client, delete);
		log.debug("Deleted " + uri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#exists(java.
	 * net.URI)
	 */
	@Override
	public Boolean exists(URI uri) throws IOException {
		if (head(uri) == HttpStatus.SC_OK) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#mkDir(java.net
	 * .URI)
	 */
	@Override
	public void mkDir(URI uri) throws IOException {
		DavMethod mkCol = new MkColMethod(uri.toString());
		executeMethod(client, mkCol);

		// Since we use the multithreaded Connection manager we have to wait
		// until the directory is created
		// The problem doesn't occur in debug mode since the main thread is
		// slower there
		// You get a 403 if you try to PUT something in an non existing
		// COLection
		while (true) {
			try {
				Thread.sleep(mkColWait);
			} catch (InterruptedException e) {
				log.error("The current Thread was interupted", e);
			}
			Integer status = head(uri);
			if (status == HttpStatus.SC_OK) {
				break;
			}
			if (status == HttpStatus.SC_FORBIDDEN) {
				log.error("Got HTTP Code " + status + " for " + uri.toString());
				throw new IllegalStateException("Got HTTP Code " + status);
			}
		}
	}

	private void put(String uri, File file) throws HttpException, IOException {
		if (!file.exists()) {
			log.error("File " + file + " doesn't exist.");
			throw new IllegalArgumentException("File " + file
					+ " doesn't exist.");
		}
		PutMethod put = new PutMethod(uri);
		String fileName = file.getPath();
		String mimeType = URLConnection.guessContentTypeFromName(fileName);
		put.setRequestEntity(new FileRequestEntity(file, mimeType));
		executeMethod(client, put);
	}

	private Integer head(URI uri) {
		HeadMethod head = new HeadMethod(uri.toString());
		Integer status = 0;
		
		int timesToTry = 10;
		try {
			for (int i = 1; i <= timesToTry; i++) {
				try {
					status = client.executeMethod(head);
					break;
				} catch (IOException e) {
					if (i == timesToTry) {
						log.error("Error connecting to server. URL is " + uri, e);
						throw new IllegalStateException("Error connecting to server. URL is " + uri, e);
					}
					log.warn("Problem connecting to server. Retry number " + i + "... URL is " + uri);
					try{
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						log.error("interrupted while sleeping");
					}
				}
			}
		} finally {
			head.releaseConnection();
		}
		return status;
	}

	private static void executeMethod(HttpClient client, DavMethod method) 
	throws URIException {
		Integer responseCode = 0;
		
		int timesToTry = 10;
		try {
			for (int i = 1; i <= timesToTry; i++) {
				try {
					responseCode = client.executeMethod(method);
					log.trace("Response code in executeMethod: " + responseCode);
					if (responseCode >= HttpStatus.SC_UNAUTHORIZED) {
						throw new IOException("Got illegal response code " + responseCode);
					}
					// method was executed correctly, stop retrying
					break;
				} catch (IOException e) {
					if (i == timesToTry) {
						log.error("Error connecting to server. URL is " + method.getURI(), e);
						throw new IllegalStateException("Error connecting to server. URL is " + method.getURI(), e);
					}
					log.warn("Problem connecting to server. Retry number " + i + "... URL is " + method.getURI());
					try{
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						log.error("interrupted while sleeping");
					}
				}
			}
		} finally {
			method.releaseConnection();
		}
	}
	
	private static HttpClient initConnection(String webdavURL,
			String webdavUsername, String webdavPassword)
			throws IOException {
		if (webdavURL == null) {
			throw new IllegalStateException("no host given");
		}

		URL url;
		try {
			url = new URL(webdavURL);
		} catch (MalformedURLException e) {
			throw new IllegalStateException("no valid host given: "
					+ e.toString());
		}

		HostConfiguration hostConfig = new HostConfiguration();
		hostConfig.setHost(url.getHost(), url.getDefaultPort(),
				url.getProtocol());

		HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		int maxHostConnections = 10;

		params.setMaxConnectionsPerHost(hostConfig, maxHostConnections);
		params.setStaleCheckingEnabled(true);
		params.setSoTimeout(10000);
		connectionManager.setParams(params);
		HttpClient client = new HttpClient(connectionManager);
		if (webdavUsername != null || webdavPassword != null) {
			Credentials creds = new UsernamePasswordCredentials(webdavUsername,
					webdavPassword);
			client.getParams().setAuthenticationPreemptive(true);
			client.getState().setCredentials(AuthScope.ANY, creds);
		}
		client.setHostConfiguration(hostConfig);
		return client;
	}

	private void getWebdavFile(URI uri, File localFile) throws IOException {
		GetMethod method = new GetMethod(uri.toString());
		InputStream is = null;
		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(3, false));
		try {
			// Execute the method.
			Integer statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				log.error("Method failed: " + method.getStatusLine());
			}
			is = method.getResponseBodyAsStream();
			org.apache.commons.io.FileUtils
					.copyInputStreamToFile(is, localFile);

		} catch (IOException e) {
			log.error("Fatal transport error: ", e);
		} finally {
			// Release the connection.
			is.close();
			method.releaseConnection();
		}
	}

	@Override
	public Long getTotalSize(URI uri) throws IOException {
		Long size = 0l;
	//	for (URI u : listURIs(uri)) {
			MultiStatus multiStatus;
			try {
				multiStatus = propFind(uri);
			} catch (DavException e) {
				throw new IOException("Could not execute MultiStatus method", e);
			}
			List<MultiStatusResponse> responses = Arrays.asList(multiStatus
					.getResponses());
			for (MultiStatusResponse response : responses) {
				DavPropertySet props = response.getProperties(200);
				if (props.contains(DavPropertyName.GETCONTENTLENGTH)
						&& props.get(DavPropertyName.GETCONTENTLENGTH)
								.getValue() != null) {
					size += Long.parseLong((String) props.get(
							DavPropertyName.GETCONTENTLENGTH).getValue());
				}
			}
	//	}
		return size;
	}

	@Override
	// TODO: Finish this
	public Boolean isDirectory(URI uri) throws IOException {
//		MultiStatus multiStatus;
//		try {
//			multiStatus = propFind(uri);
//		} catch (DavException e) {
//			throw new IOException("Could not execute MultiStatus method", e);
//		}
		/*
		 * multiStatus.g
		 * 
		 * List<MultiStatusResponse> responses =
		 * Arrays.asList(multiStatus.getResponses()); if (responses.get(0).)
		 * throw new NotImplementedException();
		 */
		// return null;
		throw new NotImplementedException();
	}

	@Override
	public List<URI> listURIs(URI uri) throws IOException {
		List<URI> uriList = new ArrayList<URI>();
		MultiStatus multiStatus;
		try {
			multiStatus = propFind(uri);
		} catch (DavException e) {
			throw new IOException("Could not execute MultiStatus method", e);
		}
		List<MultiStatusResponse> responses = Arrays.asList(multiStatus
				.getResponses());
		for (MultiStatusResponse response : responses) {
			String path = response.getHref();
			try {
				uriList.add(new URI(path));
			} catch (URISyntaxException e) {
				log.error("Error while coverting URI " + path);
				throw new IllegalStateException(e);
			}
		}
		return uriList;
	}

	/*
	 * protected void copyServerFiles(Map<String, String> copyFiles) throws
	 * InterruptedException { //This is a bit odd, the Server returns 200 for a
	 * head but the files ared really there already, so wait we a bit. //This is
	 * probably the reason why the remote cleanup fails as well
	 * Thread.sleep(checkInterval); for (String from : copyFiles.keySet()) {
	 * OCRStreamHandler handler; // getOCRResults(from, "",
	 * copyFiles.get(from)); GetMethod get = null; try { get = new
	 * GetMethod(from); get.setFollowRedirects(true);
	 * this.client.executeMethod(get); InputStream in =
	 * get.getResponseBodyAsStream();
	 * 
	 * handler = new SaveFileStreamHandler(new File(copyFiles.get(from)), in);
	 * 
	 * 
	 * handler.handle(); logger.info("Trying to handle " + from + " to output "
	 * + copyFiles.get(from)); delete(from); logger.debug("Deleted " + from);
	 * 
	 * } catch (HttpException e) {
	 * logger.error("HttpException: Failed to get file: " + from + " to " +
	 * copyFiles.get(from), e); } catch (IOException e) {
	 * logger.error("IOException: Failed to get file: " + from + " to " +
	 * copyFiles.get(from), e); } finally { get.releaseConnection(); }
	 * 
	 * 
	 * } }
	 */

	/*
	 * protected Map<String, Long> getRemoteSizes(String uri) throws
	 * HttpException, IOException, DavException { Map<String, Long> infoMap =
	 * new LinkedHashMap<String, Long>(); MultiStatus multiStatus =
	 * propFind(uri); List<MultiStatusResponse> responses =
	 * Arrays.asList(multiStatus.getResponses());
	 * 
	 * for (MultiStatusResponse response : responses) { String path =
	 * response.getHref(); DavPropertySet props = response.getProperties(200);
	 * if (props.contains(DavPropertyName.GETCONTENTLENGTH) &&
	 * props.get(DavPropertyName.GETCONTENTLENGTH).getValue() != null) {
	 * infoMap.put(path, Long.parseLong((String)
	 * props.get(DavPropertyName.GETCONTENTLENGTH).getValue())); } else {
	 * infoMap.put(path, null); } } return infoMap; }
	 */

	private MultiStatus propFind(URI uri) throws IOException, DavException {
		DavMethod probFind = new PropFindMethod(uri.toString(),
				DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
		executeMethod(client, probFind);
		// TODO: Check if this really works since the connection is already
		// closed if executed by the static methos
		return probFind.getResponseBodyAsMultiStatus();
	}

	@Override
	public InputStream openInputStream(URI uri) throws IOException {
		GetMethod method = new GetMethod(uri.toString());

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(3, false));

		Integer statusCode = client.executeMethod(method);

		if (statusCode != HttpStatus.SC_OK) {
			log.error("Method failed: " + method.getStatusLine());
			throw new IOException(method.getStatusLine().toString());
		}
		return method.getResponseBodyAsStream();

	}

	@Override
	public Long getSize(URI uri) throws IOException {
		
		throw new NotImplementedException();
		// return null;
	}

}