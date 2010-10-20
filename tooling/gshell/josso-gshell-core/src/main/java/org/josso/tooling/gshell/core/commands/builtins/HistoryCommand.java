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
import org.apache.geronimo.gshell.command.annotation.Requirement;
import org.josso.tooling.gshell.core.support.JOSSOCommandSupport;
import jline.History;

/**
 * Displays the history of commands
 *
 * @version $Rev: 974 $ $Date: 2009-01-13 22:39:45 -0200 (Tue, 13 Jan 2009) $
 */
@CommandComponent(id="gshell-builtins:history", description="Displays the history of commands")
public class HistoryCommand
    extends JOSSOCommandSupport
{
    @Requirement
    private History history;

    @Option(name="-n", description="Do not print the trailing newline character")
    private boolean trailingNewline = true;

    @Argument(description="Arguments")
    private List<String> args;

    public HistoryCommand(History history) {
        this.history = history;
    }

    protected JOSSOCommandSupport createCommand() throws Exception {
        return new HistoryCommand(history);
    }

    protected Object doExecute() throws Exception {
        for (Object o : history.getHistoryList()) {
            io.out.println(o);
        }
        return SUCCESS;
    }
}