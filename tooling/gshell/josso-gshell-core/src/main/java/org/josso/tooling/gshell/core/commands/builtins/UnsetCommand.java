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

import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.DefaultVariables;
import org.josso.tooling.gshell.core.support.JOSSOCommandSupport;

import java.util.List;

/**
 * Unset a variable or property.
 *
 * @version $Rev: 974 $ $Date: 2009-01-13 22:39:45 -0200 (Tue, 13 Jan 2009) $
 */
@CommandComponent(id="gshell-builtins:unset", description="Unset a variable")
public class UnsetCommand
    extends JOSSOCommandSupport
{
    enum Mode
    {
        VARIABLE,
        PROPERTY
    }

    @Option(name="-m", aliases={"--mode"}, description="Unset mode")
    private Mode mode = Mode.VARIABLE;

    @Argument(required=true, description="Variable name")
    private List<String> args;

    protected Object doExecute() throws Exception {
        for (String arg : args) {
            String namevalue = String.valueOf(arg);

            switch (mode) {
                case PROPERTY:
                    unsetProperty(namevalue);
                    break;

                case VARIABLE:
                    unsetVariable(namevalue);
                    break;
            }
        }

        return SUCCESS;
    }

    private void ensureIsIdentifier(final String name) {
        if (!DefaultVariables.isIdentifier(name)) {
            throw new RuntimeException("Invalid identifer name: " + name);
        }
    }

    private void unsetProperty(final String name) {
        log.info("Unsetting system property: " + name);

        ensureIsIdentifier(name);

        System.getProperties().remove(name);
    }

    private void unsetVariable(final String name) {
        log.info("Unsetting variable: " + name);

        ensureIsIdentifier(name);

        // Command vars always has a parent, set only makes sence when setting in parent's scope
        Variables vars = variables.parent();

        vars.unset(name);
    }
}
