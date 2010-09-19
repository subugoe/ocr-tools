package de.uni_goettingen.sub.commons.ocr.abbyy.server;
/*

© 2010, SUB Göttingen. All rights reserved.
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

*/

import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.OCRExecuter;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;


public class AbbyyServerEngine implements OCREngine{
	
	protected Integer maxThreads = 5;
	protected ExecutorService pool = new OCRExecuter(maxThreads);
	final Logger logger = LoggerFactory.getLogger(AbbyyServerEngine.class);

	
	
	public AbbyyServerEngine(){		
	}
	
	@Override
	public void recognize() throws OCRException {
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

	protected void finalize () {
		pool.shutdown();
		try {
			//TODO: Calculate the right expected timeout
			pool.awaitTermination(3600, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Got a problem with thread pool: ", e);
		}
	}

}
