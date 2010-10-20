package org.josso.gateway.identity.service.store.virtual;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.josso.auth.Credential;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.BaseUser;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: RuleBasedIdentityDataMappingPolicy.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="rule-based-mapping-policy"
 * <p/>
 * An Identity Data Mapping Policy implementation which delegates identity mapping concerns to entity-specific rules.
 * There are 4 types of rules :
 * <p/>
 * a. User Mapping rules for selecting, joining, transforming and validating user entries.
 * b. Role Mapping rules for selecting, joining, transforming and validating user role entries.
 * c. Credential Mapping rules for selecting, joining, transforming and validating user credential entries.
 * d. User Identifier Mapping rules for selecting, joining, transforming and validating user identification entries.
 */
public class RuleBasedIdentityDataMappingPolicy implements IdentityDataMappingPolicy {
    private List<UserMappingRule> userMappingRules;
    private List<RoleMappingRule> roleMappingRules;
    private List<CredentialMappingRule> credentialMappingRules;
    private List<UIDMappingRule> uidMappingRules;
    private List<BindMappingRule> bindMappingRules;
    private List<UserExistsMappingRule> userExistsMappingRules;

    public Collection<BaseUser> selectUser(Collection<BaseUser> sourceUsers) throws VirtualIdentityStoreException {

        Collection<BaseUser> selectedUsers = sourceUsers;

        for (Iterator<UserMappingRule> userMappingRulesIterator = userMappingRules.iterator(); userMappingRulesIterator.hasNext();) {
            UserMappingRule userMappingRule = userMappingRulesIterator.next();

            Collection<BaseUser> users = userMappingRule.select(selectedUsers);

            if (users != null) {
                selectedUsers = users;
            }
        }

        return selectedUsers;
    }

    public BaseUser joinUsers(Collection<BaseUser> selectedUsers) throws VirtualIdentityStoreException {

        BaseUser jointUser = null;

        for (Iterator<UserMappingRule> userMappingRulesIterator = userMappingRules.iterator(); userMappingRulesIterator.hasNext();) {
            UserMappingRule userMappingRule = userMappingRulesIterator.next();

            jointUser = userMappingRule.join(selectedUsers);

            if (jointUser != null)
                break;
        }

        return jointUser;
    }

    public BaseUser transformUser(BaseUser jointUser) throws VirtualIdentityStoreException {
        BaseUser transformedUser = jointUser;

        for (Iterator<UserMappingRule> userMappingRulesIterator = userMappingRules.iterator(); userMappingRulesIterator.hasNext();) {
            UserMappingRule userMappingRule = userMappingRulesIterator.next();

            BaseUser user;
            user = userMappingRule.transform(transformedUser);

            if (user != null)
                transformedUser = user;

        }

        return transformedUser;
    }

    public void validateUser(BaseUser transformedUser) throws VirtualIdentityStoreException {

        for (Iterator<UserMappingRule> userMappingRulesIterator = userMappingRules.iterator(); userMappingRulesIterator.hasNext();) {
            UserMappingRule userMappingRule = userMappingRulesIterator.next();

            userMappingRule.validate(transformedUser);

        }

    }

    public Collection<BaseRole> selectRoles(Collection<BaseRole> sourceRoles) throws VirtualIdentityStoreException {

        Collection<BaseRole> selectedRoles = sourceRoles;

        for (Iterator<RoleMappingRule> roleMappingRulesIterator = roleMappingRules.iterator(); roleMappingRulesIterator.hasNext();) {
            RoleMappingRule roleMappingRule = roleMappingRulesIterator.next();

            Collection<BaseRole> roles = roleMappingRule.select(selectedRoles);

            if (roles != null) {
                selectedRoles = roles;
            }
        }

        return selectedRoles;
    }

    public Collection<BaseRole> joinRoles(Collection<BaseRole> selectedRoles) throws VirtualIdentityStoreException {

        Collection<BaseRole> jointRoles = selectedRoles;

        for (Iterator<RoleMappingRule> roleMappingRulesIterator = roleMappingRules.iterator(); roleMappingRulesIterator.hasNext();) {
            RoleMappingRule roleMappingRule = roleMappingRulesIterator.next();

            Collection<BaseRole> roles = roleMappingRule.join(selectedRoles);

            if (roles != null) {
                jointRoles = roles;
            }
        }

        return jointRoles;
    }

    public Collection<BaseRole> transformRoles(Collection<BaseRole> jointRoles) throws VirtualIdentityStoreException {
        Collection<BaseRole> transformedRoles = jointRoles;

        for (Iterator<RoleMappingRule> roleMappingRulesIterator = roleMappingRules.iterator(); roleMappingRulesIterator.hasNext();) {
            RoleMappingRule roleMappingRule = roleMappingRulesIterator.next();

            Collection<BaseRole> roles;
            roles = roleMappingRule.transform(transformedRoles);

            if (roles != null) {
                transformedRoles = roles;
            }

        }

        return transformedRoles;
    }

    public void validateRoles(Collection<BaseRole> transformedRoles) throws VirtualIdentityStoreException {

        for (Iterator<RoleMappingRule> roleMappingRulesIterator = roleMappingRules.iterator(); roleMappingRulesIterator.hasNext();) {
            RoleMappingRule roleMappingRule = roleMappingRulesIterator.next();

            roleMappingRule.validate(transformedRoles);

        }

    }

    public Collection<Credential> selectCredentials(Collection<Credential> sourceCredentials) throws VirtualIdentityStoreException {

        Collection<Credential> selectedCredentials = sourceCredentials;

        for (Iterator<CredentialMappingRule> credentialMappingRulesIterator = credentialMappingRules.iterator(); credentialMappingRulesIterator.hasNext();) {
            CredentialMappingRule credentialMappingRule = credentialMappingRulesIterator.next();

            Collection<Credential> credentials = credentialMappingRule.select(selectedCredentials);

            if (credentials != null) {
                selectedCredentials = credentials;
            }
        }

        return selectedCredentials;
    }

    public Collection<Credential> joinCredentials(Collection<Credential> selectedCredentials) throws VirtualIdentityStoreException {

        Collection<Credential> jointCredentials = selectedCredentials;

        for (Iterator<CredentialMappingRule> credentialMappingRulesIterator = credentialMappingRules.iterator(); credentialMappingRulesIterator.hasNext();) {
            CredentialMappingRule credentialMappingRule = credentialMappingRulesIterator.next();

            Collection<Credential> credentials = credentialMappingRule.join(selectedCredentials);

            if (credentials != null) {
                jointCredentials = credentials;
            }
        }

        return jointCredentials;
    }

    public Collection<Credential> transformCredentials(Collection<Credential> jointCredentials) throws VirtualIdentityStoreException {
        Collection<Credential> transformedCredentials = jointCredentials;

        for (Iterator<CredentialMappingRule> credentialMappingRulesIterator = credentialMappingRules.iterator(); credentialMappingRulesIterator.hasNext();) {
            CredentialMappingRule credentialMappingRule = credentialMappingRulesIterator.next();

            Collection<Credential> credentials;
            credentials = credentialMappingRule.transform(transformedCredentials);

            if (credentials != null) {
                transformedCredentials = credentials;
            }
        }

        return transformedCredentials;
    }

    public void validateCredentials(Collection<Credential> transformedCredentials) throws VirtualIdentityStoreException {

        for (Iterator<CredentialMappingRule> credentialMappingRulesIterator = credentialMappingRules.iterator(); credentialMappingRulesIterator.hasNext();) {
            CredentialMappingRule credentialMappingRule = credentialMappingRulesIterator.next();

            credentialMappingRule.validate(transformedCredentials);

        }

    }

    public Collection<String> selectUID(Collection<String> sourceUIDs) {
        Collection<String> selectedUIDs = sourceUIDs;

        for (Iterator<UIDMappingRule> uidMappingRulesIterator = uidMappingRules.iterator(); uidMappingRulesIterator.hasNext();) {
            UIDMappingRule uidMappingRule = uidMappingRulesIterator.next();

            Collection<String> uids = uidMappingRule.select(selectedUIDs);

            if (uids != null) {
                selectedUIDs = uids;
            }
        }

        return selectedUIDs;
    }

    public String joinUIDs(Collection<String> selectedUIDs) {
        String jointUID = null;

        for (Iterator<UIDMappingRule> uidMappingRulesIterator = uidMappingRules.iterator(); uidMappingRulesIterator.hasNext();) {
            UIDMappingRule uidMappingRule = uidMappingRulesIterator.next();

            String uid = uidMappingRule.join(selectedUIDs);

            if (uid != null) {
                jointUID = uid;
                break;
            }
        }

        return jointUID;
    }

    public String transformUID(String jointUID) {
        String transformedUID = jointUID;

        for (Iterator<UIDMappingRule> uidMappingRulesIterator = uidMappingRules.iterator(); uidMappingRulesIterator.hasNext();) {
            UIDMappingRule uidMappingRule = uidMappingRulesIterator.next();

            String uid;
            uid = uidMappingRule.transform(transformedUID);

            if (uid != null)
                transformedUID = uid;
        }

        return transformedUID;
    }

    public void validateUID(String transformedUID) {
        for (Iterator<UIDMappingRule> uidMappingRulesIterator = uidMappingRules.iterator(); uidMappingRulesIterator.hasNext();) {
            UIDMappingRule uidMappingRule = uidMappingRulesIterator.next();

            uidMappingRule.validate(transformedUID);

        }
    }

    public Collection<BindOutcome> selectBindOutcomes(Collection<BindOutcome> sourceOutcomes) {
		Collection<BindOutcome> selectedOutcomes = sourceOutcomes;

        for (Iterator<BindMappingRule> bindMappingRulesIterator = bindMappingRules.iterator(); bindMappingRulesIterator.hasNext();) {
            BindMappingRule bindMappingRule = bindMappingRulesIterator.next();

            Collection<BindOutcome> bindOutcomes = bindMappingRule.select(selectedOutcomes);

            if (bindOutcomes != null) {
                selectedOutcomes = bindOutcomes;
            }
        }

        return selectedOutcomes;
	}
    
    public BindOutcome joinBindOutcomes(Collection<BindOutcome> selectedOutcomes) {
    	BindOutcome jointOutcome = null;

        for (Iterator<BindMappingRule> bindMappingRulesIterator = bindMappingRules.iterator(); bindMappingRulesIterator.hasNext();) {
            BindMappingRule bindMappingRule = bindMappingRulesIterator.next();

            jointOutcome = bindMappingRule.join(selectedOutcomes);

            if (jointOutcome != null)
                break;
        }

        return jointOutcome;
	}

	public BindOutcome transformBindOutcome(BindOutcome jointEntry) {
		BindOutcome transformedOutcome = jointEntry;

        for (Iterator<BindMappingRule> bindMappingRulesIterator = bindMappingRules.iterator(); bindMappingRulesIterator.hasNext();) {
            BindMappingRule bindMappingRule = bindMappingRulesIterator.next();

            BindOutcome bindOutcome;
            bindOutcome = bindMappingRule.transform(transformedOutcome);

            if (bindOutcome != null)
                transformedOutcome = bindOutcome;
        }

        return transformedOutcome;
	}

	public void validateBindOutcome(BindOutcome transformedEntry) {
		for (Iterator<BindMappingRule> bindMappingRulesIterator = bindMappingRules.iterator(); bindMappingRulesIterator.hasNext();) {
            BindMappingRule bindMappingRule = bindMappingRulesIterator.next();
            bindMappingRule.validate(transformedEntry);
        }
	}
	
	public Collection<UserExistsOutcome> selectUserExistsOutcomes(Collection<UserExistsOutcome> sourceOutcomes) {
		Collection<UserExistsOutcome> selectedOutcomes = sourceOutcomes;

        for (Iterator<UserExistsMappingRule> userExistsMappingRulesIterator = userExistsMappingRules.iterator(); userExistsMappingRulesIterator.hasNext();) {
            UserExistsMappingRule userExistsMappingRule = userExistsMappingRulesIterator.next();

            Collection<UserExistsOutcome> userExistsOutcomes = userExistsMappingRule.select(selectedOutcomes);

            if (userExistsOutcomes != null) {
                selectedOutcomes = userExistsOutcomes;
            }
        }

        return selectedOutcomes;
	}
    
    public UserExistsOutcome joinUserExistsOutcomes(Collection<UserExistsOutcome> selectedOutcomes) {
    	UserExistsOutcome jointOutcome = null;

        for (Iterator<UserExistsMappingRule> userExistsMappingRulesIterator = userExistsMappingRules.iterator(); userExistsMappingRulesIterator.hasNext();) {
            UserExistsMappingRule userExistsMappingRule = userExistsMappingRulesIterator.next();

            jointOutcome = userExistsMappingRule.join(selectedOutcomes);

            if (jointOutcome != null)
                break;
        }

        return jointOutcome;
	}

	public UserExistsOutcome transformUserExistsOutcome(UserExistsOutcome jointEntry) {
		UserExistsOutcome transformedOutcome = jointEntry;

        for (Iterator<UserExistsMappingRule> userExistsMappingRulesIterator = userExistsMappingRules.iterator(); userExistsMappingRulesIterator.hasNext();) {
            UserExistsMappingRule userExistsMappingRule = userExistsMappingRulesIterator.next();

            UserExistsOutcome userExistsOutcome;
            userExistsOutcome = userExistsMappingRule.transform(transformedOutcome);

            if (userExistsOutcome != null)
                transformedOutcome = userExistsOutcome;
        }

        return transformedOutcome;
	}

	public void validateUserExistsOutcome(UserExistsOutcome transformedEntry) {
		for (Iterator<UserExistsMappingRule> userExistsMappingRulesIterator = userExistsMappingRules.iterator(); userExistsMappingRulesIterator.hasNext();) {
            UserExistsMappingRule userExistsMappingRule = userExistsMappingRulesIterator.next();
            userExistsMappingRule.validate(transformedEntry);
        }
	}
	
    /**
     * @return
     * @org.apache.xbean.Property alias="user-mapping-rules" nestedType="org.josso.gateway.identity.service.store.virtual.UserMappingRule"
     */
    public List<UserMappingRule> getUserMappingRules() {
        return userMappingRules;
    }

    public void setUserMappingRules(List<UserMappingRule> userMappingRules) {
        this.userMappingRules = userMappingRules;
    }

    /**
     * @return
     * @org.apache.xbean.Property alias="roles-mapping-rules" nestedType="org.josso.gateway.identity.service.store.virtual.RoleMappingRule"
     */
    public List<RoleMappingRule> getRoleMappingRules() {
        return roleMappingRules;
    }

    public void setRoleMappingRules(List<RoleMappingRule> roleMappingRules) {
        this.roleMappingRules = roleMappingRules;
    }

    /**
     * @return
     * @org.apache.xbean.Property alias="credentials-mapping-rules" nestedType="org.josso.gateway.identity.service.store.virtual.CredentialMappingRule"
     */
    public List<CredentialMappingRule> getCredentialMappingRules() {
        return credentialMappingRules;
    }

    public void setCredentialMappingRules(List<CredentialMappingRule> credentialMappingRules) {
        this.credentialMappingRules = credentialMappingRules;
    }

    /**
     * @return
     * @org.apache.xbean.Property alias="uid-mapping-rules" nestedType="org.josso.gateway.identity.service.store.virtual.UIDMappingRule"
     */
    public List<UIDMappingRule> getUIDMappingRules() {
        return uidMappingRules;
    }

    public void setUIDMappingRules(List<UIDMappingRule> uidMappingRules) {
        this.uidMappingRules = uidMappingRules;
    }
    
    /**
     * @return
     * @org.apache.xbean.Property alias="bind-mapping-rules" nestedType="org.josso.gateway.identity.service.store.virtual.BindMappingRule"
     */
    public List<BindMappingRule> getBindMappingRules() {
        return bindMappingRules;
    }

    public void setBindMappingRules(List<BindMappingRule> bindMappingRules) {
        this.bindMappingRules = bindMappingRules;
    }
    
    /**
     * @return
     * @org.apache.xbean.Property alias="user-exists-mapping-rules" nestedType="org.josso.gateway.identity.service.store.virtual.UserExistsMappingRule"
     */
    public List<UserExistsMappingRule> getUserExistsMappingRules() {
        return userExistsMappingRules;
    }

    public void setUserExistsMappingRules(List<UserExistsMappingRule> userExistsMappingRules) {
        this.userExistsMappingRules = userExistsMappingRules;
    }
}
