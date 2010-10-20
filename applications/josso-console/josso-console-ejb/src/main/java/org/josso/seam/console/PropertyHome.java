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
import org.josso.seam.console.model.Property;
import org.josso.seam.console.model.Username;


@Name("propertyHome")
public class PropertyHome extends EntityHome<Property> {

    private static final long serialVersionUID = 1L;
    
	@In(create = true)
	UsernameHome usernameHome;

	public void setPropertyId(Integer id) {
		setId(id);
	}

	public Integer getPropertyId() {
		return (Integer) getId();
	}

	@Override
	protected Property createInstance() {
		Property property = new Property();
		return property;
	}

	public void wire() {
		Username username = usernameHome.getDefinedInstance();
		if (username != null) {
			getInstance().setUsername(username);
		}
	}

	public boolean isWired() {
		if (getInstance().getUsername() == null)
			return false;
		return true;
	}

	public Property getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
