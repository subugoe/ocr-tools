package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;

public class ServerMain {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		ServerStarter.startAbbyyDavServer(9001);

		File hotfolder = ServerStarter.davFolder;
		File expected = new File(System.getProperty("user.dir") + "/src/test/resources/expected");
		
		AbbyyServerSimulator sim = new AbbyyServerSimulator(hotfolder, expected);
		
		sim.start();
		
	}

}
