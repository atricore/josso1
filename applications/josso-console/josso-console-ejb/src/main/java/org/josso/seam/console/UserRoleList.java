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
import org.jboss.seam.framework.EntityQuery;
import org.josso.seam.console.model.UserRole;

import java.util.Arrays;
import java.util.List;

@Name("userRoleList")
public class UserRoleList extends EntityQuery {

    private static final String[] RESTRICTIONS = {};

	private UserRole userRole = new UserRole();

	@Override
	public String getEjbql() {
		return "select userRole from UserRole userRole";
	}

	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public UserRole getUserRole() {
		return userRole;
	}

	@Override
	public List<String> getRestrictions() {
		return Arrays.asList(RESTRICTIONS);
	}

}
