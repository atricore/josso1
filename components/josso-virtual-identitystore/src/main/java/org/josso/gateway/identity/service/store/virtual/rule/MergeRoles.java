package org.josso.gateway.identity.service.store.virtual.rule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.store.virtual.RoleMappingRule;
import org.josso.gateway.identity.service.store.virtual.BaseRoleMappingRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: MergeRoles.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="merge-roles"
 * <p/>
 * Create a set of virtual roles by leaving out duplicate from source roles.
 */
public class MergeRoles extends BaseRoleMappingRule implements RoleMappingRule {

    private static final Log logger = LogFactory.getLog(MergeRoles.class);

    public Collection<BaseRole> join(Collection<BaseRole> selectedRoles) {
        HashSet<BaseRole> jointRoleSet = new HashSet<BaseRole>();

        logger.debug("Joining roles  " + selectedRoles + " with duplicate removal");

        for (Iterator<BaseRole> selectedRolesIterator = selectedRoles.iterator(); selectedRolesIterator.hasNext();) {
            BaseRole selectedRole = selectedRolesIterator.next();

            jointRoleSet.add(selectedRole);
        }

        return jointRoleSet;
    }

}