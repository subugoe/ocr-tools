package de.uni_goettingen.sub.commons.ocr.abbyy.server;
/*

Copyright 2010 SUB Goettingen. All rights reserved.
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

*/

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
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

import com.abbyy.fineReaderXml.fineReader6SchemaV1.DocumentDocument;
import com.abbyy.fineReaderXml.fineReader6SchemaV1.DocumentDocument.Document;
import com.abbyy.recognitionServer10Xml.xmlResultSchemaV1.XmlResultDocument;
import com.abbyy.recognitionServer10Xml.xmlResultSchemaV1.XmlResultDocument.XmlResult;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.JackrabbitHotfolderImpl;

/* * WebDAV Test
 * JackrabbitHotfolderImpTest
 * @author abergna
 */
public class JackrabbitHotfolderImpTest {
	static Server server;
	private Context rootContext;
	private static InputStream isResult = null;
	protected static XmlResultDocument xmlResultDocument; 
	XmlResult xm ;
	protected static DocumentDocument documentDocument;
	Document doc;
	//private static File BASEFOLDER_FILE;
	static JackrabbitHotfolderImpl imp;
	static File WEBDAV;
	final static Logger logger = LoggerFactory
			.getLogger(JackrabbitHotfolderImpTest.class);

	@Before
	public void setUp() throws Exception {
		File file = new File(System.getProperty("user.dir")
				+ "/src/test/resources");
		//File file = getBaseFolderAsFile();
	    WEBDAV = new File(file.toString() + "/WEBDAV");
		WEBDAV.mkdir();
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
	public void testcopyfileFromServerToLocal() throws Exception {
		WEBDAV = new File(WEBDAV.toString().replace("file:/", "")+ "/"+ "from/FromTO");
		URI to = WEBDAV.toURI();
		URI from = new URI("http://localhost:8090/to/FromTO");
		imp.copyFile(from, to);
		assertTrue(new File(WEBDAV.toString()).exists());
	}
	
	@Test
	public void testcopyfileFromLocalToServer() throws Exception {
		WEBDAV = new File(WEBDAV.toString().replace("file:/", "")+ "/"+ "from/FromTO");
		URI from = WEBDAV.toURI();
		URI to = new URI("http://localhost:8090/TestA/FromTO");
		imp.copyFile(from, to);
		assertTrue(imp.exists(to));
	}
	
	@Test
	public void testOpenInputStream() throws Exception {
		assertTrue(imp.exists(new URI("http://localhost:8090/xmlExport.xml")));
		for (int i = 1 ; i <= 30 ; i++){
			System.out.println(i);
			isResult = imp.openInputStream(new URI("http://localhost:8090/xmlExport"+ i + ".xml"));
			assertTrue(isResult != null);
			documentDocument = DocumentDocument.Factory.parse(isResult);
			doc = documentDocument.getDocument();
		    System.out.println(doc.toString());
		    isResult = null;
		}	
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
		
		imp.deleteIfExists(new URI("http://localhost:8090/from/FromTO"));
		assertFalse(imp.exists(new URI("http://localhost:8090/from/FromTO")));
	}
	
	
	@After
	public void destroy() throws Exception {
		server.stop();
		System.out.println("Server Stopped");
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
