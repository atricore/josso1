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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A password filter that uses to regular expressions to filter commonly
 * forbidden patterns in passwords. The class supports also a blacklist
 * filtering. By default the blacklist is empty.
 *
 * @author unrz205
 * @org.apache.xbean.XBean element="regex-passwordfilter"
 */
public class DefaultRegExFilter implements IPasswordFilter, IPwGenConstants,
        IPwGenRegEx {

    // A list that stores the forbidden words.
    private List<String> blacklist = new ArrayList<String>();

    // Filters passwords starting with small letter
    private static final Pattern REGEX_STARTS_NO_SMALL_LETTER_P = Pattern
            .compile(REGEX_STARTS_NO_SMALL_LETTER);

    // Filters passwords ending with small letter
    private static final Pattern REGEX_ENDS_NO_SMALL_LETTER_P = Pattern
            .compile(REGEX_ENDS_NO_SMALL_LETTER);

    // Filters passwords starting with a capital letter
    private static final Pattern REGEX_STARTS_NO_UPPER_LETTER_P = Pattern
            .compile(REGEX_STARTS_NO_UPPER_LETTER);

    // Filters passwords ending with a capital letter
    private static final Pattern REGEX_ENDS_NO_UPPER_LETTER_P = Pattern
            .compile(REGEX_ENDS_NO_UPPER_LETTER);

    // Filters passwords starting with a digit letter
    private static final Pattern REGEX_STARTS_NO_DIGIT_P = Pattern
            .compile(REGEX_STARTS_NO_DIGIT);

    // Filters passwords ending with a digit letter
    private static final Pattern REGEX_ENDS_NO_DIGIT_P = Pattern
            .compile(REGEX_ENDS_NO_DIGIT);

    // Filters passwords starting with a symbol letter
    private static final Pattern REGEX_STARTS_NO_SYMBOL_P = Pattern
            .compile(REGEX_STARTS_NO_SYMBOL);

    // Filters passwords ending with a symbol letter
    private static final Pattern REGEX_ENDS_NO_SYMBOL_P = Pattern
            .compile(REGEX_ENDS_NO_SYMBOL);

    // Filters passwords that do not contain exactly one capital letter
    private static final Pattern REGEX_ONLY_1_CAPITAL_P = Pattern
            .compile(REGEX_ONLY_1_CAPITAL);

    // Filters passwords that do not contain exactly one symbol
    private static final Pattern REGEX_ONLY_1_SYMBOL_P = Pattern
            .compile(REGEX_ONLY_1_SYMBOL);

    // Filters passwords that do not contain exactly one digit
    private static final Pattern REGEX_ONLY_1_DIGIT_P = Pattern
            .compile(REGEX_ONLY_1_DIGIT);

    // Filters passwords that contain less than two symbols
    private static final Pattern REGEX_AT_LEAST_2_SYMBOLS_P = Pattern
            .compile(REGEX_AT_LEAST_2_SYMBOLS);

    // Filters passwords that contain less than two digits
    private static final Pattern REGEX_AT_LEAST_2_DIGITS_P = Pattern
            .compile(REGEX_AT_LEAST_2_DIGITS);

    // Filters passwords that contain less than two capital letters
    // private static final Pattern REGEX_AT_LEAST_2_CAPITALS_P = Pattern
    // .compile(REGEX_AT_LEAST_2_CAPITALS);

    // A logger instance
    private static final Log logger = LogFactory
            .getLog(DefaultRegExFilter.class);

    /**
     * Default construct.
     */
    public DefaultRegExFilter() {
    }

    /*
      * (non-Javadoc)
      *
      * @see org.josso.selfservices.password.generator.IPassowrdFilter#filter(int,
      *      java.lang.String)
      */
    public String filter(int passwordFlags, String password) {

        // ------------------ End character filters
        // ----------------------------- //

        if ((passwordFlags & REGEX_ENDS_NO_SMALL_LETTER_FLAG) != 0) {
            Matcher matcher = REGEX_ENDS_NO_SMALL_LETTER_P.matcher(password);
            if (matcher.find()) {
                logger.debug(Messages.getString("DefaultRegExFilter.debug_PASSWD") + password + Messages.getString("DefaultRegExFilter.ENDS_SMALL"));
                return null;
            }
        }

        if ((passwordFlags & REGEX_ENDS_NO_UPPER_LETTER_FLAG) != 0) {
            Matcher matcher = REGEX_ENDS_NO_UPPER_LETTER_P.matcher(password);
            if (matcher.find()) {
                logger.debug(Messages.getString("DefaultRegExFilter.debug_PASSWD") + password + Messages.getString("DefaultRegExFilter.ENDS_UPPER"));
                return null;
            }
        }

        if ((passwordFlags & REGEX_ENDS_NO_DIGIT_FLAG) != 0) {
            Matcher matcher = REGEX_ENDS_NO_DIGIT_P.matcher(password);
            if (matcher.find()) {
                logger.debug(Messages.getString("DefaultRegExFilter.debug_PASSWD") + password + Messages.getString("DefaultRegExFilter.ENDS_DIGIT"));
                return null;
            }
        }
        if ((passwordFlags & REGEX_ENDS_NO_SYMBOL_FLAG) != 0) {
            Matcher matcher = REGEX_ENDS_NO_SYMBOL_P.matcher(password);
            if (matcher.find()) {
                logger.debug(Messages.getString("DefaultRegExFilter.debug_PASSWD") + password + Messages.getString("DefaultRegExFilter.debug_ENDS_SYMBOL"));
                return null;
            }
        }

        // -------------------- Starts character filters
        // ------------------------------- //
        if ((passwordFlags & REGEX_STARTS_NO_SYMBOL_FLAG) != 0) {
            Matcher matcher = REGEX_STARTS_NO_SYMBOL_P.matcher(password);
            if (matcher.find()) {
                logger.debug(Messages.getString("DefaultRegExFilter.debug_PASSWD") + password + Messages.getString("DefaultRegExFilter.debug_STARTS_SYMBOL"));
                return null;
            }
        }
        if ((passwordFlags & REGEX_STARTS_NO_DIGIT_FLAG) != 0) {
            Matcher matcher = REGEX_STARTS_NO_DIGIT_P.matcher(password);
            if (matcher.find()) {
                logger.debug(Messages.getString("DefaultRegExFilter.debug_PASSWD") + password
                        + Messages
                        .getString("DefaultRegExFilter.debug_STARTS_DIGIT"));
                return null;
            }
        }

        if ((passwordFlags & REGEX_STARTS_NO_UPPER_LETTER_FLAG) != 0) {
            Matcher matcher = REGEX_STARTS_NO_UPPER_LETTER_P.matcher(password);
            if (matcher.find()) {
                logger.debug(Messages.getString("DefaultRegExFilter.debug_PASSWD") + password + Messages.getString("DefaultRegExFilter.debug_STARTS_UPERCASE"));
                return null;
            }
        }
        if ((passwordFlags & REGEX_STARTS_NO_SMALL_LETTER_FLAG) != 0) {
            Matcher matcher = REGEX_STARTS_NO_SMALL_LETTER_P.matcher(password);
            if (matcher.find()) {
                logger.debug(Messages.getString("DefaultRegExFilter.debug_PASSWD") + password + Messages.getString("DefaultRegExFilter.debug_STARTS_SMALL"));
                return null;
            }
        }

        // ------------------- Count character filters
        // ----------------------------- //
        if ((passwordFlags & REGEX_ONLY_1_SYMBOL_FLAG) != 0) {
            Matcher matcher = REGEX_ONLY_1_SYMBOL_P.matcher(password);
            if (!matcher.find()) {
                logger.debug(Messages.getString("DefaultRegExFilter.debug_PASSWD") + password + Messages.getString("DefaultRegExFilter.debug_MORE_SYMBOLS"));
                return null;
            }
        }

        if ((passwordFlags & REGEX_AT_LEAST_2_SYMBOLS_FLAG) != 0) {
            Matcher matcher = REGEX_AT_LEAST_2_SYMBOLS_P.matcher(password);
            if (!matcher.find()) {
                logger.debug(Messages.getString("DefaultRegExFilter.debug_PASSWD") + password + Messages .getString("DefaultRegExFilter.debug_NO_SYMBOLS"));
                return null;
            }
        }

        if ((passwordFlags & REGEX_ONLY_1_DIGIT_FLAG) != 0) {
            Matcher matcher = REGEX_ONLY_1_DIGIT_P.matcher(password);
            if (!matcher.find()) {
                logger.debug(Messages.getString("DefaultRegExFilter.debug_PASSWD") + password + Messages.getString("DefaultRegExFilter.debug_MORE_DIGITS"));
                return null;
            }
        }

        if ((passwordFlags & REGEX_AT_LEAST_2_DIGITS_FLAG) != 0) {
            Matcher matcher = REGEX_AT_LEAST_2_DIGITS_P.matcher(password);
            if (!matcher.find()) {
                logger.debug(Messages.getString("DefaultRegExFilter.debug_PASSWD") + password + Messages .getString("DefaultRegExFilter.debug_NO_DIGITS"));
                return null;
            }
        }

        if ((passwordFlags & REGEX_ONLY_1_CAPITAL_FLAG) != 0) {
            Matcher matcher = REGEX_ONLY_1_CAPITAL_P.matcher(password);
            if (!matcher.find()) {
                logger.debug(Messages.getString("DefaultRegExFilter.debug_PASSWD") + password + Messages .getString("DefaultRegExFilter.debug_MORE_UPPERCASE"));
                return null;
            }
        }

        return password;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.josso.selfservices.password.generator.IPassowrdFilter#getDescription()
      */
    public String getDescription() {
        return Messages.getString("DefaultRegExFilter.DESC");
    }

    /*
      * (non-Javadoc)
      *
      * @see org.josso.selfservices.password.generator.IPassowrdFilter#filter(int,
      *      java.util.List)
      */
    public List<String> filter(int passwordFlags, List<String> password) {
        List<String> suiatble = new ArrayList<String>();
        for (Iterator<String> iter = password.iterator(); iter.hasNext();) {
            String element = (String) iter.next();
            if (filter(passwordFlags, password) != null)
                suiatble.add(element);
        }
        return suiatble;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.josso.selfservices.password.generator.IPassowrdFilter#getId()
      */
    public String getId() {
        return this.getClass().getName();
    }

    /*
      * (non-Javadoc)
      *
      * @see org.josso.selfservices.password.generator.IPassowrdFilter#setDescription(java.lang.String)
      */
    public void setDescription(String description) {

    }

    /*
      * (non-Javadoc)
      *
      * @see org.josso.selfservices.password.generator.IPassowrdFilter#setId(java.lang.String)
      */
    public void setId(String id) {
        System.err.println(Messages.getString("DefaultRegExFilter.ID_CHANGE"));
    }

    /*
      * (non-Javadoc)
      *
      * @see org.josso.selfservices.password.generator.IPassowrdFilter#getBlacklist()
      * @org.apache.xbean.Property alias="blacklist" nestedType="java.lang.String"
      */
    public List<String> getBlacklist() {
        return blacklist;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.josso.selfservices.password.generator.IPassowrdFilter#setBlacklist(java.util.List)
      */
    public void setBlacklist(List<String> blacklist) {

        if (logger.isDebugEnabled()) {
            for (String blacklisted : blacklist) {
                logger.debug("Blacklisted password with regular expression ["+blacklisted+"] ");
            }
        }

        this.blacklist = blacklist;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.josso.selfservices.password.generator.IPassowrdFilter#addToBlacklist(java.lang.String)
      */
    public boolean addToBlacklist(String blackWord) {
        return blacklist.add(blackWord);
    }

    /*
      * (non-Javadoc)
      *
      * @see org.josso.selfservices.password.generator.IPassowrdFilter#removeFromBlacklist(java.lang.String)
      */
    public boolean removeFromBlacklist(String blackWord) {
        return blacklist.remove(blackWord);
    }

}
