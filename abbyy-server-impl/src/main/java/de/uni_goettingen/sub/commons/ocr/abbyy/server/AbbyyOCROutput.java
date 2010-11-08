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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;

/**
 * The Class AbbyyOCRImage. Is a representation of an OCROutput.
 * -remoteFileName, the file name as used on the remote
 * system, usally a relative file name and thus represented as a String. -
 * remoteURL, an URL representing the remote file, it should be resolveable from
 * the local Server.-remoteLocation, The remote location represents the location 
 * on the remote system, something like D\:\\Recognition\\GDZ\\output. -singleFile,
 * If this is set the process is split into separate files. -resultFragments, This 
 * is used, if we don't create the result in single file mode
 */
public class AbbyyOCROutput extends AbstractOCROutput {

	/** The remote location represents the location on the remote system, something like D\:\\Recognition\\GDZ\\output */
	private String remoteLocation;
	
	/** The local file name on the remote system. */
	private String remoteFilename;
	
	/** The URI of the file, need to be resolvable from the local machine. */
	protected URI remoteUri;
	

	/** If this is set the process is split into separate files  */
	private Boolean singleFile = true;
	//This is used, if we don't create the result in single file mode
	private List<URI> resultFragments = new ArrayList<URI>();

	//Add some informations about the location of error files
	/** The URIs to expect in case an error has happened on the server */
	protected List<URI> errorImages = new ArrayList<URI>();
	
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
	/**
	 * Instantiates a new abbyy ocr output.
	 *
	 * @param uri the uri of the local image
	 * @param params, @see de.uni_goettingen.sub.commons.ocr.api.OCROutput#setParams(java.util.Map)
	 * @param remoteUri, This represents the URI to the remote system.
	 * @param remoteLocation, The remote location represents the location on the remote system
	 * @param singleFile, If this is set the process is split into separate files
	 * @param resultFragments, This is used, if we don't create the result in single file mode
	 */
	public AbbyyOCROutput(URI uri, Map<String, String> params, URI remoteUri, String remoteLocation, Boolean singleFile, List<URI> resultFragments) {
		super();
		this.outputUri = uri;
		this.params = params;
		this.remoteUri = remoteUri;
		this.remoteLocation = remoteLocation;
		this.singleFile = singleFile;
		this.resultFragments = resultFragments;
	}

	/**
	 * Instantiates a new abbyy ocr output.
	 *
	 * @param aoo the aoo
	 */
	public AbbyyOCROutput(AbbyyOCROutput aoo) {
		this(aoo.outputUri, aoo.params, aoo.remoteUri, aoo.remoteLocation, aoo.singleFile, aoo.resultFragments);		
	}

	/**
	 * The remote location represents the location on the remote system
	 * @return the remoteLocation
	 */
	public String getRemoteLocation () {
		return remoteLocation;
	}

	/**
	 * The remote location represents the location on the remote system
	 * @param remoteLocation, 
	 *            the remoteLocation to set
	 */
	public void setRemoteLocation (String remoteLocation) {
		this.remoteLocation = remoteLocation;
	}

	/**
	 * @return the remoteUri
	 * 				This represents the URI to the remote system.
	 */
	public URI getRemoteUri () {
		return remoteUri;
	}

	/**
	 * @param remoteUri
	 *            the remoteUri to set
	 *            This represents the URI to the remote system.
	 */
	public void setRemoteUri (URI remoteUri) {
		this.remoteUri = remoteUri;
	}

	/**
	 * @return the singleFile
	 * 				If this is set the process is split into separate files
	 */
	public Boolean isSingleFile () {
		return singleFile;
	}

	/**
	 * @return the resultFragments
	 * 				This is used, if we don't create the result in single file mode
	 */
	public List<URI> getResultFragments () {
		return resultFragments;
	}

	public void addResultFragment (URI uri) {
		this.resultFragments.add(uri);
		singleFile = false;
	}

	/**
	 * @param resultFragments
	 *            the resultFragments to set
	 *            This is used, if we don't create the result in single file mode
	 */
	public void setResultFragments (List<URI> resultFragments) {
		this.resultFragments = resultFragments;
		singleFile = false;
	}

	/**
	 * @return the remoteFilename
	 * 				The local file name on the remote system.
	 */
	public String getRemoteFilename () {
		return remoteFilename;
	}

	/**
	 * @param remoteFilename
	 *            the remoteFilename to set
	 *            The local file name on the remote system.
	 */
	public void setRemoteFilename (String remoteFilename) {
		this.remoteFilename = remoteFilename;
	}
	
	protected void setSingleFile (Boolean singleFile) {
		this.singleFile = singleFile;
	}

}
