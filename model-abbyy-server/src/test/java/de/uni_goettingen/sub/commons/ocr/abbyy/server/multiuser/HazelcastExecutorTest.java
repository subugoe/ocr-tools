package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrPriority;

public class HazelcastExecutorTest {
	
	private HazelcastExecutor executorSut;
	private AbbyyProcess processMock1 = mock(AbbyyProcess.class);
	private AbbyyProcess processMock2 = mock(AbbyyProcess.class);
	private HazelcastInstance hazelMock = mock(HazelcastInstance.class);
	private Lock lock = new ReentrantLock();
	private Condition conditionForExecution = lock.newCondition();
	
	private final static boolean ENOUGH_SPACE = true;
	private final static boolean NOT_ENOUGH_SPACE = false;
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
		when(hazelMock.getLock(anyString())).thenReturn(mock(ILock.class));
		executorSut = new HazelcastExecutor(2, hazelMock);
		executorSut.setLock(lock);
		executorSut.setCondition(conditionForExecution);
		executorSut.setQueuedProcesses(new HashMap<String, AbbyyProcess>());
		executorSut.setRunningProcesses(new HashSet<String>());
				
		
	}

	@Test
	public void shouldExecuteOne() throws InterruptedException {
		configureProcessMock(processMock1, "book1", OcrPriority.NORMAL, 1L, ENOUGH_SPACE);

		executorSut.execute(processMock1);
		shutdownExecutor();
		
		verify(processMock1).run();
	}

	@Test
	public void shouldExecuteTwo() throws InterruptedException {
//		doAnswer(withShortPause).when(processMock1).run();
//		doAnswer(withShortPause).when(processMock2).run();
		configureProcessMock(processMock1, "book1", OcrPriority.NORMAL, 1L, ENOUGH_SPACE);
		configureProcessMock(processMock2, "book2", OcrPriority.NORMAL, 2L, ENOUGH_SPACE);
		
		executorSut.execute(processMock1);
		executorSut.execute(processMock2);
		shutdownExecutor();
		
		verify(processMock1).run();
		verify(processMock2).run();
	}
	
	@Test
	public void shouldWakeUpByItself() throws InterruptedException {
		configureProcessMock(processMock1, "book1", OcrPriority.NORMAL, 1L, NOT_ENOUGH_SPACE, ENOUGH_SPACE);
		
		executorSut.setWaitingTime(10);
		executorSut.execute(processMock1);
		shutdownExecutor();
		
		verify(processMock1).run();
	}
	
	@Test
	public void shouldNeverWakeUp() throws InterruptedException {
		configureProcessMock(processMock1, "book1", OcrPriority.NORMAL, 1L, NOT_ENOUGH_SPACE);
		
		executorSut.setWaitingTime(10);
		executorSut.execute(processMock1);
		shutdownExecutor();
		
		verify(processMock1, never()).run();
	}
	
	@Test
	public void secondShouldFreeFirst() throws InterruptedException {
		configureProcessMock(processMock1, "book1", OcrPriority.NORMAL, 1L, NOT_ENOUGH_SPACE, ENOUGH_SPACE);
		configureProcessMock(processMock2, "book2", OcrPriority.NORMAL, 2L, ENOUGH_SPACE);
		
		executorSut.setWaitingTime(VERY_LONG);
		executorSut.execute(processMock1);
		Thread.sleep(10);
		executorSut.execute(processMock2);
		shutdownExecutor();
		
		verify(processMock1).run();
		verify(processMock2).run();
	}
	

	private void configureProcessMock(AbbyyProcess processMock, String processId, 
			OcrPriority prio, long startedAtMillis, boolean... hasEnoughSpace) {
		when(processMock.getProcessId()).thenReturn(processId);
		when(processMock.getPriority()).thenReturn(prio);
		when(processMock.getStartedAt()).thenReturn(startedAtMillis);
		if (hasEnoughSpace.length == 1) {
			when(processMock.hasEnoughSpaceForExecution()).thenReturn(hasEnoughSpace[0]);
		} else {
			when(processMock.hasEnoughSpaceForExecution()).thenReturn(hasEnoughSpace[0], hasEnoughSpace[1]);
		}
	}
	
	private void shutdownExecutor() throws InterruptedException {
		executorSut.shutdown();
		executorSut.awaitTermination(50, TimeUnit.MILLISECONDS);
	}

}
