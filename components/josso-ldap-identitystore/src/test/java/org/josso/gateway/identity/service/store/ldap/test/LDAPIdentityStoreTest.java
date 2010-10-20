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

package org.josso.gateway.identity.service.store.ldap.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.directory.server.configuration.ApacheDS;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.josso.auth.Credential;
import org.josso.auth.CredentialKey;
import org.josso.auth.BaseCredential;
import org.josso.auth.scheme.PasswordCredential;
import org.josso.auth.scheme.UsernameCredential;
import org.josso.auth.scheme.UsernamePasswordCredentialProvider;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.store.SimpleUserKey;
import org.josso.gateway.identity.service.store.ldap.LDAPIdentityStore;
import org.josso.selfservices.ChallengeResponseCredential;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.File;

/**
 * User: <a href=mailto:ajadzinsky@atricor.org>ajadzinsky</a>
 * Date: Dec 4, 2008
 * Time: 12:55:48 PM
 */
public class LDAPIdentityStoreTest {
    private static final Log logger = LogFactory.getLog( LDAPIdentityStoreTest.class );

    private static ApacheDS apacheDS;
    private static LDAPIdentityStore ldap;

    @BeforeClass
    public static void prepareTest () throws Exception {
        logger.debug( "preparing test..." );
        configureDirectoryService();

        if ( !apacheDS.isStarted() )
            apacheDS.startup();

        prepareIdentityStore();

    }

    @AfterClass
    public static void tearDownTest () throws Exception {
        logger.debug( "teraing down test..." );
        if ( apacheDS.isStarted() )
            apacheDS.shutdown();
    }

    @Test
    public void testLoadUser () throws Exception {
        final SimpleUserKey uk = new SimpleUserKey( "user1" );
        BaseUser bu = ldap.loadUser( uk );
        assert bu != null : "can not load user " + uk.getId();
        assert bu.getName().equals( uk.getId() ) : "expected user name \"" + uk.getId() + "\" got \"" + bu.getName() + "\"";

        for( SSONameValuePair nvp : bu.getProperties()){
            if( nvp.getName().equals( "description" ) )
                assert nvp.getValue().equals( "User1 CN" );

            if( nvp.getName().equals( "mail" ) )
                assert nvp.getValue().equals( "user1@josso.org" );

            if( nvp.getName().equals( "josso.user.dn" ) )
                assert nvp.getValue().equals( "uid=user1,ou=People,dc=my-domain,dc=com" );
        }
    }

    @Test
    public void testRolesByUser() throws Exception {
        final SimpleUserKey uk = new SimpleUserKey( "user1" );
        BaseRole[] brs = ldap.findRolesByUserKey( uk );
        assert brs.length == 2 : "expected 2 roles got " + brs.length;
        assert brs[0].getName().equals( "role1" ) : "expected role \"role1\" got " + brs[0].getName();
        assert brs[1].getName().equals( "role2" ) : "expected role \"role2\" got " + brs[1].getName();
    }

    @Test
    public void testLoadCredentials() throws Exception {
        final CredentialKey uk = new SimpleUserKey( "user1" );

        Credential[] cs = ldap.loadCredentials( uk, new UsernamePasswordCredentialProvider() );
        assert cs.length == 2 : "expected 2 credentials got " + cs.length;
        for(Credential c : cs){
            if(UsernameCredential.class.isInstance( c ))
                assert ((UsernameCredential)c).getValue().toString().equals( "user1" ) : "expected User Credential \"user1\" got \"" + ((UsernameCredential)c).getValue() + "\"";

            if(PasswordCredential.class.isInstance( c ))
                assert ((PasswordCredential)c).getValue().toString().equals( "user1pwd" ) : "expected Password Credential \"user1pwd\" got \"" + ((PasswordCredential)c).getValue() + "\"";
        }
    }

    @Test
    public void testResetCredential() throws Exception {
        final SimpleUserKey uk = new SimpleUserKey( "user1" );
        final BaseCredential bc = new BaseCredential( "pwd1Changed" );

        ldap.updateAccountPassword( uk, bc );

        Credential[] cs = ldap.loadCredentials( uk, new UsernamePasswordCredentialProvider() );

        for(Credential c : cs){
            if(UsernameCredential.class.isInstance( c ))
                assert ((UsernameCredential)c).getValue().toString().equals( "user1" ) : "expected User Credential \"user1\" got \"" + ((UsernameCredential)c).getValue() + "\"";

            if(PasswordCredential.class.isInstance( c ))
                assert ((PasswordCredential)c).getValue().equals( bc.getValue() ) : "expected Password Credential \"" + bc.getValue() + "\" got \"" + ((PasswordCredential)c).getValue() + "\"";
        }
    }

    @Test
    public void testUsernameByRelayPassword() throws Exception {
        ChallengeResponseCredential cred = new ChallengeResponseCredential( "mail", null );
        cred.setResponse( "user1@josso.org" );

        String username = ldap.loadUsernameByRelayCredential( cred );
        assert username.equals( "user1" );
    }

    private static void configureDirectoryService () throws Exception {
        ApplicationContext factory = new ClassPathXmlApplicationContext( "META-INF/spring/josso-apacheds.xml" );

        apacheDS = (ApacheDS) factory.getBean( "apacheDS" );
        assert apacheDS != null : "could not create ApacheDS";

        File workingDirFile = new File( "target/ads" );
        if( workingDirFile.exists() )
            deletWorkingDirectory( workingDirFile );
        
        apacheDS.getDirectoryService().setWorkingDirectory( workingDirFile );

        String ldifPath = "src/test/resources/org/josso/gateway/identity/service/store/ldap/test";

        File ldif_dir = new File( ldifPath );
        apacheDS.setLdifDirectory( ldif_dir );
    }

    private static void deletWorkingDirectory(File dir){
        if( dir.isDirectory() ){
            for( File f : dir.listFiles() ){
                deletWorkingDirectory( f );
            }
        }
        if(!dir.delete())
            logger.error( "could not delet " + dir.getPath() );
    }

    private static void prepareIdentityStore() throws Exception {
        ApplicationContext factory = new ClassPathXmlApplicationContext( "META-INF/spring/ldap-identity-store.xml" );
        ldap = (LDAPIdentityStore)factory.getBean( "josso-ldap-store" );
        assert ldap != null : "could not create LDAPIdentityStore";
    }
}
