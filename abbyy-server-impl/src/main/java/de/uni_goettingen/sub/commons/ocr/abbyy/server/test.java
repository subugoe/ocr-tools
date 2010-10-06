package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.vfs.FileSystemException;

public class test {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 * @throws FileSystemException 
	 */
	public static void main(String[] args) throws MalformedURLException, FileSystemException {
		// TODO Auto-generated method stub
		Hotfolder hotfolder = new Hotfolder();
		URL imageUrl = hotfolder.fileToURL(new File("C:/test/getImage"));
		URL remoteURL = hotfolder.fileToURL(new File("C:/test/getRemote"));
		String remoteFileName = "C:/test";
		
		
		AbbyyOCRFile abb = new AbbyyOCRFile(imageUrl, remoteURL, remoteFileName);
		
		System.out.print(abb.getRemoteURL());
	}

}
