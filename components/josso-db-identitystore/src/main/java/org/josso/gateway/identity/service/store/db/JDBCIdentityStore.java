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
package org.josso.gateway.identity.service.store.db;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.exceptions.SSOIdentityException;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @org.apache.xbean.XBean element="jdbc-store"
 *
 * JDBC Implementation of a DB Identity and Credential Store.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: JDBCIdentityStore.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public class JDBCIdentityStore extends AbstractDBIdentityStore {

    private static final Log logger = LogFactory.getLog(JDBCIdentityStore.class);

    /**
     * The connection username to use when trying to connect to the database.
     */
    protected String _connectionName = null;

    /**
     * The connection URL to use when trying to connect to the database.
     */
    protected String _connectionPassword = null;


    /**
     * The connection URL to use when trying to connect to the database.
     */
    protected String _connectionURL = null;

    /**
     * Instance of the JDBC Driver class we use as a connection factory.
     */
    protected Driver _driver = null;

    /**
     * The JDBC driver to use.
     */
    protected String _driverName = null;

    /**
     * Use a Datasource connection to improve performance.
     *
     * @throws SSOIdentityException
     */
    protected Connection getDBConnection() throws SSOIdentityException {

        // Instantiate our database driver if necessary
        if (_driver == null) {
            try {
                Class clazz = Class.forName(_driverName);
                _driver = (Driver) clazz.newInstance();
            } catch (Throwable e) {
                throw new SSOIdentityException(e.getMessage(), e);
            }
        }

        // Open a new connection
        Properties props = new Properties();
        if (_connectionName != null)
            props.put("user", _connectionName);

        if (_connectionPassword != null)
            props.put("password", _connectionPassword);

        try {
            Connection c = _driver.connect(_connectionURL, props);
            c.setAutoCommit(false);
            return (c);

        } catch (SQLException e) {
            logger.error("[getDBConnection()]:" + e.getErrorCode() + "/" + e.getSQLState() + "]" + e.getMessage());
            throw new SSOIdentityException(e.getMessage(), e);
        }

    }

    public String getConnectionName() {
        return _connectionName;
    }

    public void setConnectionName(String connectionName) {
        _connectionName = connectionName;
    }

    public String getConnectionPassword() {
        return _connectionPassword;
    }

    public void setConnectionPassword(String connectionPassword) {
        _connectionPassword = connectionPassword;
    }

    public String getConnectionURL() {
        return _connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        _connectionURL = connectionURL;
    }

    public String getDriverName() {
        return _driverName;
    }

    public void setDriverName(String driverName) {
        _driverName = driverName;
    }

}
