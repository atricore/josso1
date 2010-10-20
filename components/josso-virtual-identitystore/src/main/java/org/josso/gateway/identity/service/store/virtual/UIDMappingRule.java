package org.josso.gateway.identity.service.store.virtual;

import java.util.Collection;

/**
 * Service Provider Interface (SPI) for realizing components capable of mapping
 * multiple source user id entries to a set of virtual user id entries.
 * <p/>
 * User ID Mapping Rules support :
 * <p/>
 * a. selecting a set of UID entries from the entire set of UIDs entries provided by the associated sources
 * b. joining multiple source UID entries into one virtual UID
 * c. transforming the joint UID
 * d. validating the virtual UID
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: UIDMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public interface UIDMappingRule {
    Collection<String> select(Collection<String> selectedUIDs);

    String join(Collection<String> selectedUIDs);

    String transform(String transformedUID);

    void validate(String transformedUID);
}
