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

package org.josso.tooling.gshell.core.commands.builtins;

import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.command.annotation.Requirement;
import org.apache.geronimo.gshell.command.CommandExecutor;
import org.apache.geronimo.gshell.clp.Argument;
import org.codehaus.plexus.util.IOUtil;
import org.josso.tooling.gshell.core.support.JOSSOCommandSupport;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;

/**
 * Read and execute commands from a file/url in the current shell environment.
 *
 * @version $Rev: 974 $ $Date: 2009-01-13 22:39:45 -0200 (Tue, 13 Jan 2009) $
 */
@CommandComponent(id="gshell-builtins:source", description="Load a file/url into the current shell")
public class SourceCommand
    extends JOSSOCommandSupport
{
    @Requirement
    private CommandExecutor executor;

    @Argument(required=true, description="Source file")
    private String source;

    protected Object doExecute() throws Exception {
        URL url;

        try {
            url = new URL(source);
        }
        catch (MalformedURLException e) {
            url = new File(source).toURI().toURL();
        }

        BufferedReader reader = openReader(url);
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String tmp = line.trim();

                // Ignore empty lines and comments
                if (tmp.length() == 0 || tmp.startsWith("#")) {
                    continue;
                }

                executor.execute(line);
            }
        }
        finally {
            IOUtil.close(reader);
        }

        return SUCCESS;
    }

    private BufferedReader openReader(final Object source) throws IOException {
        BufferedReader reader;

        if (source instanceof File) {
            File file = (File)source;
            log.info("Using source file: " + file);

            reader = new BufferedReader(new FileReader(file));
        }
        else if (source instanceof URL) {
            URL url = (URL)source;
            log.info("Using source URL: " + url);

            reader = new BufferedReader(new InputStreamReader(url.openStream()));
        }
        else {
            String tmp = String.valueOf(source);

            // First try a URL
            try {
                URL url = new URL(tmp);
                log.info("Using source URL: " + url);

                reader = new BufferedReader(new InputStreamReader(url.openStream()));
            }
            catch (MalformedURLException ignore) {
                // They try a file
                File file = new File(tmp);
                log.info("Using source file: " + file);

                reader = new BufferedReader(new FileReader(tmp));
            }
        }

        return reader;
    }
}
