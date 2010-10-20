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

import org.josso.auth.BaseCredential;

/**
 * It represents a credential that can be used to prove user's identity without requiring a password.  This type of credentials
 * are used in password management process to recover lost passwords, password resets, etc.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: ChallengeResponseCredential.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class ChallengeResponseCredential extends BaseCredential {

    private String id;

    private String challenge;

    private String response;

    public ChallengeResponseCredential(String id, String challenge) {
        super();
        this.id = id;
        this.challenge = challenge;
    }

    public ChallengeResponseCredential(String id, String challenge, Object credential) {
        super(credential);
        this.id = id;
        this.challenge = challenge;
    }


    public String getId() {
        return id;
    }

    public String getChallenge() {
        return challenge;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public void setValue(Object credential) {
        this.setResponse((String) credential);
    }

    @Override
    public Object getValue() {
        return this.getResponse();
    }
}
