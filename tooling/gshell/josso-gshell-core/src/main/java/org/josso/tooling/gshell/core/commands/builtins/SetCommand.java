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

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.geronimo.gshell.DefaultVariables;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.josso.tooling.gshell.core.support.JOSSOCommandSupport;

/**
 * Set a variable or property.
 *
 * @version $Rev: 974 $ $Date: 2009-01-13 22:39:45 -0200 (Tue, 13 Jan 2009) $
 */
@CommandComponent(id="gshell-builtins:set", description="Set a variable")
public class SetCommand
    extends JOSSOCommandSupport
{
    enum Mode
    {
        VARIABLE,
        PROPERTY
    }

    @Option(name="-m", aliases={"--mode"}, description="Set mode")
    private Mode mode = Mode.VARIABLE;

    @Argument(description="Variable definition")
    private List<String> args;

    protected Object doExecute() throws Exception {
        // No args... list all properties or variables
        if (args == null || args.size() == 0) {
            switch (mode) {
                case PROPERTY: {
                    Properties props = System.getProperties();

                    for (Object o : props.keySet()) {
                        String name = (String) o;
                        String value = props.getProperty(name);

                        io.out.print(name);
                        io.out.print("=");
                        io.out.print(value);
                        io.out.println();
                    }
                    break;
                }

                case VARIABLE: {
                    Iterator<String> iter = variables.names();

                    while (iter.hasNext()) {
                        String name = iter.next();
                        Object value = variables.get(name);

                        io.out.print(name);
                        io.out.print("=");
                        io.out.print(value);
                        io.out.println();
                    }
                    break;
                }
            }

            return SUCCESS;
        }

        //
        // FIXME: This does not jive well with the parser, and stuff like foo = "b a r"
        //

        //
        // NOTE: May want to make x=b part of the CL grammar
        //

        for (Object arg : args) {
            String namevalue = String.valueOf(arg);

            switch (mode) {
                case PROPERTY:
                    setProperty(namevalue);
                    break;

                case VARIABLE:
                    setVariable(namevalue);
                    break;
            }
        }

        return SUCCESS;
    }

    class NameValue
    {
        String name;
        String value;
    }

    private NameValue parse(final String input) {
        NameValue nv = new NameValue();

        int i = input.indexOf("=");

        if (i == -1) {
            nv.name = input;
            nv.value = "true";
        }
        else {
            nv.name = input.substring(0, i);
            nv.value = input.substring(i + 1, input.length());
        }

        nv.name = nv.name.trim();

        return nv;
    }

    private void ensureIsIdentifier(final String name) {
        if (!DefaultVariables.isIdentifier(name)) {
            throw new RuntimeException("Invalid identifer name: " + name);
        }
    }

    private void setProperty(final String namevalue) {
        NameValue nv = parse(namevalue);

        log.info("Setting system property: "+nv.name+"="+nv.value);

        ensureIsIdentifier(nv.name);

        System.setProperty(nv.name, nv.value);
    }

    private void setVariable(final String namevalue) {
        NameValue nv = parse(namevalue);

        log.info("Setting variable: "+nv.name+"="+nv.value);

        ensureIsIdentifier(nv.name);

        // Command vars always has a parent, set only makes sence when setting in parent's scope
        Variables vars = variables.parent();

        vars.set(nv.name, nv.value);
    }
}
