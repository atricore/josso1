package org.josso.gateway.identity.service.store.virtual;

import org.josso.gateway.identity.service.BaseRole;

import java.util.Collection;

/**
 * Service Provider Interface (SPI) for realizing components capable of mapping
 * multiple source roles entries to virtual roles entries.
 * <p/>
 * Role Mapping Rules support :
 * <p/>
 * a. selecting a set of roles entries from the entire set of role entries provided by the associated sources
 * b. joining multiple source role entries
 * c. transforming the joint role entry
 * d. validating the virtual role entry
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: RoleMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public interface RoleMappingRule {
    Collection<BaseRole> select(Collection<BaseRole> sourceRoles);

    Collection<BaseRole> join(Collection<BaseRole> selectedRoles);

    Collection<BaseRole> transform(Collection<BaseRole> jointRoles);

    void validate(Collection<BaseRole> transformedRoles);

}
