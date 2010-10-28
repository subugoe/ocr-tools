package de.uni_goettingen.sub.commons.ocr.abbyy.server;

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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;

public class AbbyyOCROutput extends AbstractOCROutput {

	protected String remoteLocation;
	protected String remoteFilename;
	protected URI remoteUri;
	protected URI reportUri;
	protected Boolean singleFile = true;
	//This is used, if we don't create the result in single file mode
	protected List<URI> resultFragments = new ArrayList<URI>();

	public AbbyyOCROutput(URI uri) {
		super(uri, null);
	}

	//This calls a copy constructor for the base class
	public AbbyyOCROutput(OCROutput ocrOutput) {
		super(ocrOutput);
	}

	protected AbbyyOCROutput() {
		super();
	}

	//More copy constructors
	public AbbyyOCROutput(URI uri, Map<String, String> params, URI remoteUri, String remoteLocation, URI reportUri, Boolean singleFile, List<URI> resultFragments) {
		super();
		this.outputUri = uri;
		this.params = params;
		this.remoteUri = remoteUri;
		this.remoteLocation = remoteLocation;
		this.reportUri = reportUri;
		this.singleFile = singleFile;
		this.resultFragments = resultFragments;
	}

	public AbbyyOCROutput(AbbyyOCROutput aoo) {
		this(aoo.outputUri, aoo.params, aoo.remoteUri, aoo.remoteLocation, aoo.reportUri, aoo.singleFile, aoo.resultFragments);
		//TODO: This ist full of nulls, can't dereference the map and list
		//this(aoo.outputUri, new HashMap<String, String>(aoo.params), aoo.remoteUri, aoo.remoteLocation, aoo.reportUri, aoo.singleFile, new ArrayList<URI>(aoo.resultFragments));
	}

	/**
	 * @return the remoteLocation
	 */
	public String getRemoteLocation () {
		return remoteLocation;
	}

	/**
	 * @param remoteLocation
	 *            the remoteLocation to set
	 */
	public void setRemoteLocation (String remoteLocation) {
		this.remoteLocation = remoteLocation;
	}

	/**
	 * @return the remoteUri
	 */
	public URI getRemoteUri () {
		return remoteUri;
	}

	/**
	 * @param remoteUri
	 *            the remoteUri to set
	 */
	public void setRemoteUri (URI remoteUri) {
		this.remoteUri = remoteUri;
	}

	/**
	 * @return the singleFile
	 */
	public Boolean getSingleFile () {
		return singleFile;
	}

	/**
	 * @param singleFile
	 *            the singleFile to set
	 */
	public void setSingleFile (Boolean singleFile) {
		this.singleFile = singleFile;
	}

	/**
	 * @return the resultFragments
	 */
	public List<URI> getResultFragments () {
		return resultFragments;
	}

	public void addResultFragment (URI uri) {
		this.resultFragments.add(uri);
	}

	/**
	 * @param resultFragments
	 *            the resultFragments to set
	 */
	public void setResultFragments (List<URI> resultFragments) {
		this.resultFragments = resultFragments;
	}

	/**
	 * @return the remoteFilename
	 */
	public String getRemoteFilename () {
		return remoteFilename;
	}

	/**
	 * @param remoteFilename
	 *            the remoteFilename to set
	 */
	public void setRemoteFilename (String remoteFilename) {
		this.remoteFilename = remoteFilename;
	}

}
