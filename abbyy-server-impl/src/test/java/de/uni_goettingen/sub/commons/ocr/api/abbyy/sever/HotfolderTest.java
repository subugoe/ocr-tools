package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.vfs.FileSystemException;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder;

public class HotfolderTest {
	
	@BeforeClass
	public static void init () throws FileSystemException {
		
	}
	
	@Test
	public void testHotfolder () throws HttpException, IOException, InterruptedException {
		Hotfolder hot = new Hotfolder();
		HttpClient client = new HttpClient();
		Credentials defaultcreds = new UsernamePasswordCredentials("gdz", "***REMOVED***");
	    client.getState().setCredentials(AuthScope.ANY, defaultcreds);
	    hot.setClient(client);
	    
	    //hot.copyFilesToServer(files);
	   
	    // Ok
	    hot.mkCol("http://dl380-130.gbv.de/webdav/GDZ/input/test/");
	    
	    // 
	    //hot.put(new URL("http://dl380-130.gbv.de/webdav/GDZ/input/test.xml/"), new File("C:/test.xml/") );
	   
	    //ok
	    hot.delete("http://dl380-130.gbv.de/webdav/GDZ/input/test/");
		
	    
	    
	    
	}
}
