package de.uni_goettingen.sub.commons.ocr.api;

/*

Copyright 2010 SUB Goettingen. All rights reserved.
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


// TODO: Auto-generated Javadoc
/**
 * A factory for creating OCREngine objects.
 * @version 0.9
 * @author cmahnke
 * @since 0.9
 */
public class OCREngineFactory {
	
	/** The field _instance contains the reference to this singleton. */
	private static OCREngineFactory _instance = null;
	private OCREngine engine;

	/**
	 * Instantiates a new OCR engine factory.
	 */
	private OCREngineFactory () {
		
	}
	
	/**
	 * Gets the instance of the factory. 
	 *
	 * @return the OCR engine factory
	 */
	public static OCREngineFactory getInstance () {
		if (_instance == null) {
			_instance = new OCREngineFactory();
		}
		return _instance;

	}

	public static OCREngineFactory getNewFactory () {		
			_instance = new OCREngineFactory();
		return _instance;

	}
	/**
	 * Returns a new ocr engine instance.
	 *
	 * @return the OCR engine
	 */
	public OCREngine newOcrEngine () {
		return this.engine;
	}

	public void setEngine(OCREngine engine) {
		this.engine = engine;
	}
	
	


}
