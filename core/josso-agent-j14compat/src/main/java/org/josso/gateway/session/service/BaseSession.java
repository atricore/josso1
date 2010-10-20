package org.josso.gateway.session.service;

import org.josso.gateway.session.SSOSession;

/**
 * A base implementation of an SSO session, that provides extra functionality used by the service.
 * This is a mutable SSO session.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: BaseSession.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public interface BaseSession extends SSOSession {

    /**
     * Update the accessed time information for this session.
     */
    void access();

    /**
     * This method expires a session.  The isValid method will return false.
     */
    void expire();

    /**
     * Set the id of this session, used when initializing new sessions.
     * Used while building or recycling a session.
     *
     * @param id the session id.
     */
    void setId(String id);

    /**
     * Set the creation time for this session.
     * Used while building or recycling a session.
     *
     * @param time The new creation time
     */
    void setCreationTime(long time);

    /**
     * Set the valid flag for this session.
     * Used while building or recycling a session.
     *
     * @param valid The new value for the valid property.
     */
    void setValid(boolean valid);


    /**
     * Setter for the username associated to this session.
     */
    void setUsername(String name);


}
