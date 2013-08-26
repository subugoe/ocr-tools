package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;


public class Http {

	private String user;
	private String password;
	
	public Http(String user, String password) {
		this.user = user;
		this.password = password;
	}
	
	public InputStream submitPost(String url, byte[] postData) {
		InputStream response = null;
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
			
			response = getResponseFrom(connection);

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

	public InputStream submitGet(String url) {
		InputStream response = null;
		try {
			URL u = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) u.openConnection();
			setupAuthorization(connection);
			
			response = getResponseFrom(connection);

		} catch (IOException e) {
			throw new RuntimeException("Error with connection.", e);
		}
		return response;

	}

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
