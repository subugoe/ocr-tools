package de.unigoettingen.sub.commons.ocrComponents.hazelcast;

import static java.lang.Thread.sleep;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISet;

import static java.lang.Thread.sleep;

import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.unigoettingen.sub.commons.ocrComponents.webservice.Item;
import de.unigoettingen.sub.commons.ocrComponents.webservice.ItemComparator;




public class AbbyyOCREngine {
	// The max threads.
	protected static Integer maxThreads = 2;
	protected HazelcastInstance h = Hazelcast.newHazelcastInstance(null);

	/** The Constant logger. */
	final static Logger logger = LoggerFactory
			.getLogger(AbbyyOCREngine.class);
	
	/** single instance of AbbyyServerOCREngine. */
	private static AbbyyOCREngine _instance;
	
	// OCR Processes
	protected Queue<AbbyyOCRProcess> processes = new ConcurrentLinkedQueue<AbbyyOCRProcess>();
	/** A simple list containing {@link OCRProcess} that will be processed */
	protected List<AbbyyOCRProcess> ocrProcess = new ArrayList<AbbyyOCRProcess>();

	
	
	
	private AbbyyOCREngine(){

		
	}
	
	protected void start() throws InterruptedException {
		logger.trace("Add OCRProcess");
		
		for(int i=1 ; i < 4; i++){
       	 	sleep(2000);
       	 	AbbyyOCRProcess a = new AbbyyOCRProcess(i, new Date().getTime(), "I" +i );
       	    ocrProcess.add(a);
           /* sleep(10);
            a = new AbbyyOCRProcess(1, new Date().getTime(), "J"+i );
            ocrProcess.add(a);	
            sleep(10);
            a = new AbbyyOCRProcess(1, new Date().getTime(), "K"+i);
            ocrProcess.add(a);
            sleep(10);
            a = new AbbyyOCRProcess(1, new Date().getTime(), "L"+i );
            ocrProcess.add(a);
            sleep(10);
            a = new AbbyyOCRProcess(1, new Date().getTime(), "M" +i);
            ocrProcess.add(a); 	*/
       }
		
		
		
		
		
		ExecutorService pool = new HazelcastExecuter(maxThreads, h);

		for (AbbyyOCRProcess process : getOcrProcess()) {
			AbbyyOCRProcess p = (AbbyyOCRProcess) process;

			processes.add(p);

		}
		
		
		for (AbbyyOCRProcess p : processes) {
			pool.execute(p);
		}
		
		pool.shutdown();
		
		try {
			pool.awaitTermination(3600, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Got a problem with thread pool: ", e);
		}
		/*ISet <AbbyyOCRProcess> set = hazel.getSet("default");
		if(set.size() == 0) Hazelcast.shutdownAll();*/
		System.out.println("************"+h.getCluster().getLocalMember());
		h.getLifecycleService().shutdown();
	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCREngine#getOcrProcess()
	 */
	public List<AbbyyOCRProcess> getOcrProcess() {
		return ocrProcess;
	}
	
	public Observable recognize() throws InterruptedException {
		if (processes.isEmpty()) {
			start();
		} else if (processes.isEmpty()) {
			throw new IllegalStateException("Queue is empty!");
		}
		return null;
	}
	
	public static AbbyyOCREngine getInstance() {

		if (_instance == null) {
			_instance = new AbbyyOCREngine();
		}
		return _instance;
	}
}
