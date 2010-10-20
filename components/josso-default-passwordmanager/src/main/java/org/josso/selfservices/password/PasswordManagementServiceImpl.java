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

package org.josso.selfservices.password;

import org.josso.util.id.IdGenerator;
import org.josso.gateway.SSOException;
import org.josso.selfservices.annotations.Action;
import org.josso.selfservices.annotations.Extension;
import org.josso.selfservices.ProcessRequest;
import org.josso.selfservices.ProcessResponse;
import org.josso.selfservices.ProcessState;
import org.josso.selfservices.BaseProcessState;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;

/**
 * @org.apache.xbean.XBean element="password-manager"
 *
 * Default password management service implementation. This acts like a request dispatcher using the request action
 * to invoke a process method.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: PasswordManagementServiceImpl.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class PasswordManagementServiceImpl implements PasswordManagementService {

    private static final Log log = LogFactory.getLog(PasswordManagementServiceImpl.class);

    private IdGenerator idGenerator;

    private Map<String, PasswordManagementProcess> prototypeProcesses = new HashMap<String, PasswordManagementProcess>();

    private Map<String, PasswordManagementProcess> runningProcesses = new HashMap<String, PasswordManagementProcess>();

    private PasswordManagementMonitor monitor;

    public void initialize() {
        // Start session monitor.
        monitor = new PasswordManagementMonitor(this);
        Thread t = new Thread(monitor);
        t.setName("JOSSOPasswordManagementMonitor");
        t.start();
    }


    public ProcessResponse startProcess(String name) throws SSOException {

        String id = idGenerator.generateId();

        PasswordManagementProcess p = getPrototype(name);
        if (p == null)
            throw new SSOException("No such process : " + name);

        // Create a new process based on the received prototype
        p = p.createNewProcess(id);
        runningProcesses.put(p.getProcessId(), p);
        ProcessResponse r = p.start();
        ((BaseProcessState) p.getState()).setNextStep(r.getNextStep());
        return r;

    }

    public ProcessResponse handleRequest(ProcessRequest request) throws PasswordManagementException {

        String processId = request.getProcessId();
        String actionName = null;

        if (log.isDebugEnabled())
            log.debug("Handling request for process ["+processId+"]");

        try {
            
            PasswordManagementProcess p = runningProcesses.get(processId);
            if (p == null)
                throw new PasswordManagementException("No such process " + processId);

            String nextStep = p.getState().getNextStep();

            if (log.isDebugEnabled())
                log.debug("Handling request for process ["+processId+"]");


            Method[] methods = p.getClass().getMethods();
            for (Method method : methods) {

                if (log.isDebugEnabled())
                    log.debug("Processing method : " + method.getName());

                if (!method.isAnnotationPresent(Action.class))
                    continue;

                Action action = method.getAnnotation(Action.class);

                if (log.isDebugEnabled())
                    log.debug("Processing method annotation : " + action);

                for (String actionStep : action.fromSteps()) {

                    if (log.isDebugEnabled())
                        log.debug("Processing annotation step : " + actionStep);

                    if (actionStep.equals(nextStep)) {
                        actionName = method.getName();

                        if (log.isDebugEnabled())
                            log.debug("Dispatching request from step " + nextStep + " to process ["+processId+"] action " + actionName);

                        // Store response next step in process state :
                        ProcessResponse r = (ProcessResponse) method.invoke(p, request);
                        ((BaseProcessState) p.getState()).setNextStep(r.getNextStep());
                        return r;
                    }
                }

            }

            throw new PasswordManagementException("Step ["+nextStep+"] not supported by process");

        } catch (InvocationTargetException e) {
            throw new PasswordManagementException("Cannot invoke process action [" + actionName + "] : " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new PasswordManagementException("Cannot invoke process action [" + actionName + "] : " + e.getMessage(), e);
        }

    }

    public ProcessState getProcessState(String processId) {
        return this.runningProcesses.get(processId).getState();
    }

    public ProcessRequest createRequest(String processId) throws PasswordManagementException {
        PasswordManagementProcess p = this.runningProcesses.get(processId);
        if (p == null)
            throw new PasswordManagementException("Invalid proces ID : " + processId);
        
        return p.createRequest();
    }

    protected PasswordManagementProcess getPrototype(String name) {
        return prototypeProcesses.get(name);
    }


    public IdGenerator getProcessIdGenerator() {
        return idGenerator;
    }

    /**
     * @org.apache.xbean.Property alias="process-id-generator"
     *
     */
    public void setProcessIdGenerator(IdGenerator idGen) {
        this.idGenerator = idGen;
    }


    public Collection<PasswordManagementProcess> getPrototypeProcesses() {
        return prototypeProcesses.values();
    }

    /**
     * @org.apache.xbean.Property alias="processes" nestedType="org.josso.selfservices.passwdmanagement.PasswordManagementProcess"
     *
     * @param prototypeProcesses
     */
    public void setPrototypeProcesses(Collection<PasswordManagementProcess> prototypeProcesses) {
        for (PasswordManagementProcess prototype : prototypeProcesses) {
            this.prototypeProcesses.put(prototype.getName(), prototype);
        }
    }


    public void checkPendingProcesses() {
        try {

            long now = System.currentTimeMillis();

            for (PasswordManagementProcess process : runningProcesses.values()) {

                try {
                    List<PasswordManagementProcess> toRemove =  new ArrayList<PasswordManagementProcess>();

                    // Ignore valid assertions, they have not expired yet.
                    if (!process.isRunning() || process.getCreationTime() - now > process.getMaxTimeToLive()) {
                        toRemove.add(process);
                        ///registry.unregisterToken(securityDomainName, TOKEN_TYPE, process.getId());
                        if (log.isDebugEnabled())
                            log.debug("[checkPendingProcesses()] Process expired : " + process.getProcessId());
                    }

                    for (PasswordManagementProcess passwordManagementProcess : toRemove) {
                        try { passwordManagementProcess.stop(); } catch (Exception e) { log.debug(e.getMessage(), e); }
                        runningProcesses.remove(passwordManagementProcess.getProcessId());
                    }

                } catch (Exception e) {
                    log.warn("Can't remove process " + e.getMessage() != null ? e.getMessage() : e.toString(), e);
                }
            }
        } catch (Exception e) {
            log.error("Cannot check pending processes! " + e.getMessage(), e);
        }

    }

    public void register(String processId, String name, Object extension) {

        if (log.isDebugEnabled())
            log.debug("Registering " + name);

        PasswordManagementProcess p = this.runningProcesses.get(processId);

        Class clazz = p.getClass();


        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {

                if (log.isDebugEnabled())
                    log.debug("Checking field : " + field.getName());

                if (field.isAnnotationPresent(Extension.class)) {

                    Extension ex = field.getAnnotation(Extension.class);

                    if (ex.value().equals(name)) {
                        log.debug("Injecting extension : " + name);
                        try {

                            // Make field accessible ...
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            field.set(p, extension);
                            return;
                        } catch (IllegalAccessException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

    }

    /**
     * Checks for assertions which have not been consumed yet.
     */
    private class PasswordManagementMonitor implements Runnable {

        private long _interval;

        private PasswordManagementService _m;

        PasswordManagementMonitor(PasswordManagementService m) {
            _m = m;
        }

        PasswordManagementMonitor(PasswordManagementService m, long interval) {
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
         * Check for pending PasswordManagementg ...
         */
        public void run() {

            do {
                try {

                    if (log.isDebugEnabled())
                        log.debug("[run()] calling checkPendingProcesses ... ");

                    _m.checkPendingProcesses();

                    synchronized (this) {
                        try {

                            if (log.isDebugEnabled())
                                log.debug("[run()] waiting " + _interval + " ms");

                            wait(_interval);

                        } catch (InterruptedException e) {
                            log.warn(e, e);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Exception received : " + e.getMessage() != null ? e.getMessage() : e.toString(), e);
                }

            } while (true);
        }
    }


}
