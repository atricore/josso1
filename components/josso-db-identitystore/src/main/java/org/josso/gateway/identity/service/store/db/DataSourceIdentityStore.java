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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @org.apache.xbean.XBean element="datasource-store"
 *
 * DataSource based implementation of a DB Identity and Credential store.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: DataSourceIdentityStore.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class DataSourceIdentityStore extends AbstractDBIdentityStore {

    private static final Log logger = LogFactory.getLog(DataSourceIdentityStore.class);

    private String _dsJndiName;
    private DataSource _datasource;


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
        _datasource = null; // clear the previous reference to the DS.
    }

    // --------------------------------------------------------------------------
    // Proteced utils
    // --------------------------------------------------------------------------

    /**
     * Lazy load the datasource instace used by this store.
     *
     *
     * @throws SSOIdentityException
     */
    protected DataSource getDataSource() throws SSOIdentityException {

        if (_datasource == null) {

            try {

                if (logger.isDebugEnabled()) logger.debug("[getDatasource() : ]" + _dsJndiName);

                InitialContext ic = new InitialContext();
                _datasource = (DataSource) ic.lookup(_dsJndiName);

            } catch (NamingException ne) {
                logger.error("Error during DB connection lookup", ne);
                throw new SSOIdentityException(
                        "Error During Lookup\n" + ne.getMessage());
            }

        }

        return _datasource;
    }

    /**
     * Opens a new DB Connection, retrieved from the DS.
     *
     * @throws SSOIdentityException
     */
    protected Connection getDBConnection() throws SSOIdentityException {
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            logger.error("[getDBConnection()]:" + e.getErrorCode() + "/" + e.getSQLState() + "]" + e.getMessage());
            throw new SSOIdentityException(
                    "Exception while getting connection: \n " + e.getMessage());
        }
    }


}
