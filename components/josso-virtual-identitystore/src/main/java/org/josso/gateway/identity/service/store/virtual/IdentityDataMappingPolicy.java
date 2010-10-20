package org.josso.gateway.identity.service.store.virtual;

import java.util.Collection;

import org.josso.auth.Credential;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.BaseUser;

/**
 * Service Provider Interface for Identity Data Mapping Policies.
 * Identity Mapping Policies can be realized for acting upon user identity lookup operations
 * in order to supply virtual user and entitlement records from records provided by associated sources.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: IdentityDataMappingPolicy.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public interface IdentityDataMappingPolicy {
    Collection<BaseUser> selectUser(Collection<BaseUser> sourceUsers) throws VirtualIdentityStoreException;

    BaseUser joinUsers(Collection<BaseUser> selectedUsers) throws VirtualIdentityStoreException;

    BaseUser transformUser(BaseUser jointEntry) throws VirtualIdentityStoreException;

    void validateUser(BaseUser transformedEntry) throws VirtualIdentityStoreException;

    Collection<BaseRole> selectRoles(Collection<BaseRole> sourceRoles) throws VirtualIdentityStoreException;

    Collection<BaseRole> joinRoles(Collection<BaseRole> selectedRoles) throws VirtualIdentityStoreException;

    Collection<BaseRole> transformRoles(Collection<BaseRole> jointRoles) throws VirtualIdentityStoreException;

    void validateRoles(Collection<BaseRole> transformedRoles) throws VirtualIdentityStoreException;

    Collection<Credential> selectCredentials(Collection<Credential> sourceCredentials) throws VirtualIdentityStoreException;

    Collection<Credential> joinCredentials(Collection<Credential> selectedCredentials) throws VirtualIdentityStoreException;

    Collection<Credential> transformCredentials(Collection<Credential> jointCredentials) throws VirtualIdentityStoreException;

    void validateCredentials(Collection<Credential> transformedCredentials) throws VirtualIdentityStoreException;

    Collection<String> selectUID(Collection<String> strings);

    String joinUIDs(Collection<String> selectedUIDs);

    String transformUID(String jointEntry);

    void validateUID(String transformedEntry);
    
    Collection<BindOutcome> selectBindOutcomes(Collection<BindOutcome> sourceOutcomes);

    BindOutcome joinBindOutcomes(Collection<BindOutcome> selectedOutcomes);

    BindOutcome transformBindOutcome(BindOutcome jointEntry);

    void validateBindOutcome(BindOutcome transformedEntry);
    
    Collection<UserExistsOutcome> selectUserExistsOutcomes(Collection<UserExistsOutcome> sourceOutcomes);

    UserExistsOutcome joinUserExistsOutcomes(Collection<UserExistsOutcome> selectedOutcomes);

    UserExistsOutcome transformUserExistsOutcome(UserExistsOutcome jointEntry);

    void validateUserExistsOutcome(UserExistsOutcome transformedEntry);
}
