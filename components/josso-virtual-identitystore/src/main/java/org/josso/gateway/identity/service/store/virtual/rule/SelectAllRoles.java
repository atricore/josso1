package org.josso.gateway.identity.service.store.virtual.rule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.store.virtual.RoleMappingRule;
import org.josso.gateway.identity.service.store.virtual.BaseRoleMappingRule;

import java.util.Collection;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: SelectAllRoles.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="select-all-roles"
 * <p/>
 * Selects all the whole set of role records supplied by the configured sources.
 */
public class SelectAllRoles extends BaseRoleMappingRule implements RoleMappingRule {

    private static final Log logger = LogFactory.getLog(SelectAllRoles.class);

    public Collection<BaseRole> select(Collection<BaseRole> sourceRoles) {
        return sourceRoles;
    }

}