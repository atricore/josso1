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

import org.apache.geronimo.gshell.ansi.Renderer;
import org.apache.geronimo.gshell.branding.Branding;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.layout.LayoutManager;
import org.apache.geronimo.gshell.layout.model.Node;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.geronimo.gshell.shell.ShellInfo;

/**
 * A prompter that displays the current sub-shell.
 */
public class Prompter implements Console.Prompter {

    private Renderer renderer = new Renderer();
    private ShellInfo shellInfo;
    private Environment env;
    private Branding branding;

    public Prompter(ShellInfo shellInfo, Environment env) {
    	this.shellInfo = shellInfo;
    	this.env = env;
    }

    public Branding getBranding() {
		return branding;
	}

	public void setBranding(Branding branding) {
		this.branding = branding;
	}


    public String prompt() {
        String userName = shellInfo.getUserName();
        String hostName = shellInfo.getLocalHost().getHostName();

        Node start = (Node) env.getVariables().get(LayoutManager.CURRENT_NODE);
        String path = "";
        if (start != null) {
            path = start.getPath();
            path = path.replace('/', ' ');
        }

        // return renderer.render("@|bold " + userName + "|@" + hostName + ":@|bold " + path + "|> ");
        // I think a simpler prompt would be best.
        return renderer.render("@|bold "+branding.getName()+path+"|> ");
    }
}
