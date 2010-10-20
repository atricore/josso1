package org.josso.gateway.identity.service.store.virtual;

import org.josso.gateway.identity.exceptions.SSOIdentityException;

/**
 * Exception triggered upon error conditions with virtual identity store
 * operations.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: VirtualIdentityStoreException.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class VirtualIdentityStoreException extends SSOIdentityException {
    public VirtualIdentityStoreException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
