package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Before;
import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;

public class HazelcastExecutorTest {

	private HazelcastExecutor executorSutSpy;
	private HazelcastInstance hazelMock = mock(HazelcastInstance.class);
	private Lock lock = new ReentrantLock();
	private Condition conditionForExecution = lock.newCondition();
	
	@Before
	public void beforeEachTest() throws Exception {
		HazelcastExecutor executorSut = new HazelcastExecutor(2, hazelMock);
		executorSutSpy = spy(executorSut);
		
		//when(hazelMock.getLock(anyString())).thenReturn(lock);
	}

	@Test
	public void test() {
		
	}

}
