package de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder;

import java.io.IOException;
import java.net.URI;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.ServerHotfolder;

public class HotfolderMockProvider extends ServerHotfolder {

	public static ServerHotfolder mock;
	
	public static ServerHotfolder getMock() {
		return mock;
	}
	
	@Override
	public void delete(URI uri) throws IOException {
	}

	@Override
	public boolean exists(URI uri) throws IOException {
		return false;
	}

	@Override
	public byte[] getResponse(URI uri) throws IOException {
		return null;
	}

	@Override
	public void configureConnection(String serverUrl, String username,
			String password) {
	}

	@Override
	public void upload(URI fromLocal, URI toRemote) throws IOException {
	}

	@Override
	public void download(URI fromRemote, URI toLocal) throws IOException {
	}

	@Override
	public long getUsedSpace(URI uri) throws IOException {
		return 0;
	}

}
