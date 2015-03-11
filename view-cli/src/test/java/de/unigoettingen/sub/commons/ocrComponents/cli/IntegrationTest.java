package de.unigoettingen.sub.commons.ocrComponents.cli;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ConcurrentModificationException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.HotfolderMockProvider;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.ServerHotfolder;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocrComponents.cli.testutil.FileAccessMockProvider;

public class IntegrationTest {

	private ByteArrayOutputStream baos;
	private Main mainSut;
	private FileAccess fileAccessMock;
	private ServerHotfolder hotfolderMock;
	
	@Before
	public void beforeEachTest() {
		mainSut = new Main();
		baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		mainSut.redirectSystemOutputTo(out);

		fileAccessMock = mock(FileAccess.class);
		FileAccessMockProvider.mock = fileAccessMock;
		
		hotfolderMock = mock(ServerHotfolder.class, withSettings().serializable());
		HotfolderMockProvider.mock = hotfolderMock;
	}

	@Test
	public void shouldComplainAboutInput() throws UnsupportedEncodingException {
		when(fileAccessMock.isReadableFolder("/tmp/in")).thenReturn(false);
		when(fileAccessMock.isWritableFolder("/tmp/out")).thenReturn(true);
		mainSut.execute(validOptions());

		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Illegal options: Input folder not found or it is not readable"));
	}
	
	@Test
	public void shouldComplainAboutOutput() throws UnsupportedEncodingException {
		when(fileAccessMock.isReadableFolder("/tmp/in")).thenReturn(true);
		when(fileAccessMock.isWritableFolder("/tmp/out")).thenReturn(false);
		mainSut.execute(validOptions());

		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Illegal options: Output folder not found or it is not writable"));
	}
	
	@Test(expected=ConcurrentModificationException.class)
	public void shouldComplainAboutExistingLockFile() throws URISyntaxException, IOException {
		prepareFileAccessMockForSuccess();
		when(hotfolderMock.exists(new URI("http://localhost:9001/server.lock"))).thenReturn(true);
		mainSut.execute(validOptions());
	}

	@Test
	public void shouldDeleteLockFile() throws URISyntaxException, IOException {
		prepareFileAccessMockForSuccess();
		prepareHotfolderMockForSuccess();
		
		String[] opts = validOptions();
		opts[17] = "lock.overwrite=true";
		mainSut.execute(opts);
		
		verify(hotfolderMock, times(2)).deleteIfExists(new URI("http://localhost:9001/server.lock"));
	}

	@Test
	public void shouldPassUserCredentials() throws URISyntaxException, IOException {
		prepareFileAccessMockForSuccess();
		prepareHotfolderMockForSuccess();
		
		mainSut.execute(validOptions());
		
		verify(hotfolderMock, times(2)).configureConnection("http://localhost:9001/", "me", "pass");;
	}

	@Test
	public void shouldCompleteSuccessfully() throws IOException, URISyntaxException {
		prepareFileAccessMockForSuccess();
		prepareHotfolderMockForSuccess();
		
		mainSut.execute(validOptions());
		
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
		mainSut.execute(opts);
		
		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Finished OCR"));
		verify(hotfolderMock).download(new URI("http://localhost:9001/output/in.xml"), new File("/tmp/out/in.xml").toURI());
	}
	
	@Test
	public void shouldSplitAndMerge() throws IOException, URISyntaxException {
		prepareFileAccessMockForSuccess();
		prepareHotfolderMockForSuccess();
		
		File[] images = new File[]{new File("/tmp/in/01.tif"), new File("/tmp/in/02.tif")};
		when(fileAccessMock.getAllImagesFromFolder(any(File.class), any(String[].class))).thenReturn(images);
		when(hotfolderMock.exists(new URI("http://localhost:9001/output/in_1of2.xml.result.xml"))).thenReturn(true);
		when(hotfolderMock.exists(new URI("http://localhost:9001/output/in_1of2.xml"))).thenReturn(true);
		when(hotfolderMock.exists(new URI("http://localhost:9001/output/in_2of2.xml.result.xml"))).thenReturn(false, true);
		when(hotfolderMock.exists(new URI("http://localhost:9001/output/in_2of2.xml"))).thenReturn(false, true);
		when(fileAccessMock.inputStreamForFile(new File("/tmp/out/in_1of2.xml.result.xml"))).thenReturn(resultXml());
		when(fileAccessMock.inputStreamForFile(new File("/tmp/out/in_2of2.xml.result.xml"))).thenReturn(resultXml());
		when(fileAccessMock.inputStreamForFile(new File("/tmp/out/in_1of2.xml"))).thenReturn(abbyyXml());
		when(fileAccessMock.inputStreamForFile(new File("/tmp/out/in_2of2.xml"))).thenReturn(abbyyXml());
		when(fileAccessMock.outputStreamForFile(any(File.class))).thenReturn(new ByteArrayOutputStream());
		
		String[] opts = validOptions();
		opts[17] = "books.split=true";
		mainSut.execute(opts);
		
		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Finished OCR"));
		verify(hotfolderMock).exists(new URI("http://localhost:9001/output/in_1of2.xml"));
		// times(2) because of thenReturn(false, true) above
		verify(hotfolderMock, times(2)).exists(new URI("http://localhost:9001/output/in_2of2.xml"));
		verify(fileAccessMock).outputStreamForFile(new File("/tmp/out/in.xml"));
	}

	@Test
	public void shouldReportTimeout() throws IOException, URISyntaxException {
		prepareFileAccessMockForSuccess();
		prepareHotfolderMockForSuccess();		
		when(hotfolderMock.exists(new URI("http://localhost:9001/output/in.xml"))).thenReturn(false);
		
		mainSut.execute(validOptions());

		// TODO: try to propagate the TimeoutException from the thread, also in the other tests
		verify(hotfolderMock, never()).download(new URI("http://localhost:9001/output/in.xml"), new File("/tmp/out/in.xml").toURI());
	}
	
	private InputStream resultXml() {
		String xml = "<XmlResult/>";
		return new ByteArrayInputStream(xml.getBytes());
	}

	private InputStream abbyyXml() {
		String xml = "<document/>";
		return new ByteArrayInputStream(xml.getBytes());
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
		fileProps.setProperty("maxImagesInSubprocess", "1");
		fileProps.setProperty("maxParallelProcesses", "3");
		fileProps.setProperty("maxServerSpace", "1000000000");
		fileProps.setProperty("minMillisPerFile", "10");
		fileProps.setProperty("maxMillisPerFile", "100");
		fileProps.setProperty("checkInterval", "1");
		return fileProps;
	}

}
