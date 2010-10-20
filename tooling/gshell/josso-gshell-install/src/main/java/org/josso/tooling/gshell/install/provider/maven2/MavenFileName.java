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

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileName;

public class MavenFileName extends AbstractFileName {

	public MavenFileName(String scheme, String absPath, FileType type) {
		super(scheme, absPath, type);
	}

	@Override
	protected void appendRootUri(StringBuffer buffer, boolean addPassword) {
        buffer.append(getScheme());
        buffer.append("://");
        buffer.append(getPath());
	}

	@Override
	public FileName createName(String absPath, FileType type) {
		return new MavenFileName(getScheme(), absPath, type);
	}

}
