package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.helpers.Loader;
import org.junit.After;
import org.junit.Before;

import org.junit.Test;
import it.could.webdav.DAVServlet;

import org.mortbay.jetty.Server;

import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.JackrabbitHotfolderImpl;

/* * WebDAV Wagon Test
 * @author abergna
 */
public class JackrabbitHotfolderImpTest {
	static Server server;
	private Context rootContext;
	static JackrabbitHotfolderImpl imp;
	static File WEBDAV;
	final static Logger logger = LoggerFactory
			.getLogger(JackrabbitHotfolderImpTest.class);

	@Before
	public void setUp() throws Exception {
		File file = getBaseFolderAsFile();
	    WEBDAV = new File(file.toString() + "/WEBDAV");
		server = new Server(8090);
		ServletHolder davServletHolder = new ServletHolder(new DAVServlet());
		davServletHolder.setInitParameter("rootPath", WEBDAV.toString()
				.replace("file:/", ""));
		rootContext = new Context(server, "/", Context.SESSIONS);
		rootContext.addServlet(davServletHolder, "/*");
		imp = new JackrabbitHotfolderImpl("http://localhost:8090/", "user",
				"pw");
		server.start();
	}

	
	
	@Test
	public void testMKDIR() throws Exception {
		imp.mkDir(new URI("http://localhost:8090/TestA"));
		assertTrue(new File(WEBDAV.toString().replace("file:/", "")+ "/"+ "TestA").exists());
		imp.mkDir(new URI("http://localhost:8090/TestB"));
		assertTrue(new File(WEBDAV.toString().replace("file:/", "")+ "/"+ "TestB").exists());
	}

	@Test
	public void testexists() throws Exception {
		assertTrue(imp.exists(new URI("http://localhost:8090/TestA")));
		assertTrue(imp.exists(new URI("http://localhost:8090/TestB")));
	}
	
	@Test
	public void testcopyfileFromLocalToServer() throws Exception {
		WEBDAV = new File(WEBDAV.toString().replace("file:/", "")+ "/"+ "from/test");
		URI from = WEBDAV.toURI();
		URI to = new URI("http://localhost:8090/to/test/");
		imp.copyFile(from, to);
		assertFalse(imp.exists(new URI("http://localhost:8090/TestB")));
	}
	
	@Test
	public void testcopyfileFromServerToLocal() throws Exception {
		WEBDAV = new File(WEBDAV.toString().replace("file:/", "")+ "/"+ "from/FromTO");
		URI to = WEBDAV.toURI();
		URI from = new URI("http://localhost:8090/to/FromTO/");
		imp.copyFile(from, to);
		assertFalse(imp.exists(to));
	}
	
	@Test
	public void testdelete() throws Exception {
		imp.delete(new URI("http://localhost:8090/TestA"));
		assertFalse(imp.exists(new URI("http://localhost:8090/TestA")));
	}

	@Test
	public void testdeleteIfExists() throws Exception {
		imp.deleteIfExists(new URI("http://localhost:8090/TestB"));
		assertFalse(imp.exists(new URI("http://localhost:8090/TestB")));
		
		imp.deleteIfExists(new URI("http://localhost:8090/to/test"));
		assertFalse(imp.exists(new URI("http://localhost:8090/to/test")));
	}
	
	@After
	public void destroy() throws Exception {
		server.stop();
		System.out.println("Server Stop");
	}

	public static File getBaseFolderAsFile() {
		File basefolder;
		// TODO: GDZ: Do wee really need to depend on Log4J here? I don't think
		// so...
		URL url = Loader.getResource("");
		try {
			basefolder = new File(url.toURI());
		} catch (URISyntaxException ue) {
			basefolder = new File(url.getPath());
		}
		return basefolder;
	}
}
