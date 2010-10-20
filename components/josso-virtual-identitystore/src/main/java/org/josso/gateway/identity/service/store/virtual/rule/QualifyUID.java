package org.josso.gateway.identity.service.store.virtual.rule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.service.store.virtual.UIDMappingRule;
import org.josso.gateway.identity.service.store.virtual.BaseUIDMappingRule;

import java.util.Collection;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: QualifyUID.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="qualify-uid"
 * <p/>
 * Issue a qualified virtual user identifier entry by prefixing a source entry with
 * the supplied namespace URI.
 */
public class QualifyUID extends BaseUIDMappingRule implements UIDMappingRule {

    private static final Log logger = LogFactory.getLog(QualifyUID.class);

    private String namespace;

    public String transform(String jointUID) {
        logger.debug("Qualifying UID [" + jointUID + "] with namespace [" + namespace + "]");
        return getNamespace() != null ? getNamespace() + ",uid=" + jointUID : null;
    }


    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }



}
