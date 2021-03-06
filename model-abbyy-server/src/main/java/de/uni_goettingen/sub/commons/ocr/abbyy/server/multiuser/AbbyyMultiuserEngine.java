package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyEngine;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.LockFileHandler;

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
	protected ThreadPoolExecutor createPool(int maxThreads) {
		return new HazelcastExecutor(maxThreads, hazelcast);
	}
	
	@Override
	protected LockFileHandler createLockHandler() {
		return new HazelcastLockFileHandler(hazelcast);
	}
	
}
