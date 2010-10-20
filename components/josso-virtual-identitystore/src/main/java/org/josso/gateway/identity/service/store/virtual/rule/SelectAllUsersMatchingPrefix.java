package org.josso.gateway.identity.service.store.virtual.rule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.store.virtual.UserMappingRule;
import org.josso.gateway.identity.service.store.virtual.BaseUserMappingRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: SelectAllUsersMatchingPrefix.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="select-all-users-prefix"
 * <p/>
 * Selects only users matching the supplied prefix.
 */
public class SelectAllUsersMatchingPrefix extends BaseUserMappingRule implements UserMappingRule {

    private static final Log logger = LogFactory.getLog(SelectAllUsersMatchingPrefix.class);

    private String usernamePrefix;

    public Collection<BaseUser> select(Collection<BaseUser> selectedUsers) {
        Collection<BaseUser> usersMatchingPrefix = new ArrayList<BaseUser>();

        for (Iterator<BaseUser> baseUserIterator = selectedUsers.iterator(); baseUserIterator.hasNext();) {
            BaseUser baseUser = baseUserIterator.next();

            if (baseUser.getName().startsWith(usernamePrefix)) {
                usersMatchingPrefix.add(baseUser);
            }
        }

        return Collections.unmodifiableCollection(usersMatchingPrefix);
    }

    public String getUsernamePrefix() {
        return usernamePrefix;
    }

    public void setUsernamePrefix(String usernamePrefix) {
        this.usernamePrefix = usernamePrefix;
    }
}