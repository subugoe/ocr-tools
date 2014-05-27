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

import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.unigoettingen.sub.commons.ocrComponents.cli.Main;

public class OcrCliTestOld {
//	static List<String> files;
	Main ocrCli;

	//@Before
	public void init() throws URISyntaxException {
		ocrCli = new Main();

		String[] args = new String[5];
		args[0] = "-l de,en";
		args[1] = "-o /src/test/java/output";
		args[2] = "-fTXT,PDF";
		args[3] = "-tNORMAL";
		args[4] = "/src/test/java/books";
		ocrCli.execute(args);
//		files = ocrCli.configureFromArgs(args);

	}

	//@Test
	public void testFormatParser() {
		List<OCRFormat> formats = new Main().parseOCRFormat("PDF,HTML");
		assertTrue(formats.contains(OCRFormat.HTML));
		assertTrue(formats.contains(OCRFormat.PDF));

	}

	//@Test
	public void testFormat() {
		assertTrue(ocrCli.f.contains(OCRFormat.TXT));
		assertTrue(ocrCli.f.contains(OCRFormat.PDF));
	}

	//@Test
	public void testLanguage() {
		assertTrue(ocrCli.langs.toString().contains("en"));
		assertTrue(ocrCli.langs.toString().contains("de"));

	}

//	@Test
//	public void testintput() {
//		assertTrue(files.toString().equals("[/src/test/java/books]"));
//	}

	//@Test
	public void testoutput() {
		assertTrue(ocrCli.localOutputDir.equals(" /src/test/java/output"));
	}

}
