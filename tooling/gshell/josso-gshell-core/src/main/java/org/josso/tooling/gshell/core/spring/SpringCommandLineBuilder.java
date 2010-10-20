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
import java.io.Reader;
import java.io.StringReader;

import org.apache.geronimo.gshell.CommandLine;
import org.apache.geronimo.gshell.CommandLineBuilder;
import org.apache.geronimo.gshell.ErrorNotification;
import org.apache.geronimo.gshell.ExecutingVisitor;
import org.apache.geronimo.gshell.command.CommandExecutor;
import org.apache.geronimo.gshell.parser.ASTCommandLine;
import org.apache.geronimo.gshell.parser.CommandLineParser;
import org.apache.geronimo.gshell.parser.ParseException;
import org.apache.geronimo.gshell.shell.Environment;

/**
 * A CommandLineBuilder that uses a single executor and environment, expecting
 * those to be proxies to some thread local instances.  Use setter injection to
 * avoid a circular dependency with the SpringCommandExecutor.
 */
public class SpringCommandLineBuilder implements CommandLineBuilder {

    private CommandLineParser parser = new CommandLineParser();
    private CommandExecutor executor;
    private Environment environment;

    public SpringCommandLineBuilder() {
    }

    public void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private ASTCommandLine parse(final String input) throws ParseException {
         assert input != null;

         Reader reader = new StringReader(input);
         ASTCommandLine cl;
         try {
             cl = parser.parse(reader);
         }
         finally {
             try {
                 reader.close();
             } catch (IOException e) {
                 // Ignore
             }
         }

         return cl;
     }

     public CommandLine create(final String commandLine) throws ParseException {
         assert commandLine != null;

         if (commandLine.trim().length() == 0) {
             throw new IllegalArgumentException("Command line is empty");
         }

         try {
             final ExecutingVisitor visitor = new ExecutingVisitor(executor, environment);
             final ASTCommandLine root = parse(commandLine);

             return new CommandLine() {
                 public Object execute() throws Exception {
                     return root.jjtAccept(visitor, null);
                 }
             };
         }
         catch (Exception e) {
             throw new ErrorNotification(e);
         }
     }

}
