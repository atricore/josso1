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

package org.josso.gateway.session.service.store.db;

import org.josso.gateway.session.exceptions.SSOSessionException;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This DB Session store obtains DB connections directly from the JDBC Driver.
 *
 * @author Jeff Gutierrez (code@gutierrez.ph)
 *
 * @org.apache.xbean.XBean element="jdbc-store"
 */
public class JdbcSessionStore extends DbSessionStore {

    /**
     * The connection username to use when trying to connect to the database.
     */
    private String _connectionName = null;

    /**
     * The connection URL to use when trying to connect to the database.
     */
    private String _connectionPassword = null;

    /**
     * The connection URL to use when trying to connect to the database.
     */
    private String _connectionURL = null;

    /**
     * Instance of the JDBC Driver class we use as a connection factory.
     */
    private Driver _driver = null;

    /**
     * The JDBC driver to use.
     */
    private String _driverName = null;


    // --------------------------------
    // JdbcSessionStore specific
    // --------------------------------

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
        _driver = null; // Clear old driver reference, if any
    }


    // -----------------------------
    // DbSessionStore-implementation
    // -----------------------------

    public Connection getConnection() throws SQLException, SSOSessionException {
        Connection retval = null;

        try {
            // Instantiate our database driver if necessary
            if (_driver == null) {
                Class clazz = Class.forName(_driverName);
                _driver = (Driver) clazz.newInstance();
            }

            // Open a new connection
            final Properties props = new Properties();
            if (_connectionName != null)
                props.put("user", _connectionName);

            if (_connectionPassword != null)
                props.put("password", _connectionPassword);

            retval = _driver.connect(_connectionURL, props);
            retval.setAutoCommit(false);
        }
        catch (InstantiationException e) {
            throw new SSOSessionException(e);
        }
        catch (IllegalAccessException e) {
            throw new SSOSessionException(e);
        }
        catch (ClassNotFoundException e) {
            throw new SSOSessionException(e);
        }

        return retval;
    }
}
