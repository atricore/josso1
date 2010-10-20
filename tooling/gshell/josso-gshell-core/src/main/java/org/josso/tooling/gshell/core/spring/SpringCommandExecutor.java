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

package org.josso.tooling.gshell.core.spring;

import org.apache.geronimo.gshell.CommandLineBuilder;
import org.apache.geronimo.gshell.DefaultCommandExecutor;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.command.CommandExecutor;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.layout.LayoutManager;
import org.apache.geronimo.gshell.shell.Environment;

/**
 * A simple wrapper around the DefaultCommandExecutor to avoid
 * using constructor injection which causes a circular dependency
 * in spring.
 */
public class SpringCommandExecutor implements CommandExecutor {

    private CommandExecutor executor;
    private LayoutManager layoutManager;
    private CommandRegistry commandRegistry;
    private CommandLineBuilder commandLineBuilder;
    private Environment env;

    public void setLayoutManager(LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public void setCommandRegistry(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    public void setCommandLineBuilder(CommandLineBuilder commandLineBuilder) {
        this.commandLineBuilder = commandLineBuilder;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }

    public void init() {
        executor = new DefaultCommandExecutor(layoutManager, commandRegistry, commandLineBuilder, env) {
            @Override
            protected Thread createThread(final Runnable run) {
                final IO proxyio = ProxyIO.getIO();
                final Environment env = EnvironmentTargetSource.getEnvironment();
                return new Thread() {
                    @Override
                    public void run() {
                        EnvironmentTargetSource.setEnvironment(env);
                        ProxyIO.setIO(proxyio);
                        run.run();
                    }
                };
            }

        };
    }

    public Object execute(String s) throws Exception {
        return executor.execute(s);
    }

    public Object execute(String s, Object[] objects) throws Exception {
        return executor.execute(s, objects);
    }

    public Object execute(Object... objects) throws Exception {
        return executor.execute(objects);
    }

    public Object execute(Object[][] objects) throws Exception {
        return executor.execute(objects);
    }
}
