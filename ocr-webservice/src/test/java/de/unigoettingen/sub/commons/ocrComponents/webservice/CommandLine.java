package de.unigoettingen.sub.commons.ocrComponents.webservice;

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

import java.io.IOException;
import java.net.URISyntaxException;



import org.junit.Ignore;
import org.junit.Test;

import de.unigoettingen.sub.commons.ocrComponents.cli.OCRCli;

public class CommandLine   
{
	@Ignore
	@Test 
	public synchronized void one() throws InterruptedException, IOException, URISyntaxException {
		Thread.sleep(3000);
		OCRCli.main(new String[] {"-lde", "-fTXT", "-tNORMAL", "-otarget/result", "/src/test/resources/book"});		
	}
}