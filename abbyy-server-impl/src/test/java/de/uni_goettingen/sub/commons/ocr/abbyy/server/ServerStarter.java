package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import it.could.webdav.DAVServlet;

import java.io.File;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServerStarter {
	final static Logger logger = LoggerFactory.getLogger(ServerStarter.class);
	private static Server davServer = null; // 9001

	public static final File davFolder = new File(System.getProperty("user.dir")
			+ "/target/dav");
	
	public static void startAbbyyDavServer(int port) throws Exception {
		startDavServer(port);
		new File(davFolder, "input").mkdir();
		new File(davFolder, "output").mkdir();
		new File(davFolder, "error").mkdir();
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

}
