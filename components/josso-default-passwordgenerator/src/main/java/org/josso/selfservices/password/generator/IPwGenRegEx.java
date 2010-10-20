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
 * Interface containing basic regular expressions used for matching and
 * filtering of passwords based on various criteria.
 * 
 * @author unrz205
 * 
 */
public interface IPwGenRegEx
{
	/**
	 * Flag that disables passwords starting with a lower letter character
	 */
	public static final int REGEX_STARTS_NO_SMALL_LETTER_FLAG = 0x10;

	/**
	 * Regular expression that matches passwords starting with a lower letter
	 * character
	 */
	public static final String REGEX_STARTS_NO_SMALL_LETTER = "^[a-z]";

	/**
	 * Flag that disables passwords ending with a lower letter character
	 */
	public static final int REGEX_ENDS_NO_SMALL_LETTER_FLAG = 0x20;

	/**
	 * Regular expression that matches passwords ending with a lower letter
	 * character
	 */
	public static final String REGEX_ENDS_NO_SMALL_LETTER = "[a-z]$";

	/**
	 * Flag that disables passwords starting with a upper letter character
	 */
	public static final int REGEX_STARTS_NO_UPPER_LETTER_FLAG = 0x40;

	/**
	 * Regular expression that matches passwords starting with a upper letter
	 * character
	 */
	public static final String REGEX_STARTS_NO_UPPER_LETTER = "^[A-Z]";

	/**
	 * Flag that disables passwords ending with a upper letter character
	 */
	public static final int REGEX_ENDS_NO_UPPER_LETTER_FLAG = 0x80;

	/**
	 * Regular expression that matches passwords ending with a upper letter
	 * character
	 */
	public static final String REGEX_ENDS_NO_UPPER_LETTER = "[A-Z]$";

	/**
	 * Flag that disables passwords ending with a digit
	 */
	public static final int REGEX_ENDS_NO_DIGIT_FLAG = 0x100;

	/**
	 * Regular expression that matches passwords ending with a digit
	 */
	public static final String REGEX_ENDS_NO_DIGIT = "\\d$";

	/**
	 * Flag that disables passwords starting with a digit
	 */
	public static final int REGEX_STARTS_NO_DIGIT_FLAG = 0x200;

	/**
	 * Regular expression that matches passwords starting with a digit
	 */
	public static final String REGEX_STARTS_NO_DIGIT = "^\\d";

	/**
	 * Flag that disables passwords starting with a symbol
	 */
	public static final int REGEX_STARTS_NO_SYMBOL_FLAG = 0x400;

	/**
	 * Regular expression that matches passwords starting with a symbol
	 */
	public static final String REGEX_STARTS_NO_SYMBOL = "^\\W";

	/**
	 * Flag that disables passwords ending with a symbol
	 */
	public static final int REGEX_ENDS_NO_SYMBOL_FLAG = 0x800;

	/**
	 * Regular expression that matches passwords ending with a symbol
	 */
	public static final String REGEX_ENDS_NO_SYMBOL = "[\\W]$";

	/**
	 * Flag that disables passwords containing more than one upper case letter
	 */
	public static final int REGEX_ONLY_1_CAPITAL_FLAG = 0x1000;

	/**
	 * Regular expression that matches passwords containing exactly one upper
	 * case letter
	 */
	public static final String REGEX_ONLY_1_CAPITAL = "^[^A-Z]*[A-Z][^A-Z]*$";

	/**
	 * Flag that disables passwords containing more than one upper case letter
	 */
	public static final int REGEX_ONLY_1_SYMBOL_FLAG = 0x2000;

	/**
	 * Regular expression that matches passwords containing exactly one symbol
	 */
	public static final String REGEX_ONLY_1_SYMBOL = "^\\w*\\W\\w*$";

	/**
	 * Flag that disables passwords containing less than two upper case letter
	 */
	public static final int REGEX_AT_LEAST_2_SYMBOLS_FLAG = 0x4000;

	/**
	 * Regular expression that matches passwords containing at least 2 symbols
	 */
	public static final String REGEX_AT_LEAST_2_SYMBOLS = "\\w*[^\\w]\\w*[^\\w]\\w*";

	/**
	 * Flag that disables passwords containing more than one digit
	 */
	public static final int REGEX_ONLY_1_DIGIT_FLAG = 0x8000;

	/**
	 * Regular expression that matches passwords containing exactly one digit
	 */
	public static final String REGEX_ONLY_1_DIGIT = "^[\\D]*\\d[\\D]*$";;

	/**
	 * Flag that disables passwords containing less than two upper case letter
	 */
	public static final int REGEX_AT_LEAST_2_DIGITS_FLAG = 0x10000;

	/**
	 * Regular expression that matches passwords containing at least two digits
	 */
	public static final String REGEX_AT_LEAST_2_DIGITS = "\\w*[\\W]*[\\d]\\w*[\\W]*[\\d]\\w*[\\W]*";

	// Not needed any more

	// public static final int REGEX_AT_LEAST_2_CAPITALS_FLAG = 0x2000;

	// public static final String REGEX_AT_LEAST_2_CAPITALS =
	// "\\w*[^\\w]*[A-Z]\\w*[^\\w]*[A-Z]\\w*[^\\w]*";

}
