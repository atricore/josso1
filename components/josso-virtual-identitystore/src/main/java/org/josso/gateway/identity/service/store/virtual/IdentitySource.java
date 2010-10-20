package org.josso.gateway.identity.service.store.virtual;

import org.josso.gateway.identity.service.store.IdentityStore;

/**
 * A source for user and entitlement information which is backed by an identity store.
 * A virtual identity store aggregates one or more identity sources.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: IdentitySource.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public interface IdentitySource {

    String getAlias();

    IdentityStore getBackingIdentityStore();

}
