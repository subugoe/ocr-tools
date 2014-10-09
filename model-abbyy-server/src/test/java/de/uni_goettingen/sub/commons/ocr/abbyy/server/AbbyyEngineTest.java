package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;

public class AbbyyEngineTest {

	private AbbyyEngine engineSut;
	private BeanProvider beanProviderMock = mock(BeanProvider.class);
	private FileAccess fileAccessMock = mock(FileAccess.class);
	private OcrExecutor executorMock = mock(OcrExecutor.class);
	private LockFileHandler lockHandlerMock = mock(LockFileHandler.class);
	
	@Before
	public void beforeEachTest() throws Exception {
		AbbyyEngine engineSutNoSpy = new AbbyyEngine();
		engineSut = spy(engineSutNoSpy);
		doReturn(executorMock).when(engineSut).createPool(anyInt());
		doReturn(lockHandlerMock).when(engineSut).createLockHandler();
		when(beanProviderMock.getFileAccess()).thenReturn(fileAccessMock);
		when(fileAccessMock.getPropertiesFromFile(anyString())).thenReturn(validFileProps());
		engineSut.setBeanProvider(beanProviderMock);
	}

	@Test
	public void shouldReadFileProperties() {
		engineSut.initialize(validUserProps());
		
		verify(fileAccessMock).getPropertiesFromFile("test.properties");
	}

	@Test
	public void shouldReadPropertiesFromDefaultFile() {
		Properties userProps = validUserProps();
		userProps.remove("abbyy.config");
		engineSut.initialize(userProps);
		
		verify(fileAccessMock).getPropertiesFromFile("gbv-antiqua.properties");
	}
	
	@Test
	public void shouldNotStartWithEmptyQueue() {
		engineSut.initialize(validUserProps());
		engineSut.recognize();
		
		verify(executorMock, never()).execute(any(Runnable.class));
	}

	@Test
	public void shouldNotStartNonStartableProcess() {
		engineSut.initialize(validUserProps());
		AbbyyProcess processMock = mock(AbbyyProcess.class);
		when(processMock.canBeStarted()).thenReturn(false);
		engineSut.addOcrProcess(processMock);
		engineSut.recognize();
		
		verify(executorMock, never()).execute(any(Runnable.class));
	}

	//@Test
	public void should() {
		engineSut.initialize(validUserProps());
		AbbyyProcess processMock = mock(AbbyyProcess.class);
		when(processMock.canBeStarted()).thenReturn(true);
		engineSut.addOcrProcess(processMock);
		engineSut.recognize();
		
		verify(executorMock, never()).execute(any(Runnable.class));
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
