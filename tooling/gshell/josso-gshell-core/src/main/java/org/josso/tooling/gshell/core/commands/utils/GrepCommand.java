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
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.codehaus.plexus.util.StringUtils;
import org.josso.tooling.gshell.core.support.JOSSOCommandSupport;

import java.util.regex.Pattern;
import java.io.IOException;
import java.io.Reader;

@CommandComponent(id="utils:grep", description="Print lines matching a pattern")
public class GrepCommand extends JOSSOCommandSupport {

    @Argument(required=true, description="Regular expression")
    private String regex;

    @Option(name="-n", aliases = { "--line-number" }, description="Prefix each line of output with the line number within its input file.")
    private boolean lineNumber;

    @Option(name = "-v", aliases = { "--invert-match" }, description = "Invert the sense of matching, to select non-matching lines.")
    private boolean invertMatch;

    @Option(name = "-w", aliases = { "--word-regexp" }, description = "Select only those lines containing matches that form whole " +
                                                                      "words.  The test is that the matching substring must either be " +
                                                                      "at  the beginning of the line, or preceded by a non-word constituent " +
                                                                      "character.  Similarly, it must be either at the end of " +
                                                                      "the line or followed by a non-word constituent character.  " +
                                                                      "Word-constituent characters are letters, digits, and the underscore.")
    private boolean wordRegexp;

    @Option(name = "-x", aliases = { "--line-regexp" }, description = "Select only those matches that exactly match the whole line.")
    private boolean lineRegexp;

    protected Object doExecute() throws Exception {
        String regexp = regex;
        if (wordRegexp) {
            regexp = "\\b" + regexp + "\\b";
        }
        if (lineRegexp) {
            regexp = "^" + regexp + "$";
        } else {
            regexp = ".*" + regexp + ".*";
        }
        Pattern p = Pattern.compile(regexp);
        try {
            int lineno = 1;
            String line;
            while ((line = readLine(io.in)) != null) {
                if (p.matcher(line).matches() ^ invertMatch) {
                    if (lineNumber) {
                        String gutter = StringUtils.leftPad(String.valueOf(lineno), 6);
                        io.out.print(gutter);
                        io.out.print("  ");
                    }
                    io.out.println(line);
                    lineno++;
                }
            }
        } catch (IOException e) {
        }
        return null;
    }

    private String readLine(Reader in) throws IOException {
        StringBuffer buf = new StringBuffer();
        while (true) {
            int i = in.read();
            if (i == -1 && buf.length() == 0) {
                throw new IOException("break");
            }
            if (i == -1 || i == '\n' || i == '\r') {
                return buf.toString();
            }
            buf.append((char) i);
        }
    }

}
