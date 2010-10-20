package org.josso.gateway.identity.service.store.virtual;

import org.josso.gateway.identity.exceptions.SSOIdentityException;

/**
 * User exists result.
 */
public class UserExistsOutcome {
	
	private boolean exists;
	
    private SSOIdentityException exception;

	public UserExistsOutcome(boolean exists) {
		this.exists = exists;
	}

	public UserExistsOutcome(SSOIdentityException exception) {
		this.exception = exception;
	}

	public UserExistsOutcome(boolean exists, SSOIdentityException exception) {
		this.exists = exists;
		this.exception = exception;
	}

	/**
	 * @return the exists
	 */
	public boolean isExists() {
		return exists;
	}

	/**
	 * @param exists the exists to set
	 */
	public void setExists(boolean exists) {
		this.exists = exists;
	}

	/**
	 * @return the exception
	 */
	public SSOIdentityException getException() {
		return exception;
	}

	/**
	 * @param exception the exception to set
	 */
	public void setException(SSOIdentityException exception) {
		this.exception = exception;
	}
}
