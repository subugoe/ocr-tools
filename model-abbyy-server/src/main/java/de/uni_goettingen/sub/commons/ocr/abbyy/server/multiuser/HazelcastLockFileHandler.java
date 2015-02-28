package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.LockFileHandler;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.HotfolderProvider;

public class HazelcastLockFileHandler extends LockFileHandler {

	private final static Logger logger = LoggerFactory.getLogger(HazelcastLockFileHandler.class);
	private HazelcastInstance hazelcast;

	// for unit tests
	void setProvider(HotfolderProvider newProvider) {
		hotfolderProvider = newProvider;
	}

	public HazelcastLockFileHandler(HazelcastInstance hazelcast) {
		this.hazelcast = hazelcast;
	}

	/**
	 * Handles the server lock with Hazelcast in mind. 
	 * 
	 */
	@Override
	public void createOrOverwriteLock(boolean overwriteLock) {
		boolean threwException = false;
		
		// we need to synchronize cluster-wide
		Lock monitor = hazelcast.getLock("monitor");
		monitor.lock();
		try {

			if (overwriteLock) {
				// the lock is deleted here, but a new one is created later
				hotfolder.deleteIfExists(lockUri);
			}

			// the set is used as a hint for incoming instances that 
			// it is OK to ignore the lock file on the server
			Set<String> lockRegisteringSet = hazelcast.getSet("lockSet");
			boolean lockExists = hotfolder.exists(lockUri);
			
			if (!lockExists) {
				// no lock file, ie no other processes running, so write it
				writeLockFile();
				// just add some string so that the next instances can check if it's there.
				// "Registering" the external lock inside the current Hazelcast cluster
				lockRegisteringSet.add("lockEntry");
			} else if (lockExists && !lockRegisteringSet.contains("lockEntry")) {
				// there is a lock file, but it is not "registered" in the running cluster
				// which means another cluster or program instance is running.
				threwException = true;
				throw new ConcurrentModificationException("Another client instance is running! See the lock file at " + lockUri);
			}
			// last case: lock exists and is "registered". Lock can be ignored, 
			// because we are part of the right cluster.
	
			
		} catch (IOException e) {
			logger.error("Error with server lock file: " + lockUri, e);
		} finally {
			// can only use the lock if hazelcast is still active
			monitor.unlock();
			if (threwException) {
				hazelcast.getLifecycleService().shutdown();
			}
		}
	}

	@Override
	public void deleteLockAndCleanUp() {
		// we need to synchronize cluster-wide
		Lock monitor = hazelcast.getLock("monitor");
		monitor.lock();
		try {
			if (hazelcast.getCluster().getMembers().size() == 1) {
				// the current instance is the only one in the cluster, so the
				// lock file can be deleted.
				hotfolder.delete(lockUri);
			}
		} catch (IOException e) {
			logger.error("Error while deleting lock file: " + lockUri, e);
		} finally {
			monitor.unlock();
			hazelcast.getLifecycleService().shutdown();
		}

	}
	
}
