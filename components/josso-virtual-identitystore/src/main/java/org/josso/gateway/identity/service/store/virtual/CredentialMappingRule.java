package org.josso.gateway.identity.service.store.virtual;

import org.josso.auth.Credential;

import java.util.Collection;

/**
 * Service Provider Interface (SPI) for realizing components capable of mapping
 * multiple source credential entries to a set of virtual credential entries.
 * <p/>
 * Credential Mapping Rules support :
 * <p/>
 * a. selecting a set of credential entries from the entire set of credentials entries provided by the associated sources
 * b. joining multiple source credential entries
 * c. transforming the joint credential entries
 * d. validating the virtual credential entries
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: CredentialMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public interface CredentialMappingRule {

    Collection<Credential> select(Collection<Credential> sourceCredentials);

    Collection<Credential> join(Collection<Credential> selectedCredentials);

    Collection<Credential> transform(Collection<Credential> jointCredentials);

    void validate(Collection<Credential> transformedCredentials);

}
