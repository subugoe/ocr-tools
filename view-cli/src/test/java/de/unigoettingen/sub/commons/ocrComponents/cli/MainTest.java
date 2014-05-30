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
import static org.hamcrest.Matchers.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;

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
	
	//@Test
	public void shouldPrintHelp() throws UnsupportedEncodingException {
		//when(validatorMock.validateParameters(any(OcrParameters.class))).thenReturn("OK");
		main.execute(new String[]{"-help"});
		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("usage: java -jar"));
		assertThat(outString, containsString("-help"));
	}

	//@Test
	public void shouldDenyWrongArgument() throws UnsupportedEncodingException {
		//when(validatorMock.validateParameters(any(OcrParameters.class))).thenReturn("OK");
		main.execute(new String[]{"-wrongargument"});
		String outString = new String(baos.toByteArray());
		assertThat(outString, containsString("Illegal arguments."));
		assertThat(outString, containsString("usage: java -jar"));
	}

}
