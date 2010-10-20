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

package org.josso.tooling.gshell.core.support;

import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.FileSystemException;

/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 14, 2008
 * Time: 8:10:51 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class VfsCommandSupport extends JOSSOCommandSupport {

    private FileSystemManager fsManager;

    protected FileSystemManager getFileSystemManager() {
        if (fsManager == null) {
            try {
                fsManager = VFS.getManager();
            }
            catch (FileSystemException e) {
                throw new RuntimeException(e);
            }
        }

        return fsManager;
    }
}
