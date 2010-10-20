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

package org.josso.tooling.gshell.core.support;

import org.apache.geronimo.gshell.clp.CommandLineProcessor;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.clp.Printer;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.command.annotation.Requirement;
import org.apache.geronimo.gshell.common.Arguments;
import org.apache.geronimo.gshell.ansi.Renderer;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.BeansException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 3, 2007
 * Time: 9:44:39 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class JOSSOCommandSupport implements Command, ApplicationContextAware {

    protected String shell;

    protected Log log = LogFactory.getLog(getClass());

    protected CommandContext context;

    protected MessagePrinter printer;

    protected IO io;

    protected Variables variables;

    private ApplicationContext springContext;

    @Requirement
    private Renderer renderer = new Renderer();

    @Option(name="-h", aliases={"--help"}, description="Display this help message", requireOverride = true)
    private boolean displayHelp;

    /**
     * We use the 'Prototype' pattern.  We want to make sure that we do not mix instances!
     * @return
     * @throws Exception
     */
    protected JOSSOCommandSupport createCommand() throws Exception {
        JOSSOCommandSupport newCommand = getClass().newInstance();
        newCommand.setApplicationContext(springContext);
        return newCommand;
    }


    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.springContext = applicationContext;
    }

    protected ApplicationContext getApplicationContext() {
        return springContext;
    }

    public String getShell() {
        return shell;
    }

    public void setShell(String shell) {
        this.shell = shell;
    }

    public String getId() {
        CommandComponent cmd = getClass().getAnnotation(CommandComponent.class);
        if (cmd == null) {
            throw new IllegalStateException("Command id not found");
        }
        return cmd.id();
    }

    public String getDescription() {
        CommandComponent cmd = getClass().getAnnotation(CommandComponent.class);
        if (cmd == null) {
            throw new IllegalStateException("Command description not found");
        }
        return cmd.description();
    }

    public Object execute(final CommandContext context, final Object... args) throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        JOSSOCommandSupport cmd = createCommand();
        cmd.init(context);
        return cmd.doExecute(args);
    }

    public void init(final CommandContext context) {
        assert context != null;

        this.context = context;
        this.printer = new MessagePrinter(context.getIO());
        this.io = context.getIO();
        this.variables = context.getVariables();

        // Re-setup logging using our id
        String id = getId();
        log = LogFactory.getLog(getClass().getName() + "." + id);
    }

    public Object doExecute(final Object... args) throws Exception {
        assert args != null;

        log.info("Executing w/args: [{}] :" + Arguments.asString(args));

        CommandLineProcessor clp = new CommandLineProcessor(this);
        clp.process(Arguments.toStringArray(args));

        // Handle --help/-h automatically for the command
        if (displayHelp) {
            //
            // TODO: Make a special PrinterHandler to abstrat this muck from having to process it by hand
            //

            displayHelp(clp);

            return SUCCESS;
        }

        assert printer != null;
        assert variables != null;

        return doExecute();
        
    }

    protected abstract Object doExecute() throws Exception;

    protected void printValue(String name, String value) {
        printer.printMsg("  " + StringUtils.rightPad(renderer.render("@|bold " + name + "|"), 62)  + "   " + value);
        log.debug(name + ":" + value);
    }


    protected void displayHelp(final CommandLineProcessor clp) {
        assert clp != null;

        //
        // TODO: Need to ask the LayoutManager what the real name is for our command's ID
        //

        this.printer.printMsg(getId());
        this.printer.printMsg(" -- ");
        this.printer.printMsg();

        Printer p = new Printer(clp);
        p.printUsage(printer.getOut());
        printer.printMsg();
    }


}
