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
package org.josso.gateway.assertion.service.store;

import org.josso.gateway.assertion.AuthenticationAssertion;
import org.josso.gateway.assertion.exceptions.AssertionException;

/**
 * Represents a resource to store assertions.
 * Implementations define the specific persistence mechanism to store assertions.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: AssertionStore.java 602 2008-08-20 23:58:11Z gbrigand $
 */

public interface AssertionStore {

    /**
     * Return the number of Assertions present in this Store.
     */
    int getSize() throws AssertionException;


    /**
     * Return an array containing the assertion identifiers of all Assertions
     * currently saved in this Store.  If there are no such Assertions, a
     * zero-length array is returned.
     */
    String[] keys() throws AssertionException;

    /**
     * Return an array of all AuthenticationAssertions in this store.  If there are no
     * assertions, then return a zero-length array.
     */
    AuthenticationAssertion[] loadAll() throws AssertionException;

    /**
     * Load and return the AuthenticationAssertion associated with the specified assertion
     * identifier from this Store, without removing it.  If there is no
     * such stored AuthenticationAssertion, return <code>null</code>.
     *
     * @param id AuthenticationAssertion identifier of the assertion to load
     */
    AuthenticationAssertion load(String id)
            throws AssertionException;


    /**
     * Remove the AuthenticationAssertion with the specified assertion identifier from
     * this Store, if present.  If no such AuthenticationAssertion is present, this method
     * takes no action.
     *
     * @param id AuthenticationAssertion identifier of the AuthenticationAssertion to be removed
     */
    void remove(String id) throws AssertionException;


    /**
     * Remove all Assertions from this Store.
     */
    void clear() throws AssertionException;

    /**
     * Save the specified AuthenticationAssertion into this Store.  Any previously saved
     * information for the associated assertion identifier is replaced.
     *
     * @param assertion AuthenticationAssertion to be saved
     */
    void save(AuthenticationAssertion assertion) throws AssertionException;


}