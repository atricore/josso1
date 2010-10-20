package org.josso.gateway.session.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: BaseSessionImpl.java 595 2008-08-15 15:32:38Z sgonzalez $
 */

public class BaseSessionImpl implements BaseSession {

    private static final Log logger = LogFactory.getLog(BaseSessionImpl.class);

    // This session identifier.
    protected String _id;

    // Flag indicating if this session is valid.
    protected boolean _valid;

    // Session creation time.
    protected long _creationTime;

    // Session max inactive interval.
    protected int _maxInactiveInterval = -1;

    // Session access time.
    protected long _lastAccessedTime;

    // Session access count.
    protected long _accessCount;

    // Indicates that this session is expiring.
    protected boolean _expiring;

    // The username associated to this session
    protected String _username;

    public BaseSessionImpl() {
    }

    // ---------------------------------------------------------------
    // SSOSession interface.
    // ---------------------------------------------------------------

    /**
     * The SSO Session id. This is a unique id.
     *
     * @return the session id.
     */
    public String getId() {
        return _id;
    }

    /**
     * This method returns true if the session is valid.
     * It checks if this session should be expired.
     *
     * @return the session status.
     */
    public boolean isValid() {

        if (!_valid) {
            return _valid;
        }

        if (_maxInactiveInterval >= 0) {
            long timeNow = System.currentTimeMillis();
            int timeIdle = (int) ((timeNow - _lastAccessedTime) / 1000L);
            if (timeIdle >= _maxInactiveInterval) {
                expire();
            }
        }

        return (_valid);
    }

    /**
     * Set the maximum time interval, in seconds, between client requests
     * before the SSO Service will invalidate the session.  A negative
     * time indicates that the session should never time out.
     *
     * @param interval The new maximum interval
     */
    public void setMaxInactiveInterval(int interval) {
        _maxInactiveInterval = interval;
        isValid();
    }

    public int getMaxInactiveInterval() {
        return _maxInactiveInterval;
    }

    /**
     * Gets this session creation time in milliseconds.
     */
    public long getCreationTime() {
        return _creationTime;
    }

    /**
     * Gets this session last access time in milliseconds.
     */
    public long getLastAccessTime() {
        return _lastAccessedTime;
    }

    /**
     * Gets this session access count.
     */
    public long getAccessCount() {
        return _accessCount;
    }

    // ----------------------------------------------------------
    // Base Session
    // ----------------------------------------------------------

    /**
     * Update the accessed time information for this session.
     */
    public void access() {

        _lastAccessedTime = System.currentTimeMillis();

        // Check if the session is valid ...
        isValid();

        _accessCount++;

    }

    /**
     * This method expires a session.  The isValid method will return false.
     */
    public void expire() {

        setValid(false);

        // Mark this session as "being expired" if needed
        if (_expiring)
            return;

        synchronized (this) {

            _expiring = true;
            _accessCount = 0;
            setValid(false);

            // Notify interested session event listeners
            fireSessionEvent(BaseSession.SESSION_DESTROYED_EVENT, null);

            // We have completed expire of this session
            _expiring = false;

        }

    }

    // -----------------------------------------------------------------------
    // Package utils
    // -----------------------------------------------------------------------

    /**
     * Set the id of this session, used when initializing new sessions.
     *
     * @param id the session id.
     */
    public void setId(String id) {
        _id = id;
    }

    /**
     * Set the creation time for this session.
     *
     * @param time The new creation time
     */
    public void setCreationTime(long time) {
        _creationTime = time;
        _lastAccessedTime = time;
    }

    /**
     * Set the valid flag for this session.
     *
     * @param valid The new value for the valid property.
     */
    public void setValid(boolean valid) {
        _valid = valid;
    }


    /**
     * Notify all session event listeners that a particular event has
     * occurred for this Session.  The default implementation performs
     * this notification synchronously using the calling thread.
     * <p/>
     * Note : Do not use this method outside the GWY ...
     *
     * @param type Event type
     * @param data Event data
     */
    public void fireSessionEvent(String type, Object data) {

        throw new UnsupportedOperationException("Agent Compat Session imple cannot be used as live session");
    }

    /**
     * Getter for the username associated with this session, if any
     */
    public String getUsername() {
        return _username;
    }

    /**
     * Setter for the username associated with this session
     */
    public void setUsername(String username) {
        _username = username;
    }

    public String toString() {
        return _id + " [" + _username + "] " + new java.util.Date(_creationTime);
    }


}
