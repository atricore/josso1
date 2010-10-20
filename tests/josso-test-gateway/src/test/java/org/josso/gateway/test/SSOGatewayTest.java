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

package org.josso.gateway.test;

import org.junit.Test;
import org.junit.Before;
import org.josso.Lookup;
import org.josso.auth.scheme.UsernameCredential;
import org.josso.auth.scheme.PasswordCredential;
import org.josso.auth.Credential;
import org.josso.gateway.SSOGateway;
import org.josso.gateway.SSOContext;
import org.josso.gateway.assertion.AuthenticationAssertion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Rev: 974 $ $Date: 2009-01-13 22:39:45 -0200 (Tue, 13 Jan 2009) $
 */
public class SSOGatewayTest  {

    private static Log logger = LogFactory.getLog(SSOGatewayTest.class);

    private Lookup lkp = Lookup.getInstance();

    @Before
    public void initLookup() {
        lkp.init("/org/josso/gateway/test/josso-gateway-config.xml");
    }

    @Test
    public void init() throws Exception {
        SSOGateway gwy = Lookup.getInstance().lookupSSOGateway();
    }


    @Test
    public void authBasic() throws Exception {

        java.net.URL url = getClass().getResource("/josso-credentials.xml");

        logger.info(url);


        SSOGateway gwy = Lookup.getInstance().lookupSSOGateway();

        gwy.prepareDefaultSSOContext(); //No security domain used here 

        Credential[] creds = new Credential[] { new UsernameCredential("user1"), new PasswordCredential("user1pwd")};

        AuthenticationAssertion a = gwy.assertIdentity(creds, "basic-authentication");


    }
}
