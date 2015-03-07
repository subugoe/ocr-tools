package de.unigoettingen.sub.commons.ocrComponents.cli;

import static org.hamcrest.Matchers.containsString;
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
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.ServerHotfolder;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocrComponents.cli.testutil.FileAccessMockProvider;
import de.unigoettingen.sub.commons.ocrComponents.cli.testutil.HotfolderMockProvider;

public class IntegrationTest {

	private ByteArrayOutputStream baos;
	private Main main;
	private FileAccess fileAccessMock;
	private ServerHotfolder hotfolderMock;
	
	@Before
	public void beforeEachTest() {
		main = new Main();
		baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		main.redirectSystemOutputTo(out);

		fileAccessMock = mock(FileAccess.class);
		FileAccessMockProvider.mock = fileAccessMock;
		
		hotfolderMock = mock(ServerHotfolder.class, withSettings().serializable());
		HotfolderMockProvider.mock = hotfolderMock;
	}

	@Test
	public void shouldComplainAboutInput() throws UnsupportedEncodingException {
		when(fileAccessMock.isReadableFolder("/tmp/in")).thenReturn(false);
		when(fileAccessMock.isWritableFolder("/tmp/out")).thenReturn(true);
		main.execute(validOptions());

		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Illegal options: Input folder not found or it is not readable"));
	}
	
	@Test
	public void shouldComplainAboutOutput() throws UnsupportedEncodingException {
		when(fileAccessMock.isReadableFolder("/tmp/in")).thenReturn(true);
		when(fileAccessMock.isWritableFolder("/tmp/out")).thenReturn(false);
		main.execute(validOptions());

		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Illegal options: Output folder not found or it is not writable"));
	}
	
	@Test(expected=ConcurrentModificationException.class)
	public void shouldComplainAboutExistingLockFile() throws URISyntaxException, IOException {
		prepareFileAccessMockForSuccess();
		when(hotfolderMock.exists(new URI("http://localhost:9001/server.lock"))).thenReturn(true);
		main.execute(validOptions());
	}

	@Test
	public void shouldDeleteLockFile() throws URISyntaxException, IOException {
		prepareFileAccessMockForSuccess();
		prepareHotfolderMockForSuccess();
		
		String[] opts = validOptions();
		opts[17] = "lock.overwrite=true";
		main.execute(opts);
		
		verify(hotfolderMock, times(2)).deleteIfExists(new URI("http://localhost:9001/server.lock"));
	}

	@Test
	public void shouldPassUserCredentials() throws URISyntaxException, IOException {
		prepareFileAccessMockForSuccess();
		prepareHotfolderMockForSuccess();
		
		main.execute(validOptions());
		
		verify(hotfolderMock, times(2)).configureConnection("http://localhost:9001/", "me", "pass");;
	}

	@Test
	public void shouldCompleteSuccessfully() throws IOException, URISyntaxException {
		prepareFileAccessMockForSuccess();
		prepareHotfolderMockForSuccess();
		
		main.execute(validOptions());
		
		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Finished OCR."));
		verify(hotfolderMock).download(new URI("http://localhost:9001/output/in.xml"), new File("/tmp/out/in.xml").toURI());
	}
	
	@Test
	public void shouldCompleteSuccessfullyWithMultiuser() throws IOException, URISyntaxException {
		prepareFileAccessMockForSuccess();
		prepareHotfolderMockForSuccess();
		
		String[] opts = validOptions();
		opts[15] = "abbyy-multiuser";
		main.execute(opts);
		
		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Finished OCR"));
		verify(hotfolderMock).download(new URI("http://localhost:9001/output/in.xml"), new File("/tmp/out/in.xml").toURI());
	}

	private void prepareFileAccessMockForSuccess() {
		when(fileAccessMock.isReadableFolder("/tmp/in")).thenReturn(true);
		when(fileAccessMock.isWritableFolder("/tmp/out")).thenReturn(true);
		when(fileAccessMock.getAllFolders(anyString(), any(String[].class))).thenReturn(new File[]{new File("/tmp/in")});
		when(fileAccessMock.getAllImagesFromFolder(any(File.class), any(String[].class))).thenReturn(new File[]{new File("/tmp/in/01.tif")});
		when(fileAccessMock.getPropertiesFromFile(anyString())).thenReturn(validFileProps());
	}

	private void prepareHotfolderMockForSuccess() throws IOException, URISyntaxException {
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
	
	private Properties validFileProps() {
		Properties fileProps = new Properties();
		fileProps.setProperty("serverUrl", "http://localhost:9001/");
		fileProps.setProperty("outputFolder", "output");
		fileProps.setProperty("resultXmlFolder", "output");
		fileProps.setProperty("maxParallelProcesses", "5");
		fileProps.setProperty("maxServerSpace", "1000000000");
		fileProps.setProperty("minMillisPerFile", "10");
		fileProps.setProperty("maxMillisPerFile", "1000");
		fileProps.setProperty("checkInterval", "1");
		return fileProps;
	}

}
