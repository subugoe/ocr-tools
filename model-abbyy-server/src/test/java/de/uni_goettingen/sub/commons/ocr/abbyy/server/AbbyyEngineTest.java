package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
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
	private ProcessSplitter splitterMock = mock(ProcessSplitter.class);
	
	@Before
	public void beforeEachTest() throws Exception {
		AbbyyEngine engineSutNoSpy = new AbbyyEngine();
		engineSut = spy(engineSutNoSpy);
		doReturn(executorMock).when(engineSut).createPool(anyInt());
		doReturn(lockHandlerMock).when(engineSut).createLockHandler();
		when(beanProviderMock.getFileAccess()).thenReturn(fileAccessMock);
		when(fileAccessMock.getPropertiesFromFile(anyString())).thenReturn(validFileProps());
		engineSut.setBeanProvider(beanProviderMock);
		engineSut.setProcessSplitter(splitterMock);
		
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

	@Test
	public void shouldExecuteOneProcess() {
		engineSut.initialize(validUserProps());
		AbbyyProcess processMock = validProcessMock();
		engineSut.addOcrProcess(processMock);
		engineSut.recognize();
		
		verify(lockHandlerMock).setConnectionData("http://test.com", "u", "p");
		verify(lockHandlerMock).createOrOverwriteLock(false);
		verify(engineSut).createPool(5);
		verify(executorMock).execute(processMock);
	}

	@Test
	public void shouldExecuteTwoProcesses() {
		engineSut.initialize(validUserProps());
		AbbyyProcess processMock1 = validProcessMock();
		engineSut.addOcrProcess(processMock1);
		AbbyyProcess processMock2 = validProcessMock();
		engineSut.addOcrProcess(processMock2);
		engineSut.recognize();
		
		verify(executorMock).execute(processMock1);
		verify(executorMock).execute(processMock2);
	}

	@Test
	public void shouldOverwriteLockFile() {
		Properties userProps = validUserProps();
		userProps.setProperty("lock.overwrite", "true");
		engineSut.initialize(userProps);
		AbbyyProcess processMock = validProcessMock();
		engineSut.addOcrProcess(processMock);
		engineSut.recognize();

		verify(lockHandlerMock).createOrOverwriteLock(true);
	}

	@Test
	public void shouldSplitIntoThree() {
		Properties userProps = validUserProps();
		userProps.setProperty("books.split", "true");
		engineSut.initialize(userProps);
		AbbyyProcess processMock = validProcessMock();
		engineSut.addOcrProcess(processMock);
		when(splitterMock.split(processMock, 2)).thenReturn(threeSubProcesses());
		engineSut.recognize();
		
		verify(executorMock, times(3)).execute(any(AbbyyProcess.class));
	}
	
	@Test
	public void shouldBeZeroSecondsWhenUnititialized() {
		assertEquals("Estimated time", 0, engineSut.getEstimatedDurationInSeconds());
	}
	
	@Test
	public void shouldEstimateBasedOnMillisPerFile() {
		engineSut.initialize(validUserProps());
		AbbyyProcess processMock = validProcessMock();
		when(processMock.getNumberOfImages()).thenReturn(4);
		engineSut.addOcrProcess(processMock);
		
		assertEquals("Estimated time", 8, engineSut.getEstimatedDurationInSeconds());
	}
	
	private List<AbbyyProcess> threeSubProcesses() {
		List<AbbyyProcess> subProcesses = new ArrayList<AbbyyProcess>();
		subProcesses.add(mock(AbbyyProcess.class));
		subProcesses.add(mock(AbbyyProcess.class));
		subProcesses.add(mock(AbbyyProcess.class));
		return subProcesses;
	}

	private Properties validUserProps() {
		Properties userProps = new Properties();
		userProps.setProperty("abbyy.config", "test.properties");
		return userProps;
	}

	private Properties validFileProps() {
		Properties fileProps = new Properties();
		fileProps.setProperty("serverUrl", "http://test.com");
		fileProps.setProperty("user", "u");
		fileProps.setProperty("password", "p");
		fileProps.setProperty("maxParallelProcesses", "5");
		fileProps.setProperty("maxImagesInSubprocess", "2");
		fileProps.setProperty("minMillisPerFile", "2000");
		return fileProps;
	}

	private AbbyyProcess validProcessMock() {
		AbbyyProcess processMock = mock(AbbyyProcess.class);
		when(processMock.canBeStarted()).thenReturn(true);
		return processMock;
	}
	
}
