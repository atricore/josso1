package org.josso.gateway.identity.service.store.virtual;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: BaseUIDMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class BaseUIDMappingRule {

    public Collection<String> select(Collection<String> sourceUIDs) {
        return null;
    }

    public String join(Collection<String> selectedUIDs) {
        return null;
    }

    public String transform(String transformedUID) {
        return null;
    }

    public void validate(String transformedUID) {

    }

}
