package de.unigoettingen.sub.commons.ocrComponents.cli;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.ServerHotfolder;

public class FakeHotfolder extends ServerHotfolder {

	public static FakeHotfolder instance;
	
	public static FakeHotfolder getInstance() {
		return instance;
	}
	
	@Override
	public void copyFile(URI from, URI to) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(URI uri) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mkDir(URI uri) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Boolean exists(URI uri) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getSize(URI uri) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isDirectory(URI uri) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<URI> listURIs(URI uri) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream openInputStream(URI uri) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void configureConnection(String serverUrl, String username,
			String password) {
		// TODO Auto-generated method stub
		
	}

}
