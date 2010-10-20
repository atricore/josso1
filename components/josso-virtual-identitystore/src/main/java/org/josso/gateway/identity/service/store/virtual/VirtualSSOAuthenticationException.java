package org.josso.gateway.identity.service.store.virtual;

import org.josso.auth.exceptions.SSOAuthenticationException;

/**
 * Exception triggered upon error conditions with virtual identity store
 * bind operation.
 */
public class VirtualSSOAuthenticationException extends SSOAuthenticationException {
    
	public VirtualSSOAuthenticationException(String message) {
        super(message);
    }
	
	public VirtualSSOAuthenticationException(Throwable cause) {
        super(cause);
    }
	
	public VirtualSSOAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
