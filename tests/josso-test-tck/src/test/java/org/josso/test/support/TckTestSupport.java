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

package org.josso.test.support;

import org.junit.Before;
import org.junit.After;
import org.springframework.context.ApplicationContext;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.mortbay.jetty.Server;

import java.io.IOException;
import java.util.Map;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Dec 5, 2008
 * Time: 1:27:55 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class TckTestSupport implements org.josso.gateway.signon.Constants {

    private static final Log log = LogFactory.getLog(TckTestSupport.class);

    private static Set<String> testIds = new HashSet<String>();

    // This should start and stop jetty on demand, using spring !

    protected ApplicationContext ctx;

    protected HttpClient client;

    @Before
    public void setup() {

        String tckTestId = this.getTckId();
        assert !testIds.contains(tckTestId) : "Test ID : " + tckTestId + " has already been used!";

        String springDescriptor = "/tck-spring-" + getTckId()+ ".xml";

        log.info("-------------------------------------------------------------------------------");
        log.info("setup TCK : ["+tckTestId +"] from " + springDescriptor);
        log.info("-------------------------------------------------------------------------------");
        log.info("");
        log.info("Use the 'org.josso.test.tck.block' System property to keep the server up until ");
        log.info("pressing Ctrl+C");
        log.info("");
        log.info("-------------------------------------------------------------------------------");

        assert tckTestId  != null : "TCK Test ID cannot be null.";


        // Export some properties needed by test cases and bean definitions:

        setSystemProperty("josso.tck.id", tckTestId);
        setSystemProperty("josso.tck.basedir", "./target/tck-"+tckTestId+".dir/tck/");
        setSystemProperty("java.security.auth.login.config", "target/tck-"+tckTestId+".dir/tck/etc/jaas.conf");
        setSystemProperty("josso.tck.serverPort", "8181");

        log.info("-------------------------------------------------------------------------------");

        // Start spring context
        ctx = new ClassPathXmlApplicationContext( new String[] { springDescriptor } );

        // Create new HTTP Client
        this.client = doMakeClient();

    }

    @After
    public void tearDown() {
        if (blockTest()) {
            synchronized (this) {
                log.info("");
                log.info("tearDown TCK : ["+this.getTckId()+"] : BLOCKING TEST ...");
                log.info("-------------------------------------------------------------------------------");
                try { wait(); } catch (InterruptedException e) { /**/ }
            }
        }

        log.info("");
        log.info("tearDown TCK : ["+this.getTckId()+"]");
        log.info("-------------------------------------------------------------------------------");

        try {

            Server s = getServer();
            s.stop();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // Just in case ....
            client = null;
            ctx = null;
        }

    }

    /**
     * Subclasses should provide a unique test id
     */
    protected abstract String getTckId() ;

    protected void setSystemProperty(String name, String value) {
        log.info("System:" + name + "=" + value);
        System.setProperty(name, value);
    }

    protected String getProperty(String name) {
        return System.getProperty(name);
    }

    protected boolean blockTest() {

        String p = System.getProperty("org.josso.test.tck.block");
        if (p != null && Boolean.parseBoolean(p))
            return true;

        return false;
    }

    protected Server getServer() {
        Collection servers = ctx.getBeansOfType(Server.class).values();
        if (servers == null || servers.size() != 1) {
            log.warn("Too many/Too few Jetty Servers !");
            return null;
        }
        return (Server) servers.iterator().next();
    }

    /**
     * Take a look at http://hc.apache.org/httpclient-3.x/preference-api.html for more details
     */
    protected HttpClient doMakeClient() {
        HttpClient  c = new HttpClient();
        c.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        c.getParams().setBooleanParameter("http.protocol.allow-circular-redirects", true);
        return c;
    }

    protected HttpClient getClient() {
        return client;
    }

    /**
     * Simplified doGet method
     */
    protected GetMethod doGet(String url) throws IOException {
        GetMethod getMethod = new GetMethod(url);
        this.getClient().executeMethod(getMethod);
        return getMethod;
    }

    /**
     * Simplified doPost method
     */
    protected PostMethod doPost(String url, NameValuePair ... params) throws IOException {
        PostMethod postMethod = new PostMethod(url);
        postMethod.setRequestBody(params);

        this.getClient().executeMethod(postMethod);

        return postMethod;
    }
    
    protected PostMethod doPost(String url, String referer, NameValuePair ... params) throws IOException {
        PostMethod postMethod = new PostMethod(url);
        postMethod.setRequestBody(params);
        postMethod.addRequestHeader("REFERER", referer);

        this.getClient().executeMethod(postMethod);

        return postMethod;
    }     

    
}
