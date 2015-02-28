package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ConcurrentModificationException;

import org.junit.Before;
import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISet;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.HotfolderProvider;

public class HazelcastLockFileHandlerTest {

	private HazelcastLockFileHandler lockHandlerSut;
	private HazelcastInstance hazelMock = mock(HazelcastInstance.class, RETURNS_DEEP_STUBS);
	@SuppressWarnings("unchecked")
	private ISet<Object> registeringSetMock = mock(ISet.class);
	private HotfolderProvider providerMock = mock(HotfolderProvider.class);
	private Hotfolder hotfolderMock = mock(Hotfolder.class);
	private URI lockUri;
	
	@Before
	public void beforeEachTest() throws Exception {
		when(hazelMock.getSet(anyString())).thenReturn(registeringSetMock);
		
		lockHandlerSut = new HazelcastLockFileHandler(hazelMock);
		when(providerMock.createHotfolder(anyString(), anyString(), anyString())).thenReturn(hotfolderMock);
		lockHandlerSut.setProvider(providerMock);
		lockUri = new URI("http://test/server.lock");

		lockHandlerSut.initConnection("http://test/", "user", "password");
	}

	@Test
	public void shouldCreateNewLock() throws IOException {
		when(hotfolderMock.exists(lockUri)).thenReturn(false);
		when(hotfolderMock.createTmpFile(anyString())).thenReturn(new ByteArrayOutputStream());

		lockHandlerSut.createOrOverwriteLock(false);
		
		verify(hotfolderMock, never()).deleteIfExists(lockUri);
		verify(hotfolderMock).copyTmpFile("lock", lockUri);
	}

	@Test(expected=ConcurrentModificationException.class)
	public void shouldCancelWhenLockIsPresentButNotRegistered() throws IOException {
		when(hotfolderMock.exists(lockUri)).thenReturn(true);
		when(registeringSetMock.contains(anyObject())).thenReturn(false);

		lockHandlerSut.createOrOverwriteLock(false); // not overwrite	
	}

	@Test
	public void shouldIgnoreLockIfPresentAndRegistered() throws IOException {
		when(hotfolderMock.exists(lockUri)).thenReturn(true);
		when(registeringSetMock.contains(anyObject())).thenReturn(true);

		lockHandlerSut.createOrOverwriteLock(false); // not overwrite	

		verify(hotfolderMock, never()).deleteIfExists(lockUri);
		verify(hotfolderMock, never()).copyTmpFile("lock", lockUri);
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
		when(hazelMock.getCluster().getMembers().size()).thenReturn(1);
		
		lockHandlerSut.deleteLockAndCleanUp();
		
		verify(hotfolderMock).delete(lockUri);
	}

}
