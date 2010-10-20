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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.auth.BindableCredentialStore;
import org.josso.auth.exceptions.SSOAuthenticationException;

/**
 * Virtual Bindable Identity Store which builds on multiple identity data sources for looking up user and entitlement
 * records.
 * It enables providing a unified view of one or more identity silos for consumption by Single Sign-On services.
 * It is useful in scenarios with multiple idenitity silos, potentially based on multiple technologies and information
 * models, for which an authoriative source for user information cannot be created or significant
 * effort is required.
 * 
 * @org.apache.xbean.XBean element="virtual-bind-store"
 */
public class VirtualBindIdentityStore extends VirtualIdentityStore implements BindableCredentialStore {

    private static final Log logger = LogFactory.getLog(VirtualBindIdentityStore.class);

    public VirtualBindIdentityStore() {
        super();
    }

	public boolean bind(String username, String password)
			throws SSOAuthenticationException {
		
		BindOutcome virtualBindOutcome = null;

		Collection<BindOutcome> sourceBindOutcomes = new ArrayList<BindOutcome>();
        
        // Collect bind outcome information from the configured sources

        for (Iterator<IdentitySource> identitySourceIterator = getIdentitySources().iterator(); identitySourceIterator.hasNext();) {
            IdentitySource identitySource = identitySourceIterator.next();

            if (identitySource.getBackingIdentityStore() instanceof BindableCredentialStore) {
            	try {
            		boolean bindResult = ((BindableCredentialStore)identitySource.getBackingIdentityStore()).bind(username, password);
            		sourceBindOutcomes.add(new BindOutcome(bindResult));
            	} catch (SSOAuthenticationException e) {
            		sourceBindOutcomes.add(new BindOutcome(e));
            	}
            }
        }

        // Use the configured mapping policy to select bind outcomes

        Collection<BindOutcome> selectedBindOutcomes;
        selectedBindOutcomes = getIdentityDataMappingPolicy().selectBindOutcomes(Collections.unmodifiableCollection(sourceBindOutcomes));

        if (selectedBindOutcomes != null && selectedBindOutcomes.size() > 0) {

            // Use the configured mapping policy to create a joint result from selected entries
            BindOutcome jointEntry;
            jointEntry = getIdentityDataMappingPolicy().joinBindOutcomes(selectedBindOutcomes);

            if (jointEntry == null) {
                throw new VirtualSSOAuthenticationException("Joining Bind Outcomes Failed");
            }

            // Use the configured mapping policy to transformBindOutome joint entry
            BindOutcome transformedEntry;
            transformedEntry = getIdentityDataMappingPolicy().transformBindOutcome(jointEntry);

            // Transformation is not mandatory
            if (transformedEntry == null)
                transformedEntry = jointEntry;

            // Use the configured mapping policy to assert the transformed joint entry
            getIdentityDataMappingPolicy().validateBindOutcome(transformedEntry);
            virtualBindOutcome = transformedEntry;
        }

        if (virtualBindOutcome == null) {
        	throw new VirtualSSOAuthenticationException("Virtual Binding Failed");
        }
        
        if (virtualBindOutcome.isSuccessful()) {
        	return true;
        } else if (virtualBindOutcome.getException() != null) {
        	//we must throw original exception (in case of AuthenticationFailureException)
        	throw virtualBindOutcome.getException();
        } else {
        	return false;
        }
	}
}
