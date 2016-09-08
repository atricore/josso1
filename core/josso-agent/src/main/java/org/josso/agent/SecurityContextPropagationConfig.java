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
package org.josso.agent;

import java.io.Serializable;

/**
 * @org.apache.xbean.XBean element="security-context-propagation-config"
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id$
 */

public class SecurityContextPropagationConfig implements Serializable {


    private String _binding;
    private String _userPlaceHolder = "JOSSO_USER";
    private String _rolesPlaceHolder = "JOSSO_ROLE";
    private String _propertiesPlaceHolder = "JOSSO_PROPERTY";


    public String getBinding() {
        return _binding;
    }

    public void setBinding(String binding) {
        this._binding = binding;
    }

    public String getUserPlaceHolder() {
        return _userPlaceHolder;
    }

    public void setUserPlaceHolder(String userPlaceHolder) {
        this._userPlaceHolder = userPlaceHolder;
    }

    public String getRolesPlaceHolder() {
        return _rolesPlaceHolder;
    }

    public void setRolesPlaceHolder(String rolesPlaceHolder) {
        this._rolesPlaceHolder = rolesPlaceHolder;
    }

    public String getPropertiesPlaceHolder() {
        return _propertiesPlaceHolder;
    }

    public void setPropertiesPlaceHolder(String _propertiesPlaceHolder) {
        this._propertiesPlaceHolder = _propertiesPlaceHolder;
    }

    public String toString() {
        return _binding + ":" + (_userPlaceHolder != null ? _userPlaceHolder : "") +
                (_rolesPlaceHolder != null ? "," + _rolesPlaceHolder : "") +
                (_propertiesPlaceHolder != null ? "," + _propertiesPlaceHolder : "");
    }


}

