package org.josso.gateway.identity.service.store.virtual.rule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.service.store.virtual.UIDMappingRule;
import org.josso.gateway.identity.service.store.virtual.BaseUIDMappingRule;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: SelectFirstUID.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="select-first-uid"
 * <p/>
 * Selects the first UID from the set supplied by the configured sources.
 */
public class SelectFirstUID extends BaseUIDMappingRule implements UIDMappingRule {

    private static final Log logger = LogFactory.getLog(SelectFirstUID.class);

    public Collection<String> select(Collection<String> sourceUIDs) {
        Collection<String> firstUID = new ArrayList<String>();

        // select only the first UID of the supplied set
        if (sourceUIDs != null && sourceUIDs.size() > 0) {
            firstUID.add((String) sourceUIDs.toArray()[0]);
        }

        return firstUID.size() == 1 ? firstUID : null;
    }

    public String join(Collection<String> selectedUIDs) {
        return selectedUIDs.size() == 1 ? (String) selectedUIDs.toArray()[0] : null;
    }

    public String transform(String transformedUID) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void validate(String transformedUID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}