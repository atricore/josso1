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
import java.io.PrintWriter;

import jline.ConsoleReader;
import jline.Terminal;
import org.apache.geronimo.gshell.ansi.ANSI;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.command.annotation.Requirement;
import org.josso.tooling.gshell.core.support.JOSSOCommandSupport;

/**
 * Clear the terminal screen.
 *
 * @version $Rev: 595891 $ $Date: 2007-11-17 02:23:47 +0100 (Sat, 17 Nov 2007) $
 */
@CommandComponent(id="gshell-builtins:clear", description="Clear the terminal screen")
public class ClearCommand
    extends JOSSOCommandSupport
{
    @Requirement
    private Terminal terminal;

    public ClearCommand() {
    }

    public ClearCommand(Terminal terminal) {
        this.terminal = terminal;
    }

    protected JOSSOCommandSupport createCommand() throws Exception {
        return new ClearCommand(terminal);
    }

    protected Object doExecute() throws Exception {
        ConsoleReader reader = new ConsoleReader(io.inputStream, new PrintWriter(io.outputStream, true), /*bindings*/ null, terminal);

        if (!ANSI.isEnabled()) {
        	io.out.println("ANSI is not enabled.  The clear command is not functional");
        }
        else {
        	reader.clearScreen();
        	return SUCCESS;
        }

        return SUCCESS;
    }
}

