package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
	
	private HazelcastExecutor executorSutA;
	private AbbyyProcess processMockA1 = mock(AbbyyProcess.class);
	private AbbyyProcess processMockA2 = mock(AbbyyProcess.class);
	private HazelcastExecutor executorSutB;
	private AbbyyProcess processMockB1 = mock(AbbyyProcess.class);
	private AbbyyProcess processMockB2 = mock(AbbyyProcess.class);
	private HazelcastExecutor executorSutC;
	private AbbyyProcess processMockC1 = mock(AbbyyProcess.class);
	private HazelcastInstance hazelMock = mock(HazelcastInstance.class);
	private Lock lock = new ReentrantLock();
	private Condition conditionForExecution = lock.newCondition();
	private Map<String, AbbyyProcess> queuedProcesses = new HashMap<String, AbbyyProcess>();
	private Set<String> runningProcesses = new HashSet<String>();
	
	private final static boolean ENOUGH_SPACE = true;
	private final static boolean NOT_ENOUGH_SPACE = false;
	private final static long VERY_LONG = 100000000;

	private Answer<Void> withShortPause = new Answer<Void>() {
		@Override
		public Void answer(InvocationOnMock invocation) throws Throwable {
			Thread.sleep(50);
			return null;
		}
	};

	@Before
	public void beforeEachTest() throws Exception {
		when(hazelMock.getLock(anyString())).thenReturn(mock(ILock.class));
		
		executorSutA = initSut(2);
		executorSutB = initSut(2);
		executorSutC = initSut(2);
	}

	private HazelcastExecutor initSut(int poolSize) {
		HazelcastExecutor executor = new HazelcastExecutor(poolSize, hazelMock);
		executor.setLock(lock);
		executor.setCondition(conditionForExecution);
		executor.setQueuedProcesses(queuedProcesses);
		executor.setRunningProcesses(runningProcesses);
		return executor;
	}
	
	@Test
	public void shouldExecuteOne() throws InterruptedException {
		configureProcessMock(processMockA1, "book1", OcrPriority.NORMAL, 1L, ENOUGH_SPACE);

		executorSutA.execute(processMockA1);
		shutdownExecutors();
		
		verify(processMockA1).run();
	}

	@Test
	public void shouldExecuteTwo() throws InterruptedException {
		configureProcessMock(processMockA1, "book1", OcrPriority.NORMAL, 1L, ENOUGH_SPACE);
		configureProcessMock(processMockA2, "book2", OcrPriority.NORMAL, 2L, ENOUGH_SPACE);
		
		executorSutA.execute(processMockA1);
		executorSutA.execute(processMockA2);
		shutdownExecutors();
		
		verify(processMockA1).run();
		verify(processMockA2).run();
	}
	
	@Test
	public void shouldWakeUpByItself() throws InterruptedException {
		configureProcessMock(processMockA1, "book1", OcrPriority.NORMAL, 1L, NOT_ENOUGH_SPACE, ENOUGH_SPACE);
		
		executorSutA.setWaitingTime(10);
		executorSutA.execute(processMockA1);
		shutdownExecutors();
		
		verify(processMockA1).run();
	}
	
	@Test
	public void shouldNeverWakeUp() throws InterruptedException {
		configureProcessMock(processMockA1, "book1", OcrPriority.NORMAL, 1L, NOT_ENOUGH_SPACE);
		
		executorSutA.setWaitingTime(10);
		executorSutA.execute(processMockA1);
		shutdownExecutors();
		
		verify(processMockA1, never()).run();
	}
	
	@Test
	public void secondShouldFreeFirst() throws InterruptedException {
		configureProcessMock(processMockA1, "book1", OcrPriority.NORMAL, 1L, NOT_ENOUGH_SPACE, ENOUGH_SPACE);
		configureProcessMock(processMockA2, "book2", OcrPriority.ABOVENORMAL, 2L, ENOUGH_SPACE);
		
		executorSutA.setWaitingTime(VERY_LONG);
		executorSutA.execute(processMockA1);
		Thread.sleep(10);
		executorSutA.execute(processMockA2);
		shutdownExecutors();
		
		verify(processMockA1).run();
		verify(processMockA2).run();
	}
	
	@Test
	public void shouldRunOneInEachExecutor() throws InterruptedException {
		configureProcessMock(processMockA1, "bookA", OcrPriority.NORMAL, 1L, ENOUGH_SPACE);
		configureProcessMock(processMockB1, "bookB", OcrPriority.NORMAL, 2L, ENOUGH_SPACE);
		
		executorSutA.execute(processMockA1);
		executorSutB.execute(processMockB1);
		shutdownExecutors();
		
		verify(processMockA1).run();
		verify(processMockB1).run();
	}
	
	@Test
	public void shouldRunTwoInEachExecutor() throws InterruptedException {
		configureProcessMock(processMockA1, "bookA1", OcrPriority.NORMAL, 1L, ENOUGH_SPACE);
		configureProcessMock(processMockA2, "bookA2", OcrPriority.NORMAL, 2L, ENOUGH_SPACE);
		configureProcessMock(processMockB1, "bookB1", OcrPriority.NORMAL, 3L, ENOUGH_SPACE);
		configureProcessMock(processMockB2, "bookB2", OcrPriority.NORMAL, 4L, ENOUGH_SPACE);
		
		executorSutA.execute(processMockA1);
		executorSutA.execute(processMockA2);
		executorSutB.execute(processMockB1);
		executorSutB.execute(processMockB2);
		shutdownExecutors();
		
		verify(processMockA1).run();
		verify(processMockA2).run();
		verify(processMockB1).run();
		verify(processMockB2).run();
	}
	
	@Test
	public void shouldPreferHigherPriority() throws InterruptedException {
		executorSutA = initSut(1);
		executorSutB = initSut(1);
		executorSutC = initSut(1);
		
		configureProcessMock(processMockA1, "bookA1", OcrPriority.NORMAL, 1L, ENOUGH_SPACE);
		doAnswer(withShortPause).when(processMockA1).run();
		configureProcessMock(processMockB1, "bookB1", OcrPriority.NORMAL, 4L, ENOUGH_SPACE);
		configureProcessMock(processMockC1, "bookC1", OcrPriority.ABOVENORMAL, 5L, ENOUGH_SPACE);
		
		executorSutA.execute(processMockA1);
		Thread.sleep(3);
		executorSutB.execute(processMockB1);
		Thread.sleep(3);
		executorSutC.execute(processMockC1);
		shutdownExecutors();
		
		verify(processMockA1).run();
		verify(processMockB1).run();
		verify(processMockC1).run();
		
		boolean lastWasPrioritized = executorSutC.getStartedLastProcessAt() < executorSutB.getStartedLastProcessAt();
		assertTrue("The last process should move up the queue", lastWasPrioritized);
	}

	@Test
	public void shouldPreferSmallerTimestamp() throws InterruptedException {
		executorSutA = initSut(1);
		executorSutB = initSut(1);
		executorSutC = initSut(1);
		
		configureProcessMock(processMockA1, "bookA1", OcrPriority.NORMAL, 1L, ENOUGH_SPACE);
		doAnswer(withShortPause).when(processMockA1).run();
		configureProcessMock(processMockB1, "bookB1", OcrPriority.NORMAL, 4L, ENOUGH_SPACE);
		configureProcessMock(processMockC1, "bookC1", OcrPriority.NORMAL, 5L, ENOUGH_SPACE);
		
		executorSutA.execute(processMockA1);
		Thread.sleep(3);
		executorSutC.execute(processMockC1);
		executorSutB.execute(processMockB1);
		shutdownExecutors();
		
		verify(processMockA1).run();
		verify(processMockB1).run();
		verify(processMockC1).run();

		boolean orderedByTimestamp = executorSutB.getStartedLastProcessAt() < executorSutC.getStartedLastProcessAt();
		assertTrue("The processes should have been ordered by timestamp", orderedByTimestamp);
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
	
	private void shutdownExecutors() throws InterruptedException {
		executorSutA.shutdown();
		executorSutA.awaitTermination(100, TimeUnit.MILLISECONDS);
		executorSutB.shutdown();
		executorSutB.awaitTermination(100, TimeUnit.MILLISECONDS);
		executorSutC.shutdown();
		executorSutC.awaitTermination(100, TimeUnit.MILLISECONDS);
	}

}
