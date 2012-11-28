package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;

public class ServerMain {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		MyServers.startDavServer(9001);

		File hotfolder = PathConstants.DAV_FOLDER;
		File expected = PathConstants.EXPECTED_ROOT;
		
		AbbyyServerSimulator sim = new AbbyyServerSimulator(hotfolder, expected);
		
		sim.start();
		
	}

}
