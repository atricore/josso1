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

package org.josso.tooling.gshell.install.test;

import org.junit.Test;
import org.junit.Before;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileObject;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Dec 29, 2008
 * Time: 5:30:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class VFSTest {

    private static final Log log = LogFactory.getLog(VFSTest.class);

    protected ApplicationContext ctx;

    protected FileSystemManager fsm;

    protected MockInstaller installer;


    @Before
    public void setup() {
        // Start spring context
        ctx = new ClassPathXmlApplicationContext( new String[] { "josso-gshell-test.xml" } );

        installer = (MockInstaller) ctx.getBean("test-installer");
        fsm = installer.getFSMgr();
        log.debug("Got FS Manager instance");

    }

    @Test
    public void testUnjarExploded() throws Exception {

        FileObject home = fsm.resolveFile(System.getProperty("java.home"));
        assert home.exists() : "JAVA_HOME folder not found ? " + home.getName().getFriendlyURI();

        FileObject target = fsm.resolveFile(new File(".").getAbsolutePath() + "/target");
        assert target.exists() : "target folder not found : " + target.getName().getFriendlyURI();

        FileObject targetFolder = target.resolveFile("unjar");
        targetFolder.createFolder();

        FileObject srcFile = home.resolveFile("./lib/rt.jar");
        assert srcFile.exists() : "File not found " + srcFile.getURL();

        log.info("JAR FILE    : " + srcFile.getName().getFriendlyURI());
        log.info("ROOT        : " + srcFile.getName().getRoot());
        log.info("ROOT URI    : " + srcFile.getName().getRootURI());
        log.info("PATH DECODED: " + srcFile.getName().getPathDecoded());
        log.info("ROOT DECODED: " + srcFile.getName().getRoot().getPathDecoded());
        log.info("PATH        : " + srcFile.getName().getPath());

        assert installer.installJar(srcFile, targetFolder, "rt.jar-exploded", true, true): "Cannot unjar exploded !";

    }

    @Test
    public void testUnjar() throws Exception {

        FileObject home = fsm.resolveFile(System.getProperty("java.home"));
        assert home.exists() : "JAVA_HOME folder not found ? " + home.getName().getFriendlyURI();

        FileObject target = fsm.resolveFile(new File(".").getAbsolutePath() + "/target");
        assert target.exists() : "target folder not found : " + target.getName().getFriendlyURI();

        FileObject targetFolder = target.resolveFile("unjar");
        targetFolder.createFolder();

        FileObject srcFile = home.resolveFile("./lib/rt.jar");
        assert srcFile.exists() : "File not found " + srcFile.getURL();

        assert installer.installJar(srcFile, targetFolder, "rt.jar", false, true): "Cannot unjar !";

    }

}
