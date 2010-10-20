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

package org.josso.tooling.gshell.core.branding;

import org.apache.geronimo.gshell.branding.BrandingSupport;
import org.apache.geronimo.gshell.branding.VersionLoader;
import org.apache.geronimo.gshell.ansi.RenderWriter;
import org.apache.geronimo.gshell.ansi.Buffer;
import org.apache.geronimo.gshell.ansi.Code;
import org.codehaus.plexus.util.StringUtils;
import jline.Terminal;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 7, 2008
 * Time: 3:20:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class JOSSOBranding extends BrandingSupport {


    private static final String[] BANNER = {
        "    __ _____ _____ _____ _____ ",
        " __|  |     |   __|   __|     |",
        "|  |  |  |  |__   |__   |  |  |",
        "|_____|_____|_____|_____|_____|"
    };

    private VersionLoader versionLoader;

    private Terminal terminal;

    public JOSSOBranding(final VersionLoader versionLoader, final Terminal terminal) {
        this.versionLoader = versionLoader;
        this.terminal = terminal;
    }

    public String getName() {
        return "josso";
    }

    public String getDisplayName() {
        return "JOSSO Admin Shell";
    }

    public String getProgramName() {
        return System.getProperty("program.name", "gsh");
    }

    public String getAbout() {
        StringWriter writer = new StringWriter();
        PrintWriter out = new RenderWriter(writer);

        out.println("For information about @|cyan Atricore JOSSO|, visit:");
        out.println("    @|bold http://www.josso.org| ");
        out.flush();

        return writer.toString();
    }

    public String getVersion() {
        return versionLoader.getVersion();
    }

    public String getWelcomeBanner() {
        StringWriter writer = new StringWriter();
        PrintWriter out = new RenderWriter(writer);
        Buffer buff = new Buffer();

        for (String line : BANNER) {
            buff.attrib(line, Code.CYAN);
            out.println(buff);
        }

        out.println();
        out.println(" @|bold Atricore JOSSO| (" + getVersion() + ")");
        out.println();
        out.println("Type '@|bold help|' for more information.");

        // If we can't tell, or have something bogus then use a reasonable default
        int width = terminal.getTerminalWidth();
        if (width < 1) {
            width = 80;
        }

        out.print(StringUtils.repeat("-", width - 1));

        out.flush();

        return writer.toString();
    }
}

