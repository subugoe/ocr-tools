package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.DAV_FOLDER;
import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.EXPECTED_ROOT;
import it.could.webdav.DAVServlet;

import java.io.File;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MyServers {
	final static Logger logger = LoggerFactory.getLogger(MyServers.class);
	private static Server davServer;
	private static AbbyyServerSimulator abbyyServer;

	private static final File davFolder = PathConstants.DAV_FOLDER;
	
	public static void startDavServer() throws Exception {
		startDavServer(PathConstants.DAV_PORT);
	}
		
	public static void startDavServer(int port) throws Exception {
	    
		davFolder.mkdirs();
	    davServer = new Server(port);
		ServletHolder davServletHolder = new ServletHolder(new DAVServlet());
		davServletHolder.setInitParameter("rootPath", davFolder.toString()
				.replace("file:/", ""));
		Context rootContext = new Context(davServer, "/", Context.SESSIONS);
		rootContext.addServlet(davServletHolder, "/*");

		davServer.start();
		logger.info("Started Webdav Server on port " + port);
		logger.info("Mapped directory is " + davFolder);

	}
	
	public static void stopDavServer() throws Exception {
		davServer.stop();
	}
	
	public static void startAbbyySimulator() {
		abbyyServer = new AbbyyServerSimulator(DAV_FOLDER, EXPECTED_ROOT);
		abbyyServer.start();
	}

	public static void stopAbbyySimulator() {
		abbyyServer.finish();
	}
}
