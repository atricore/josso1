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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.session.exceptions.SSOSessionException;
import org.josso.gateway.session.service.BaseSession;
import org.josso.gateway.session.service.MutableBaseSession;
import org.josso.gateway.session.service.store.AbstractSessionStore;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * An abstraction of a SessionStore backed by a database.
 * <p/>
 * <p>
 * Additional component properties include:
 * <ul>
 * <li>sizeQuery = The SQL Query used to add a new session to the store.</li>
 * <li>keysQuery = The SQL Query used to retrieve all session ids. The first column for each row in the result set must be the session id.</li>
 * <li>loadAllQuery = The SQL Query used to load all sessions from the store.</li>
 * <li>loadQuery = The SQL Query used to load one session from the store based on its id.</li>
 * <li>loadByUserNameQuery = The SQL Query used to load all sessions associated to a given user.</li>
 * <li>loadByLastAccesstimeQuery = The SQL Query used to load all sessions last accessed before the given date.</li>
 * <li>loadByValidQuery = The SQL Query used to load all sessions whose valid property is equals to the gvien argument.</li>
 * <li>deleteDml = The SQL Query used to remove a session from the store.</li>
 * <li>deletAllDml = The SQL Query used to remove ALL sessions from the store.</li>
 * <li>insertDml = The SQL Query used to add a new session to the store.</li>
 * </ul>
 * </p>
 * <p>
 * The columns in the result set for all load methods must be in the following order :
 * sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
 * </p>
 * <p>
 * lastAccessTime and creationTime are treated as a longs, not dates.
 * </p>
 *
 * @author Jeff Gutierrez (code@gutierrez.ph) ca
 */
public abstract class DbSessionStore extends AbstractSessionStore {
    private static final Log __log = LogFactory.getLog(DbSessionStore.class);

    private String _sizeQuery = null;
    private String _keysQuery = null;
    private String _loadAllQuery = null;
    private String _loadQuery = null;
    private String _loadByUserNameQuery = null;
    private String _loadByLastAccessTimeQuery = null;
    private String _loadByValidQuery = null;

    private String _deleteDml = null;
    private String _deleteAllDml = null;
    private String _insertDml = null;
    private String _updateDml = null;


    // -------------------------------------
    // DbSessinStore-specific
    // -------------------------------------

    /**
     * Implementation classes implement this method.
     *
     * @return A database connection.
     */
    protected abstract Connection getConnection() throws SQLException, SSOSessionException;


    /**
     * Close the given db connection.
     *
     * @param dbConnection
     */
    protected void close(Connection dbConnection) throws SSOSessionException {
        try {
            if (dbConnection != null
                    && !dbConnection.isClosed()) {
                dbConnection.close();
            }
        }
        catch (SQLException se) {
            if (__log.isDebugEnabled())
                __log.debug("Error while clossing connection");

            throw new SSOSessionException("Error while clossing connection\n" + se.getMessage());
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug("Error while clossing connection");

            throw new SSOSessionException("Error while clossing connection\n" + e.getMessage());
        }

    }


    /**
     * Close the given db connection.
     *
     * @param statement
     */
    protected void close(Statement statement) throws SSOSessionException {
        try {
            if (statement != null) {
                statement.close();
            }
        }
        catch (SQLException se) {
            if (__log.isDebugEnabled())
                __log.debug("Error while clossing statement");

            throw new SSOSessionException("Error while clossing statement\n" + se.getMessage());
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug("Error while clossing statement");

            throw new SSOSessionException("Error while clossing statement\n" + e.getMessage());
        }

    }
    // -------------------------------------------------
    // Configuration properties
    // -------------------------------------------------

    /**
     * The SQL Query used to add a new session to the store.
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
     * The SQL Query used to add a new session to the store.
     *
     * @param query
     */
    public void setUpdateDml(String query) {
        _updateDml = query;
    }

    public String getUpdateDml() {
        return _updateDml;
    }

    /**
     * The SQL Query used to remove ALL sessions from the store.
     */
    public void setDeleteAllDml(String query) {
        _deleteAllDml = query;
    }

    public String getDeleteAllDml() {
        return _deleteAllDml;
    }


    /**
     * The SQL Query used to remove a session from the store.
     */
    public void setDeleteDml(String query) {
        _deleteDml = query;
    }

    public String getDeleteDml() {
        return _deleteDml;
    }


    /**
     * The SQL Query used to load all sessions last accessed before the given date.
     * <p/>
     * The columns in the result set must be in the following order :
     * sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     */
    public void setLoadByLastAccessTimeQuery(String query) {
        _loadByLastAccessTimeQuery = query;
    }

    public String getLoadByLastAccessTimeQuery() {
        return _loadByLastAccessTimeQuery;
    }

    /**
     * The SQL Query used to load all sessions whose valid property is equals to the given valid.
     * <p/>
     * The columns in the result set must be in the following order :
     * sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     */
    public void setLoadByValidQuery(String query) {
        _loadByValidQuery = query;
    }

    public String getLoadByValidQuery() {
        return _loadByValidQuery;
    }

    /**
     * The SQL Query used to load all sessions associated to a given user.
     * <p/>
     * The columns in the result set must be in the following order :
     * sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     *
     * @param query
     */
    public void setLoadByUserNameQuery(String query) {
        _loadByUserNameQuery = query;
    }

    public String getLoadByUserNameQuery() {
        return _loadByUserNameQuery;
    }


    /**
     * The SQL query used to retrieve the number of sessions in the store.
     * The first column of the first row in the result set must be the number of sessions.
     */
    public void setSizeQuery(String query) {
        _sizeQuery = query;
    }

    public String getSizeQuery() {
        return _sizeQuery;
    }

    /**
     * The SQL Query used to retrieve all session ids.
     * The first column for each row in the result set must be the session id.
     */
    public void setKeysQuery(String query) {
        _keysQuery = query;
    }

    public String getKeysQuery() {
        return _keysQuery;
    }


    /**
     * The SQL Query used to load all sessions from the store.
     * <p/>
     * The columns in the result set must be in the following order :
     * sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
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
     * The SQL Query used to load one session from the store based on its id.
     * <p/>
     * The columns in the result set must be in the following order :
     * sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     * <p/>
     * example : SELECT sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid FROM JOSSO_SESSION WHERE sessionId = ?
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
    // SessionStore implementation
    // --------------------------------


    /**
     * This method returns the number of stored sessions.
     * <p/>
     * The first column of the first row in the result set must be the number of sessions.
     *
     * @return the number of sessions
     * @throws SSOSessionException
     * @see #setSizeQuery(String)
     */
    public int getSize() throws SSOSessionException {
        int retval = 0;

        Connection conn = null;
        Statement stmt = null;

        // - Submit query
        // - First column of the first row is the number of sessions.
        //

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            final ResultSet rs = stmt.executeQuery(_sizeQuery);
            if (rs != null && rs.next()) {
                retval = rs.getInt(1);
            }

            if ( rs != null )
                rs.close();
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug(e, e);
            throw new SSOSessionException(e);
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
     * Returns all session keys (ids)
     * <p/>
     * The first column for each row in the result set must be the session id.
     *
     * @return The session keys
     * @throws SSOSessionException
     * @see #setKeysQuery(String)
     */
    public String[] keys() throws SSOSessionException {
        String[] retval = null;

        Connection conn = null;
        Statement stmt = null;

        // - Submit query
        // - First column for each row is the session id.
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
            throw new SSOSessionException(e);
        }
        finally {
            close(stmt);
            close(conn);
        }

        return retval;
    }


    /**
     * Loads all stored sessions.
     * <p/>
     * The columns in the result set must be in the following order :
     * sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     *
     * @see #setLoadAllQuery(String)
     */
    public BaseSession[] loadAll() throws SSOSessionException {
        BaseSession[] retval = null;

        Connection conn = null;
        Statement stmt = null;

        // - Submit query
        // - Expected columns, in order:
        // sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
        //

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            final ResultSet rs = stmt.executeQuery(_loadAllQuery);
            retval = getSessions(rs);
            rs.close();
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug(e, e);
            throw new SSOSessionException(e);
        }
        finally {
            close(stmt);
            close(conn);
        }

        return retval;
    }


    /**
     * Loads a session based on its id.
     * <p/>
     * The columns in the result set must be in the following order :
     * sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     *
     * @param id
     * @return
     * @throws SSOSessionException
     * @see #setLoadQuery(String)
     */
    public BaseSession load(String id) throws SSOSessionException {
        BaseSession retval = null;
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
            throw new SSOSessionException(e);
        }
        finally {
            close( stmt );
            close(conn);
        }

        id = (retval == null)
                ? "NOT FOUND"
                : retval.getId();

        if (__log.isDebugEnabled())
            __log.debug("Loaded session: " + id);

        return retval;
    }


    /**
     * Loads all sessions based on the associated username
     * <p/>
     * The columns in the result set must be in the following order :
     * sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     *
     * @see #setLoadByUserNameQuery(String)
     */
    public BaseSession[] loadByUsername(String userName) throws SSOSessionException {
        BaseSession[] retval = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(_loadByUserNameQuery);
            stmt.setString(1, userName);

            final ResultSet rs = stmt.executeQuery();
            retval = getSessions(rs);
            rs.close();
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug(e, e);
            throw new SSOSessionException(e);
        }
        finally {
            close(stmt);
            close(conn);
        }

        return retval;
    }


    /**
     * Loads all sessions last accessed before the given date.
     * <p/>
     * The date is converted to java.sql.Date when setting up the prepared statement.
     * <p/>
     * The columns in the result set must be in the following order :
     * sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     *
     * @see #setLoadByLastAccessTimeQuery(String)
     */
    public BaseSession[] loadByLastAccessTime(Date date)
            throws SSOSessionException {
        BaseSession[] retval = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(_loadByLastAccessTimeQuery);
            stmt.setLong(1, date.getTime());

            final ResultSet rs = stmt.executeQuery();
            retval = getSessions(rs);

            rs.close();
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug(e, e);
            throw new SSOSessionException(e);
        }
        finally {
            close(stmt);
            close(conn);
        }

        return retval;
    }

    /**
     * Loads all sessions whose valid property is equals to the received argument.
     *
     * @see #setLoadByValidQuery(String)
     */
    public BaseSession[] loadByValid(boolean valid)
            throws SSOSessionException {
        BaseSession[] retval = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(_loadByValidQuery);
            stmt.setBoolean(1, valid);

            final ResultSet rs = stmt.executeQuery();
            retval = getSessions(rs);

            rs.close();
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug(e, e);
            throw new SSOSessionException(e);
        }
        finally {
            close(stmt);
            close(conn);
        }

        return retval;
    }


    /**
     * Removes a session from the store based on its id
     *
     * @throws SSOSessionException
     * @see #setDeleteDml(String)
     */
    public void remove(String id) throws SSOSessionException {
        Connection conn = null;

        try {
            conn = getConnection();
            delete(conn, id);
            conn.commit();
        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug(e, e);
            throw new SSOSessionException(e);
        }
        finally {
            close(conn);
        }
    }


    /**
     * Removes ALL stored sessions
     *
     * @throws SSOSessionException
     * @see #setDeleteAllDml(String)
     */
    public void clear() throws SSOSessionException {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute(_deleteAllDml);
            conn.commit();

        }
        catch (Exception e) {
            if (__log.isDebugEnabled())
                __log.debug(e, e);

            throw new SSOSessionException(e);
        }
        finally {
            close(stmt);
            close(conn);
        }
    }

    /**
     * Stores a session in the DB.  This method opens a transaccion, removes the old session if present, the creates
     * it again using the configured savenDml Query and commits the transaction.
     * <p/>
     * Session attributes will be passed to the prepared statemetn in the following order :
     * sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     *
     * @param session
     * @throws SSOSessionException
     * @see #setInsertDml(String)
     */
    public void save(BaseSession session) throws SSOSessionException {
        Connection conn = null;

        // - Expected columns, in order:
        // sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid

        try {
            conn = getConnection();

            if (load(session.getId()) != null) {
                update(conn, session);
            } else {
                insert(conn, session);
            }

            conn.commit();

            if (__log.isDebugEnabled())
                __log.debug("Session committed: " + session.getId());
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

            throw new SSOSessionException(e);
        }
        finally {
            close(conn);
        }

        if (__log.isDebugEnabled())
            __log.debug("Saved session: " + session.getId());
    }


    // ---------------------------
    // Private Methods
    // ---------------------------

    /**
     * This removes a session, using the value of the removeDml property as prepared statement.
     *
     * @param conn
     * @param sessionId
     * @throws SQLException
     */
    protected void delete(Connection conn, String sessionId) throws SQLException {
        final PreparedStatement ps = conn.prepareStatement(_deleteDml);
        ps.setString(1, sessionId);
        ps.execute();

        if (__log.isDebugEnabled())
            __log.debug("Session Removed: " + sessionId);
    }

    protected void insert(Connection conn, BaseSession session) throws SQLException {
        final PreparedStatement ps = conn.prepareStatement(_insertDml);
        ps.setString(1, session.getId());
        ps.setString(2, session.getUsername());
        ps.setLong(3, session.getCreationTime());
        ps.setLong(4, session.getLastAccessTime());
        ps.setInt(5, (int) session.getAccessCount());
        ps.setInt(6, session.getMaxInactiveInterval());
        ps.setBoolean(7, session.isValid());
        ps.execute();

        if (__log.isDebugEnabled())
            __log.debug("Creation, LastAccess: " + session.getCreationTime() + ", " + session.getCreationTime());

        if (__log.isDebugEnabled())
            __log.debug("Session inserted: " + session.getId());
    }

    protected void update(Connection conn, BaseSession session) throws SQLException {
        final PreparedStatement ps = conn.prepareStatement(_updateDml);
        ps.setString(1, session.getUsername());
        ps.setLong(2, session.getCreationTime());
        ps.setLong(3, session.getLastAccessTime());
        ps.setInt(4, (int) session.getAccessCount());
        ps.setInt(5, session.getMaxInactiveInterval());
        ps.setBoolean(6, session.isValid());
        ps.setString(7, session.getId());
        ps.execute();

        if (__log.isDebugEnabled())
            __log.debug("Creation, LastAccess: " + session.getCreationTime() + ", " + session.getCreationTime());

        if (__log.isDebugEnabled())
            __log.debug("Session updated: " + session.getId());
    }

    /**
     *
     */
    protected BaseSession[] getSessions(ResultSet rs) throws SQLException {
        final ArrayList bucket = new ArrayList();

        // - Collect the sessions
        // - Return the sessions in an array.
        //

        while (rs.next())
            bucket.add(createFromResultSet(rs));

        final BaseSession[] retval = new BaseSession[bucket.size()];
        bucket.toArray(retval);

        return retval;
    }

    /**
     * This method builds a session instance based on a result set.
     * <p/>
     * Expected columns, in order:
     * sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    protected BaseSession createFromResultSet(ResultSet rs) throws SQLException {
        // - Expected columns, in order:
        // sessionId, userName, creationTime, lastAccessTime, accessCount, maxInactiveInterval, valid

        final MutableBaseSession bsi = new MutableBaseSession();
        bsi.setId(rs.getString(1));
        bsi.setUsername(rs.getString(2));
        bsi.setCreationTime(rs.getLong(3));
        bsi.setLastAccessedTime(rs.getLong(4));
        bsi.setAccessCount(rs.getLong(5));
        bsi.setMaxInactiveInterval(rs.getInt(6));
        bsi.setValid(rs.getBoolean(7));

        return bsi;
    }
}
