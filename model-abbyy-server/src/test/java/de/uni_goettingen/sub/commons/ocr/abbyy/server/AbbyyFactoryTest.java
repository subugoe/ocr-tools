package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;

public class AbbyyFactoryTest {

	private AbbyyFactory factorySut;
	private BeanProvider providerMock = mock(BeanProvider.class);
	private FileAccess fileAccessMock = mock(FileAccess.class);
	
	@Before
	public void beforeEachTest() throws Exception {
		factorySut = new AbbyyFactory(userProps());
		when(providerMock.getFileAccess()).thenReturn(fileAccessMock);
		when(fileAccessMock.getPropertiesFromFile(anyString())).thenReturn(fileProps());
		factorySut.setBeanProvider(providerMock);
	}

	@Test
	public void shouldCombineProperties() {
		Properties combinedProps = factorySut.getCombinedProps();
		assertEquals("userValue", combinedProps.getProperty("userKey"));
		assertEquals("fileValue", combinedProps.getProperty("fileKey"));
	}
	
	@Test
	public void shouldUseDefaultConfigFile() {
		factorySut.getCombinedProps();
		verify(fileAccessMock).getPropertiesFromFile("gbv-antiqua.properties");
	}

	@Test
	public void shouldUseGivenConfigFile() {
		factorySut = new AbbyyFactory(userPropsWithConfigFile());
		factorySut.setBeanProvider(providerMock);
		factorySut.getCombinedProps();
		verify(fileAccessMock).getPropertiesFromFile("other-file.properties");
	}

	private Properties userProps() {
		Properties userProps = new Properties();
		userProps.setProperty("userKey", "userValue");
		return userProps;
	}

	private Properties fileProps() {
		Properties userProps = new Properties();
		userProps.setProperty("fileKey", "fileValue");
		return userProps;
	}

	private Properties userPropsWithConfigFile() {
		Properties userProps = new Properties();
		userProps.setProperty("abbyy.config", "other-file.properties");
		return userProps;
	}

}
