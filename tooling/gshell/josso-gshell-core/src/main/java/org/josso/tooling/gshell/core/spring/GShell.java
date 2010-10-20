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

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.geronimo.gshell.DefaultEnvironment;
import org.apache.geronimo.gshell.DefaultShell;
import org.apache.geronimo.gshell.ExitNotification;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.layout.NotFoundException;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.geronimo.gshell.shell.InteractiveShell;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.tooling.gshell.core.cli.Main;

/**
 * This class represents the local shell console and is also used when passing a command to execute on the command line.
 *
 */
public class GShell implements Runnable {

    private static final Log log = LogFactory.getLog(GShell.class);

    private InteractiveShell shell;
    private Thread thread;
    private IO io;
    private Environment env;
    private boolean start;

    private volatile boolean closed;
    private GShellListener listener;

    public GShell(InteractiveShell shell) throws IOException {

        // TODO : Support command line args to run commands so we can avoid the interactive console
        
        this.shell = shell;
        if (shell instanceof DefaultShell) {
            DefaultShell sh = (DefaultShell) shell;
            sh.setErrorHandler(wrapErrorHandler(sh.getErrorHandler()));
        }
        this.io = new IO(new NoCloseInputStream(System.in),
                         new NoCloseOutputStream(System.out),
                         new NoCloseOutputStream(System.err));
        this.env = new DefaultEnvironment(new ProxyIO());
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
        closed = false;
    }

    public void stop() throws Exception {
        closed = true;
        io.close();
        if (thread != null) {
            thread.interrupt();
            thread.join();
            thread = null;
        }
    }

    public void run() {
        try {
            ProxyIO.setIO(io);
            EnvironmentTargetSource.setEnvironment(env);

            // go into a command shell.
            log.debug("Go into a command shell");
            shell.run();
            log.debug("Go out of a command shell");

        } catch (Throwable e) {

            log.debug("GShell:" + e.getMessage(), e);

            if (e instanceof ExitNotification) {

                synchronized (listener) {
                    listener.notifyAll();
                }

                if (closed) {
                    // Ignore notifications coming because the spring app has been destroyed
                    return;
                }
            }
            

        }
    }

   protected Console.ErrorHandler wrapErrorHandler(final Console.ErrorHandler handler) {
        return new Console.ErrorHandler() {
            public Result handleError(Throwable error) {
                if (closed) {
                    throw new ExitNotification();
                } else if (error instanceof NotFoundException) {
                    // Spit out the terse reason why we've failed
                    io.err.println("@|bold,red ERROR| Command not found: @|bold,red " + error.getMessage() + "|");
                    return Result.CONTINUE;
                } else {
                    return handler.handleError(error);
                }
            }
        };
    }


    /**
     * This could be much better but works for now.
     * @param listener
     */
    public void registerListener(GShellListener listener) {
        this.listener = listener;
    }
}
