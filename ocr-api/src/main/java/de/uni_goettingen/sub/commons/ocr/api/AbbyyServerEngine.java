package de.uni_goettingen.sub.commons.ocr.api;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Observer;
import java.util.Set;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.Ticket;




public class AbbyyServerEngine implements OCREngine{

	private Ticket ticket;
	private static File basefolderFile = null;
	private static List<File> inputFiles = new ArrayList<File>();
	private OCRProcess ocrp;
	private static File ticketFile;
	OCRImage ocri = null;
	protected static List<Locale> langs;
	List<OCRFormat> enums = new ArrayList<OCRFormat>();
	protected List<OCRImage> ocrImage = new ArrayList<OCRImage>();
	
	public AbbyyServerEngine(){
		
	}
	//Protected
	public AbbyyServerEngine(OCRProcess ocrp){
		this.ocrp= ocrp;
	}
		

	
	@Override
	public void recognize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOCRProcess(OCRProcess ocrp) {
		// TODO Auto-generated method stub
		//this.ocrp = ocrp; 
	}

	@Override
	public OCRProcess getOCRProcess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OCROutput getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setObserver(Observer observer) {
		// TODO Auto-generated method stub
		
	}

}
