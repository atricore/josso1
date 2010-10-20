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
package org.josso.selfservices.password.generator;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Loads the ResourceBundle associated with the PwGenerator. This ResourceBundle
 * contains the localized(externalized) strings used by the classes.
 * 
 * @author unrz205
 * 
 */
public class Messages
{
	/**
	 * The identifier of the ResourceBundle.
	 */
	public static final String BUNDLE_NAME = "org.josso.selfservices.password.generator.messages";

	// The ResourceBundle instance
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	/**
	 * Returns the localized message for this key. If the key is not found its
	 * value is returned surrounded by exclamation marks.
	 * 
	 * @param key the key to be searched for in the resource bundle
	 * @return the localized value for the key
	 */
	public static String getString(String key)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e)
		{
			System.err.println(e.getLocalizedMessage());
			return '!' + key + '!';
		}
	}
}
