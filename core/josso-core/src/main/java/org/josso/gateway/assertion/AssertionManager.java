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

package org.josso.gateway.assertion;

import org.josso.gateway.assertion.service.AssertionIdGenerator;
import org.josso.gateway.assertion.service.store.AssertionStore;

/**
 * Manages Assertion genration
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id$
 */
public interface AssertionManager {

    static final String TOKEN_TYPE = AssertionManager.class.getName();

    void initialize();

    void destroy();

    void setSecurityDomainName(String securityDomainName);

    void setAssertionStore(AssertionStore ss);

    void setAssertionIdGenerator(AssertionIdGenerator assertionIdGenerator);

    AuthenticationAssertion requestAssertion(String ssoSessionId);

    AuthenticationAssertion consumeAssertion(String assertionId);

    void checkPendingAssertions();
}
