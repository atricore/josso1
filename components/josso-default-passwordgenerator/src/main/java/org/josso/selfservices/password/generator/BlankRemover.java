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
 * This class provides functionality for handling white-spaces at different
 * places. It can clean up leading or trailing white-spaces. It can also replace
 * several white-spaces with a single one. <br>
 * <br>
 * Exampe:
 * <ul>
 * <br>
 * String oldStr = " > <1-2-1-2-1-2-1-2-1-2-1-----2-1-2-1-2-1-2-1-2-1-2-1-2> < ";
 * <br>
 * String newStr = oldStr.replaceAll("-", " "); <br>
 * System.out.println(newStr); <br>
 * System.out.println(BlankRemover.ltrim(newStr)); <br>
 * System.out.println(BlankRemover.rtrim(newStr)); <br>
 * System.out.println(BlankRemover.itrim(newStr)); <br>
 * System.out.println(BlankRemover.lrtrim(newStr));
 * </ul>
 * Results in:
 * <ul>
 * <br>" > <1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2> < " <br>"> <1 2 1
 * 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2> < " <br>" > <1 2 1 2 1 2 1 2 1 2
 * 1 2 1 2 1 2 1 2 1 2 1 2 1 2> <" <br>" > <1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2
 * 1 2 1 2 1 2> < " <br>"> <1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2> <"
 * </ul>
 * 
 * @author unrz205
 */
public class BlankRemover
{

	/**
	 * Removes leading whitespaces if any. Uses the following <b>regex</b>
	 * <em>"^\\s+"</em>
	 * 
	 * @param source
	 *            the string to be manipulated
	 * @return a resulting string with leading whitespace removed
	 */
	public static String ltrim(String source)
	{
		return source.replaceAll("^\\s+", "");
	}

	/**
	 * Removes trailing whitespaces if any. Uses the following <b>regex</b>
	 * <em>"\\s+$"</em>
	 * 
	 * @param source
	 *            the string to be manipulated
	 * @return a resulting string with trailing whitespace removed
	 */
	public static String rtrim(String source)
	{
		return source.replaceAll("\\s+$", "");
	}

	/**
	 * Replace multiple whitespaces between words with a single one. Uses the
	 * following <b>regex</b> <em>"\\b\\s{2,}\\b"</em>
	 * 
	 * @param source
	 *            the string to be manipulated
	 * @return a resulting string with cleaned up whitespaces
	 */
	public static String itrim(String source)
	{
		return source.replaceAll("\\b\\s{2,}\\b", " ");
	}

	/**
	 * Removes all superfluous whitespaces in source string . Uses the following
	 * sequence of calls to other methods <em>"itrim(ltrim(rtrim(source)))"</em>
	 * 
	 * @param source
	 *            the string to be manipulated
	 * @return a resulting string with cleaned up whitespaces
	 */
	public static String trim(String source)
	{
		return itrim(ltrim(rtrim(source)));
	}

	/**
	 * Removes leading and trailing whitespaces in source string . Uses the
	 * following sequence of calls to other methods
	 * <em>"ltrim(rtrim(source))"</em>
	 * 
	 * @param source
	 *            the string to be manipulated
	 * @return a resulting string with cleaned up leading and trailing
	 *         whitespaces
	 */
	public static String lrtrim(String source)
	{
		return ltrim(rtrim(source));
	}


}