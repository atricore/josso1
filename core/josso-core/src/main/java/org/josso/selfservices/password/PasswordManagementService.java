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
import org.josso.selfservices.ProcessRequest;
import org.josso.selfservices.ProcessResponse;
import org.josso.selfservices.ProcessState;
import org.josso.selfservices.password.lostpassword.LostPasswordUrlProvider;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: PasswordManagementService.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public interface PasswordManagementService {

    ProcessResponse startProcess(String name) throws SSOException;

    ProcessResponse handleRequest(ProcessRequest request) throws PasswordManagementException;

    ProcessRequest createRequest(String processId) throws PasswordManagementException;

    ProcessState getProcessState(String processId);

    void checkPendingProcesses();

    /**
     * TODO : Could we use spring here ?
     * @param processId
     * @param extensionname
     * @param extension
     */
    void register(String processId, String extensionname, Object extension);
}
