package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * Simple HTTP client for POST and GET requests.
 * 
 * @author dennis
 *
 */
public class Http {

	private String user;
	private String password;
	
	/**
	 * 
	 * @param user Username for basic authentication
	 * @param password Password for basic authentication
	 */
	public Http(String user, String password) {
		this.user = user;
		this.password = password;
	}
	
	/**
	 * Connects to a given URL and sends data in a POST request.
	 * 
	 * @param url URL to connect to
	 * @param postData Binary data to be sent
	 * @return Response document, eg XML or HTML
	 */
	public String submitPost(String url, byte[] postData) {
		String response = "";
		try {
			URL u = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) u.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			setupAuthorization(connection);
			connection.setRequestProperty("Content-Type", "applicaton/octet-stream");
			connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
			connection.getOutputStream().write(postData);
			
			InputStream is = getResponseFrom(connection);
			response = IOUtils.toString(is);
		} catch (IOException e) {
			throw new RuntimeException("Error with connection.", e);
		}
		return response;
	}
	
	private void setupAuthorization(URLConnection connection) {
		String toEncode = user + ":" + password;
		String encoded = Base64.encodeBase64String(toEncode.getBytes());
		String authString = "Basic: " + encoded;
		authString = authString.replaceAll("\n", "");
		connection.addRequestProperty("Authorization", authString);
	}

	private InputStream getResponseFrom(HttpURLConnection connection) throws IOException {
		InputStream response = null;
		int responseCode = connection.getResponseCode();
		if (responseCode == 200) {
			response = connection.getInputStream();
		} else if (responseCode == 401) {
			throw new IOException("Access denied. Check your username and password");
		} else {
			throw new IOException("Illegal response code: " + responseCode);
		}
		return response;
	}

	/**
	 * Gets the response from a given URL.
	 * 
	 * @param url URL to connect to
	 * @return Response document, eg XML or HTML
	 */
	public String submitGet(String url) {
		String response = "";
		try {
			URL u = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) u.openConnection();
			setupAuthorization(connection);
			
			InputStream is = getResponseFrom(connection);
			response = IOUtils.toString(is);
		} catch (IOException e) {
			throw new RuntimeException("Error with connection.", e);
		}
		return response;

	}

	/**
	 * Gets the response from a given URL skipping basic authentication.
	 * 
	 * @param url URL to connect to
	 * @return Response data, may be text or binary
	 */
	public InputStream submitGetWithoutAuthentication(String url) {
		InputStream response = null;
		try {
			URL u = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) u.openConnection();
			
			response = getResponseFrom(connection);

		} catch (IOException e) {
			throw new RuntimeException("Error with connection.", e);
		}
		return response;

	}

}
