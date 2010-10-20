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

package org.josso.tooling.gshell.core.commands.builtins;

import org.apache.geronimo.gshell.ExitNotification;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.layout.LayoutManager;
import org.josso.tooling.gshell.core.support.JOSSOCommandSupport;


/**
 * Exit the current shell.
 *
 * @version $Rev: 593392 $ $Date: 2007-11-09 03:14:15 +0100 (Fri, 09 Nov 2007) $
 */
@CommandComponent(id="gshell-builtins:exit", description="Exit the (sub)shell")
public class ExitCommand
    extends JOSSOCommandSupport
{
    @Argument(description="System exit code")
    private int exitCode = 0;

    protected Object doExecute() throws Exception {
        if (context.getVariables().get(LayoutManager.CURRENT_NODE) != null)
        {
            log.info("Exiting subshell");
            Variables v = context.getVariables();
            while (v != null && v.get(LayoutManager.CURRENT_NODE) != null) {
                v.unset(LayoutManager.CURRENT_NODE);
                v = v.parent();
            }
            return SUCCESS;
        }
        else
        {
            log.info("Exiting w/code: " + exitCode);

            //
            // DO NOT Call System.exit() !!!
            //

            throw new ExitNotification(exitCode);
        }
    }
}