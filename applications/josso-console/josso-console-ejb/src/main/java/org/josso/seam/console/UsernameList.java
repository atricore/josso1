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
import org.josso.seam.console.model.Username;

import java.util.Arrays;
import java.util.List;

@Name("usernameList")
public class UsernameList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(username.login) like concat(lower(#{usernameList.username.login}),'%')",
			"lower(username.description) like concat(lower(#{usernameList.username.description}),'%')",
			"lower(username.name) like concat(lower(#{usernameList.username.name}),'%')",
			"lower(username.passwd) like concat(lower(#{usernameList.username.passwd}),'%')",};

	private Username username = new Username();

	@Override
	public String getEjbql() {
		return "select username from Username username";
	}

	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public Username getUsername() {
		return username;
	}

	@Override
	public List<String> getRestrictions() {
		return Arrays.asList(RESTRICTIONS);
	}

}
