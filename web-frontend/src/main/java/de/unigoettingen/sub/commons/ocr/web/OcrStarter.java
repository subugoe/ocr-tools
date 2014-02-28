package de.unigoettingen.sub.commons.ocr.web;

public class OcrStarter implements Runnable {

	private OcrParameters ocrParameters;

	public void setParameters(OcrParameters newParameters) {
		ocrParameters = newParameters;
	}

	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("ocr started");
			System.out.println(Thread.currentThread().getName());
			System.out.println();
		}
		
	}


}
