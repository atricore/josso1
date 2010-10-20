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
package org.josso.seam.console;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;
import org.josso.seam.console.model.Role;
import org.josso.seam.console.model.UserRole;
import org.josso.seam.console.model.Username;

@Name("userRoleHome")
public class UserRoleHome extends EntityHome<UserRole> {

	@In(create = true)
	RoleHome roleHome;
	@In(create = true)
	UsernameHome usernameHome;

	public void setUserRoleId(Integer id) {
		setId(id);
	}

	public Integer getUserRoleId() {
		return (Integer) getId();
	}

	@Override
	protected UserRole createInstance() {
		UserRole userRole = new UserRole();
		return userRole;
	}

	public void wire() {
		Role role = roleHome.getDefinedInstance();
		if (role != null) {
			getInstance().setRole(role);
		}
		Username username = usernameHome.getDefinedInstance();
		if (username != null) {
			getInstance().setUsername(username);
		}
	}

	public boolean isWired() {
		if (getInstance().getRole() == null)
			return false;
		if (getInstance().getUsername() == null)
			return false;
		return true;
	}

	public UserRole getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
