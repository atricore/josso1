/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2009, Atricore, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.josso.tooling.gshell.install.provider.maven2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.UriParser;
import org.ops4j.pax.url.mvn.internal.Configuration;
import org.ops4j.pax.url.mvn.internal.ConfigurationImpl;
import org.ops4j.pax.url.mvn.internal.Connection;
import org.ops4j.util.property.PropertiesPropertyResolver;
import org.ops4j.util.property.PropertyResolver;

public class MavenFileObject extends AbstractFileObject
implements FileObject {

    private URI uri;
	private MavenFileSystem fileSystem;

	protected MavenFileObject(FileName name, MavenFileSystem fs) {
		super(name, fs);
		fileSystem = fs;
	}
	
    protected void doAttach() throws Exception
    {
        if (uri == null)
        {
            // url = new URL(getName().getURI());
        	String encodedUrl = encodeURI(getName().getURI());
            uri =  new URI(encodedUrl);//createURL(getName());
        }
    }

//    protected URI createURL(final FileName name) throws MalformedURLException, FileSystemException, URIException
//    {
//        if (name instanceof URLFileName)
//        {
//            URLFileName urlName = (URLFileName) getName();
//
//            // TODO: charset
//            return new URL(urlName.getURIEncoded(null));
//        }
//        return new URL(getName().getURI());
//    }	

	private String encodeURI(String uri) {
		return uri.replace(" ", "%20");
	}

	@Override
	protected long doGetContentSize() throws Exception {
        final URLConnection conn = uri.toURL().openConnection();
        final InputStream in = conn.getInputStream();
        try
        {
            return conn.getContentLength();
        }
        finally
        {
            in.close();
        }		
	}

	@Override
	protected InputStream doGetInputStream() throws Exception {
		final URLConnection conn = uri.toURL().openConnection();
        return conn.getInputStream();
	}

	@Override
	protected FileType doGetType() throws Exception {
        try
        {
            // Attempt to connect & check status
            final URLConnection conn = uri.toURL().openConnection();
            final InputStream in = conn.getInputStream();
            try
            {
                if (conn instanceof HttpURLConnection)
                {
                    final int status = ((HttpURLConnection) conn).getResponseCode();
                    // 200 is good, maybe add more later...
                    if (HttpURLConnection.HTTP_OK != status)
                    {
                        return FileType.IMAGINARY;
                    }
                }

                return FileType.FILE;
            }
            finally
            {
                in.close();
            }
        }
        catch (final FileNotFoundException e)
        {
            return FileType.IMAGINARY;
        }
	}

	@Override
	protected String[] doListChildren() throws Exception {
        throw new FileSystemException("Not implemented.");
	}
	
    /**
     * Returns the last modified time of this file.
     */
    protected long doGetLastModifiedTime()
        throws Exception
    {
        final URLConnection conn = uri.toURL().openConnection();
        final InputStream in = conn.getInputStream();
        try
        {
            return conn.getLastModified();
        }
        finally
        {
            in.close();
        }
    }	

}
