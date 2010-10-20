/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2009, Atricore, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */
package org.josso.gateway.identity.service.store.virtual;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.auth.Credential;
import org.josso.auth.CredentialKey;
import org.josso.auth.CredentialProvider;
import org.josso.auth.CredentialStore;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.store.AbstractStore;
import org.josso.gateway.identity.service.store.UserKey;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: VirtualIdentityStore.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="virtual-store"
 * <p/>
 * Virtual Identity Store which builds on multiple identity data sources for looking up user and entitlement
 * records.
 * It enables providing a unified view of one or more identity silos for consumption by Single Sign-On services.
 * It is useful in scenarios with multiple idenitity silos, potentially based on multiple technologies and information
 * models, for which an authoriative source for user information cannot be created or significant
 * effort is required.
 */

public class VirtualIdentityStore extends AbstractStore {

    private static final Log logger = LogFactory.getLog(VirtualIdentityStore.class);

    private List<IdentitySource> identitySources;
    private IdentityDataMappingPolicy identityDataMappingPolicy;


    public VirtualIdentityStore() {
        super();

    }

    // Identity Store SPI implementation

    public BaseUser loadUser(UserKey key) throws NoSuchUserException, SSOIdentityException {
        BaseUser virtualUser = null;

        Collection<BaseUser> sourceUsers = new ArrayList<BaseUser>();

        // Collect user information from the configured sources

        for (Iterator<IdentitySource> identitySourceIterator = identitySources.iterator(); identitySourceIterator.hasNext();) {
            IdentitySource identitySource = identitySourceIterator.next();

            BaseUser sourceUser;
            try {
            	sourceUser = identitySource.getBackingIdentityStore().loadUser(key);
            	if (sourceUser != null) {
                    sourceUsers.add(sourceUser);
                }
            } catch (NoSuchUserException e) {
            	// do nothing ?
            }
        }

        if (sourceUsers.size() == 0) {
        	throw new VirtualNoSuchUserException(key);
        }
        
        // Use the configured mapping policy to select user entries

        Collection<BaseUser> selectedUsers;
        selectedUsers = identityDataMappingPolicy.selectUser(Collections.unmodifiableCollection(sourceUsers));

        if (selectedUsers != null && selectedUsers.size() > 0) {

            // Use the configured mapping policy to create a joint result from selected entries
            BaseUser jointEntry;
            jointEntry = identityDataMappingPolicy.joinUsers(selectedUsers);

            if (jointEntry == null) {
                throw new SSOIdentityException("Joining User Entries Failed");
            }

            // Use the configured mapping policy to transformUser joint entry
            BaseUser transformedEntry;
            transformedEntry = identityDataMappingPolicy.transformUser(jointEntry);

            // Transformation is not mandatory
            if (transformedEntry == null)
                transformedEntry = jointEntry;

            // Use the configured mapping policy to assert the transformed joint entry
            identityDataMappingPolicy.validateUser(transformedEntry);
            virtualUser = transformedEntry;
        }

        return virtualUser;
    }

    public BaseRole[] findRolesByUserKey(UserKey key) throws SSOIdentityException {
        Collection<BaseRole> virtualUserRoles = new ArrayList<BaseRole>();

        // Collect user information from the configured sources

        for (Iterator<IdentitySource> identitySourceIterator = identitySources.iterator(); identitySourceIterator.hasNext();) {
            IdentitySource identitySource = identitySourceIterator.next();

            BaseRole[] baseRoles;
            baseRoles = identitySource.getBackingIdentityStore().findRolesByUserKey(key);

            if (baseRoles != null) {
                virtualUserRoles.addAll(Arrays.asList(baseRoles));
            }
        }

        // Use the configured mapping policy to select role entries

        Collection<BaseRole> selectedRoles;
        selectedRoles = identityDataMappingPolicy.selectRoles(Collections.unmodifiableCollection(virtualUserRoles));

        if (selectedRoles != null && selectedRoles.size() > 0) {

            // Use the configured mapping policy to create a joint result from selected entries
            Collection<BaseRole> jointRoles;
            jointRoles = identityDataMappingPolicy.joinRoles(selectedRoles);

            if (jointRoles == null) {
                throw new SSOIdentityException("Error Joining User Roles");
            }

            // Use the configured mapping policy to transformUser joint entry
            Collection<BaseRole> transformedRoles;
            transformedRoles = identityDataMappingPolicy.transformRoles(jointRoles);

            // Transformation is not mandatory
            if (transformedRoles == null)
                transformedRoles = jointRoles;

            // Use the configured mapping policy to assert the transformed joint entry
            identityDataMappingPolicy.validateRoles(transformedRoles);
            virtualUserRoles = transformedRoles;
        }

        return virtualUserRoles.toArray(new BaseRole[0]);
    }

    public Credential[] loadCredentials(CredentialKey key, CredentialProvider cp) throws SSOIdentityException {
        Collection<Credential> virtualUserCredentials = new ArrayList<Credential>();

        // Collect user information from the configured sources

        for (Iterator<IdentitySource> identitySourceIterator = identitySources.iterator(); identitySourceIterator.hasNext();) {
            IdentitySource identitySource = identitySourceIterator.next();

            Credential[] Credentials;
            Credentials = ((CredentialStore) identitySource.getBackingIdentityStore()).loadCredentials(key, cp);

            if (Credentials != null) {
                virtualUserCredentials.addAll(Arrays.asList(Credentials));
            }
        }

        // Use the configured mapping policy to select credential entries

        Collection<Credential> selectedCredentials;
        selectedCredentials = identityDataMappingPolicy.selectCredentials(Collections.unmodifiableCollection(virtualUserCredentials));

        if (selectedCredentials != null && selectedCredentials.size() > 0) {

            // Use the configured mapping policy to create a joint result from selected entries
            Collection<Credential> jointCredentials;
            jointCredentials = identityDataMappingPolicy.joinCredentials(selectedCredentials);

            if (jointCredentials == null) {
                throw new SSOIdentityException("Error Joining User Credentials");
            }

            // Use the configured mapping policy to transformUser joint entry
            Collection<Credential> transformedCredentials;
            transformedCredentials = identityDataMappingPolicy.transformCredentials(jointCredentials);

            // Transformation is not mandatory
            if (transformedCredentials == null)
                transformedCredentials = jointCredentials;

            // Use the configured mapping policy to assert the transformed joint entry
            identityDataMappingPolicy.validateCredentials(transformedCredentials);
            virtualUserCredentials = transformedCredentials;
        }

        return virtualUserCredentials.toArray(new Credential[0]);
    }

    public String loadUID(CredentialKey key, CredentialProvider cp) throws SSOIdentityException {
        String virtualUID = null;

        Collection<String> sourceUIDs = new ArrayList<String>();

        // Collect UID information from the configured sources
        for (Iterator<IdentitySource> identitySourceIterator = identitySources.iterator(); identitySourceIterator.hasNext();) {
            IdentitySource identitySource = identitySourceIterator.next();

            String sourceUID;
            sourceUID = ((CredentialStore) identitySource.getBackingIdentityStore()).loadUID(key, cp);

            if (sourceUID != null) {
                sourceUIDs.add(sourceUID);
            }

        }

        // Use the configured mapping policy to select UID entries

        Collection<String> selectedUIDs;
        selectedUIDs = identityDataMappingPolicy.selectUID(Collections.unmodifiableCollection(sourceUIDs));

        if (selectedUIDs != null && selectedUIDs.size() > 0) {

            // Use the configured mapping policy to create a joint result from selected entries
            String jointEntry;
            jointEntry = identityDataMappingPolicy.joinUIDs(selectedUIDs);

            if (jointEntry == null) {
                throw new SSOIdentityException("Joining UID Entries Failed");
            }

            // Use the configured mapping policy to transformUID joint entry
            String transformedEntry;
            transformedEntry = identityDataMappingPolicy.transformUID(jointEntry);

            // Transformation is not mandatory
            if (transformedEntry == null)
                transformedEntry = jointEntry;

            // Use the configured mapping policy to assert the transformed joint entry
            identityDataMappingPolicy.validateUID(transformedEntry);
            virtualUID = transformedEntry;
        }

        return virtualUID;
    }

    @Override
	public boolean userExists(UserKey key) throws SSOIdentityException {
		UserExistsOutcome virtualUserExistsOutcome = null;

		Collection<UserExistsOutcome> sourceUserExistsOutcomes = new ArrayList<UserExistsOutcome>();
        
        // Collect user exists outcome information from the configured sources

        for (Iterator<IdentitySource> identitySourceIterator = getIdentitySources().iterator(); identitySourceIterator.hasNext();) {
            IdentitySource identitySource = identitySourceIterator.next();
            boolean userExists = identitySource.getBackingIdentityStore().userExists(key);
            sourceUserExistsOutcomes.add(new UserExistsOutcome(userExists));
        }

        // Use the configured mapping policy to select user exists outcomes

        Collection<UserExistsOutcome> selectedUserExistsOutcomes;
        selectedUserExistsOutcomes = getIdentityDataMappingPolicy().selectUserExistsOutcomes(Collections.unmodifiableCollection(sourceUserExistsOutcomes));

        if (selectedUserExistsOutcomes != null && selectedUserExistsOutcomes.size() > 0) {

            // Use the configured mapping policy to create a joint result from selected entries
            UserExistsOutcome jointEntry;
            jointEntry = getIdentityDataMappingPolicy().joinUserExistsOutcomes(selectedUserExistsOutcomes);

            if (jointEntry == null) {
                throw new SSOIdentityException("Joining User Exists Outcomes Failed");
            }

            // Use the configured mapping policy to transformUserExistsOutome joint entry
            UserExistsOutcome transformedEntry;
            transformedEntry = getIdentityDataMappingPolicy().transformUserExistsOutcome(jointEntry);

            // Transformation is not mandatory
            if (transformedEntry == null)
                transformedEntry = jointEntry;

            // Use the configured mapping policy to assert the transformed joint entry
            getIdentityDataMappingPolicy().validateUserExistsOutcome(transformedEntry);
            virtualUserExistsOutcome = transformedEntry;
        }

        if (virtualUserExistsOutcome == null) {
        	throw new SSOIdentityException("Virtual User Exists Failed");
        }
        
        return virtualUserExistsOutcome.isExists();
	}
    
    /**
     * @return
     * @org.apache.xbean.Property alias="sources"
     * nestedType="org.josso.gateway.identity.service.store.virtual.IdentitySource"
     */
    public List<IdentitySource> getIdentitySources() {
        return identitySources;
    }

    public void setIdentitySources(List<IdentitySource> identitySources) {
        this.identitySources = identitySources;
    }

    /**
     * @return
     * @org.apache.xbean.Property alias="mapping-policy"
     */
    public IdentityDataMappingPolicy getIdentityDataMappingPolicy() {
        return identityDataMappingPolicy;
    }

    public void setIdentityDataMappingPolicy(IdentityDataMappingPolicy identityDataMappingPolicy) {
        this.identityDataMappingPolicy = identityDataMappingPolicy;
    }
}
