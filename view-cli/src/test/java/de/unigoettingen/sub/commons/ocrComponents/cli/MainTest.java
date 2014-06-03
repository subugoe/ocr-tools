package de.unigoettingen.sub.commons.ocrComponents.cli;

/*

 © 2010, SUB Göttingen. All rights reserved.
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
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import de.unigoettingen.sub.commons.ocrComponents.cli.Main;
import de.unigoettingen.sub.ocr.controller.OcrEngineStarter;
import de.unigoettingen.sub.ocr.controller.OcrParameters;
import de.unigoettingen.sub.ocr.controller.Validator;

public class MainTest {

	private ByteArrayOutputStream baos;
	private Main main;
	private Validator validatorMock;
	private OcrEngineStarter engineStarterMock;
	
	@Before
	public void beforeEachTest() {
		main = new Main();
		baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		main.redirectSystemOutputTo(out);
		
		validatorMock = mock(Validator.class);
		main.setValidator(validatorMock);
		engineStarterMock = mock(OcrEngineStarter.class);
		main.setOcrEngineStarter(engineStarterMock);
	}
	
	@Test
	public void shouldPrintHelp() throws UnsupportedEncodingException {
		main.execute(new String[]{"-help"});
		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("usage: java -jar"));
		assertThat(outString, containsString("-help"));
	}

	@Test
	public void shouldDenyWrongArgument() throws UnsupportedEncodingException {
		main.execute(new String[]{"-wrongargument"});
		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Illegal arguments. Use -help."));
	}

	@Test
	public void shouldPassAllParams() throws UnsupportedEncodingException {
		ArgumentCaptor<OcrParameters> captor = ArgumentCaptor.forClass(OcrParameters.class);
		when(validatorMock.validateParameters(any(OcrParameters.class))).thenReturn("OK");
		main.execute(validOptions());
		verify(validatorMock).validateParameters(captor.capture());
		OcrParameters param = captor.getValue();
		assertEquals("/tmp/in", param.inputFolder);
		assertEquals("tif", param.inputFormats[0]);
		assertEquals("jpg", param.inputFormats[1]);
		assertEquals("normal", param.inputTextType);
		assertEquals("de", param.inputLanguages[0]);
		assertEquals("en", param.inputLanguages[1]);
		assertEquals("/tmp/out", param.outputFolder);
		assertEquals("pdf", param.outputFormats[0]);
		assertEquals("xml", param.outputFormats[1]);
		assertEquals("2", param.priority);
		assertEquals("abbyy", param.ocrEngine);
		assertEquals("me", param.options.get("user"));
		assertEquals("pass", param.options.get("password"));
	}

	@Test
	public void shouldComplainAboutMissingOptions() throws UnsupportedEncodingException {
		main.execute(new String[]{"-indir", "/tmp"});
		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Required options are missing. Use -help."));
	}
	
	@Test
	public void shouldStartEngine() throws UnsupportedEncodingException {
		when(validatorMock.validateParameters(any(OcrParameters.class))).thenReturn("OK");
		main.execute(validOptions());
		verify(engineStarterMock).startOcrWithParams(any(OcrParameters.class));
	}
	
	@Test
	public void shouldNotStartEngine() throws UnsupportedEncodingException {
		when(validatorMock.validateParameters(any(OcrParameters.class))).thenReturn("Input folder does not exist");
		main.execute(validOptions());
		verify(engineStarterMock, times(0)).startOcrWithParams(any(OcrParameters.class));
		
		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Illegal options: Input folder does not exist"));

	}
	
	@Test
	public void shouldSetDefaultValues() throws UnsupportedEncodingException {
		ArgumentCaptor<OcrParameters> captor = ArgumentCaptor.forClass(OcrParameters.class);
		main.execute(onlyRequiredOptions());
		verify(validatorMock).validateParameters(captor.capture());
		OcrParameters param = captor.getValue();
		
		assertEquals("tif", param.inputFormats[0]);
		assertEquals("jpg", param.inputFormats[1]);
		assertEquals("gif", param.inputFormats[2]);
		assertEquals("0", param.priority);
		assertEquals("abbyy", param.ocrEngine);
		assertNotNull(param.options);
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
				"-options", "user=me,password=pass"};
	}

	private String[] onlyRequiredOptions() {
		return new String[]{"-indir", "/tmp/in", 
				"-texttype", "normal",
				"-langs", "de,en",
				"-outdir", "/tmp/out",
				"-outformats", "pdf,xml"};
	}

}
