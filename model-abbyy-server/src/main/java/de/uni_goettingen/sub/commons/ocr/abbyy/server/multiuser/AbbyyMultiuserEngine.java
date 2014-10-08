package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyEngine;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.LockFileHandler;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.OcrExecutor;

public class AbbyyMultiuserEngine extends AbbyyEngine {
	final static Logger logger = LoggerFactory
			.getLogger(AbbyyMultiuserEngine.class);

	private HazelcastInstance hazelcast;
	
	public AbbyyMultiuserEngine() {
		System.setProperty("hazelcast.logging.type", "log4j");
		hazelcast = Hazelcast.newHazelcastInstance(null);
	}
	
	// for unit tests
	protected AbbyyMultiuserEngine(HazelcastInstance haz) {
		hazelcast = haz;
	}

	@Override
	protected OcrExecutor createPool(int maxThreads) {
		return new HazelcastExecutor(maxThreads, hazelcast);
	}
	
	@Override
	protected LockFileHandler createLockHandler() {
		return new HazelcastLockFileHandler(hazelcast);
	}

	@Override
	protected void cleanUp() {
		// we probably must synchronize cluster-wide
		Lock lock = hazelcast.getLock("monitor");
		lock.lock();

		try {
			lockHandler.deleteLock();
		} finally {
			lock.unlock();
			hazelcast.getLifecycleService().shutdown();
		}

	}
	
}
