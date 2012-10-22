package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerOCREngine;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.OCRExecuter;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

public class MultiUserAbbyyOCREngine extends AbbyyServerOCREngine {
	final static Logger logger = LoggerFactory
			.getLogger(MultiUserAbbyyOCREngine.class);

	private static MultiUserAbbyyOCREngine _instance;
	
	private MultiUserAbbyyOCREngine() throws ConfigurationException {
		super();
	}

	public static MultiUserAbbyyOCREngine getInstance() {

		if (_instance == null) {
			try {
				_instance = new MultiUserAbbyyOCREngine();
			} catch (ConfigurationException e) {
				logger.error("Can't read configuration", e);
				throw new OCRException(e);
			}
		}
		return _instance;
	}

	/**
	 * Handles the server lock with Hazelcast in mind. 
	 * 
	 */
	@Override
	protected void handleLock() throws IOException {
		// the set is used as a hint for incoming instances that 
		// it is OK to ignore the lock file on the server
		Set<String> lockSet = Hazelcast.getSet("lockSet");
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
				throw new RuntimeException("Another client instance is running! See the lock file at " + lockURI);
			} finally {
				Hazelcast.getLifecycleService().shutdown();
			}
		}

		// last case: lock exists and is "registered". Lock can be ignored, 
		// because we are part of the right cluster.

	}

	@Override
	protected OCRExecuter createPool() {
		return new HazelcastOCRExecutor(maxThreads, hotfolder, config);
	}

	@Override
	protected void cleanUp() {
		try {
			if (Hazelcast.getCluster().getMembers().size() == 1) {
				// the current instance is the only one in the cluster, so the
				// lock can be deleted.
				hotfolder.delete(lockURI);
			}
		} catch (IOException e) {
			logger.error("Error while deleting lock file: " + lockURI, e);
		} finally {
			Hazelcast.getLifecycleService().shutdown();
		}

	}
	
}
