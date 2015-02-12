package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class OcrExecutorTest {

	private OcrExecutor executorSut;
	private AbbyyProcess processMock = mock(AbbyyProcess.class);
	
	@Before
	public void beforeEachTest() throws Exception {
		executorSut = new OcrExecutor(2);
	}

	@Test
	public void should() throws IOException, InterruptedException {
		doThrow(new IllegalStateException("first")).when(processMock).noSpaceForExecution();
		
		executorSut.execute(processMock);

		Thread.sleep(500);

		
		AbbyyProcess processMock2 = mock(AbbyyProcess.class);
		executorSut.execute(processMock2);
		AbbyyProcess processMock3 = mock(AbbyyProcess.class);
		executorSut.execute(processMock3);

		Thread.sleep(1000);
		
		verify(processMock).run();
	}

}
