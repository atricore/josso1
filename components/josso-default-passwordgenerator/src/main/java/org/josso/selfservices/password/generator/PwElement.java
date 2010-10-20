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

/**
 * A helper construct representing an element used for the generation of
 * passwords. An instance of this class is either a vowel or a consonant. Both
 * vowels and consonants can be marked as dipthongs. The marking is implemented
 * as a bit masked int. By using this class a pronounceable password is created
 * which should be easy to remember.
 * 
 * @author unrz205
 */
public class PwElement
{
	// the string representation of the vowel or consonant - (probably also a
	// dipthong)
	protected String value;

	// the flags describing the password element - actually its type
	// it is build up by a bit mask
	protected int type;

	/**
	 * Constructor of the Password Element class
	 * 
	 * @param value
	 *            the string representation of the vowel, consonant (dipthong)
	 * @param type
	 *            the type of the password element
	 */
	public PwElement(String value, int type)
	{
		this.value = value;
		this.type = type;
	}

	/**
	 * Returns the type of this password element
	 * 
	 * @return a bit-masked type describing the type of element
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * Sets the type of this password element
	 * 
	 * @param type
	 */
	public void setType(int type)
	{
		this.type = type;
	}

	/**
	 * Returns the string representation of this password element
	 * 
	 * @return the string representation of this password element
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Sets the string representation of this password element
	 * 
	 * @param value
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
}
