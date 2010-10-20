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
package org.josso.gateway.identity.service;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.Principal;
import java.util.*;

/**
 * An implementation of BaseRole that manages a collection of Principal
 * objects based on their hashCode() and equals() methods.
 * This class is not thread safe.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: BaseRoleImpl.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public class BaseRoleImpl implements BaseRole {

    private static final Log logger = LogFactory.getLog(BaseRoleImpl.class);

    private String _name;
    private HashMap members;

    public BaseRoleImpl(String name) {
        this();
        _name = name;
    }

    public BaseRoleImpl() {
        members = new HashMap(3);
    }

    /**
     * Adds the specified member to the group.
     *
     * @param user the principal to add to this group.
     * @return true if the member was successfully added,
     *         false if the principal was already a member.
     */
    public boolean addMember(Principal user) {
        boolean isMember = members.containsKey(user);
        if (isMember == false)
            members.put(user, user);
        return isMember == false;
    }

    /**
     * Returns true if the passed principal is a member of the group.
     * This method does a recursive search, so if a principal belongs to a
     * group which is a member of this group, true is returned.
     * <p/>
     * A special check is made to see if the member is an instance of
     * org.jboss.security.AnybodyPrincipal or org.jboss.security.NobodyPrincipal
     * since these classes do not hash to meaningful values.
     *
     * @param member the principal whose membership is to be checked.
     * @return true if the principal is a member of this group,
     *         false otherwise.
     */
    public boolean isMember(Principal member) {
        // logger.debug("Begin, isMember");

        // First see if there is a key with the member name
        boolean isMember = members.containsKey(member);
        if (isMember == false) {   // Check any Groups for membership
            Collection values = members.values();
            Iterator iter = values.iterator();
            while (isMember == false && iter.hasNext()) {
                Object next = iter.next();
                if (next instanceof BaseRole) {
                    BaseRole role = (BaseRole) next;
                    isMember = role.isMember(member);
                }
            }
        }

        // logger.debug("End, isMember, return=" + isMember);
        return isMember;
    }

    /**
     * Returns an enumeration of the members in the group.
     * The returned objects can be instances of either Principal
     * or Group (which is a subinterface of Principal).
     *
     * @return an enumeration of the group members.
     */
    public Enumeration members() {
        return Collections.enumeration(members.values());
    }

    /**
     * Removes the specified member from the group.
     *
     * @param user the principal to remove from this group.
     * @return true if the principal was removed, or
     *         false if the principal was not a member.
     */
    public boolean removeMember(Principal user) {
        Object prev = members.remove(user);
        return prev != null;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String toString() {
        StringBuffer tmp = new StringBuffer(getName());
        tmp.append("(members:");
        Iterator iter = members.keySet().iterator();
        while (iter.hasNext()) {
            tmp.append(iter.next());
            tmp.append(',');
        }
        tmp.setCharAt(tmp.length() - 1, ')');
        return tmp.toString();
    }

    /**
     * Compare this BaseRole's name against another BaseRole
     *
     * @return true if name equals another.getName();
     */
    public boolean equals(Object another) {
        if (!(another instanceof BaseRole))
            return false;
        String anotherName = ((BaseRole) another).getName();
        boolean equals = false;
        if (_name == null)
            equals = anotherName == null;
        else
            equals = _name.equals(anotherName);
        return equals;
    }

    public int hashCode() {
        return (_name == null ? 0 : _name.hashCode());
    }

}
