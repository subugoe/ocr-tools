package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.net.URL;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;

public class AbbyyOCRFile extends AbstractOCRImage implements OCRImage {
	//This represents the filename that should be written to the ticket.
	protected String remoteFileName;

	//This represents the URL to the remote system
	protected URL remoteURL;

	public AbbyyOCRFile(URL imageUrl) {
		super(imageUrl);
		
	}
	
	public AbbyyOCRFile(URL imageUrl, URL remoteURL, String remoteFileName) {
		super(imageUrl);
		this.remoteURL = remoteURL;
		this.remoteFileName = remoteFileName;
	}

	public String getRemoteFileName() {
		return remoteFileName;
	}

	public void setRemoteFileName(String remoteFileName) {
		this.remoteFileName = remoteFileName;
	}

	public URL getRemoteURL() {
		return remoteURL;
	}

	public void setRemoteURL(URL remoteURL) {
		this.remoteURL = remoteURL;
	}

}
