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

package org.josso.tooling.gshell.install;

/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 18, 2008
 * Time: 1:18:15 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TargetPlatform {

    String getId();

    String getFamily();

    String getPlatformName();

    String getDescription();

    String getVersion();

    String getDeployDir();

    String getLibDir();

    String getEndorsedLibDir();

    String getBinDir();

    String getConfigDir();

    String getJOSSOSharedLibDir();

    String getJOSSOLibDir();

    String getJOSSOConfDir();

    String getJOSSOWarName();

    boolean isJOSSOWarExploded();

}
