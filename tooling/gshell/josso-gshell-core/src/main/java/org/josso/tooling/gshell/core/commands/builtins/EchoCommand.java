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

import java.util.List;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.josso.tooling.gshell.core.support.JOSSOCommandSupport;


/**
 * A simple command to <em>echo</em> all given arguments to the commands standard output.
 *
 * @version $Rev: 593392 $ $Date: 2007-11-09 03:14:15 +0100 (Fri, 09 Nov 2007) $
 */
@CommandComponent(id="gshell-builtins:echo", description="Echo or print arguments to STDOUT")
public class EchoCommand
    extends JOSSOCommandSupport
{
    @Option(name="-n", description="Do not print the trailing newline character")
    private boolean trailingNewline = true;

    @Argument(description="Arguments")
    private List<String> args;

    protected Object doExecute() throws Exception {
        if (args != null) {
            int c=0;

            for (String arg : args) {
                io.out.print(arg);
                if (++c + 1 < args.size()) {
                    io.out.print(" ");
                }
            }
        }

        if (trailingNewline) {
            io.out.println();
        }

        return SUCCESS;
    }
}