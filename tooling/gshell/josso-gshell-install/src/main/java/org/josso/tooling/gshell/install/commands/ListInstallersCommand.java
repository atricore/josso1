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

import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.josso.tooling.gshell.install.installer.Installer;
import org.josso.tooling.gshell.core.support.JOSSOCommandSupport;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * @org.apache.xbean.XBean element="list-installers"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: ListInstallersCommand.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
@CommandComponent(id="josso-install:list-installers", description="List Available installers")
public class ListInstallersCommand extends JOSSOCommandSupport {

    public ListInstallersCommand() {
        this.setShell("install");
    }


    protected Object doExecute() throws Exception {

        Map installers = getApplicationContext().getBeansOfType(Installer.class);
        Set keys = installers.keySet();

        io.out.println("Available Installers for Platform ID");

        for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
            String isntallerId = (String) iterator.next();
            Installer installer = (Installer) installers.get(isntallerId);

            printValue(installer.getPlatformDescription(), installer.getPlatformId());
        }

        return null;

    }
}
