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

package org.josso.auth.scheme.test;

import org.springframework.context.ApplicationContext;
import org.josso.auth.scheme.RememberMeAuthScheme;
import org.josso.auth.Credential;
import org.junit.Test;
import org.junit.Before;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 10, 2008
 * Time: 4:54:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class RememberMeAuthSchemeTest  {

    private static Log log = LogFactory.getLog(RememberMeAuthSchemeTest.class);

    protected ApplicationContext applicationContext;

    @Before
    public void initAppContext() {
        applicationContext = new ClassPathXmlApplicationContext("/org/josso/auth/scheme/test/josso-rememberme-spring.xml");
    }

    @Test
    public void testRememberMe() throws Exception {

        RememberMeAuthScheme scheme = (RememberMeAuthScheme) applicationContext.getBean("josso-rememberme-authentication");
        assert scheme != null : "No authentication scheme configured";

        String tokenValue = scheme.getRemembermeTokenForUser("user1");

        Credential token = scheme.newCredential(RememberMeAuthScheme.REMEMBER_ME_TOKEN_CREDENTIAL_NAME, tokenValue);
        assert token != null : "No 'token' Credential created by provider";

        Credential username = scheme.newCredential(RememberMeAuthScheme.USERNAME_CREDENTIAL_NAME, "user1");
        assert username != null : "No 'username' Credential created by provider";

        Subject s = new Subject();
        scheme.initialize(new Credential[] {token, username}, s);

        scheme.authenticate();
        scheme.confirm();

        assert s.getPrincipals().size() == 1 : "Expected one principal, got : " + s.getPrincipals().size();

        Principal user = s.getPrincipals().iterator().next();
        assert user.getName().equals("user1") : "Expected user1 principal, got : " + user.getName();
    }
}
