package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerOCREngine;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.OCRExecuter;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

public class MultiUserAbbyyOCREngine extends AbbyyServerOCREngine {
	final static Logger logger = LoggerFactory
			.getLogger(MultiUserAbbyyOCREngine.class);

	private static MultiUserAbbyyOCREngine instance;
	
	private HazelcastInstance hazelcast;
	
	private MultiUserAbbyyOCREngine() throws ConfigurationException {
		super();
		System.setProperty("hazelcast.logging.type", "log4j");
		hazelcast = Hazelcast.newHazelcastInstance(null);
	}
	
	// for unit tests
	protected MultiUserAbbyyOCREngine(HazelcastInstance haz) throws ConfigurationException {
		super();
		hazelcast = haz;
	}

	public static synchronized MultiUserAbbyyOCREngine getInstance() {

		if (instance == null) {
			try {
				instance = new MultiUserAbbyyOCREngine();
			} catch (ConfigurationException e) {
				logger.error("Can't read configuration", e);
				throw new OCRException(e);
			}
		}
		return instance;
	}
	// we need this for our Web Service, because each request needs its own instance
	public static MultiUserAbbyyOCREngine newOCREngine() {
		MultiUserAbbyyOCREngine engine = null;
		try {
			engine = new MultiUserAbbyyOCREngine();
		} catch (ConfigurationException e) {
			logger.error("Can't read configuration", e);
			throw new OCRException(e);
		}
		return engine;
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
			boolean lockExists = hotfolder.exists(lockURI);
			
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
					throw new ConcurrentModificationException("Another client instance is running! See the lock file at " + lockURI);
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
	protected OCRExecuter createPool() {
		return new HazelcastOCRExecutor(maxThreads, hotfolder, hazelcast);
	}

	@Override
	protected void cleanUp() {
		// we probably must synchronize cluster-wide
		Lock lock = hazelcast.getLock("monitor");
		lock.lock();

		try {
			if (hazelcast.getCluster().getMembers().size() == 1) {
				// the current instance is the only one in the cluster, so the
				// lock file can be deleted.
				hotfolder.delete(lockURI);
			}
		} catch (IOException e) {
			logger.error("Error while deleting lock file: " + lockURI, e);
		} finally {
			lock.unlock();
			hazelcast.getLifecycleService().shutdown();
		}

	}
	
}
