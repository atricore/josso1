package org.josso.gateway.identity.service.store.virtual;

import org.josso.gateway.identity.service.store.IdentityStore;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: IdentitySourceImpl.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="source"
 * <p/>
 * Default implementation for Identity Sources.
 */
public class IdentitySourceImpl implements IdentitySource {

    private String alias;
    private IdentityStore backingIdentityStore;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @org.apache.xbean.Property alias="backing-store"
     */
    public IdentityStore getBackingIdentityStore() {
        return backingIdentityStore;
    }

    public void setBackingIdentityStore(IdentityStore backingIdentityStore) {
        this.backingIdentityStore = backingIdentityStore;
    }
}
