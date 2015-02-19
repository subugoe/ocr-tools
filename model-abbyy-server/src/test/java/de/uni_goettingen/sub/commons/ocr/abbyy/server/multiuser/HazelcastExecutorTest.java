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
		
		when(processMock1.hasEnoughSpaceForExecution()).thenReturn(true);
		when(processMock1.getProcessId()).thenReturn("book1");
		when(processMock1.getPriority()).thenReturn(OcrPriority.NORMAL);
		when(processMock1.getStartedAt()).thenReturn(1L);
		
		when(processMock2.hasEnoughSpaceForExecution()).thenReturn(true);
		when(processMock2.getProcessId()).thenReturn("book2");
		when(processMock2.getPriority()).thenReturn(OcrPriority.NORMAL);
		when(processMock2.getStartedAt()).thenReturn(2L);
	}

	@Test
	public void shouldExecuteOne() throws InterruptedException {
		executorSut.execute(processMock1);
		shutdownExecutor();
		
		verify(processMock1).run();
	}

	@Test
	public void shouldExecuteTwo() throws InterruptedException {
		doAnswer(withShortPause).when(processMock1).run();
		doAnswer(withShortPause).when(processMock2).run();
		executorSut.execute(processMock1);
		executorSut.execute(processMock2);
		shutdownExecutor();
		
		verify(processMock1).run();
	}

	private void shutdownExecutor() throws InterruptedException {
		executorSut.shutdown();
		executorSut.awaitTermination(100, TimeUnit.MILLISECONDS);
	}

}
