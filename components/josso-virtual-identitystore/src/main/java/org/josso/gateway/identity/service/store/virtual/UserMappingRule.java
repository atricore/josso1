package org.josso.gateway.identity.service.store.virtual;

import org.josso.gateway.identity.service.BaseUser;

import java.util.Collection;

/**
 * Service Provider Interface (SPI) for realizing components capable of mapping
 * multiple source user entries to one virtual user entry.
 * <p/>
 * User Mapping Rules support :
 * <p/>
 * a. selecting a set of user entries from the entire set of entries provided by the associated sources.
 * b. joining multiple source user entries into one
 * c. transforming the joint user entry
 * d. validating the virtual user entry
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: UserMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public interface UserMappingRule {
    Collection<BaseUser> select(Collection<BaseUser> selectedUsers);

    BaseUser join(Collection<BaseUser> selectedUsers);

    BaseUser transform(BaseUser transformedUser);

    void validate(BaseUser transformedUser);
}
