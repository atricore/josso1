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
package org.josso.gateway.assertion.service.store.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.assertion.exceptions.AssertionException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @org.apache.xbean.XBean element="datasource-store"
 *
 * A DB assertion store that uses a DataSource to obtain connections to the DB server.
 * The only configuration property specific to this implementation is the datasource JNDI name.
 * See DbAssertionStore for more configuration details.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 */
public class DataSourceAssertionStore extends DbAssertionStore {
    private static final Log logger = LogFactory.getLog(DataSourceAssertionStore.class);

    private String _dsJndiName;
    private DataSource _datasource;


    /**
     * @return A db Connection.
     */
    protected Connection getConnection() throws SQLException, AssertionException {
        final Connection conn = getDataSource().getConnection();
        conn.setAutoCommit(false);

        return conn;
    }


    // ------------------------------------------------------------------
    // Configuration properties.
    // ------------------------------------------------------------------

    /**
     * Sets the JNDI name of the DS associated to this Store.
     *
     * @param dsJndiName the JNDI name of the datasource used by the store.
     */
    public void setDsJndiName(String dsJndiName) {
        _dsJndiName = dsJndiName;
        _datasource = null; // Clear the previous reference to the DS.
    }

    public String getDsJndiName() {
        return _dsJndiName;
    }

    // --------------------------------------------------------------------------
    // Proteced utils
    // --------------------------------------------------------------------------

    /**
     * Lazy load the datasource instace used by this store.
     *
     * @throws org.josso.gateway.assertion.exceptions.AssertionException
     *
     */
    protected synchronized DataSource getDataSource() throws AssertionException {
        if (_datasource == null) {
            try {
                if (logger.isDebugEnabled())
                    logger.debug("[getDatasource() : ]" + _dsJndiName);

                InitialContext ic = new InitialContext();
                _datasource = (DataSource) ic.lookup(_dsJndiName);

            }
            catch (NamingException ne) {
                logger.error("Error during DB connection lookup", ne);
                throw new AssertionException(
                        "Error During Lookup\n" + ne.getMessage());
            }

        }

        return _datasource;
    }

}