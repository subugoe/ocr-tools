package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class OcrExecutorTest {

	private OcrExecutor executorSut;
	private AbbyyProcess processMock1 = mock(AbbyyProcess.class);
	private AbbyyProcess processMock2 = mock(AbbyyProcess.class);
	private final static long VERY_LONG = 100000000;
	
	private Answer<Void> withShortPause = new Answer<Void>() {
		@Override
		public Void answer(InvocationOnMock invocation) throws Throwable {
			Thread.sleep(10);
			return null;
		}
	};
	
	@Before
	public void beforeEachTest() throws Exception {
		executorSut = new OcrExecutor(2);
	}
	
	@Test
	public void shouldExecuteOneProcess() throws IOException, InterruptedException {
		when(processMock1.hasEnoughSpaceForExecution()).thenReturn(true);
		executorSut.execute(processMock1);
		shutdownExecutor();
		
		verify(processMock1).run();
	}

	@Test
	public void shouldExecuteTwoProcesses() throws IOException, InterruptedException {
		when(processMock1.hasEnoughSpaceForExecution()).thenReturn(true);
		when(processMock2.hasEnoughSpaceForExecution()).thenReturn(true);
		executorSut.execute(processMock1);
		executorSut.execute(processMock2);
		shutdownExecutor();
		
		verify(processMock1).run();
		verify(processMock2).run();
	}
	
	@Test
	public void shouldWakeUpAfterClearingSpace() throws IOException, InterruptedException {
		when(processMock1.hasEnoughSpaceForExecution()).thenReturn(false, true);
		executorSut.setWaitingTime(10);
		executorSut.execute(processMock1);
		shutdownExecutor();
		
		verify(processMock1).run();		
	}

	@Test
	public void shouldWaitTooLong() throws IOException, InterruptedException {
		when(processMock1.hasEnoughSpaceForExecution()).thenReturn(false);
		executorSut.setWaitingTime(50);
		executorSut.execute(processMock1);
		shutdownExecutor();
		
		verify(processMock1, never()).run();		
	}

	@Test
	public void secondProcessShouldWakeTheFirst() throws IOException, InterruptedException {
		executorSut.setWaitingTime(VERY_LONG);
		when(processMock1.hasEnoughSpaceForExecution()).thenReturn(false, true);
		when(processMock2.hasEnoughSpaceForExecution()).thenReturn(true);
		executorSut.execute(processMock1);
		Thread.sleep(10);
		executorSut.execute(processMock2);
		shutdownExecutor();
		
		verify(processMock1).run();
		verify(processMock2).run();
	}
	
	@Test
	public void firstProcessShouldWakeTheSecond() throws IOException, InterruptedException {
		executorSut.setWaitingTime(VERY_LONG);
		when(processMock1.hasEnoughSpaceForExecution()).thenReturn(true);
		when(processMock2.hasEnoughSpaceForExecution()).thenReturn(false, true);
		
		doAnswer(withShortPause).when(processMock1).run();
		executorSut.execute(processMock1);
		Thread.sleep(5);
		executorSut.execute(processMock2);
		shutdownExecutor();
		
		verify(processMock1).run();
		verify(processMock2).run();
	}
	
	private void shutdownExecutor() throws InterruptedException {
		executorSut.shutdown();
		executorSut.awaitTermination(100, TimeUnit.MILLISECONDS);
	}

}
