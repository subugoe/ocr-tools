package de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.googlecode.sardine.util.SardineException;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;
import de.unigoettingen.sub.commons.util.file.FileUtils;

/**
 * The Class SardineHotfolderImpl is a Hotfolder implementation based on Sardine
 */
public class SardineHotfolderImpl extends AbstractHotfolder implements Hotfolder {

	/** The Constant logger. */
	final static Logger logger = LoggerFactory.getLogger(SardineHotfolderImpl.class);

	/** The instance of this singleton */
	private SardineHotfolderImpl _instance = null;
	/** The sardine instance */
	private static Sardine sardine;

	/**
	 * Instantiates a new sardine hotfolder implementation, the Configuration
	 * should be given, to be able to connect to a Server secured with username
	 * and password.
	 * 
	 * @param config
	 *            the ConfigParser Object needed for authentification.
	 */
	private SardineHotfolderImpl(ConfigParser config) {
		try {
			sardine = SardineFactory.begin(config.getUsername(), config.getPassword());
		} catch (SardineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#copyFile(java.lang.String, java.lang.String)
	 */
	@Override
	public void copyFile (URI from, URI to) throws IOException {
		//We have two methods that must be called here, one for local to remote and the other way arround
		if (isLocal(from) && !isLocal(to)) {
			//This should be an upload
			put(to.toString(), new File(from));
		} else if (!isLocal(from) && isLocal(to)) {
			//This should be a download
			//outdir = outdir.endsWith(File.separator) ? outdir : outdir + File.separator;
			//File localFile = new File(outdir + localfilename);
			getWebdavFile(from.toString(), new File(to));
		} else if (isLocal(from) && isLocal(to)) {
			//Just copy local files
			FileUtils.copyDirectory(new File(from), new File(to));
		} else {
			throw new NotImplementedException();
		}

	}

	/**
	 * This method downloads a WebDAV file contents to the given local file.
	 * 
	 * @param string
	 *            the URI of the remote file as String
	 * @param file
	 *            the file to write to
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	//TODO: Check if we can reuse a method to copy the stream to a file here
	private void getWebdavFile (String uri, File file) throws IOException {
		InputStream is = sardine.getInputStream(uri);
		org.apache.commons.io.FileUtils.copyInputStreamToFile(is, file); 
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#delete(java.net.URI)
	 */
	@Override
	public void delete (URI uri) throws IOException {
		sardine.delete(uri.toString());

	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#exists(java.net.URI)
	 */
	@Override
	public Boolean exists (URI uri) throws IOException {
		return sardine.exists(uri.toString());
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#getSize(java.net.URI)
	 */
	@Override
	public Long getSize (URI uri) throws IOException {
		return getResouce(uri).getContentLength();
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#isDirectory(java.net.URI)
	 */
	@Override
	public Boolean isDirectory (URI uri) throws IOException {
		return getResouce(uri).isDirectory();
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#listURIs(java.net.URI)
	 */
	@Override
	public List<URI> listURIs (URI uri) throws IOException {
		List<URI> uris = new ArrayList<URI>();
		for (DavResource dr : sardine.getResources(uri.toString())) {
			try {
				uris.add(new URI(dr.getAbsoluteUrl()));
			} catch (URISyntaxException e) {
				logger.error("Can't convert returned URIs");
			}
		}
		return uris;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#mkDir(java.net.URI)
	 */
	@Override
	public void mkDir (URI uri) throws IOException {
		sardine.createDirectory(uri.toString());

	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#openInputStream(java.net.URI)
	 */
	@Override
	public InputStream openInputStream (URI uri) throws IOException {
		return sardine.getInputStream(uri.toString());
	}

	/**
	 * Gets the instance of the SardineHotfolderImpl singleton and sets its
	 * configuration (username and password).
	 * 
	 * @param cp
	 *            the ConfigPArser object used to configure this instance. The
	 *            configuration is only passed if this is the first call of this
	 *            method.
	 * @return the instance of the SardineHotfolderImpl object
	 */
	public SardineHotfolderImpl getInstance (ConfigParser cp) {
		if (_instance == null) {
			_instance = new SardineHotfolderImpl(cp);
		}
		return _instance;
	}

	/**
	 * Put the given file on the WebDAV Server at the given location.
	 * 
	 * @param to
	 *            the URI to put the file to as String.
	 * @param from
	 *            the file that should be uploaded.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void put (String to, File from) throws IOException {
		InputStream fis = new FileInputStream(from);
		sardine.put(to, fis);
	}

	/**
	 * Gets the resouce.
	 * 
	 * @param uri
	 *            the uri
	 * @return the resouce
	 */
	//TODO: Finish this method
	private DavResource getResouce (URI uri) {
		return null;

	}

}
