package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.HotfolderProvider;
import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;

public class AbbyyEngineTest {

	private AbbyyEngine engineSut;
	private BeanProvider beanProviderMock = mock(BeanProvider.class);
	private FileAccess fileAccessMock = mock(FileAccess.class);
	private HotfolderProvider hotfolderProviderMock = mock(HotfolderProvider.class);
	private Hotfolder hotfolderMock = mock(Hotfolder.class);
	
	@Before
	public void beforeEachTest() throws Exception {
		engineSut = new AbbyyEngine();
		when(beanProviderMock.getFileAccess()).thenReturn(fileAccessMock);
		when(fileAccessMock.getPropertiesFromFile(anyString())).thenReturn(validFileProps());
		engineSut.setBeanProvider(beanProviderMock);
		when(hotfolderProviderMock.createHotfolder(anyString(), anyString(), anyString())).thenReturn(hotfolderMock);
		engineSut.setHotfolderProvider(hotfolderProviderMock);
	}

	@Test
	public void test() throws IOException {
		engineSut.initialize(validUserProps());
	}

	private Properties validUserProps() {
		Properties userProps = new Properties();
		userProps.setProperty("abbyy.config", "test.properties");
		userProps.setProperty("bla", "blub_important");
		return userProps;
	}

	private Properties validFileProps() {
		Properties fileProps = new Properties();
		fileProps.setProperty("bla", "blub");
		return fileProps;
	}


}
