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
import it.could.webdav.DAVServlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.JackrabbitHotfolderImpl;

/* * WebDAV Test
 * JackrabbitHotfolderImpTest
 * @author abergna
 */
public class JackrabbitHotfolderImpTest {
	static Server server;
	private static JackrabbitHotfolderImpl hotfolder;
	private static File davFolder;
	private static File resourcesFolder;

	@BeforeClass
	public static void setUp() throws Exception {
		resourcesFolder = new File(System.getProperty("user.dir")
				+ "/src/test/resources");
		davFolder = new File(System.getProperty("user.dir")
				+ "/target/dav");
//		delete(davFolder);
		davFolder.mkdirs();
	    server = new Server(8090);
		ServletHolder davServletHolder = new ServletHolder(new DAVServlet());
		davServletHolder.setInitParameter("rootPath", davFolder.getAbsolutePath());
		Context rootContext = new Context(server, "/", Context.SESSIONS);
		rootContext.addServlet(davServletHolder, "/*");
		hotfolder = new JackrabbitHotfolderImpl("http://localhost:8090/", "user",
				"pw");
		server.start();
	}

	
	
	@Test
	public void testMkDir() throws IOException, URISyntaxException {
		hotfolder.mkDir(new URI("http://localhost:8090/testMkDir"));
		List<File> subFolders = Arrays.asList(davFolder.listFiles());

		assertTrue(subFolders.toString().contains("testMkDir"));
	}

	@Test
	public void testExists() throws Exception {
		new File(davFolder + "/testExists").mkdir();

		assertTrue(hotfolder.exists(new URI("http://localhost:8090/testExists")));
	}
		
	@Ignore
	@Test
	public void testCopyFileFromServerToLocal() throws Exception {
		davFolder = new File(davFolder.toString().replace("file:/", "")+ "/"+ "from/FromTO");
		URI to = davFolder.toURI();
		URI from = new URI("http://localhost:8090/to/FromTO");
		hotfolder.copyFile(from, to);
		assertTrue(new File(davFolder.toString()).exists());
	}
	
	@Test
	public void testCopyFileFromLocalToServer() throws Exception {
		File sourceFile = new File(resourcesFolder + "/WEBDAV/xmlExport.xml");
		URI from = sourceFile.toURI();
		URI to = new URI("http://localhost:8090/xmlExport.xml");
		hotfolder.copyFile(from, to);
		
		assertTrue(new File(davFolder + "/xmlExport.xml").exists());
	}
	
	@Test
	public void testOpenInputStream() throws Exception {
		File source = new File(resourcesFolder + "/WEBDAV/xmlExport.xml");
		File target = new File(davFolder + "/inputStream.xml");
		FileUtils.copyFile(source, target);
		InputStream isResult = hotfolder.openInputStream(new URI("http://localhost:8090/inputStream.xml"));
		assertTrue(isResult != null);
	}
	
	@Test
	public void testDelete() throws Exception {
		File toDelete = new File(davFolder + "/deleteTest");
		toDelete.mkdir();
		hotfolder.delete(new URI("http://localhost:8090/deleteTest"));

		assertFalse(toDelete.exists());
	}

	@Test
	public void testDeleteIfExists() throws Exception {
		File toDelete = new File(davFolder + "/deleteIfExists");
		toDelete.mkdir();
		hotfolder.deleteIfExists(new URI("http://localhost:8090/deleteIfExists"));

		assertFalse(toDelete.exists());

		hotfolder.deleteIfExists(new URI("http://localhost:8090/deleteIfExists"));
		// no exception thrown
		assertTrue(true);
	}
	
	
	@AfterClass
	public static void destroy() throws Exception {
		delete(davFolder);
		server.stop();
		
	}
	
	// deletes a complete directory structure
	public static void delete(File file) {
		if (file.isFile()) {
			file.delete();
		} else if (file.isDirectory() && Arrays.asList(file.list()).isEmpty()) {
			file.delete();
		} else {
			for (File child : file.listFiles()) {
				delete(child);
			}
			file.delete();
		}
	}
	
}
