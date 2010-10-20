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

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;
import org.josso.seam.console.model.Role;
import org.josso.seam.console.model.UserRole;

import java.util.ArrayList;
import java.util.List;

@Name("roleHome")
public class RoleHome extends EntityHome<Role> {

    private static final long serialVersionUID = 1L;
    
	public void setRoleName(String id) {
		setId(id);
	}

	public String getRoleName() {
		return (String) getId();
	}

	@Override
	protected Role createInstance() {
		Role role = new Role();
		return role;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public Role getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public List<UserRole> getUserRoles() {
		return getInstance() == null ? null : new ArrayList<UserRole>(
				getInstance().getUserRoles());
	}

}
