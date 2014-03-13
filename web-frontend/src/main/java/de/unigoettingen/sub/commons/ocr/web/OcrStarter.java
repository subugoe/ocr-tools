package de.unigoettingen.sub.commons.ocr.web;

public class OcrStarter implements Runnable {

	private OcrParameters ocrParameters;

	public void setParameters(OcrParameters newParameters) {
		ocrParameters = newParameters;
	}

	public String checkParameters() {
		System.out.println(ocrParameters.email);
		return "OK";
	}

	@Override
	public void run() {
		while(true) {
			System.out.println("ocr started");
			System.out.println(Thread.currentThread().getName());
			System.out.println(ocrParameters.email);
			System.out.println();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}


}
