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

package org.josso.gateway.identity.service.store.db.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.BeforeClass;
import org.junit.Test;
import org.josso.gateway.identity.service.store.db.JDBCIdentityStore;
import org.josso.gateway.identity.service.store.SimpleUserKey;
import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.auth.CredentialKey;
import org.josso.auth.Credential;
import org.josso.auth.BaseCredential;
import org.josso.auth.scheme.UsernamePasswordCredentialProvider;
import org.josso.auth.scheme.UsernameCredential;
import org.josso.auth.scheme.PasswordCredential;
import org.josso.selfservices.ChallengeResponseCredential;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * User: <a href=mailto:ajadzinsky@atricor.org>ajadzinsky</a>
 * Date: Dec 3, 2008
 * Time: 6:33:23 PM
 */
public class JDBCIdentityStoreTest {
    private static final Log logger = LogFactory.getLog( JDBCIdentityStoreTest.class );

    protected static JDBCIdentityStore db;

    @BeforeClass
    public static void beforeTest () throws Exception {
        ApplicationContext ctxt = new ClassPathXmlApplicationContext( "org/josso/gateway/identity/service/store/db/test/hsdb-identity-store.xml" );
        db = (JDBCIdentityStore) ctxt.getBean( "dbStore" );

        JdbcTemplate template = new JdbcTemplate( (DataSource) ctxt.getBean( "dataSource" ) );
        createTables( template );
        insertData( template );
    }

    @Test
    public void testSelectUser() throws Exception {
        final SimpleUserKey uk = new SimpleUserKey( "user1" );
        BaseUser bu = db.loadUser( uk );
        assert bu != null : "can not load user " + uk.getId();
        assert bu.getName().equals( "User 1 full name" ) : "expected user name \"User 1 full name\" got \"" + bu.getName() + "\"";
        assert bu.getProperties().length == 3 : "expected 3 properties got " + bu.getProperties().length;
    }

    @Test
    public void testRolesByUser() throws Exception {
        final SimpleUserKey uk = new SimpleUserKey( "user1" );
        BaseRole[] brs = db.findRolesByUserKey( uk );
        assert brs.length == 2 : "expected 2 roles got " + brs.length;
        assert brs[0].getName().equals( "role1" ) : "expected role \"role1\" got " + brs[0].getName();
        assert brs[1].getName().equals( "role2" ) : "expected role \"role2\" got " + brs[1].getName();
    }

    @Test
    public void testLoadCredentials() throws Exception {
        final CredentialKey uk = new SimpleUserKey( "user1" );

        Credential[] cs = db.loadCredentials( uk, new UsernamePasswordCredentialProvider() );
        assert cs.length == 2 : "expected 2 credentials got " + cs.length;
        assert UsernameCredential.class.isInstance( cs[0] ) : "expected UsernameCredential class got " + cs[0].getClass().getName();
        assert PasswordCredential.class.isInstance( cs[1] ) : "expected PasswordCredential class got " + cs[1].getClass().getName();
    }

    @Test
    public void testResetCredential() throws Exception {
        final SimpleUserKey uk = new SimpleUserKey( "user1" );
        final BaseCredential bc = new BaseCredential( "pwd1Changed" );

        db.updateAccountPassword( uk, bc );

        Credential[] cs = db.loadCredentials( uk, new UsernamePasswordCredentialProvider() );
        boolean changed = false;
        for(Credential c : cs){
            if( PasswordCredential.class.isInstance( c ) ){
                changed = ((PasswordCredential)c).getValue().equals( bc.getValue() );
            }
        }
        assert changed : "Credential was not updated";
    }

    @Test
    public void testUsernameByRelayPassword() throws Exception {
        ChallengeResponseCredential cred = new ChallengeResponseCredential( "E_MAIL", null );
        cred.setResponse( "user1@josso.org" );
        String username = db.loadUsernameByRelayCredential( cred );
        assert username.equals( "user1" );

        cred.setResponse( "user3@josso.org" );
        username = db.loadUsernameByRelayCredential( cred );
        assert username == null;

        try {
            cred = new ChallengeResponseCredential( "DESCRIPTION", null );
            cred.setResponse( "josso user" );
            username = db.loadUsernameByRelayCredential( cred );
        } catch ( SSOIdentityException e ) {
            assert true;
        }
    }

    private static void createTables ( JdbcTemplate template ) throws Exception {
        template.execute( getQueryFromFile( "sso.sql" ) );
    }

    private static void insertData ( JdbcTemplate template ) throws Exception {
        template.execute( getQueryFromFile( "sso-data.sql" ) );
    }

    private static String getQueryFromFile ( String resource ) throws Exception {
        InputStream is = JDBCIdentityStoreTest.class.getResourceAsStream( resource );
        InputStreamReader isr = new InputStreamReader( is );
        BufferedReader br = new BufferedReader( isr );

        String s = br.readLine();
        StringBuilder sb = new StringBuilder();
        while ( s != null ) {
            sb.append( s );
            s = br.readLine();
        }

        is.close();
        return sb.toString();
    }
}
