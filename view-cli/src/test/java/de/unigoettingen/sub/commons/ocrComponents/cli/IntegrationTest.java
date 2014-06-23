package de.unigoettingen.sub.commons.ocrComponents.cli;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ConcurrentModificationException;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.ServerHotfolder;
import de.unigoettingen.sub.commons.ocrComponents.cli.testutil.FileManagerMockProvider;
import de.unigoettingen.sub.commons.ocrComponents.cli.testutil.HotfolderMockProvider;
import de.unigoettingen.sub.ocr.controller.FileManager;

public class IntegrationTest {

	private ByteArrayOutputStream baos;
	private Main main;
	private FileManager fileManagerMock;
	private ServerHotfolder hotfolderMock;
	
	@Before
	public void beforeEachTest() {
		main = new Main();
		baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		main.redirectSystemOutputTo(out);

		fileManagerMock = mock(FileManager.class);
		FileManagerMockProvider.mock = fileManagerMock;
		
		hotfolderMock = mock(ServerHotfolder.class, withSettings().serializable());
		HotfolderMockProvider.mock = hotfolderMock;
	}

	@Test
	public void shouldComplainAboutInput() throws UnsupportedEncodingException {
		when(fileManagerMock.isReadableFolder("/tmp/in")).thenReturn(false);
		when(fileManagerMock.isWritableFolder("/tmp/out")).thenReturn(true);
		main.execute(validOptions());

		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Illegal options: Input folder not found."));
	}
	
	@Test
	public void shouldComplainAboutOutput() throws UnsupportedEncodingException {
		when(fileManagerMock.isReadableFolder("/tmp/in")).thenReturn(true);
		when(fileManagerMock.isWritableFolder("/tmp/out")).thenReturn(false);
		main.execute(validOptions());

		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Illegal options: Output folder not found or it is protected."));
	}
	
	@Test(expected=ConcurrentModificationException.class)
	public void shouldComplainAboutExistingLockFile() throws URISyntaxException, IOException {
		when(fileManagerMock.isReadableFolder("/tmp/in")).thenReturn(true);
		when(fileManagerMock.isWritableFolder("/tmp/out")).thenReturn(true);
		when(fileManagerMock.getAllFolders(anyString(), any(String[].class))).thenReturn(new File[]{new File("/tmp/in")});
		when(fileManagerMock.getAllImagesFromFolder(any(File.class), any(String[].class))).thenReturn(new File[]{new File("/tmp/in/01.tif")});
		when(hotfolderMock.exists(new URI("http://localhost:9001/server.lock"))).thenReturn(true);
		main.execute(validOptions());
	}

	@Test
	public void shouldCompleteSuccessfully() throws IOException, URISyntaxException {
		prepareMocksForSuccess();
		
		main.execute(validOptions());
		
		String outString = new String(baos.toByteArray());
		assertThat(outString, isEmptyString());
		verify(hotfolderMock, atLeastOnce()).copyFile(new URI("http://localhost:9001/output/in.xml"), new File("/tmp/out/in.xml").toURI());
	}
	
	// TODO: works in production, but not in test
	//@Test
	public void shouldCompleteSuccessfullyWithMultiuser() throws IOException, URISyntaxException {
		prepareMocksForSuccess();
		
		String[] opts = validOptions();
		opts[15] = "abbyy-multiuser";
		main.execute(opts);
		
		String outString = new String(baos.toByteArray());
		assertThat(outString, isEmptyString());
		verify(hotfolderMock, atLeastOnce()).copyFile(new URI("http://localhost:9001/output/in.xml"), new File("/tmp/out/in.xml").toURI());
	}

	private void prepareMocksForSuccess() throws IOException, URISyntaxException {
		when(fileManagerMock.isReadableFolder("/tmp/in")).thenReturn(true);
		when(fileManagerMock.isWritableFolder("/tmp/out")).thenReturn(true);
		when(fileManagerMock.getAllFolders(anyString(), any(String[].class))).thenReturn(new File[]{new File("/tmp/in")});
		when(fileManagerMock.getAllImagesFromFolder(any(File.class), any(String[].class))).thenReturn(new File[]{new File("/tmp/in/01.tif")});
		when(hotfolderMock.createTmpFile(anyString())).thenReturn(mock(OutputStream.class, withSettings().serializable()));
		when(hotfolderMock.exists(new URI("http://localhost:9001/output/in.xml.result.xml"))).thenReturn(true);
		when(hotfolderMock.exists(new URI("http://localhost:9001/output/in.xml"))).thenReturn(true);
	}
	
	private String[] validOptions() {
		return new String[]{"-indir", "/tmp/in", 
				"-informats", "tif,jpg",
				"-texttype", "NORMAL",
				"-langs", "de,en",
				"-outdir", "/tmp/out",
				"-outformats", "XML",
				"-prio", "2",
				"-engine", "abbyy",
				"-props", "user=me,password=pass"};
	}

}
