package org.josso.gateway.identity.service.store.virtual;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: BaseBindMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class BaseBindMappingRule {

    public Collection<BindOutcome> select(Collection<BindOutcome> sourceOutcomes) {
        return null;
    }

    public BindOutcome join(Collection<BindOutcome> selectedOutcomes) {
        return null;
    }

    public BindOutcome transform(BindOutcome jointROutcome) {
        return null;
    }

    public void validate(BindOutcome transformedOutcome) {

    }

}
