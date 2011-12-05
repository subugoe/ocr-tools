package de.uni_goettingen.sub.commons.ocr.abbyy.server;


import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemListener;

public class HazelcastObserver {

	private static ISet<AbbyyOCRProcess> waiting;
	private static ISet<AbbyyOCRProcess> running;
	
	public static void main(String[] args) throws InterruptedException {
		waiting = Hazelcast.getSet("queued");
		running = Hazelcast.getSet("running");
		
		

		while (true) {
			System.out.println("-----------------");
			System.out.println("waiting");
			for (AbbyyOCRProcess pr : waiting) {
				System.out.println(pr.getName());
			}

			System.out.println();

			System.out.println("running");
			for (AbbyyOCRProcess pr : running) {
				System.out.println(pr.getName());
			}

			System.out.println("-----------------");
			
			Thread.sleep(2000);
		}
	}


}
