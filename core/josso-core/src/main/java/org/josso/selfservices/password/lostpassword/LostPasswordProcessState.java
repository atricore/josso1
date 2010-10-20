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

package org.josso.selfservices.password.lostpassword;

import org.josso.selfservices.BaseProcessState;
import org.josso.selfservices.ChallengeResponseCredential;
import org.josso.auth.Credential;
import org.josso.gateway.identity.SSOUser;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 28, 2008
 * Time: 5:49:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class LostPasswordProcessState extends BaseProcessState {

    private String assertionId;
    private String passwordConfirmUrl;
    private Set<ChallengeResponseCredential> challenges;
    private Credential newPasswordCredential;
    private SSOUser user;

    public LostPasswordProcessState(String procesId) {
        super(procesId);
    }

    public String getAssertionId() {
        return assertionId;
    }

    public void setAssertionId(String assertionId) {
        this.assertionId = assertionId;
    }

    public String getPasswordConfirmUrl() {
        return passwordConfirmUrl;
    }

    public void setPasswordConfirmUrl(String passwordConfirmUrl) {
        this.passwordConfirmUrl = passwordConfirmUrl;
    }

    public Set<ChallengeResponseCredential> getChallenges() {
        return challenges;
    }

    public void setChallenges(Set<ChallengeResponseCredential> challenges) {
        this.challenges = challenges;
    }

    public void setNewPasswordCredential(Credential newPasswordCredential) {
        this.newPasswordCredential = newPasswordCredential;
    }

    public Credential getNewPasswordCredential() {
        return newPasswordCredential;
    }

    public SSOUser getUser() {
        return user;
    }

    public void setUser(SSOUser user) {
        this.user = user;
    }
}
