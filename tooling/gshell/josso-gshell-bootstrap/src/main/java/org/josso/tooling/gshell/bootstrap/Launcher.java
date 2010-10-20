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

package org.josso.tooling.gshell.bootstrap;

import java.io.File;

/**
 * Platform independent launcher to setup common configuration and delegate to
 * the Classworlds launcher.
 *
 * @version $Rev: 974 $ $Date: 2009-01-13 22:39:45 -0200 (Tue, 13 Jan 2009) $
 */
public class Launcher
{
    private static boolean debug = Boolean.getBoolean(Launcher.class.getName() + ".debug");

    private static String programName;

    private static File homeDir;

    public static void main(final String[] args) throws Exception {
        assert args != null;

        //
        // NOTE: Branding information is not available here, so we must use the basic GShell properties to configure
        //       the bootstrap loader.
        //

        programName = getProgramName();
        setProperty("program.name", programName);

        homeDir = getHomeDir();
        setProperty("josso-gsh.home", homeDir.getCanonicalPath());

        File classworldsConf = getClassworldsConf();
        setProperty("classworlds.conf", classworldsConf.getCanonicalPath());

        File log4jConf = getLog4jConf();
        setProperty("log4j.configuration", log4jConf.toURI().toURL().toString());

        // Delegate to the Classworlds launcher to finish booting
        org.codehaus.plexus.classworlds.launcher.Launcher.main(args);
    }

    private static void debug(final String message) {
        if (debug) {
            System.err.println("[DEBUG] " + message);
        }
    }

    private static void warn(final String message) {
        System.err.println("[WARNING] " + message);
    }

    private static void setProperty(final String name, final String value) {
        System.setProperty(name, value);
        debug(name + "=" + value);
    }

    private static String getProgramName() {
        String name = System.getProperty("program.name");
        if (name == null) {
            name = "josso-gsh";
        }

        return name;
    }

    private static File getHomeDir() throws Exception {
        String path = System.getProperty("josso-gsh.home");
        File dir;

        if (path == null) {
            String jarPath = Launcher.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            jarPath = java.net.URLDecoder.decode(jarPath);

            // The jar containing this class is expected to be in <gshell.home>/lib/boot
            File bootJar = new File(jarPath);
            dir = bootJar.getParentFile().getParentFile().getParentFile().getCanonicalFile();
        }
        else {
            dir = new File(path).getCanonicalFile();
        }

        return dir;
    }

    private static File getClassworldsConf() throws Exception {
        String path = System.getProperty("classworlds.conf");
        File file;

        if (path == null) {
            file = new File(homeDir, "etc/" + programName + "-classworlds.conf");
        }
        else {
            file = new File(path).getCanonicalFile();
        }

        return file;
    }

    private static File getLog4jConf() throws Exception {
        String path = System.getProperty("log4j.configuration");
        File file;

        if (path == null) {
            file = new File(homeDir, "etc/" + programName + "-log4j.properties");
        }
        else {
            file = new File(path).getCanonicalFile();
        }

        return file;
    }
}
