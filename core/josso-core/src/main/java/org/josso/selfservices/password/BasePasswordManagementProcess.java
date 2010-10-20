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

import org.josso.gateway.SSOException;
import org.josso.selfservices.*;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: BasePasswordManagementProcess.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class BasePasswordManagementProcess implements PasswordManagementProcess {

    private String name;

    private long creationTime;

    private int maxTimeToLive;

    private boolean running;

    private ProcessState state ;

    public BasePasswordManagementProcess() {
        creationTime = System.currentTimeMillis();
    }

    /**
     * This will create a new process instace.
     * @return
     * @throws SSOException
     */
    public PasswordManagementProcess createNewProcess(String id) throws SSOException {
        try {
            BasePasswordManagementProcess newProcess = getClass().newInstance();

            newProcess.setName(name);
            newProcess.setMaxTimeToLive(maxTimeToLive);
            newProcess.setState(doMakeState(id));

            return newProcess;
        } catch (IllegalAccessException e) {
            throw new SSOException(e);
        } catch (InstantiationException e) {
            throw new SSOException(e);
        }
    }

    protected ProcessState doMakeState(String id) {
        return new BaseProcessState(id);
    }

    public ProcessRequest createRequest() {
        // TODO : Validate action ?!
        return new BaseProcessRequest(this.getProcessId());
    }

    public ProcessResponse createFinalResponse(String nextStep) {
        BaseProcessResponse r = new BaseProcessResponse(this.getProcessId(), nextStep);
        r.setNextStepFinal(true);
        return r;
    }

    public ProcessResponse createResponse(String nextStep) {
        // TODO : Validate action ?!
        return new BaseProcessResponse(this.getProcessId(), nextStep);
    }


    public ProcessResponse start() {
        this.running = true;
        return null;
    }

    public void stop() {
        this.running = false;
    }

    // ----------------------------------------------------------------



    public String getProcessId() {
        return state.getProcessId();
    }

    public boolean isRunning() {
        return running;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public int getMaxTimeToLive() {
        return maxTimeToLive;
    }

    public void setMaxTimeToLive(int maxTimeToLive) {
        this.maxTimeToLive = maxTimeToLive;
    }

    public ProcessState getState() {
        return state;
    }

    private void setState(ProcessState state) {
        this.state = state;
    }


}
