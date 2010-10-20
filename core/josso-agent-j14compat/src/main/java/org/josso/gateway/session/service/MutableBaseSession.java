package org.josso.gateway.session.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This session implementation can be modified after creation, session stores that need re-build session
 * state can use this class to restore original session attribute values.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: MutableBaseSession.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class MutableBaseSession extends BaseSessionImpl {

    private static final Log logger = LogFactory.getLog(MutableBaseSession.class);

    /**
     * Setter for the expirig property, normale set to false.
     */
    public void setExpiring(boolean expiring) {
        _expiring = expiring;
    }

    /**
     * Setter for the last access time.  This value is also modified by the setCreation time method.
     */
    public void setLastAccessedTime(long lastAccessedTime) {
        _lastAccessedTime = lastAccessedTime;
    }

    /**
     * Setter for the access count.
     */
    public void setAccessCount(long accessCount) {
        _accessCount = accessCount;
    }


}
