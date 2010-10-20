package org.josso.gateway.identity.service.store.virtual;

import org.josso.auth.exceptions.SSOAuthenticationException;

/**
 * Binding result.
 */
public class BindOutcome {
	
	private boolean successful;
	
    private SSOAuthenticationException exception;

	public BindOutcome(boolean successful) {
		this.successful = successful;
	}

	public BindOutcome(SSOAuthenticationException exception) {
		this.exception = exception;
	}

	public BindOutcome(boolean successful, SSOAuthenticationException exception) {
		this.successful = successful;
		this.exception = exception;
	}

	/**
	 * @return the successful
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * @param successful the successful to set
	 */
	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	/**
	 * @return the exception
	 */
	public SSOAuthenticationException getException() {
		return exception;
	}

	/**
	 * @param exception the exception to set
	 */
	public void setException(SSOAuthenticationException exception) {
		this.exception = exception;
	}
}
