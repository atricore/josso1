package org.josso.gateway.identity.service.store.virtual;

/**
 * Reports an error creating a virtual entry from multiple source entries.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: JointUserEntryCreationFailed.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class JointUserEntryCreationFailed extends VirtualIdentityStoreException {
    public JointUserEntryCreationFailed(String message, Throwable cause) {
        super(message, cause);
    }
}
