package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ConcurrentModificationException;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.HotfolderProvider;

public class LockFileHandlerTest {

	private LockFileHandler lockHandlerSut;
	private HotfolderProvider providerMock = mock(HotfolderProvider.class);
	private Hotfolder hotfolderMock = mock(Hotfolder.class);

	private URI lockUri;

	@Before
	public void beforeEachTest() throws Exception {
		lockHandlerSut = new LockFileHandler();
		when(providerMock.createHotfolder(anyString(), anyString(), anyString())).thenReturn(hotfolderMock);
		lockHandlerSut.setHotfolderProvider(providerMock);
		lockUri = new URI("http://test/server.lock");

		lockHandlerSut.initConnection("http://test/", "user", "password");
	}

	@Test
	public void shouldCreateNewLock() throws IOException {
		when(hotfolderMock.exists(lockUri)).thenReturn(false);
		when(hotfolderMock.createTmpFile(anyString())).thenReturn(new ByteArrayOutputStream());

		lockHandlerSut.createOrOverwriteLock(false); // not overwrite
		
		verify(hotfolderMock, never()).deleteIfExists(lockUri);
		verify(hotfolderMock).copyTmpFile("lock", lockUri);
	}
	
	@Test(expected=ConcurrentModificationException.class)
	public void shouldCancelWhenLockIsPresent() throws IOException {
		when(hotfolderMock.exists(lockUri)).thenReturn(true);

		lockHandlerSut.createOrOverwriteLock(false); // not overwrite	
	}
	
	@Test
	public void shouldOverwriteLock() throws IOException {
		when(hotfolderMock.exists(lockUri)).thenReturn(false);
		when(hotfolderMock.createTmpFile(anyString())).thenReturn(new ByteArrayOutputStream());

		lockHandlerSut.createOrOverwriteLock(true); // overwrite

		verify(hotfolderMock).deleteIfExists(lockUri);
		verify(hotfolderMock).copyTmpFile("lock", lockUri);
	}
	
	@Test
	public void shouldDeleteLock() throws IOException {
		lockHandlerSut.deleteLockAndCleanUp();
		
		verify(hotfolderMock).deleteIfExists(lockUri);
	}
	
}
