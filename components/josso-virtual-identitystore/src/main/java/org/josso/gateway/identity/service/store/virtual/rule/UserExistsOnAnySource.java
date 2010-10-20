package org.josso.gateway.identity.service.store.virtual.rule;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.service.store.virtual.BaseUserExistsMappingRule;
import org.josso.gateway.identity.service.store.virtual.UserExistsMappingRule;
import org.josso.gateway.identity.service.store.virtual.UserExistsOutcome;

/**
 * Returns successful user exists outcome if userExists was successful 
 * on any source.
 * 
 * @org.apache.xbean.XBean element="user-exists-on-any-source"
 */
public class UserExistsOnAnySource extends BaseUserExistsMappingRule implements UserExistsMappingRule {

    private static final Log logger = LogFactory.getLog(UserExistsOnAnySource.class);

    public UserExistsOutcome join(Collection<UserExistsOutcome> selectedOutcomes) {
    	UserExistsOutcome jointOutcome = null;
        for (Iterator<UserExistsOutcome> selectedOutcomesIterator = selectedOutcomes.iterator(); selectedOutcomesIterator.hasNext();) {
            UserExistsOutcome selectedOutcome = selectedOutcomesIterator.next();
            if (selectedOutcome.isExists()) {
            	jointOutcome = selectedOutcome;
            	break;
            }
        }

        if (jointOutcome == null && selectedOutcomes != null && 
        		selectedOutcomes.size() > 0) {
        	jointOutcome = selectedOutcomes.iterator().next();
        }
        
        return jointOutcome;
    }


}