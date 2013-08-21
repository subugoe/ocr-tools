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
	
	public String submitPost(URL url, byte[] data) {
		String response = "";
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			setupAuthorization(connection);
			connection
					.setRequestProperty("Content-Type", "applicaton/octet-stream");

			connection.setRequestProperty("Content-Length",
					Integer.toString(data.length));
			connection.getOutputStream().write(data);
			
			int responseCode = connection.getResponseCode();
			if (responseCode == 200) {
				InputStream inputStream = connection.getInputStream();
				response = inputStream.toString();
			} else {
				throw new IOException("Illegal response code: " + responseCode);
			}

		} catch (IOException e) {
			throw new RuntimeException("Error with connection.", e);
		}
		return response;
	}
	
	private void setupAuthorization(URLConnection connection) {
		String authString = "Basic: " + encodeUserPassword();
		authString = authString.replaceAll("\n", "");
		connection.addRequestProperty("Authorization", authString);
	}
	private String encodeUserPassword() {
		String toEncode = user + ":" + password;
		return Base64.encodeBase64String(toEncode.getBytes());
	}

}
