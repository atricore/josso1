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

package org.josso.test.tck;

import org.josso.test.support.TckTestSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Test;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 */
public class Tck003Test extends TckTestSupport {

    public static final Log log = LogFactory.getLog(Tck002Test.class);

    @Test
    public void runTck() throws Exception {
        doGetProtected1Delegated();
        doPostCredentials();
        doGetProtected2Delegated();
    }

    protected void doGetProtected1Delegated() throws Exception {
        // This will be redirected until the gateway login form is presented
        HttpMethod getMethod = doGet("http://localhost:"+getProperty("josso.tck.serverPort")+"/webapp1/protected-delegated.jsp");
        String body = getMethod.getResponseBodyAsString();

        assert body.indexOf("josso_username") > 0 : "No 'josso_username' field received in response";
        assert body.indexOf("josso_password") > 0 : "No 'josso_password' field received in response";


    }

    protected void doPostCredentials() throws Exception {
        NameValuePair username= new NameValuePair("josso_username", "user1");
        NameValuePair password = new NameValuePair("josso_password", "user1pwd");
        NameValuePair cmd = new NameValuePair(PARAM_JOSSO_CMD, "login");

        PostMethod postMethod = doPost("http://localhost:"+getProperty("josso.tck.serverPort")+"/josso/signon/usernamePasswordLogin.do", username, password, cmd);
        int status = postMethod.getStatusCode();
        assert status == HttpStatus.SC_MOVED_TEMPORARILY : "Unexpected HTTP status " + status;

        Header location = postMethod.getResponseHeader("Location");
        location.getValue();
        HttpMethod getMethod = doGet(location.getValue());
        status = getMethod.getStatusCode();

        assert status == HttpStatus.SC_OK : "Unexpected HTTP status " + status;

        String body = getMethod.getResponseBodyAsString();

        assert body.indexOf("JOSSO_SESSIONID=") > 0 : "No JOSSO_SESSIONID= recived in response boyd";
        assert body.indexOf("This is a simple JSP") > 0  : "No sample text found in response body";
        assert body.indexOf("role1") > 0 : "Role1 not found in response body";

    }

    protected void doGetProtected2Delegated() throws Exception {
        // This will be redirected until the gateway login form is presented
        HttpMethod getMethod = doGet("http://localhost:"+getProperty("josso.tck.serverPort")+"/webapp2/protected-delegated.jsp");
        String body = getMethod.getResponseBodyAsString();

        assert body.indexOf("JOSSO_SESSIONID=") > 0 : "No JOSSO_SESSIONID= recived in response boyd";
        assert body.indexOf("This is a simple JSP") > 0  : "No sample text found in response body";
        assert body.indexOf("role1") > 0 : "Role1 not found in response body";

    }


    protected String getTckId() {
        return "003";
    }
}

