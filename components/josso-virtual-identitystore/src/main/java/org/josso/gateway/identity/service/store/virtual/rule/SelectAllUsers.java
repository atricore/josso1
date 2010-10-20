package org.josso.gateway.identity.service.store.virtual.rule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.store.virtual.UserMappingRule;
import org.josso.gateway.identity.service.store.virtual.BaseRoleMappingRule;
import org.josso.gateway.identity.service.store.virtual.BaseUserMappingRule;

import java.util.Collection;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: SelectAllUsers.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="select-all-users"
 * <p/>
 * Selects all the whole set of user records supplied by the configured sources.
 */
public class SelectAllUsers extends BaseUserMappingRule implements UserMappingRule {
    private static final Log logger = LogFactory.getLog(SelectAllUsers.class);

    public Collection<BaseUser> select(Collection<BaseUser> selectedUsers) {
        return selectedUsers;
    }

}
