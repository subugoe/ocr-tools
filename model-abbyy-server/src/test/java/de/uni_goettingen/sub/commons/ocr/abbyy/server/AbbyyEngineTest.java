package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;

public class AbbyyEngineTest {

	private AbbyyEngine engineSut;
	private BeanProvider beanProviderMock = mock(BeanProvider.class);
	private FileAccess fileAccessMock = mock(FileAccess.class);
	
	@Before
	public void beforeEachTest() throws Exception {
		engineSut = new AbbyyEngine();
		when(beanProviderMock.getFileAccess()).thenReturn(fileAccessMock);
		when(fileAccessMock.getPropertiesFromFile(anyString())).thenReturn(validFileProps());
		engineSut.setBeanProvider(beanProviderMock);
	}

	@Test
	public void test() throws IOException {
		//engineSut.initialize(validUserProps());
	}

	private Properties validUserProps() {
		Properties userProps = new Properties();
		userProps.setProperty("abbyy.config", "test.properties");
		return userProps;
	}

	private Properties validFileProps() {
		Properties fileProps = new Properties();
		return fileProps;
	}


}
