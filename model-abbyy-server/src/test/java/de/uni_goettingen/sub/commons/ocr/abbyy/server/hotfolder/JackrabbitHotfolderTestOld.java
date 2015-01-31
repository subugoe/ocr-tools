package de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder;

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

import static org.junit.Assert.*;

import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.MyServers;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.JackrabbitHotfolder;

/* * WebDAV Test
 * JackrabbitHotfolderImpTest
 * @author abergna
 */
public class JackrabbitHotfolderTestOld {
	private static JackrabbitHotfolder hotfolder;

	@BeforeClass
	public static void setUp() throws Exception {
		MyServers.startDavServer();
		hotfolder = new JackrabbitHotfolder();
		hotfolder.configureConnection("http://localhost:9001/", "", "");
	}

	@Test
	public void testMkDir() throws IOException, URISyntaxException {
		hotfolder.mkDir(new URI(DAV_ADDRESS + "testMkDir"));
		List<File> subFolders = Arrays.asList(DAV_FOLDER.listFiles());

		assertTrue(subFolders.toString().contains("testMkDir"));
		new File(DAV_FOLDER, "testMkDir").delete();
	}

	@Test
	public void testExists() throws Exception {
		new File(DAV_FOLDER, "testExists").mkdir();

		assertTrue(hotfolder.exists(new URI(DAV_ADDRESS + "testExists")));
		assertFalse(hotfolder.exists(new URI(DAV_ADDRESS + "notExistent")));
	}

	@Test
	public void testCopyFileFromServerToLocal() throws Exception {
		PrintWriter test = new PrintWriter(DAV_FOLDER + "/test.txt");
		test.println("test");
		test.close();
		URI from = new URI(DAV_ADDRESS + "test.txt");
		URI to = new File(DAV_FOLDER, "test.txt").toURI();
		hotfolder.copyFile(from, to);
		assertTrue(new File(DAV_FOLDER, "test.txt").exists());
	}

	@Ignore
	// the tested method is not implemented yet
	@Test
	public void testCopyFileFromServerToServer() throws Exception {
		PrintWriter test = new PrintWriter(DAV_FOLDER + "/test_s2s.txt");
		test.println("copy from server to server");
		test.close();
		URI from = new URI(DAV_ADDRESS + "test_s2s.txt");
		URI to = new URI(DAV_ADDRESS + "test_s2s_copy.txt");
		hotfolder.copyFile(from, to);
		assertTrue(new File(DAV_FOLDER, "test_s2s_copy.txt").exists());
	}

	@Test
	public void testCopyFileFromLocalToServer() throws Exception {
		File sourceFile = new File(LOCAL_INPUT, "xmlExport.xml");
		URI from = sourceFile.toURI();
		URI to = new URI(DAV_ADDRESS + "xmlExport.xml");
		hotfolder.copyFile(from, to);

		assertTrue(new File(DAV_FOLDER, "xmlExport.xml").exists());
	}

	@Test
	public void getTotalSize() throws IOException, URISyntaxException {
		File file = new File(DAV_FOLDER, "testSize.txt");
		PrintWriter test = new PrintWriter(file);
		test.println("test");
		test.close();

		Long fileSize = hotfolder.getTotalSize(new URI(DAV_ADDRESS
				+ "testSize.txt"));
		assertEquals(new Long(5), fileSize);
	}

	@Test
	public void listURIs() throws IOException, URISyntaxException {
		new File(DAV_FOLDER, "testFile").createNewFile();
		List<URI> uris = hotfolder.listURIs(new URI(DAV_ADDRESS + "testFile"));
		assertEquals(1, uris.size());
		
		File testDir = new File(DAV_FOLDER, "testDir");
		testDir.mkdir();
		new File(testDir, "someFile").createNewFile();
		uris = hotfolder.listURIs(new URI(DAV_ADDRESS + "testDir"));
		assertEquals(2, uris.size());
	}

	@Test
	public void testOpenInputStream() throws Exception {
		File source = new File(LOCAL_INPUT, "xmlExport.xml");
		File target = new File(DAV_FOLDER, "inputStream.xml");
		FileUtils.copyFile(source, target);
		InputStream isResult = hotfolder.openInputStream(new URI(DAV_ADDRESS
				+ "inputStream.xml"));
		assertTrue(isResult != null);
		isResult.close();
	}

	@Test
	public void testDelete() throws Exception {
		File toDelete = new File(DAV_FOLDER, "deleteTest");
		toDelete.mkdir();
		assertTrue(toDelete.exists());

		hotfolder.delete(new URI(DAV_ADDRESS + "deleteTest"));

		assertFalse(toDelete.exists());
	}

	@Test
	public void testDeleteIfExists() throws Exception {
		File toDelete = new File(DAV_FOLDER, "deleteIfExists");
		toDelete.mkdir();
		hotfolder.deleteIfExists(new URI(DAV_ADDRESS + "deleteIfExists"));

		assertFalse(toDelete.exists());

		hotfolder.deleteIfExists(new URI(DAV_ADDRESS + "deleteIfExists"));
		// no exception thrown
		assertTrue(true);
	}

	@AfterClass
	public static void destroy() throws Exception {
		// delete(DAV_ROOT);
		MyServers.stopDavServer();
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
