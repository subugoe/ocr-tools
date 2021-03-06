package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ConcurrentModificationException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.HotfolderProvider;

public class LockFileHandler {

	private static Object monitor = new Object();
	private final String serverLockFile = "server.lock";
	protected Hotfolder hotfolder;
	protected URI lockUri;
	private final static Logger logger = LoggerFactory.getLogger(LockFileHandler.class);
	protected HotfolderProvider hotfolderProvider = new HotfolderProvider();

	// for unit tests
	void setHotfolderProvider(HotfolderProvider newProvider) {
		hotfolderProvider = newProvider;
	}

	public void initConnection(String serverUrl, String user, String password) {
		hotfolder = hotfolderProvider.createHotfolder(serverUrl, user, password);
		try {
			lockUri = new URI(serverUrl + serverLockFile);
		} catch (URISyntaxException e) {
			logger.error("Error with server lock file: " + serverUrl + serverLockFile, e);
		}
	}

	public void createOrOverwriteLock(boolean overwriteLock) {
		// need to synchronize on static object because of the Web Service
		synchronized(monitor) {
			try {
				if (overwriteLock) {
					// the lock is deleted here, but a new one is created later
					hotfolder.deleteIfExists(lockUri);
				}
				boolean lockExists = hotfolder.exists(lockUri);
				
				if (lockExists) {
					throw new ConcurrentModificationException("Another client instance is running! See the lock file at " + lockUri);
				}
				writeLockFile();
			} catch (IOException e) {
				logger.error("Error with server lock file: " + lockUri, e);
			}
		}
	}
	
	/**
	 * Creates a lock file containing the IP address and an ID of the current JVM process.
	 * 
	 * @throws IOException
	 */
	protected void writeLockFile() throws IOException {
		String thisIp = InetAddress.getLocalHost().getHostAddress();
		String thisJvmId = ManagementFactory.getRuntimeMXBean().getName();
		
		OutputStream tempLock = hotfolder.createTmpFile("lock");
		IOUtils.write("IP: " + thisIp + "\nID: " + thisJvmId, tempLock);
		hotfolder.copyTmpFile("lock", lockUri);
		hotfolder.deleteTmpFile("lock");
		
	}

	public void deleteLockAndCleanUp() {
		// need to synchronize because of the Web Service
		synchronized(monitor) {
			try {
				hotfolder.deleteIfExists(lockUri);
			} catch (IOException e) {
				logger.error("Error while deleting lock file: " + lockUri, e);
			}
		}
	}

}
