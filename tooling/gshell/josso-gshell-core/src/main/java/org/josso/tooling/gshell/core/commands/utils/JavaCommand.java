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

package org.josso.tooling.gshell.core.commands.utils;

import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.common.Arguments;
import org.josso.tooling.gshell.core.support.JOSSOCommandSupport;

import java.util.List;
import java.lang.reflect.Method;

/**
 * Execute a Java standard application.
 *
 * <p>By default looks for static main(String[]) to execute, but
 * you can specify a different static method that takes a String[]
 * to execute instead.
 *
 * @version $Rev: 974 $ $Date: 2009-01-13 22:39:45 -0200 (Tue, 13 Jan 2009) $
 */
@CommandComponent(id="utils:java", description="Execute a Java standard application")
public class JavaCommand extends JOSSOCommandSupport
{
    @Option(name="-m", aliases={"--method"}, metaVar="METHOD", description="Invoke a named method")
    private String methodName = "main";

    @Argument(index=0, metaVar="CLASSNAME", description="The name of the class to invoke", required=true)
    private String className;

    @Argument(index=1, metaVar="ARG", description="Arguments to pass to the METHOD of CLASSNAME")
    private List<String> args;

    protected Object doExecute() throws Exception {
        boolean info = log.isInfoEnabled();

        Class type = Thread.currentThread().getContextClassLoader().loadClass(className);
        if (info) {
            log.info("Using type: " + type);
        }

        Method method = type.getMethod(methodName, String[].class);
        if (info) {
            log.info("Using method: " + method);
        }

        if (info) {
            log.info("Invoking w/arguments: " + Arguments.asString(args));
        }

        Object result = method.invoke(null, args);

        if (info) {
            log.info("Result: " + result);
        }

        return SUCCESS;
    }
}
