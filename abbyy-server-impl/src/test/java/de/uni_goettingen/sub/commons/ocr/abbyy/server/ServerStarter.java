package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import it.could.webdav.DAVServlet;

import java.io.File;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;


public class ServerStarter {
	private static Server davServer = null; // 9001

	public static File davFolder = new File(System.getProperty("user.dir")
			+ "/target/dav");
	
	public static void startDavServer(int port) throws Exception {
	    
		davFolder.mkdirs();
	    davServer = new Server(port);
		ServletHolder davServletHolder = new ServletHolder(new DAVServlet());
		davServletHolder.setInitParameter("rootPath", davFolder.toString()
				.replace("file:/", ""));
		Context rootContext = new Context(davServer, "/", Context.SESSIONS);
		rootContext.addServlet(davServletHolder, "/*");
		davServer.start();

	}

}
