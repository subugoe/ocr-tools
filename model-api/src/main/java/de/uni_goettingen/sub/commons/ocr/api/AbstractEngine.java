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

import java.util.ArrayList;
import java.util.List;

/**
 * The Class AbstractEngine is a abstract super class for {@link OcrEngine}
 * implementations. It adds two methods to be able to provide metadata for the
 * different implementations {@link #getName()} and {@link #getVersion()}.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public abstract class AbstractEngine implements OcrEngine {

	/** A simple list containing {@link OcrProcess} that will be processed */
	protected List<OcrProcess> ocrProcess = new ArrayList<OcrProcess>();

	// State variables
	/** Indicates if the processing of the engine has started. */
	protected Boolean started = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_goettingen.sub.commons.ocr.api.OcrEngine#getOcrProcess()
	 */
	public List<OcrProcess> getOcrProcess() {
		return ocrProcess;
	}


}