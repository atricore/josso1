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

import jline.Terminal;
import jline.UnixTerminal;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

/**
 * Spring factory bean for JLine terminal.
 * The main purpose of this factory is to destroy the terminal when
 * the Spring application is terminated, so that the terminal is
 * restored to its normal state.
 */
public class TerminalFactoryBean implements FactoryBean, DisposableBean {

    private Terminal terminal;

    public synchronized Object getObject() throws Exception {
        if (terminal == null) {
            terminal = Terminal.getTerminal();
        }
        return terminal;
    }

    public Class getObjectType() {
        return Terminal.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public synchronized void destroy() throws Exception {
        if (terminal != null) {
            if (terminal instanceof UnixTerminal) {
                ((UnixTerminal) terminal).restoreTerminal();
            }
            terminal = null;
        }
    }
}
