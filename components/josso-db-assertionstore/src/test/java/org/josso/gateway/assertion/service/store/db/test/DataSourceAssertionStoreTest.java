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

package org.josso.gateway.assertion.service.store.db.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.josso.gateway.assertion.AuthenticationAssertion;
import org.josso.gateway.assertion.MutableAuthenticationAssertion;
import org.josso.gateway.assertion.service.store.db.DataSourceAssertionStore;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * User: <a href=mailto:ajadzinsky@atricor.org>ajadzinsky</a>
 * Date: Dec 3, 2008
 * Time: 3:05:53 PM
 */
public class DataSourceAssertionStoreTest {
    private static final Log logger = LogFactory.getLog( DataSourceAssertionStoreTest.class );

    protected static DataSourceAssertionStore db;

    @BeforeClass
    public static void beforeTest () throws Exception {
        ApplicationContext ctxt = new ClassPathXmlApplicationContext( "org/josso/gateway/assertion/service/store/db/test/hsdb-assertion-store.xml" );
        db = (DataSourceAssertionStore) ctxt.getBean( "dbStore" );

        DataSource ds = (DataSource) ctxt.getBean( "dataSource" );
        JdbcTemplate template = new JdbcTemplate( ds );
        createTables( template );
        insertData( template );

        SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
        builder.bind( "jndi:hsqldb/jossoDataSource", ds );
    }

    @Test
    public void testSizeQueery () throws Exception {
        int size = db.getSize();
        assert size == 1;
    }

    @Test
    public void testLoad () throws Exception {
        AuthenticationAssertion aa = db.load( "1" );
        assert aa != null;
    }

    @Test
    public void testInsert () throws Exception {
        MutableAuthenticationAssertion aa = new MutableAuthenticationAssertion( "2", "2" );
        aa.setSecurityDomainName( "SecurityNAme" );
        aa.setValid( true );

        db.save( aa );
        AuthenticationAssertion bs = db.load( "2" );
        assert bs != null : "session id 2 was not inserted";
    }

    @Test
    public void testKeys () throws Exception {
        String[] keys = db.keys();
        assert Arrays.binarySearch( keys, "1" ) > -1 : "expected assertionId 1 not found";
        assert Arrays.binarySearch( keys, "2" ) > -1 : "expected assertionId 2 not found";
    }

    @Test
    public void testLoadAll () throws Exception {
        AuthenticationAssertion[] aas = db.loadAll();
        assert aas.length == 2;
    }

    @Test
    public void testDelete () throws Exception {
        final String id = "1";
        db.remove( id );

        AuthenticationAssertion aa = db.load( id );
        assert aa == null : "assertion " + aa.getId() + " was not deleted";
    }

    @Test
    public void testDeleteAll () throws Exception {
        db.clear();

        int size = db.getSize();
        assert size == 0 : "assertions could not be removed";
    }

    private static void createTables ( JdbcTemplate template ) throws Exception {
        template.execute( getQueryFromFile( "sso-assertions.sql" ) );
    }

    private static void insertData ( JdbcTemplate template ) throws Exception {
        template.execute( getQueryFromFile( "sso-assertions-data.sql" ) );
    }

    private static String getQueryFromFile ( String resource ) throws Exception {
        InputStream is = DataSourceAssertionStoreTest.class.getResourceAsStream( resource );
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
