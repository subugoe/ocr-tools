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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
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
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unigoettingen.sub.commons.ocr.util.Pause;


public class JackrabbitHotfolder extends ServerHotfolder implements
		Hotfolder, Serializable {

	private static final long serialVersionUID = 1L;

	private final static Logger log = LoggerFactory.getLogger(JackrabbitHotfolder.class);
	private HttpClient client;
	private Pause pause = new Pause();

	// for unit tests
	void setHttpClient(HttpClient newClient) {
		client = newClient;
	}
	void setPause(Pause newPause) {
		pause = newPause;
	}
		
	public void configureConnection(String serverUrl, String username, String password) {
		URL url;
		try {
			url = new URL(serverUrl);
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
		client = new HttpClient(connectionManager);
		if (username != null || password != null) {
			Credentials creds = new UsernamePasswordCredentials(username,
					password);
			client.getParams().setAuthenticationPreemptive(true);
			client.getState().setCredentials(AuthScope.ANY, creds);
		}
		client.setHostConfiguration(hostConfig);
	}

	@Override
	public void upload(URI fromLocal, URI toRemote) throws IOException {
		File sourceFile = new File(fromLocal);
		String targetUri = toRemote.toString();
		if (!fileAccess.fileExists(sourceFile)) {
			log.error("File " + sourceFile + " doesn't exist.");
			throw new IllegalArgumentException("File " + sourceFile
					+ " doesn't exist.");
		}
		PutMethod putMethod = new PutMethod(targetUri);
		String mimeType = URLConnection.guessContentTypeFromName(sourceFile.getPath());
		putMethod.setRequestEntity(new FileRequestEntity(sourceFile, mimeType));
		execute(putMethod);
	}
	
	@Override
	public void download(URI fromRemote, URI toLocal) throws IOException {
		String sourceUri = fromRemote.toString();
		File targetFile = new File(toLocal);
		GetMethod getMethod = new GetMethod(sourceUri);
		InputStream responseStream = null;
		try {
			responseStream = execute(getMethod);
			fileAccess.copyStreamToFile(responseStream, targetFile);
		} finally {
			responseStream.close();
			getMethod.releaseConnection();
		}
	}
	
	private InputStream execute(HttpMethod method) throws URIException {
		InputStream responseStream = null;
		int responseCode = 0;
		int timesToTry = 10;
		try {
			for (int i = 1; i <= timesToTry; i++) {
				try {
					responseCode = client.executeMethod(method);
					log.trace("Response code in executeMethod: " + responseCode);
					if (responseCode >= HttpStatus.SC_MOVED_PERMANENTLY) {
						throw new IOException("Got illegal response code " + responseCode);
					}
					// method was executed correctly, stop retrying
					responseStream = method.getResponseBodyAsStream();
					break;
				} catch (IOException e) {
					if (i == timesToTry) {
						log.error("Error connecting to server. URL is " + method.getURI());
						throw new IllegalStateException("Error connecting to server. URL is " + method.getURI(), e);
					}
					log.warn("Problem connecting to server. Retry number " + i + "... URL is " + method.getURI());
					pause.forMilliseconds(10000);
				}
			}
		} finally {
			if (responseStream == null) {
				responseStream = new ByteArrayInputStream(new byte[]{});
				method.releaseConnection();
			}
		}
		return responseStream;
	}
	
	@Override
	public void delete(URI uri) throws IOException {
		execute(new DeleteMethod(uri.toString()));
	}

	@Override
	public boolean exists(URI uri) throws IOException {
		HeadMethod headMethod = new HeadMethod(uri.toString());
		try {
			headMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(10, false));
			int responseCode = client.executeMethod(headMethod);
			if (responseCode == HttpStatus.SC_OK) {
				return true;
			}
			return false;
		} finally {
			headMethod.releaseConnection();
		}
	}
	
	@Override
	public long getUsedSpace(URI uri) throws IOException {
		long size = 0;
		try {
			PropFindMethod propFindMethod = new PropFindMethod(uri.toString(),
					DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
			execute(propFindMethod);		
			MultiStatus multiStatus = getMultiStatus(propFindMethod);
			for (MultiStatusResponse response : multiStatus.getResponses()) {
				DavPropertySet props = response.getProperties(200);
				if (props.contains(DavPropertyName.GETCONTENTLENGTH)
						&& props.get(DavPropertyName.GETCONTENTLENGTH).getValue() != null) {
					size += Long.parseLong((String) props.get(
							DavPropertyName.GETCONTENTLENGTH).getValue());
				}
			}
		} catch (DavException e) {
			throw new IOException("Could not execute MultiStatus method", e);
		}
		return size;
	}

	// for unit tests
	MultiStatus getMultiStatus(PropFindMethod method) throws IOException, DavException {
		return method.getResponseBodyAsMultiStatus();
	}
	
	@Override
	public byte[] getResponse(URI uri) throws IOException {
		GetMethod getMethod = new GetMethod(uri.toString());
		InputStream responseStream = null;
		try {
			responseStream = execute(getMethod);
			byte[] responseBytes = IOUtils.toByteArray(responseStream);
			return responseBytes;
		} finally {
			responseStream.close();
			getMethod.releaseConnection();
		}
	}

}
