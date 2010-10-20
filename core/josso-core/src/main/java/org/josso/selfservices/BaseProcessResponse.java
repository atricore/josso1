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

package org.josso.selfservices;

import org.josso.selfservices.ProcessResponse;

import java.util.Map;
import java.util.HashMap;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: BaseProcessResponse.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class BaseProcessResponse implements ProcessResponse {

    private String id;

    private String nextStep;

    private boolean isNextStepFinal;

    private Map<String, Object> attributes = new HashMap<String, Object>();

    public BaseProcessResponse(String id, String nextStep) {
        this.id = id;
        this.nextStep = nextStep;
    }

    public String getProcessId() {
        return id;
    }

    public String getNextStep() {
        return nextStep;
    }

    public boolean isNextStepFinal() {
        return isNextStepFinal;
    }

    public void setNextStepFinal(boolean nextStepFinal) {
        isNextStepFinal = nextStepFinal;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }


}
