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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.Lookup;
import org.josso.gateway.SecurityDomainRegistry;
import org.josso.gateway.assertion.service.AssertionIdGenerator;
import org.josso.gateway.assertion.service.store.AssertionStore;

/**
 *
 * @org.apache.xbean.XBean element="assertion-manager"
 * Mediation componenet for handling authentication assertions.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id$
 */
public class AssertionManagerImpl implements AssertionManager {

    private static final Log logger = LogFactory.getLog(AssertionManagerImpl.class);

    private String securityDomainName;

    private AssertionMonitor _monitor;

    private Thread _monitorThread;
    
    private AssertionStore _assertionStore;

    private AssertionIdGenerator _assertionIdGenerator;
    
    private long _assertionMonitorInterval = 5000;

    public AssertionManagerImpl() {
    }

    public void initialize() {
        // Start session monitor.
        _monitor = new AssertionMonitor(this, getAssertionMonitorInterval());
        _monitorThread = new Thread(_monitor);
        _monitorThread.setDaemon(true);
        _monitorThread.setName("JOSSOAssertionMonitor");
        _monitorThread.start();
    }

    /**
     * Destroy the manager and free ressource (running threads).
     */
    public void destroy() {
        if (_monitor != null) {
            _monitor.stop();
            try {
                _monitorThread.join();
            } catch (InterruptedException e) {
                logger.warn("[destroy()] : main thread interrupted.");
            }
        }
    }
    
    public void setSecurityDomainName(String securityDomainName) {
        this.securityDomainName = securityDomainName;
    }

    /**
     * @org.apache.xbean.Property alias="assertion-store"
     * @param as
     */
    public void setAssertionStore(AssertionStore as) {
        this._assertionStore = as;
    }

    /**
     * @org.apache.xbean.Property alias="assertion-id-generator"
     * @param assertionIdGenerator
     */
    public void setAssertionIdGenerator(AssertionIdGenerator assertionIdGenerator) {
        this._assertionIdGenerator = assertionIdGenerator;
    }

    public String getSecurityDomainName() {
        return securityDomainName;
    }

    public long getAssertionMonitorInterval() {
        return _assertionMonitorInterval;
    }

    public void setAssertionMonitorInterval(long assertionMonitorInterval) {
    	_assertionMonitorInterval = assertionMonitorInterval;
        if (_monitor != null) {
            _monitor.setInterval(_assertionMonitorInterval);
        }
    }
    
    public synchronized AuthenticationAssertion requestAssertion(String ssoSessionId) {

        try {

            AuthenticationAssertionImpl assertion = new AuthenticationAssertionImpl(
                    _assertionIdGenerator.generateId(),
                    ssoSessionId
            );

            SecurityDomainRegistry registry = Lookup.getInstance().lookupSecurityDomainRegistry();

            // Now that we have all , store data ...
            ////assertions.add(assertion);
            _assertionStore.save(assertion);

            registry.registerToken(securityDomainName, TOKEN_TYPE, assertion.getId());

            return assertion;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public synchronized AuthenticationAssertion consumeAssertion(String assertionId) {

        AuthenticationAssertion targetAuthenticationAssertion = null;

        try {

            SecurityDomainRegistry registry = Lookup.getInstance().lookupSecurityDomainRegistry();
            AuthenticationAssertion assertions[] = _assertionStore.loadAll();

            for (AuthenticationAssertion aa : assertions) {

                if (aa.getId().equals(assertionId)) {
                    targetAuthenticationAssertion = aa;
                    //assertions.remove(aa);
                    _assertionStore.remove(aa.getId());
                    registry.unregisterToken(securityDomainName, TOKEN_TYPE, aa.getId());
                    break;
                }
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return targetAuthenticationAssertion;
    }

    public void checkPendingAssertions() {
        try {
            SecurityDomainRegistry registry = Lookup.getInstance().lookupSecurityDomainRegistry();

            AuthenticationAssertion assertions[] = _assertionStore.loadAll();
            for (AuthenticationAssertion assertion : assertions) {
                try {

                    // Ignore valid assertions, they have not expired yet.
                    if (!assertion.isValid()) {
                        _assertionStore.remove(assertion.getId());
                        ///registry.unregisterToken(securityDomainName, TOKEN_TYPE, assertion.getId());
                        if (logger.isDebugEnabled())
                            logger.debug("[checkPendingAssertions()] Assertion expired : " + assertion.getId());
                    }


                } catch (Exception e) {
                    logger.warn("Can't remove assertion " + e.getMessage() != null ? e.getMessage() : e.toString(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot check pending assertions! " + e.getMessage(), e);
        }

    }

    /**
     * Checks for assertions which have not been consumed yet.
     */
    private class AssertionMonitor implements Runnable {

        private long _interval;

        private AssertionManager _m;

        private boolean _stop;

        AssertionMonitor(AssertionManager m) {
            _m = m;
        }

        AssertionMonitor(AssertionManager m, long interval) {
            _interval = interval;
            _m = m;
        }

        public long getInterval() {
            return _interval;
        }

        public void setInterval(long interval) {
            _interval = interval;
        }

        /**
         * Check for pending assertiong ...
         */
        public void run() {

            _stop = false;
            do {
                try {

                    if (logger.isDebugEnabled())
                        logger.debug("[run()] calling checkPendingAssertions ... ");

                    _m.checkPendingAssertions();

                    synchronized (this) {
                        try {

                            if (logger.isDebugEnabled())
                                logger.debug("[run()] waiting " + _interval + " ms");

                            wait(_interval);

                        } catch (InterruptedException e) {
                            logger.warn(e, e);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Exception received : " + e.getMessage() != null ? e.getMessage() : e.toString(), e);
                }

            } while (!_stop);
        }

        public void stop() {
            _stop = true;
            synchronized (this) {
                notify(); // wake the thread if it was in a wait.
            }
        }
    }

}
