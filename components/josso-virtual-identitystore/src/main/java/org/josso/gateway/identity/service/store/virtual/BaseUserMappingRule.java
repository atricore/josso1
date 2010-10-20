package org.josso.gateway.identity.service.store.virtual;

import org.josso.gateway.identity.service.BaseUser;

import java.util.Collection;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: BaseUserMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class BaseUserMappingRule {

    public Collection<BaseUser> select(Collection<BaseUser> sourceUsers) {
        return null;
    }

    public BaseUser join(Collection<BaseUser> selectedUsers) {
        return null;
    }

    public BaseUser transform(BaseUser jointUser) {
        return null;
    }

    public void validate(BaseUser transformedUser) {

    }



}
