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

package org.josso.gateway.identity.service.store.virtual.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.josso.auth.BaseCredential;
import org.josso.auth.Credential;
import org.josso.auth.CredentialKey;
import org.josso.auth.scheme.UsernamePasswordCredentialProvider;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.BaseRoleImpl;
import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.store.SimpleUserKey;
import org.josso.gateway.identity.service.store.virtual.VirtualIdentityStore;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Uni Test component for testing virtual directory privitives.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: VirtualIdentityStoreTest.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class VirtualIdentityStoreTest {
    private static final Log logger = LogFactory.getLog(VirtualIdentityStoreTest.class);

    private static VirtualIdentityStore vis;

    @BeforeClass
    public static void prepareTest() throws Exception {
        prepareIdentityStore();
    }

    @AfterClass
    public static void tearDownTest() throws Exception {

    }

    @Test
    public void testLoadUser() throws Exception {
        final SimpleUserKey uk = new SimpleUserKey("user1");
        BaseUser bu = vis.loadUser(uk);
        assert bu != null : "cannot load user " + uk.getId();
        assert bu.getName().equals(uk.getId()) : "expected user \"" + uk.getId() + "\" and got \"" + bu.getName() + "\" instead";

        List<String> phoneNumbers = new ArrayList<String>();
        for (SSONameValuePair nvp : bu.getProperties()) {
            if (nvp.getName().equals("user.phoneNumber"))
                phoneNumbers.add(nvp.getValue());
        }

        assert phoneNumbers.size() == 2 : "Only " + phoneNumbers.size() + " phone numbers found instead of 2";
    }

    @Test
    public void testRolesByUser() throws Exception {
        final SimpleUserKey uk = new SimpleUserKey("user1");
        BaseRole[] brs = vis.findRolesByUserKey(uk);

        Collection<BaseRole> brc = Arrays.asList(brs);

        assert brc.size() == 3 : "expected 3 roles while received " + brc.size();
        assert brc.contains(new BaseRoleImpl("role1"));
        assert brc.contains(new BaseRoleImpl("role2"));
        assert brc.contains(new BaseRoleImpl("role3"));
    }

    @Test
    public void testLoadCredentials() throws Exception {
        final CredentialKey uk = new SimpleUserKey("user1");

        Credential[] cs = vis.loadCredentials(uk, new UsernamePasswordCredentialProvider());

        Collection<Credential> csc = Arrays.asList(cs);

        assert csc.size() == 3 : "expected 3 credentials while received " + csc.size();

        assert csc.contains(new BaseCredential("user1"));
        assert csc.contains(new BaseCredential("user1pwd"));
        assert csc.contains(new BaseCredential("user1pwd2"));

    }

    @Test
    public void testLoadUID() throws Exception {
        final CredentialKey uk = new SimpleUserKey("user1");

        String uid = vis.loadUID(uk, new UsernamePasswordCredentialProvider());

        assert uid.equals("dc=org,dc=josso,uid=user1");

    }

    private static void prepareIdentityStore() throws Exception {
        ApplicationContext factory = new ClassPathXmlApplicationContext("virtual-identity-store-1.xml");
        vis = (VirtualIdentityStore) factory.getBean("test-vis");
        assert vis != null : "could not create Virtual Identity Store";
    }
}