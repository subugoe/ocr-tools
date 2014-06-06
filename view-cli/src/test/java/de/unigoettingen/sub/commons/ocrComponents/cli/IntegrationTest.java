package de.unigoettingen.sub.commons.ocrComponents.cli;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;

import de.unigoettingen.sub.commons.ocrComponents.cli.testutil.FileManagerMockProvider;
import de.unigoettingen.sub.ocr.controller.FileManager;

public class IntegrationTest {

	private ByteArrayOutputStream baos;
	private Main main;
	private FileManager fileManagerMock;
	
	@Before
	public void beforeEachTest() {
		main = new Main();
		baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		main.redirectSystemOutputTo(out);

		fileManagerMock = mock(FileManager.class);
		FileManagerMockProvider.mock = fileManagerMock;
	}

	@Test
	public void shouldComplainAboutInput() throws UnsupportedEncodingException {
		when(fileManagerMock.isReadableFolder("/tmp/in")).thenReturn(false);
		when(fileManagerMock.isWritableFolder("/tmp/out")).thenReturn(true);
		main.execute(validOptions());

		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Illegal options: Input folder not found."));
	}

	private String[] validOptions() {
		return new String[]{"-indir", "/tmp/in", 
				"-informats", "tif,jpg",
				"-texttype", "normal",
				"-langs", "de,en",
				"-outdir", "/tmp/out",
				"-outformats", "pdf,xml",
				"-prio", "2",
				"-engine", "abbyy",
				"-props", "user=me,password=pass"};
	}

}
