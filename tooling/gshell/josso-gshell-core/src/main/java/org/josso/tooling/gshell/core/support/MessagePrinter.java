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

import org.codehaus.plexus.util.StringUtils;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.ansi.Renderer;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.io.Writer;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: MessagePrinter.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class MessagePrinter {

    private Log log = LogFactory.getLog(MessagePrinter.class);

    private Renderer renderer;

    private IO io;

    public MessagePrinter(IO io) {
        this.renderer =
                new Renderer();
        this.io = io;
    }

    public void printErrStatus(String name, String msg) {
        io.out.println("  " + StringUtils.rightPad(renderer.render("@|bold " + name + "|"), 62) + "   [@|red ERROR|] " + msg);
        log.error(name + " : " + msg);
    }

    public void printWarnStatus(String name, String msg) {
        io.out.println("  " + StringUtils.rightPad(renderer.render("@|bold " + name + "|"), 62) + "   [@|yellow WARN| ] " + msg);
        log.warn(name + " : " + msg);
    }

    public void printOkStatus(String name) {
        io.out.println("  " + StringUtils.rightPad(renderer.render("@|bold " + name + "|"), 62) + "   [@|green OK|   ] ");
        log.info(name + " : OK" );
    }

    public void printOkStatus(String name, String msg) {
        io.out.println("  " + StringUtils.rightPad(renderer.render("@|bold " + name + "|"), 62) + "   [@|green OK|   ] " + msg);
        log.info(name + " : OK.  " + msg);
    }

    public void printActionErrStatus(String action, String artifact, String msg) {
        action = StringUtils.rightPad(action, 13);
        printMsg(StringUtils.rightPad(renderer.render("  @|bold "+action+"| [@|blue " + artifact + "|]"), 75) + " [@|red ERROR|] " + msg);
        log.error(action + " [" + artifact + "] : " + msg);
    }

    public void printActionWarnStatus(String action, String artifact, String msg) {
        action = StringUtils.rightPad(action, 13);
        printMsg(StringUtils.rightPad(renderer.render("  @|bold "+action+"| [@|blue " + artifact + "|]"), 75) + " [@|yellow WARN| ] " + msg);
        log.warn(action + " [" + artifact + "] : " + msg);
    }

    public void printActionOkStatus(String action, String artifact, String msg) {
        action = StringUtils.rightPad(action, 13);
        printMsg(StringUtils.rightPad(renderer.render("  @|bold "+action+"| [@|blue " + artifact+ "|]"), 75) + " [@|green OK|   ] " + (msg != null ? msg : ""));
        log.info(action + " [" + artifact+ "] : " + msg);
    }



    public void flush() {
        io.out.flush();
    }

    public void printMsg(String msg) {
        io.out.println(msg);
        log.info(msg);
    }

    public void printMsg() {
        io.out.println();
    }

    // Getters 

    public Writer getOut() {
        return io.out;
    }

    protected Renderer getRenderer() {
        return renderer;
    }

    public IO getIo() {
        return io;
    }

}
