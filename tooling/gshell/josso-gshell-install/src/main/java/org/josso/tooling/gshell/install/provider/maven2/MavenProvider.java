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

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileProvider;
import org.ops4j.pax.url.mvn.internal.Parser;

public class MavenProvider extends AbstractFileProvider {

	/** user home system property */
	private static final String USER_HOME_PROPERTY = "user.home";
	/** m2 local user settings */
	private static final String M2_DIR = "/.m2";
	/** default local repository */
	private static final String DEFAULT_DIR = M2_DIR.concat("/repository");
	/** discovered local m2 repository home */
	private String repositoryHome;

    @SuppressWarnings("unchecked")
	protected final static Collection capabilities = Collections.unmodifiableCollection(Arrays.asList(new Capability[]
     {
         Capability.READ_CONTENT,
         Capability.URI,
         Capability.GET_LAST_MODIFIED
     }));
	
	public MavenProvider() throws FileSystemException {
        super();
        mvnInit();
	}
	
	protected void mvnInit() throws FileSystemException {
		if(repositoryHome == null){
			repositoryHome = "file://" +System.getProperty(USER_HOME_PROPERTY) + DEFAULT_DIR;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Collection getCapabilities() {
		return capabilities;
	}

	public FileObject findFile(FileObject baseFile, String uri,
			FileSystemOptions fileSystemOptions) throws FileSystemException {

		try{
			if(uri.startsWith("mvn:") && uri.length() > 4){
				uri = uri.substring(4);
			} else {
				throw new FileSystemException("uri should contain group and artifact id.");
			}
		    final FileName rootName =
		        getContext().parseURI(repositoryHome);			
			Parser parser = new Parser(uri);
			uri = parser.getArtifactPath();

			final String key = this.getClass().getName();
			FileSystem fs = findFileSystem(key, fileSystemOptions);
			if(fs == null ){
				//rootname is path to .m2 repository (considering only local repos for now)
				fs = new MavenFileSystem(rootName, fileSystemOptions);
				addFileSystem(key, fs);
			}
			return fs.resolveFile(uri);
		} catch (MalformedURLException e) {
			throw new FileSystemException(e);
		}

		
	}

}
