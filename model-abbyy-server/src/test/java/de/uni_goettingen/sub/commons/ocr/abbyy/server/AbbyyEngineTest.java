package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class AbbyyEngineTest {

	private AbbyyEngine engineSut;
	private OcrExecutor executorMock = mock(OcrExecutor.class);
	private LockFileHandler lockHandlerMock = mock(LockFileHandler.class);
	private ProcessSplitter splitterMock = mock(ProcessSplitter.class);
	
	@Before
	public void beforeEachTest() throws Exception {
		AbbyyEngine engineSutNoSpy = new AbbyyEngine();
		engineSut = spy(engineSutNoSpy);
		doReturn(executorMock).when(engineSut).createPool(anyInt());
		doReturn(lockHandlerMock).when(engineSut).createLockHandler();
		doReturn(splitterMock).when(engineSut).createProcessSplitter();
	}

	@Test
	public void shouldNotStartWithEmptyQueue() {
		engineSut.initialize(validProps());
		engineSut.recognize();
		
		verify(executorMock, never()).execute(any(Runnable.class));
	}

	@Test
	public void shouldNotStartNonStartableProcess() {
		engineSut.initialize(validProps());
		AbbyyProcess processMock = mock(AbbyyProcess.class);
		when(processMock.hasImagesAndOutputs()).thenReturn(false);
		engineSut.addOcrProcess(processMock);
		engineSut.recognize();
		
		verify(executorMock, never()).execute(any(Runnable.class));
	}

	@Test
	public void shouldExecuteOneProcess() {
		engineSut.initialize(validProps());
		AbbyyProcess processMock = validProcessMock();
		engineSut.addOcrProcess(processMock);
		engineSut.recognize();
		
		verify(lockHandlerMock).initConnection("http://test.com", "u", "p");
		verify(lockHandlerMock).createOrOverwriteLock(false);
		verify(engineSut).createPool(5);
		verify(executorMock).execute(processMock);
	}

	@Test
	public void shouldExecuteTwoProcesses() {
		engineSut.initialize(validProps());
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
		Properties props = validProps();
		props.setProperty("lock.overwrite", "true");
		engineSut.initialize(props);
		AbbyyProcess processMock = validProcessMock();
		engineSut.addOcrProcess(processMock);
		engineSut.recognize();

		verify(lockHandlerMock).createOrOverwriteLock(true);
	}

	@Test
	public void shouldSplitIntoThree() {
		Properties props = validProps();
		props.setProperty("books.split", "true");
		engineSut.initialize(props);
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
		engineSut.initialize(validProps());
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

	private Properties validProps() {
		Properties props = new Properties();
		// user properties
		props.setProperty("abbyy.config", "test.properties");
		// file properties
		props.setProperty("serverUrl", "http://test.com");
		props.setProperty("user", "u");
		props.setProperty("password", "p");
		props.setProperty("maxParallelProcesses", "5");
		props.setProperty("maxImagesInSubprocess", "2");
		props.setProperty("minMillisPerFile", "2000");
		return props;
	}

	private AbbyyProcess validProcessMock() {
		AbbyyProcess processMock = mock(AbbyyProcess.class);
		when(processMock.hasImagesAndOutputs()).thenReturn(true);
		return processMock;
	}
	
}
