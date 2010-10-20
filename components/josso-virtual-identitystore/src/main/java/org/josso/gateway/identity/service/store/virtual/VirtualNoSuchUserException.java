package org.josso.gateway.identity.service.store.virtual;

import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.identity.service.store.UserKey;

/**
 * Exception triggered upon error conditions with virtual identity store
 * loadUser operation.
 */
public class VirtualNoSuchUserException extends NoSuchUserException {
	
	public VirtualNoSuchUserException(UserKey key) {
        super(key);
    }

    public VirtualNoSuchUserException(String msg) {
        super(msg);
    }
}
