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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.assertion.AuthenticationAssertion;
import org.josso.gateway.assertion.exceptions.AssertionException;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @org.apache.xbean.XBean element="memory-store"
 * Thread-safe authentication assertion storage component based on memory persistence.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id$
 */
public class MemoryAssertionStore extends AbstractAssertionStore {

    private static final Log logger = LogFactory.getLog(MemoryAssertionStore.class);

    private Map _assertions;

    public MemoryAssertionStore() {
        _assertions = new HashMap();
    }

    public int getSize() throws AssertionException {
        synchronized (_assertions) {
            return _assertions.size();
        }
    }

    /**
     * Return an array containing the assertion identifiers of all Assertions
     * currently saved in this Store.  If there are no such Assertions, a
     * zero-length array is returned.
     */
    public String[] keys() throws AssertionException {
        synchronized (_assertions) {
            return (String[]) _assertions.keySet().toArray(new String[_assertions.size()]);
        }
    }

    /**
     * Return an array of all AuthenticationAssertions in this store.  If there are not
     * assertions, then return a zero-length array.
     */
    public AuthenticationAssertion[] loadAll() throws AssertionException {
        synchronized (_assertions) {
            return (AuthenticationAssertion[]) _assertions.values().toArray(new AuthenticationAssertion[_assertions.size()]);
        }
    }

    /**
     * Load and return the AuthenticationAssertion associated with the specified assertion
     * identifier from this Store, without removing it.  If there is no
     * such stored AuthenticationAssertion, return <code>null</code>.
     *
     * @param id AuthenticationAssertion identifier of the assertion to load
     */
    public AuthenticationAssertion load(String id) throws AssertionException {
        AuthenticationAssertion s = null;
        synchronized (_assertions) {
            s = (AuthenticationAssertion) _assertions.get(id);
        }

        if (logger.isDebugEnabled())
            logger.debug("[load(" + id + ")] Assertion " + (s == null ? " not" : "") + " found");

        return s;

    }

    /**
     * Remove the AuthenticationAssertion with the specified assertion identifier from
     * this Store, if present.  If no such AuthenticationAssertion is present, this method
     * takes no action.
     *
     * @param id AuthenticationAssertion identifier of the AuthenticationAssertion to be removed
     */
    public void remove(String id) throws AssertionException {
        AuthenticationAssertion assertion = null;
        synchronized (_assertions) {
            assertion = (AuthenticationAssertion) _assertions.remove(id);
        }

        if (logger.isDebugEnabled())
            logger.debug("[remove(" + id + ")] Assertion " + (assertion == null ? " not" : "") + " found");
    }

    /**
     * Remove all Assertions from this Store.
     */
    public void clear() throws AssertionException {
        synchronized (_assertions) {
            _assertions.clear();
        }
    }

    /**
     * Save the specified AuthenticationAssertion into this Store.  Any previously saved
     * information for the associated assertion identifier is replaced.
     *
     * @param assertion AuthenticationAssertion to be saved
     */
    public void save(AuthenticationAssertion assertion) throws AssertionException {
        AuthenticationAssertion oldAssertion = null;
        synchronized (_assertions) {
            // Replace old assertion.
            oldAssertion = (AuthenticationAssertion) _assertions.put(assertion.getId(), assertion);
        }

        if (logger.isDebugEnabled())
            logger.debug("[save(AuthenticationAssertion." + assertion.getId() + ")] Assertion " + (oldAssertion == null ? " inserted" : "") + " updated");

    }

}
