package de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder;

/*
 * This file is part of the SUB Commons project.
 * Visit the websites for more information. 
 * 		- http://www.sub.uni-goettingen.de 
 * 
 * Copyright 2009, 2010, SUB Goettingen.
 * 
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * The interface is used to access any file system like backend. This
 * can be used to integrate external systems like Grid storage or WebDAV based
 * hotfolders.
 * 
 */
public interface Hotfolder {

	public void upload(URI fromLocal, URI toRemote) throws IOException;
	
	public void download(URI fromRemote, URI toLocal) throws IOException;
	
	public abstract void delete(URI uri) throws IOException;

	public abstract void deleteIfExists(URI uri) throws IOException;

	public abstract boolean exists(URI uri) throws IOException;

	public abstract OutputStream createTmpFile(String name) throws IOException;

	public abstract void deleteTmpFile(String name) throws IOException;

	public abstract void copyTmpFile(String tmpFile, URI to)
			throws IOException;

	public abstract long getUsedSpace(URI uri) throws IOException;

	public abstract byte[] getResponse(URI uri) throws IOException;


}