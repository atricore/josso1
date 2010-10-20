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

package org.josso.tooling.gshell.install.commands;

import org.josso.tooling.gshell.core.support.JOSSOCommandSupport;
import org.josso.tooling.gshell.install.installer.Installer;
import org.josso.tooling.gshell.install.TargetPlatform;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * @org.apache.xbean.XBean element="list-platforms"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 */
@CommandComponent(id="josso-install:list-platforms", description="List Available platforms")
public class ListPlatformsCommand extends JOSSOCommandSupport {

    public ListPlatformsCommand() {
        this.setShell("install");
    }


    protected Object doExecute() throws Exception {

        Map platforms = getApplicationContext().getBeansOfType(TargetPlatform.class);
        Set keys = platforms.keySet();

        io.out.println("Available Installers for Platform ID");

        for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
            String platformId = (String) iterator.next();
            TargetPlatform platform = (TargetPlatform) platforms.get(platformId);

            printValue(platform.getPlatformName(), platform.getVersion());
        }

        return null;

    }
}

