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

package org.josso.tooling.gshell.core.cli;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.josso.tooling.gshell.core.spring.GShell;
import org.josso.tooling.gshell.core.spring.SpringCommandRegistry;
import org.josso.tooling.gshell.core.spring.GShellListener;
import org.josso.tooling.gshell.core.spring.GShellApplicationContext;
import org.josso.tooling.gshell.core.support.JOSSOCommandSupport;
import org.apache.geronimo.gshell.registry.DuplicateRegistrationException;
import org.apache.geronimo.gshell.registry.RegistryException;
import org.apache.geronimo.gshell.ExitNotification;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.classworlds.ClassWorld;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 7, 2008
 * Time: 5:39:34 PM
 * To change this template use File | Settings | File Templates.
 *
 */
public class Main implements GShellListener {

    private static final Log log = LogFactory.getLog(Main.class);

    ///CLOVER:OFF

    //
    // NOTE: Do not use logging from this class, as it is used to configure
    //       the logging level with System properties, which will only get
    //       picked up on the initial loading of Log4j
    //

    private final ClassWorld classWorld;

    private ApplicationContext ctx;

    public Main(final ClassWorld classWorld) {
        assert classWorld != null;
        this.classWorld = classWorld;

        ctx = new GShellApplicationContext( new String[] { "/META-INF/spring/josso-gshell-core.xml"} );
    }


    private void setConsoleLogLevel(final String level) {
        System.setProperty("gshell.log.console.level", level);
    }

    public void boot(String[] args) {

        try {

            setConsoleLogLevel("WARN");

            // Setup a refereence for our exit code so our callback thread can tell if we've shutdown normally or not
            final AtomicReference<Integer> codeRef = new AtomicReference<Integer>();
            int code = ExitNotification.DEFAULT_CODE;

            Runtime.getRuntime().addShutdownHook(new Thread("JOSSO GShell Shutdown Hook") {
                public void run() {
                    if (codeRef.get() == null) {
                        // Give the user a warning when the JVM shutdown abnormally, normal shutdown
                        // will set an exit code through the proper channels
                        log.error("WARNING: Abnormal JVM shutdown detected");
                    }

                }
            });

            // shell reference
            GShell sh = (GShell) ctx.getBean("gshell");

            Map cmds = ctx.getBeansOfType(JOSSOCommandSupport.class);
            SpringCommandRegistry registry = (SpringCommandRegistry) ctx.getBean("commandRegistry");
            for (Object o : cmds.values()) {
                JOSSOCommandSupport cmd = (JOSSOCommandSupport) o;
                try {
                    Map<String, Object> properties = new java.util.HashMap<String, Object>();
                    if (cmd.getShell() != null)
                        properties.put("shell", cmd.getShell());

                    registry.register(cmd, properties);
                } catch (DuplicateRegistrationException e) {
                    log.error(e.getMessage(), e);
                } catch (RegistryException e) {
                    log.error(e.getMessage(), e);
                }
            }


            // Start our shell, this should start a thread!
            sh.start();

            codeRef.set(code);

            //System.exit(code);

        } catch (Throwable t) {
            log.error("Main:" + t.getMessage(), t);
        }
        

    }

    public static void main(final String[] args, final ClassWorld world) throws Exception {
        Main main = new Main(world);
        main.boot(args);

        // When running this as a main class, add a hook to wait until
        // shell reference
        GShell sh = (GShell) main.ctx.getBean("gshell");
        sh.registerListener(main);
        // Wait until someone weaks this threda
        synchronized(main) {
            main.wait();
        }
    }

}
