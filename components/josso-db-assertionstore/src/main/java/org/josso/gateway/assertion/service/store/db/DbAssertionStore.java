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
import org.josso.gateway.assertion.AuthenticationAssertion;
import org.josso.gateway.assertion.MutableAuthenticationAssertion;
import org.josso.gateway.assertion.exceptions.AssertionException;
import org.josso.gateway.assertion.service.store.AbstractAssertionStore;

import java.sql.*;
import java.util.ArrayList;

/**
 * An abstraction of a AssertionStore backed by a database.
 * <p/>
 * <p>
 * Additional component properties include:
 * <ul>
 * <li>sizeQuery = The SQL Query used to add a new assertion to the store.</li>
 * <li>keysQuery = The SQL Query used to retrieve all assertion ids. The first column for each row in the result set must be the assertion id.</li>
 * <li>loadAllQuery = The SQL Query used to load all assertions from the store.</li>
 * <li>loadQuery = The SQL Query used to load one assertion from the store based on its id.</li>
 * <li>deleteDml = The SQL Query used to remove a assertion from the store.</li>
 * <li>deletAllDml = The SQL Query used to remove ALL assertions from the store.</li>
 * <li>insertDml = The SQL Query used to add a new assertion to the store.</li>
 * </ul>
 * </p>
 * <p>
 * The columns in the result set for all load methods must be in the following order :
 * assertionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
 * </p>
 * <p>
 * lastAccessTime and creationTime are treated as a longs, not dates.
 * </p>
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 */
public abstract class DbAssertionStore extends AbstractAssertionStore {
    private static final Log __log = LogFactory.getLog(DbAssertionStore.class);

    private String _sizeQuery = null;
    private String _keysQuery = null;
    private String _loadAllQuery = null;
    private String _loadQuery = null;
    private String _deleteDml = null;
    private String _deleteAllDml = null;
    private String _insertDml = null;


    // -------------------------------------
    // DbSessinStore-specific
    // -------------------------------------

    /**
     * Implementation classes implement this method.
     *
     * @return A database connection.
     */
    protected abstract Connection getConnection() throws SQLException, AssertionException;


    /**
     * Close the given db connection.
     *
     * @param dbConnection
     */
    protected void close(Connection dbConnection) throws AssertionException {
        try {
            if (dbConnection != null
                    && !dbConnection.isClosed()) {
                dbConnection.close();
            }
        }
        catch (SQLException se) {
            if (__log.isDebugEnabled())
                __log.debug("Error while clossing connection");

            throw new AssertionException("Error while clossing connection\n" + se.getMessage());
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug("Error while clossing connection");

            throw new AssertionException("Error while clossing connection\n" + e.getMessage());
        }

    }

       /**
     * Close the given db connection.
     *
     * @param statement
     */
    protected void close(Statement statement) throws AssertionException {
        try {
            if (statement != null) {
                statement.close();
            }
        }
        catch (SQLException se) {
            if (__log.isDebugEnabled())
                __log.debug("Error while clossing statement");

            throw new AssertionException("Error while clossing statement\n" + se.getMessage());
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug("Error while clossing statement");

            throw new AssertionException("Error while clossing statement\n" + e.getMessage());
        }

    }

    // -------------------------------------------------
    // Configuration properties
    // -------------------------------------------------

    /**
     * The SQL Query used to add a new assertion to the store.
     *
     * @param query
     */
    public void setInsertDml(String query) {
        _insertDml = query;
    }

    public String getInsertDml() {
        return _insertDml;
    }

    /**
     * The SQL Query used to remove ALL assertions from the store.
     */
    public void setDeleteAllDml(String query) {
        _deleteAllDml = query;
    }

    public String getDeleteAllDml() {
        return _deleteAllDml;
    }


    /**
     * The SQL Query used to remove a assertion from the store.
     */
    public void setDeleteDml(String query) {
        _deleteDml = query;
    }

    public String getDeleteDml() {
        return _deleteDml;
    }


    /**
     * The SQL query used to retrieve the number of assertions in the store.
     * The first column of the first row in the result set must be the number of assertions.
     */
    public void setSizeQuery(String query) {
        _sizeQuery = query;
    }

    public String getSizeQuery() {
        return _sizeQuery;
    }

    /**
     * The SQL Query used to retrieve all assertion ids.
     * The first column for each row in the result set must be the assertion id.
     */
    public void setKeysQuery(String query) {
        _keysQuery = query;
    }

    public String getKeysQuery() {
        return _keysQuery;
    }


    /**
     * The SQL Query used to load all assertions from the store.
     * <p/>
     * The columns in the result set must be in the following order :
     * assertionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     *
     * @param query
     */
    public void setLoadAllQuery(String query) {
        _loadAllQuery = query;
    }

    public String getLoadAllQuery() {
        return _loadAllQuery;
    }

    /**
     * The SQL Query used to load one assertion from the store based on its id.
     * <p/>
     * The columns in the result set must be in the following order :
     * assertionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     * <p/>
     * example : SELECT assertionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid FROM JOSSO_SESSION WHERE assertionId = ?
     *
     * @param query
     */
    public void setLoadQuery(String query) {
        _loadQuery = query;
    }

    public String getLoadQuery() {
        return _loadQuery;
    }


    // --------------------------------
    // AssertionStore implementation
    // --------------------------------


    /**
     * This method returns the number of stored assertions.
     * <p/>
     * The first column of the first row in the result set must be the number of assertions.
     *
     * @return the number of assertions
     * @throws org.josso.gateway.assertion.exceptions.AssertionException
     *
     * @see #setSizeQuery(String)
     */
    public int getSize() throws AssertionException {
        int retval = 0;

        Connection conn = null;
        Statement stmt = null;

        // - Submit query
        // - First column of the first row is the number of assertions.
        //

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            final ResultSet rs = stmt.executeQuery(_sizeQuery);
            if (rs != null && rs.next()) {
                retval = rs.getInt(1);
            }

            if ( rs != null ) {
                rs.close();
            }
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug(e, e);
            throw new AssertionException(e);
        }
        finally {
            close(stmt);
            close(conn);
        }

        if (__log.isDebugEnabled())
            __log.debug("Returning " + retval);

        return retval;
    }


    /**
     * Returns all assertion keys (ids)
     * <p/>
     * The first column for each row in the result set must be the assertion id.
     *
     * @return The assertion keys
     * @throws org.josso.gateway.assertion.exceptions.AssertionException
     *
     * @see #setKeysQuery(String)
     */
    public String[] keys() throws AssertionException {
        String[] retval = null;

        Connection conn = null;
        Statement stmt = null;

        // - Submit query
        // - First column for each row is the assertion id.
        //

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            final ResultSet rs = stmt.executeQuery(_keysQuery);
            final ArrayList bucket = new ArrayList();

            while (rs.next())
                bucket.add(rs.getString(1));

            rs.close();

            retval = new String[bucket.size()];
            bucket.toArray(retval);
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug(e, e);
            throw new AssertionException(e);
        }
        finally {
            close(stmt);
            close(conn);
        }

        return retval;
    }


    /**
     * Loads all stored assertions.
     * <p/>
     * The columns in the result set must be in the following order :
     * assertionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     *
     * @see #setLoadAllQuery(String)
     */
    public AuthenticationAssertion[] loadAll() throws AssertionException {
        AuthenticationAssertion[] retval = null;

        Connection conn = null;
        Statement stmt = null;

        // - Submit query
        // - Expected columns, in order:
        // assertionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
        //

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            final ResultSet rs = stmt.executeQuery(_loadAllQuery);
            retval = getAssertions(rs);
            rs.close();
        }
        catch (Exception e) {
            __log.error(e, e);
            throw new AssertionException(e);
        }
        finally {
            close(stmt);
            close(conn);
        }

        return retval;
    }


    /**
     * Loads a assertion based on its id.
     * <p/>
     * The columns in the result set must be in the following order :
     * assertionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     *
     * @param id
     * @return
     * @throws org.josso.gateway.assertion.exceptions.AssertionException
     *
     * @see #setLoadQuery(String)
     */
    public AuthenticationAssertion load(String id) throws AssertionException {
        AuthenticationAssertion retval = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(_loadQuery);
            stmt.setString(1, id);

            final ResultSet rs = stmt.executeQuery();
            if (rs.next())
                retval = createFromResultSet(rs);

            rs.close();

        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug(e, e);
            throw new AssertionException(e);
        }
        finally {
            close(stmt);
            close(conn);
        }

        id = (retval == null)
                ? "NOT FOUND"
                : retval.getId();

        if (__log.isDebugEnabled())
            __log.debug("Loaded assertion: " + id);

        return retval;
    }

    /**
     * Removes a assertion from the store based on its id
     *
     * @throws org.josso.gateway.assertion.exceptions.AssertionException
     *
     * @see #setDeleteDml(String)
     */
    public void remove(String id) throws AssertionException {
        Connection conn = null;

        try {
            conn = getConnection();
            delete(conn, id);
            conn.commit();
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug(e, e);
            throw new AssertionException(e);
        }
        finally {
            close(conn);
        }
    }


    /**
     * Removes ALL stored assertions
     *
     * @throws org.josso.gateway.assertion.exceptions.AssertionException
     *
     * @see #setDeleteAllDml(String)
     */
    public void clear() throws AssertionException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute(_deleteAllDml);
            conn.commit();

            stmt.close();
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug(e, e);

            throw new AssertionException(e);
        }
        finally {
            close(stmt);
            close(conn);
        }
    }

    /**
     * Stores a assertion in the DB.  This method opens a transaccion, removes the old assertion if present, the creates
     * it again using the configured savenDml Query and commits the transaction.
     * <p/>
     * Assertion attributes will be passed to the prepared statemetn in the following order :
     * assertionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     *
     * @param assertion
     * @throws org.josso.gateway.assertion.exceptions.AssertionException
     *
     * @see #setInsertDml(String)
     */
    public void save(AuthenticationAssertion assertion) throws AssertionException {
        Connection conn = null;

        // - Expected columns, in order:
        // assertionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid

        try {
            conn = getConnection();
            delete(conn, assertion.getId());
            insert(conn, assertion);
            conn.commit();
            if (__log.isDebugEnabled())
                __log.debug("Assertion committed: " + assertion.getId());
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug(e, e);

            if (conn != null) {

                try {
                    conn.rollback();
                } catch (SQLException sqle) {
                    if (__log.isDebugEnabled())
                        __log.debug("Error during ROLLBACK ", sqle);
                }
            }

            throw new AssertionException(e);
        }
        finally {
            close(conn);
        }

        if (__log.isDebugEnabled())
            __log.debug("Saved assertion: " + assertion.getId());
    }


    // ---------------------------
    // Private Methods
    // ---------------------------

    /**
     * This removes a assertion, using the value of the removeDml property as prepared statement.
     *
     * @param conn
     * @param assertionId
     * @throws java.sql.SQLException
     */
    protected void delete(Connection conn, String assertionId) throws SQLException {
        final PreparedStatement ps = conn.prepareStatement(_deleteDml);
        ps.setString(1, assertionId);
        ps.execute();

        if (__log.isDebugEnabled())
            __log.debug("Assertion Removed: " + assertionId);
    }

    protected void insert(Connection conn, AuthenticationAssertion assertion) throws SQLException {
        final PreparedStatement ps = conn.prepareStatement(_insertDml);
        ps.setString(1, assertion.getId());
        ps.setString(2, "default");
        ps.setString(3, assertion.getSSOSessionId());
        ps.setLong(4, assertion.getCreationTime());
        ps.setBoolean(5, assertion.isValid());
        ps.execute();

        if (__log.isDebugEnabled())
            __log.debug("Creation, LastAccess: " + assertion.getCreationTime() + ", " + assertion.getCreationTime());

        if (__log.isDebugEnabled())
            __log.debug("Assertion inserted: " + assertion.getId());
    }


    /**
     *
     */
    protected AuthenticationAssertion[] getAssertions(ResultSet rs) throws SQLException {
        final ArrayList bucket = new ArrayList();

        // - Collect the assertions
        // - Return the assertions in an array.
        //

        while (rs.next())
            bucket.add(createFromResultSet(rs));

        final AuthenticationAssertion[] retval = new AuthenticationAssertion[bucket.size()];
        bucket.toArray(retval);

        return retval;
    }

    /**
     * This method builds a assertion instance based on a result set.
     * <p/>
     * Expected columns, in order:
     * assertionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     *
     * @param rs
     * @return
     * @throws java.sql.SQLException
     */
    protected AuthenticationAssertion createFromResultSet(ResultSet rs) throws SQLException {
        // - Expected columns, in order:
        // assertionId, userName, SSOSessionId, creationTime, valid

        final MutableAuthenticationAssertion bsi = new MutableAuthenticationAssertion(rs.getString(1));
        bsi.setSecurityDomainName(rs.getString(2));
        bsi.setSSOSessionId(rs.getString(3));
        bsi.setCreationTime(rs.getLong(4));
        bsi.setValid(rs.getBoolean(5));

        return bsi;
    }
}