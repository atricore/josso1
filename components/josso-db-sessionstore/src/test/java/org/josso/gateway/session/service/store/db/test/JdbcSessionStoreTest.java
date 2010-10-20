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

package org.josso.gateway.session.service.store.db.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.josso.gateway.session.service.BaseSession;
import org.josso.gateway.session.service.MutableBaseSession;
import org.josso.gateway.session.service.store.db.JdbcSessionStore;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * User: <a href=mailto:ajadzinsky@atricor.org>ajadzinsky</a>
 * Date: Dec 2, 2008
 * Time: 3:24:16 PM
 */
public class JdbcSessionStoreTest {
    private static final Log logger = LogFactory.getLog( JdbcSessionStoreTest.class );

    protected static JdbcSessionStore db;

    @BeforeClass
    public static void beforeTest () throws Exception {
        ApplicationContext ctxt = new ClassPathXmlApplicationContext( "org/josso/gateway/session/service/store/db/test/hsdb-store.xml" );
        db = (JdbcSessionStore) ctxt.getBean( "dbStore" );

        JdbcTemplate template = new JdbcTemplate( (DataSource) ctxt.getBean( "dataSource" ) );
        createTables( template );
        insertData( template );
    }

    @Test
    public void testConnection () throws Exception {
        Connection conn = db.getConnection();
        assert conn != null : "No connection created";
        conn.close();
    }

    @Test
    public void testLoad () throws Exception {
        BaseSession bs = db.load( "1" );
        assert bs != null;
    }

    @Test
    public void testInsert () throws Exception {
        MutableBaseSession session = new MutableBaseSession();
        session.setUsername( "user1" );
        session.setCreationTime( Calendar.getInstance().getTimeInMillis() );
        session.setLastAccessedTime( Calendar.getInstance().getTimeInMillis() );
        session.setAccessCount( 1 );
        session.setMaxInactiveInterval( 60 );
        session.setValid( true );
        session.setId( "2" );

        db.save( session );
        BaseSession bs = db.load( "2" );
        assert bs != null : "session id 2 was not inserted";
    }

    @Test
    public void testSizeQueery () throws Exception {
        int i = db.getSize();
        assert i == 2;
    }

    @Test
    public void testKeys () throws Exception {
        String[] keys = db.keys();
        assert Arrays.binarySearch( keys, "1" ) > -1 : "expected sessionId 1 not found";
        assert Arrays.binarySearch( keys, "2" ) > -1 : "expected sessionId 2 not found";
    }

    @Test
    public void testUpdate () throws Exception {
        MutableBaseSession session = new MutableBaseSession();
        session.setUsername( "user1" );
        session.setCreationTime( Calendar.getInstance().getTimeInMillis() );
        session.setLastAccessedTime( Calendar.getInstance().getTimeInMillis() );
        session.setAccessCount( 2 );
        session.setMaxInactiveInterval( 30000 );
        session.setValid( false );
        session.setId( "2" );

        db.save( session );

        BaseSession bs = db.load( "2" );
        assert !bs.isValid() : "validity was not updated";
        assert bs.getAccessCount() == 2 : "Access Count was not updated";
        assert bs.getMaxInactiveInterval() == 30000 : "Max Inactive Interval was not updated";
    }

    @Test
    public void testLoadAll () throws Exception {
        BaseSession[] bss = db.loadAll();
        assert bss.length == 2;
    }

    @Test
    public void testLoadByUsername () throws Exception {
        final String username = "user1";

        BaseSession[] bss = db.loadByUsername( username );
        for ( BaseSession b : bss ) {
            assert b.getUsername().equals( username ) : "session " + b.getId() + " belongs to user " + b.getUsername();
        }
    }

    @Test
    public void testLoadByValid () throws Exception {
        BaseSession[] bss = db.loadByValid( false );
        for ( BaseSession b : bss ) {
            assert !b.isValid() : "session " + b.getId() + " is valid [" + b.isValid() + "]";
        }
    }

    @Test
    public void testLoadByLastAccessTime () throws Exception {
        final long time = 1228318975795L;
        BaseSession[] bss = db.loadByLastAccessTime( new Date( time ) );
        for ( BaseSession b : bss ) {
            assert b.getLastAccessTime() == time : "session " + b.getId() + " Last Accessed Time [" + b.getLastAccessTime() + "]";
        }
    }

    @Test
    public void testDelete () throws Exception {
        final String id = "1";
        db.remove( id );

        BaseSession bs = db.load( id );
        assert bs == null : "session " + bs.getId() + " was not deleted";
    }

    @Test
    public void testDeleteAll () throws Exception {
        db.clear();

        int size = db.getSize();
        assert size == 0 : "sessions could not be removed";
    }

    private static void createTables ( JdbcTemplate template ) throws Exception {
        template.execute( getQueryFromFile( "sso-session.sql" ) );
    }

    private static void insertData ( JdbcTemplate template ) throws Exception {
        template.execute( getQueryFromFile( "sso-session-data.sql" ) );
    }

    private static String getQueryFromFile ( String resource ) throws Exception {
        InputStream is = JdbcSessionStoreTest.class.getResourceAsStream( resource );
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