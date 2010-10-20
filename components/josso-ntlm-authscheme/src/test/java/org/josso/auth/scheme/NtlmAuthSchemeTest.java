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

package org.josso.auth.scheme;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.josso.auth.Credential;
import org.josso.gateway.protocol.SSOProtocolManagerImpl;
import org.josso.gateway.protocol.SSOProtocolManager;
import org.josso.gateway.protocol.handler.ProtocolHandler;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.Subject;
import java.security.Principal;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;

/**
 * User: <a href=mailto:ajadzinsky@atricor.org>ajadzinsky</a>
 * Date: Nov 25, 2008
 * Time: 2:11:37 PM
 */
public class NtlmAuthSchemeTest {
    private static final Log log = LogFactory.getLog( NtlmAuthSchemeTest.class );

    protected ApplicationContext applicationContext;

    @Before
    public void initAppContext() {
        log.debug( "creating context..." );
        applicationContext = new ClassPathXmlApplicationContext("/org/josso/auth/scheme/josso-ntlmscheme-spring.xml");
    }

    @Test
    public void dummy() {

    }

//    @Test
    public void testNtlmAuth() throws Exception {
        log.debug( "geting bean NtlmAuthScheme..." );
        NtlmAuthScheme scheme = (NtlmAuthScheme) applicationContext.getBean("josso-ntlm-authentication");
        assert scheme != null : "No authentication scheme configured";

        Credential domainCredential = scheme.newCredential( NtlmCredentialProvider.DOMAIN_CONTROLLER_CREDENTIAL, UniAddress.getByName( "130.5.5.233" ));
        Credential passCredential = scheme.newCredential( NtlmCredentialProvider.PASSWORD_AUTHENTICATION_CREDENTIAL, new NtlmPasswordAuthentication("NT-DOMAIN", "Administrator", "novascope") );
        Subject s = new Subject();
        scheme.initialize( new Credential[]{domainCredential, passCredential}, s );

        scheme.authenticate();
        scheme.confirm();

        assert s.getPrincipals().size() == 1 : "Expected one principal, got : " + s.getPrincipals().size();

        Principal user = s.getPrincipals().iterator().next();
        assert user.getName().equals("Administrator") : "Expected Administrator principal, got : " + user.getName();
    }
}
