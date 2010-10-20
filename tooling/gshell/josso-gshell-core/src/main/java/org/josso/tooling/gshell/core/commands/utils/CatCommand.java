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
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.clp.Argument;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.josso.tooling.gshell.core.support.JOSSOCommandSupport;

import java.util.List;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Concatenate and print files and/or URLs.
 *
 * @version $Rev: 974 $ $Date: 2009-01-13 22:39:45 -0200 (Tue, 13 Jan 2009) $
 */
@CommandComponent(id="utils:cat", description="Concatenate and print files and/or URLs")
public class CatCommand extends JOSSOCommandSupport
{
    @Option(name="-n", description="Number the output lines, starting at 1")
    private boolean displayLineNumbers;

    @Argument(description="File or URL", required=true)
    private List<String> args;

    protected Object doExecute() throws Exception {
        //
        // Support "-" if length is one, and read from io.in
        // This will help test command pipelines.
        //
        if (args.size() == 1 && "-".equals(args.get(0))) {
            log.info("Printing STDIN");
            cat(new BufferedReader(io.in), io);
        }
        else {
            for (String filename : args) {
                BufferedReader reader;

                // First try a URL
                try {
                    URL url = new URL(filename);
                    log.info("Printing URL: " + url);
                    reader = new BufferedReader(new InputStreamReader(url.openStream()));
                }
                catch (MalformedURLException ignore) {
                    // They try a file
                    File file = new File(filename);
                    log.info("Printing file: " + file);
                    reader = new BufferedReader(new FileReader(file));
                }

                try {
                    cat(reader, io);
                }
                finally {
                    IOUtil.close(reader);
                }
            }
        }

        return SUCCESS;
    }

    private void cat(final BufferedReader reader, final IO io) throws IOException {
        String line;
        int lineno = 1;

        while ((line = reader.readLine()) != null) {
            if (displayLineNumbers) {
                String gutter = StringUtils.leftPad(String.valueOf(lineno++), 6);
                io.out.print(gutter);
                io.out.print("  ");
            }
            io.out.println(line);
        }
    }
}
