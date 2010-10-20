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

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 */
public class GShellApplicationContext extends ClassPathXmlApplicationContext {

    private static final String DEFAULT_JOSSO_GSHELL_CFG_FILE = "classpath*:META-INF/spring/josso-gshell.xml";

    protected final transient Log logger = LogFactory.getLog(getClass());

    private DefaultNamespaceHandlerResolver nsHandlerResolver;
    // private boolean includeDefaults;
    private String[] cfgFiles;
    private URL[] cfgFileURLs;

    public GShellApplicationContext(String cf) {
        this(cf, null);
    }

    public GShellApplicationContext(String[] cfs) {
        this(cfs, null);
    }

    public GShellApplicationContext(URL url) {
        this(url, null);
    }
    public GShellApplicationContext(URL[] urls) {
        this(urls, null);
    }

    public GShellApplicationContext(String cf, ApplicationContext parent) {
        this(new String[] {cf}, parent);
    }

    public GShellApplicationContext(URL url, ApplicationContext parent) {
        this(new URL[] {url}, parent);
    }
    public GShellApplicationContext(String[] cf, ApplicationContext parent) {
        super(new String[0], false, parent);
        cfgFiles = cf;
        refresh();
    }


    public GShellApplicationContext(URL[] url, ApplicationContext parent) {
        super(new String[0], false, parent);
        cfgFileURLs = url;
        refresh();
    }

    @Override
    protected Resource[] getConfigResources() {

        List<Resource> resources = new ArrayList<Resource>();

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread
                .currentThread().getContextClassLoader());

            Collections.addAll(resources, resolver.getResources(DEFAULT_JOSSO_GSHELL_CFG_FILE));

        } catch (IOException ex) {
            // ignore
        }

        if (null == cfgFiles) {
            cfgFiles = new String[] { "josso-gshell.xml" };
        }

        for (String cfgFile : cfgFiles) {
            boolean found = false;
            Resource cpr = new ClassPathResource(cfgFile);
            if (!cpr.exists()) {
                try {
                    //see if it's a URL
                    URL url = new URL(cfgFile);
                    cpr = new UrlResource(url);
                    if (cpr.exists()) {
                        resources.add(cpr);
                        found = true;
                    }
                } catch (MalformedURLException e) {
                    //ignore
                }
                if (!found) {
                    //try loading it our way
                    URL url = getResource(cfgFile, this.getClass());
                    if (url != null) {
                        cpr = new UrlResource(url);
                        if (cpr.exists()) {
                            resources.add(cpr);
                            found = true;
                        }
                    }
                }
            } else {
                resources.add(cpr);
                found = true;
            }
            if (!found) {
                logger.warn("No Process Descriptor found: " + cfgFile);
            }
        }

        if (null != cfgFileURLs) {
            for (URL cfgFileURL : cfgFileURLs) {
                UrlResource ur = new UrlResource(cfgFileURL);
                if (ur.exists()) {
                    resources.add(ur);
                } else {
                    logger.warn("No Process Descriptor found: " + cfgFileURL);
                }
            }
        }

        logger.info("Creating application context with resources: " + resources);

        if (0 == resources.size()) {
            return null;
        }

        Resource[] res = new Resource[resources.size()];
        res = resources.toArray(res);
        return res;
    }

    /**
     * Load a given resource. <p/> This method will try to load the resource
     * using the following methods (in order):
     * <ul>
     * <li>From Thread.currentThread().getContextClassLoader()
     * <li>From ClassLoaderUtil.class.getClassLoader()
     * <li>callingClass.getClassLoader()
     * </ul>
     *
     * @param resourceName The name of the resource to load
     * @param callingClass The Class object of the calling object
     */
    private URL getResource(String resourceName, Class callingClass) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (url == null && resourceName.startsWith("/")) {
            //certain classloaders need it without the leading /
            url = Thread.currentThread().getContextClassLoader()
                .getResource(resourceName.substring(1));
        }

        if (url == null) {
            ClassLoader cl = callingClass.getClassLoader();

            if (cl != null) {
                url = cl.getResource(resourceName);
            }
        }

        if (url == null) {
            url = callingClass.getResource(resourceName);
        }

        if ((url == null) && (resourceName != null) && (resourceName.charAt(0) != '/')) {
            return getResource('/' + resourceName, callingClass);
        }

        return url;
    }

}

