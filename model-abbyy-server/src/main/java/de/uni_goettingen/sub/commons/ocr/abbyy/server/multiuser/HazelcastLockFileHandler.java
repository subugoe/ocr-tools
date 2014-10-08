package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.LockFileHandler;

public class HazelcastLockFileHandler extends LockFileHandler {

	private final static Logger logger = LoggerFactory.getLogger(HazelcastLockFileHandler.class);
	private HazelcastInstance hazelcast;

	public HazelcastLockFileHandler(HazelcastInstance hazelcast) {
		this.hazelcast = hazelcast;
	}

	/**
	 * Handles the server lock with Hazelcast in mind. 
	 * 
	 */
	@Override
	protected void handleLock() throws IOException {
		boolean isShutdown = false;
		
		// we probably must synchronize cluster-wide
		Lock lock = hazelcast.getLock("monitor");
		lock.lock();
		try {
			
			// the set is used as a hint for incoming instances that 
			// it is OK to ignore the lock file on the server
			Set<String> lockSet = hazelcast.getSet("lockSet");
			boolean lockExists = hotfolder.exists(lockUri);
			
			if (!lockExists) {
				// no lock file, ie no other processes running, so write it
				writeLockFile();
				// just add some string so that the next instances can check if it's there.
				// "Registering" the external lock inside the current Hazelcast cluster
				lockSet.add("lockEntry");
			}
			if (lockExists && !lockSet.contains("lockEntry")) {
				// there is a lock file, but it is not "registered" in the running cluster
				// which means another cluster or program instance is running.
				try {
					throw new ConcurrentModificationException("Another client instance is running! See the lock file at " + lockUri);
				} finally {
					hazelcast.getLifecycleService().shutdown();
					isShutdown = true;
				}
			}
			
	
			// last case: lock exists and is "registered". Lock can be ignored, 
			// because we are part of the right cluster.
	
			
		} finally {
			// can only use the lock if hazelcast is still active
			if (!isShutdown) {
				lock.unlock();
			}
		}
	}

	@Override
	public void deleteLock() {
		try {
			if (hazelcast.getCluster().getMembers().size() == 1) {
				// the current instance is the only one in the cluster, so the
				// lock file can be deleted.
				hotfolder.delete(lockUri);
			}
		} catch (IOException e) {
			logger.error("Error while deleting lock file: " + lockUri, e);
		}
	}
	
}
