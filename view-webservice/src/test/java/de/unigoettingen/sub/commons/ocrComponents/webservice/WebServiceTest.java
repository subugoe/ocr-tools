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


import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.xml.ws.Endpoint;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrTextType;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocr.util.OcrParameters;
import de.unigoettingen.sub.ocr.controller.OcrEngineStarter;


public class WebServiceTest {
	
	private FileAccess fileAccessMock;
	private OcrEngineStarter engineStarterMock;
	private OcrServiceImpl serviceSut;
	private OcrServiceImpl serviceSpySut;
	
	@Before
	public void beforeEachTest() {
		fileAccessMock = mock(FileAccess.class);
		Properties props = new Properties();
		props.setProperty("webserverpath", "/test/server");
		props.setProperty("localpath", "/test/local");
		props.setProperty("hostname", "http://localhost/");
		when(fileAccessMock.getPropertiesFromFile(anyString())).thenReturn(props);
		when(fileAccessMock.fileExists(any(File.class))).thenReturn(true);
		
		engineStarterMock = mock(OcrEngineStarter.class);
		
		serviceSut = new OcrServiceImpl();
		serviceSpySut = spy(serviceSut);
		doReturn(fileAccessMock).when(serviceSpySut).getFileAccess();
		doReturn(engineStarterMock).when(serviceSpySut).getEngineStarter();
		doReturn("testJob").when(serviceSpySut).getJobName();
	}

	@Test
	public void canBePublished() {
		OcrServiceImpl service = new OcrServiceImpl();
		Endpoint endpoint = Endpoint.publish("http://localhost:9001/test", service);
		endpoint.stop();
	}

	@Test
	public void shouldFinishSuccessfully() throws IOException {
		ByUrlRequestType request = getValidRequest();
		
		ByUrlResponseType response = serviceSpySut.ocrImageFileByUrl(request);
		
		verify(engineStarterMock).startOcrWithParams(any(OcrParameters.class));
		assertThat(response.getProcessingLog(), containsString("Process finished successfully"));
	}

	private ByUrlRequestType getValidRequest() {
		ByUrlRequestType request = new ByUrlRequestType();
		request.setInputUrl("http://localhost/test.tif");
		request.setOutputFormat(OcrFormat.TXT);
		RecognitionLanguages langs = new RecognitionLanguages();
		langs.getRecognitionLanguage().add(RecognitionLanguage.de);
		request.setOcrlanguages(langs);
		request.setTextType(OcrTextType.GOTHIC);
		return request;
	}
	
}