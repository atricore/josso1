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
 * This interface defines the command line options of the password generator.
 * All options,
 * 
 * @author unrz205
 * 
 */
public interface IPwGenCommandLineOptions
{

	/**
	 * The number of passwords to be generated
	 */
	public static final String CL_NUMBER_PASSWORD = "N";

	/**
	 * The number of passwords to be generated
	 */
	public static final String CL_NUMBER_PASSWORD_LONG = "number";

	/**
	 * The description of the -N and the --number command line options
	 */
	public static final String CL_NUMBER_PASSWORD_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_NUMBER_PASSWORD_DESC");

	/**
	 * The length of the generated password
	 */
	public static final String CL_PASSWORD_LENGTH = "s";

	/**
	 * The length of the generated password
	 */
	public static final String CL_PASSWORD_LENGTH_LONG = "size";

	/**
	 * The description of the -s and the --size command line options
	 */
	public static final String CL_PASSWORD_LENGTH_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_PASSWORD_LENGTH_DESC");

	/**
	 * Include at least one capital letter in the password
	 */
	public static final String CL_CAPITALIZE = "c";

	/**
	 * Include at least one capital letter in the password
	 */
	public static final String CL_CAPITALIZE_LONG = "capitalize";

	/**
	 * The description of the -c and the --capitalize command line options
	 */
	public static final String CL_CAPITALIZE_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_CAPITALIZE_DESC");

	/**
	 * Don't include capital letters in the password
	 */
	public static final String CL_NO_CAPITALIZE = "A";

	/**
	 * Don't include capital letters in the password
	 */
	public static final String CL_NO_CAPITALIZE_LONG = "no-capitalize";

	/**
	 * The description of the -A and the --no-capitalize command line options
	 */
	public static final String CL_NO_CAPITALIZE_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_NO_CAPITALIZE_DESC");

	/**
	 * Include at least one number in the password
	 */
	public static final String CL_NUMERALS = "n";

	/**
	 * Include at least one number in the password
	 */
	public static final String CL_NUMERALS_LONG = "numerals";

	/**
	 * The description of the -n and the --numerals command line options
	 */
	public static final String CL_NUMERALS_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_NUMERALS_DESC");

	/**
	 * Don't include numbers in the password
	 */
	public static final String CL_NO_NUMERALS = "O";

	/**
	 * Don't include numbers in the password
	 */
	public static final String CL_NO_NUMERALS_LONG = "no-numerals";

	/**
	 * The description of the -O and the --no-numerals command line options
	 */
	public static final String CL_NO_NUMERALS_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_NO_NUMERALS_DESC");

	/**
	 * Include at least one special symbol in the password
	 */
	public static final String CL_SYMBOLS = "y";

	/**
	 * Include at least one special symbol in the password
	 */
	public static final String CL_SYMBOLS_LONG = "symbols";

	/**
	 * The description of the -y and the --symbols command line options
	 */
	public static final String CL_SYMBOLS_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_SYMBOLS_DESC");

	/**
	 * Include no special symbols in the password
	 */
	public static final String CL_NO_SYMBOLS = "Y";

	/**
	 * Include no special symbols in the password
	 */
	public static final String CL_NO_SYMBOLS_LONG = "no-symbols";

	/**
	 * The description of the -Y and the --no-symbols command line options
	 */
	public static final String CL_NO_SYMBOLS_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_NO_SYMBOLS_DESC");

	/**
	 * Don't include ambiguous characters in the password
	 */
	public static final String CL_AMBIGOUS = "B";

	/**
	 * Don't include ambiguous characters in the password
	 */
	public static final String CL_AMBIGOUS_LONG = "ambiguous";

	/**
	 * The description of the -B and the --ambiguous command line options
	 */
	public static final String CL_AMBIGOUS_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_AMBIGOUS_DESC");

	/**
	 * Allow ambiguous characters in the password
	 */
	public static final String CL_NO_AMBIGOUS = "D";

	/**
	 * Allow ambiguous characters in the password
	 */
	public static final String CL_NO_AMBIGOUS_LONG = "allow-ambiguous";

	/**
	 * The description of the -D and the --allow-ambiguous command line options
	 */
	public static final String CL_NO_AMBIGOUS_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_NO_AMBIGOUS_DESC");

	/**
	 * Print a help message
	 */
	public static final String CL_HELP = "h";

	/**
	 * Print a help message
	 */
	public static final String CL_HELP_LONG = "help";

	/**
	 * The description of the -h and the --help command line options
	 */
	public static final String CL_HELP_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_HELP_DESC");

	/**
	 * Use simple random for password generation
	 */
	public static final String CL_RANDOM = "r";

	/**
	 * Use simple random for password generation
	 */
	public static final String CL_RANDOM_LONG = "random";

	/**
	 * The description of the -r and the --random command line options
	 */
	public static final String CL_RANDOM_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_RANDOM_DESC");

	/**
	 * Print the generated passwords in columns
	 */
	public static final String CL_COLUMN = "C";

	/**
	 * Print the generated passwords in columns
	 */
	public static final String CL_COLUMN_LONG = "columns";

	/**
	 * The description of the -C and the --columns command line options
	 */
	public static final String CL_COLUMN_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_COLUMN_DESC");

	/**
	 * Sets the character width of the PwGen terminal
	 */
	public static final String CL_TERM_WIDTH = "t";

	/**
	 * Sets the character width of the PwGen terminal
	 */
	public static final String CL_TERM_WIDTH_LONG = "term-width";

	/**
	 * The description of the -t and the --term-width command line options
	 */
	public static final String CL_TERM_WIDTH_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_TERM_WIDTH_DESC");

	/**
	 * Lists the available security service providers for SecureRandom and exits
	 */
	public static final String CL_SR_PROVIDERS = "l";

	/**
	 * Lists the available security service providers for SecureRandom and exits
	 */
	public static final String CL_SR_PROVIDERS_LONG = "list-sr-providers";

	/**
	 * The description of the -l and the --list-sr-providers command line
	 * options
	 */
	public static final String CL_SR_PROVIDERS_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_SR_PROVIDERS_DESC");

	/**
	 * Lists all available security providers and algorithms
	 */
	public static final String CL_PROVIDERS = "L";

	/**
	 * Lists all available security providers and algorithms
	 */
	public static final String CL_PROVIDERS_LONG = "list-providers";

	/**
	 * The description of the -L and the --list-providers command line options
	 */
	public static final String CL_PROVIDERS_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_PROVIDERS_DESC");

	/**
	 * Sets the random algorithm used by SecureRandom
	 */
	public static final String CL_SR_ALGORITHM = "S";

	/**
	 * Sets the random algorithm used by SecureRandom
	 */
	public static final String CL_SR_ALGORITHM_LONG = "set-algorithm";

	/**
	 * The description of the -S and the --set-algorithm command line options
	 */
	public static final String CL_SR_ALGORITHM_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_SR_ALGORITHM_DESC");

	/**
	 * Sets the maximum number of attempts for generating a password with the
	 * provided policies
	 */
	public static final String CL_MAX_ATTEMPTS = "M";

	/**
	 * Sets the maximum number of attempts for generating a password with the
	 * provided policies
	 */
	public static final String CL_MAX_ATTEMPTS_LONG = "max-attempts";

	/**
	 * The description of the -M and the --max-attempts command line options
	 */
	public static final String CL_MAX_ATTEMPTS_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_MAX_ATTEMPTS_DESC");

	/**
	 * Generates password starting with a character different than a small
	 * letter
	 */
	public static final String CL_REGEX_STARTS_NO_SMALL_LETTER = "b";

	/**
	 * Generates password starting with a character different than a small
	 * letter
	 */
	public static final String CL_REGEX_STARTS_NO_SMALL_LETTER_LONG = "start-no-small-letter";

	/**
	 * The description of the -b and the --start-no-small-letter command line
	 * options
	 */
	public static final String CL_REGEX_STARTS_NO_SMALL_LETTER_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_REGEX_STARTS_NO_SMALL_LETTER_DESC");

	/**
	 * Generates password ending with a character different than a small letter
	 */
	public static final String CL_REGEX_ENDS_NO_SMALL_LETTER = "d";

	/**
	 * Generates password ending with a character different than a small letter
	 */
	public static final String CL_REGEX_ENDS_NO_SMALL_LETTER_LONG = "end-no-small-letter";

	/**
	 * The description of the -d and the --end-no-small-letter command line
	 * options
	 */
	public static final String CL_REGEX_ENDS_NO_SMALL_LETTER_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_REGEX_ENDS_NO_SMALL_LETTER_DESC");

	/**
	 * Generates password starting with a character different than a uppercase
	 * letter
	 */
	public static final String CL_REGEX_STARTS_NO_UPPER_LETTER = "e";

	/**
	 * Generates password starting with a character different than a uppercase
	 * letter
	 */
	public static final String CL_REGEX_STARTS_NO_UPPER_LETTER_LONG = "start-no-uppercase-letter";

	/**
	 * The description of the -e and the --start-no-uppercase-letter command
	 * line options
	 */
	public static final String CL_REGEX_STARTS_NO_UPPER_LETTER_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_REGEX_STARTS_NO_UPPER_LETTER_DESC");

	/**
	 * Generates password ending with a character different than a uppercase
	 * letter
	 */
	public static final String CL_REGEX_ENDS_NO_UPPER_LETTER = "f";

	/**
	 * Generates password ending with a character different than a uppercase
	 * letter
	 */
	public static final String CL_REGEX_ENDS_NO_UPPER_LETTER_LONG = "end-no-uppercase-letter";

	/**
	 * The description of the -f and the --end-no-uppercase-letter command line
	 * options
	 */
	public static final String CL_REGEX_ENDS_NO_UPPER_LETTER_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_REGEX_ENDS_NO_UPPER_LETTER_DESC");

	/**
	 * Generates password ending with a character different than a digit
	 */
	public static final String CL_REGEX_ENDS_NO_DIGIT = "g";

	/**
	 * Generates password ending with a character different than a digit
	 */
	public static final String CL_REGEX_ENDS_NO_DIGIT_LONG = "end-no-digit";

	/**
	 * The description of the -g and the --end-no-digit command line options
	 */
	public static final String CL_REGEX_ENDS_NO_DIGIT_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_REGEX_ENDS_NO_DIGIT_DESC");

	/**
	 * Generates password starting with a character different than a digit
	 */
	public static final String CL_REGEX_STARTS_NO_DIGIT = "i";

	/**
	 * Generates password starting with a character different than a digit
	 */
	public static final String CL_REGEX_STARTS_NO_DIGIT_LONG = "start-no-digit-letter";

	/**
	 * The description of the -i and the --start-no-digit-letter command line
	 * options
	 */
	public static final String CL_REGEX_STARTS_NO_DIGIT_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_REGEX_STARTS_NO_DIGIT_DESC");

	/**
	 * Generates password starting with a character different than a symbol
	 */
	public static final String CL_REGEX_STARTS_NO_SYMBOL = "j";

	/**
	 * Generates password starting with a character different than a symbol
	 */
	public static final String CL_REGEX_STARTS_NO_SYMBOL_LONG = "start-no-symbol-letter";

	/**
	 * The description of the -j and the --start-no-symbol-letter command line
	 * options
	 */
	public static final String CL_REGEX_STARTS_NO_SYMBOL_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_REGEX_STARTS_NO_SYMBOL_DESC");

	/**
	 * Generates password ending with a character different than a symbol
	 */
	public static final String CL_REGEX_ENDS_NO_SYMBOL = "k";

	/**
	 * Generates password ending with a character different than a symbol
	 */
	public static final String CL_REGEX_ENDS_NO_SYMBOL_LONG = "end-no-symbol";

	/**
	 * The description of the -k and the --end-no-symbol command line options
	 */
	public static final String CL_REGEX_ENDS_NO_SYMBOL_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_REGEX_ENDS_NO_SYMBOL_DESC");

	/**
	 * Generates password containing exactly one uppercase letter
	 */
	public static final String CL_REGEX_ONLY_1_CAPITAL = "m";

	/**
	 * Generates password containing exactly one uppercase letter
	 */
	public static final String CL_REGEX_ONLY_1_CAPITAL_LONG = "one-upercase";

	/**
	 * The description of the -m and the --one-upercase command line options
	 */
	public static final String CL_REGEX_ONLY_1_CAPITAL_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_REGEX_ONLY_1_CAPITAL_DESC");

	/**
	 * Generates password containing exactly one symbol
	 */
	public static final String CL_REGEX_ONLY_1_SYMBOL = "o";

	/**
	 * Generates password containing exactly one symbol
	 */
	public static final String CL_REGEX_ONLY_1_SYMBOL_LONG = "one-symbol";

	/**
	 * The description of the -o and the --one-symbol command line options
	 */
	public static final String CL_REGEX_ONLY_1_SYMBOL_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_REGEX_ONLY_1_SYMBOL_DESC");

	/**
	 * Generates password containing at least two symbols
	 */
	public static final String CL_REGEX_AT_LEAST_2_SYMBOLS = "p";

	public static final String CL_REGEX_AT_LEAST_2_SYMBOLS_LONG = "two-symbol";

	/**
	 * The description of the -p and the --two-symbol command line options
	 */
	public static final String CL_REGEX_AT_LEAST_2_SYMBOLS_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_REGEX_AT_LEAST_2_SYMBOLS_DESC");

	/**
	 * Generates password containing exactly one digit
	 */
	public static final String CL_REGEX_ONLY_1_DIGIT = "q";

	public static final String CL_REGEX_ONLY_1_DIGIT_LONG = "one-digit";

	/**
	 * The description of the -q and the --one-digit command line options
	 */
	public static final String CL_REGEX_ONLY_1_DIGIT_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_REGEX_ONLY_1_DIGIT_DESC");

	/**
	 * Generates password containing at least two digits
	 */
	public static final String CL_REGEX_AT_LEAST_2_DIGITS = "u";

	/**
	 * Generates password containing at least two symbols
	 */
	public static final String CL_REGEX_AT_LEAST_2_DIGITS_LONG = "two-digits";

	/**
	 * The description of the -u and the --two-digits command line options
	 */
	public static final String CL_REGEX_AT_LEAST_2_DIGITS_DESC = Messages
			.getString("IPwGenCommandLineOptions.CL_REGEX_AT_LEAST_2_DIGITS_DESC");
}
