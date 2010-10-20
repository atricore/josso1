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

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.selfservices.password.PasswordGenerator;
import org.josso.selfservices.ChallengeResponseCredential;
import org.josso.gateway.identity.SSOUser;

/**
 * It performs the actual password
 * generation. The idea of this class is to generate passwords that are
 * relatively easily to remember but at the same time complex enough. It
 * utilizes a predefined set of vowels and consonants that are brought together by
 * an iterative algorithm. The concept of dipthong is also used which can be
 * used as an additional flag for a consonant or a vowel. Typical examples of
 * dipthongs are <em>ei</em> for vowels and <em>th</em> for consonants.
 * <p/>
 * Various algorithms for random generation can be used for feeding the process
 * of password generation. This ensures unique password creation.
 * <p/>
 * By default two filters are registered in the PwGenerator. The first one is an
 * empty black list filter. It can be used to filter out forbidden predefined
 * passwords. The second one is based on regular expressions and uses frequently
 * utilized rules for filtering passwords such as the number of contained symbols,
 * digits and so on. See the help for a detailed description.
 * <p/>
 * <p/>
 * <p/>
 * <h3>
 *
 * @org.apache.xbean.XBean element="password-generator"
 *
 * @author unrz205
 */
public class PasswordGeneratorImpl implements
        IPwGenConstants,
        IPwGenCommandLineOptions,
        IPwGenRegEx, PasswordGenerator {


    // The class logger
    private static final Log log = LogFactory.getLog(PasswordGeneratorImpl.class);

    // A static list of predefined vowels and consonants dipthongs. Suitable for
    // English speaking people.
    // This can be exchanged or extended with a different one if needed.
    private static final PwElement[] PW_ELEMENTS =
            {new PwElement("a", VOWEL), new PwElement("ae", VOWEL | DIPTHONG), //$NON-NLS-1$ //$NON-NLS-2$
                    new PwElement("ah", VOWEL | DIPTHONG), //$NON-NLS-1$
                    new PwElement("ai", VOWEL | DIPTHONG), //$NON-NLS-1$
                    new PwElement("b", CONSONANT), new PwElement("c", CONSONANT), //$NON-NLS-1$ //$NON-NLS-2$
                    new PwElement("ch", CONSONANT | DIPTHONG), //$NON-NLS-1$
                    new PwElement("d", CONSONANT), new PwElement("e", VOWEL), //$NON-NLS-1$ //$NON-NLS-2$
                    new PwElement("ee", VOWEL | DIPTHONG), //$NON-NLS-1$
                    new PwElement("ei", VOWEL | DIPTHONG), //$NON-NLS-1$
                    new PwElement("f", CONSONANT), new PwElement("g", CONSONANT), //$NON-NLS-1$ //$NON-NLS-2$
                    new PwElement("gh", CONSONANT | DIPTHONG | NOT_FIRST), //$NON-NLS-1$
                    new PwElement("h", CONSONANT), new PwElement("i", VOWEL), //$NON-NLS-1$ //$NON-NLS-2$
                    new PwElement("ie", VOWEL | DIPTHONG), //$NON-NLS-1$
                    new PwElement("j", CONSONANT), new PwElement("k", CONSONANT), //$NON-NLS-1$ //$NON-NLS-2$
                    new PwElement("l", CONSONANT), new PwElement("m", CONSONANT), //$NON-NLS-1$ //$NON-NLS-2$
                    new PwElement("n", CONSONANT), //$NON-NLS-1$
                    new PwElement("ng", CONSONANT | DIPTHONG | NOT_FIRST), //$NON-NLS-1$
                    new PwElement("o", VOWEL), new PwElement("oh", VOWEL | DIPTHONG), //$NON-NLS-1$ //$NON-NLS-2$
                    new PwElement("oo", VOWEL | DIPTHONG), //$NON-NLS-1$
                    new PwElement("p", CONSONANT), //$NON-NLS-1$
                    new PwElement("ph", CONSONANT | DIPTHONG), //$NON-NLS-1$
                    new PwElement("qu", CONSONANT | DIPTHONG), //$NON-NLS-1$
                    new PwElement("r", CONSONANT), new PwElement("s", CONSONANT), //$NON-NLS-1$ //$NON-NLS-2$
                    new PwElement("sh", CONSONANT | DIPTHONG),
                    new PwElement("t", CONSONANT), //$NON-NLS-1$
                    new PwElement("th", CONSONANT | DIPTHONG), //$NON-NLS-1$
                    new PwElement("u", VOWEL), new PwElement("v", CONSONANT), //$NON-NLS-1$ //$NON-NLS-2$
                    new PwElement("w", CONSONANT), new PwElement("x", CONSONANT), //$NON-NLS-1$ //$NON-NLS-2$
                    new PwElement("y", CONSONANT), new PwElement("z", CONSONANT)}; //$NON-NLS-1$ //$NON-NLS-2$

    // An instance of the Random number that would be used during the generation
    // process
    private Random random;

    private Map<String, IPasswordFilter> filters = new HashMap<String, IPasswordFilter>();;

    private static int DEFAULT_MAX_ATTEMPTS = 10000;

    private IRandomFactory randomFactory;

    // -------------------------------------------------< Configuration Propertyes >

    // Flags used during the process of password generation
    private int passwordFlags = 0;

    // The length of the password to be generated
    private int passwordLength = DEFAULT_PASSWORD_LENGTH;

    // Use simple random
    private boolean useSimpleRandom;

    private String secureRandomAlgorithm;

    private String secureRandomProvider;

    private boolean generateNumerals;

    private boolean generateCapitalLetters;

    private boolean includeAmbigousChars;

    private boolean includeSpecialSymbols;

    private boolean regexStartsNoSmallLetter;

    private boolean regexEndsNoSmallLetter;

    private boolean regexStartsNoUpperLetter;

    private boolean regexEndsNosUpperLetter;

    private boolean regexEndsNoDigit;

    private boolean regexStartsNoDigit;

    private boolean regexStartsNoSymbol;

    private boolean regexEndsNoSymbol;

    private boolean regexOnlyOneCapital;

    private boolean regexOnlyOneSymbol;

    private boolean regexAtLeastTwoSymbols;

    private boolean regexOnlyOneDigit;

    private boolean regexAtLeastTwoDigits;

    private int maxAttempts = DEFAULT_MAX_ATTEMPTS;

    /**
     * Constructor of the PasswordGeneratorImpl
     */
    public PasswordGeneratorImpl() {

        try {


            // We don't want to expose this to spring

            passwordFlags |= PW_UPPERS;
            log.debug(Messages.getString("PwGenerator.debug_UPPERCASE_ON"));

            passwordFlags |= PW_DIGITS;
            log.debug(Messages.getString("PwGenerator.debug_DIGITS_ON"));
            // passwordFlags |= PW_SYMBOLS;
            // passwordFlags |= PW_AMBIGUOUS;

            randomFactory = RandomFactory.getInstance();

            random = randomFactory.getSecureRandom();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            random = randomFactory.getRandom();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            random = randomFactory.getRandom();
        }
    }


    public String generateClearPassword() {
        List<String> pwds = this.process(1);
        return pwds.get(0);
    }

    public String generateClearPassword(SSOUser user, Set<ChallengeResponseCredential> challenges) {
        log.debug("User and challenges ignored!");
        List<String> pwds = this.process(1);
        return pwds.get(0);
    }

    /**
     * This method logs some general info about the given settings and tries to
     * generate passwords with the given flags and given length. The method
     * return <em>null</em> if it does not manage to create a suitable
     * password within the <em>MAX_ATTEMPTS</em>.
     *
     * @param passwordLength the length of the password to be generated
     * @param passwordFlags  the settings for the particular password
     * @return a suitable password or <em>null</em> if such could not be
     *         generated
     */
    public String generatePassword(int passwordLength, int passwordFlags) {
        if (passwordLength <= 2) {
            passwordFlags &= ~PW_UPPERS;
            log.warn(Messages.getString("PwGenerator.WARN_PL_UPERCASE_OFF")); //$NON-NLS-1$
        }
        if (passwordLength <= 2) {
            passwordFlags &= ~PW_SYMBOLS;
            log.warn(Messages.getString("PwGenerator.WARN_PL_SYMBOLS_OFF")); //$NON-NLS-1$
        }
        if (passwordLength <= 1) {
            passwordFlags &= ~PW_DIGITS;
            log.warn(Messages.getString("PwGenerator.WARN_PL_DIGITS_OFF")); //$NON-NLS-1$
        }

        String password = null;
        for (int i = 0; i < getMaxAttempts() ; i++) {
            password = phonemes(passwordLength, passwordFlags);
            Set<String> filterIDs = filters.keySet();

            for (Iterator<String> iter = filterIDs.iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                IPasswordFilter filter = filters.get(element);
                password = filter.filter(passwordFlags, password);
                if (password == null)
                    break;
            }

            if (password != null)
                break;

            log
                    .debug(Messages.getString("PwGenerator.debug_ATTEMPT") + i + Messages.getString("PwGenerator.debug_ATTEMPT_GENERATE") //$NON-NLS-1$ //$NON-NLS-2$
                            + passwordFlags);
        }

        return password;
    }

    /**
     * @param numberOfPasswords the number of passwords to be generated.
     * @return a list of passwords or <em>null</em> if no suitable passwords
     *         could be generated.
     */
    public List<String> process(int numberOfPasswords) {

        log.debug(Messages.getString("PwGenerator.PASSWORD_GENERATOR"));

        ArrayList<String> passwords = new ArrayList<String>();

        if (isUseSimpleRandom()) {
            random = randomFactory.getRandom();
            log.debug(Messages.getString("PwGenerator.NORMAL_RANDOM"));
        }


        try {
            random = randomFactory.getSecureRandom(getSecureRandomAlgorithm(), getSecureRandomProvider());
            log.debug(Messages.getString("PwGenerator.SEC_ALG") + getSecureRandomAlgorithm() + Messages.getString("PwGenerator.PROV") + getSecureRandomProvider() + Messages.getString("PwGenerator.DOR"));
        } catch (NoSuchAlgorithmException e) {
            log.error(Messages.getString("PwGenerator.ERROR") + e.getMessage() + Messages.getString("PwGenerator.NEW_LINE"));
            log.debug(Messages.getString("PwGenerator.DEFAUL_RANDOM"));
        } catch (NoSuchProviderException e) {
            log.error(Messages.getString("PwGenerator.ERROR") + e.getMessage() + Messages.getString("PwGenerator.NEW_LINE"));
            log.error(Messages.getString("PwGenerator.DEFAUL_RANDOM"));
        }

        if (isGenerateNumerals()) {
            passwordFlags |= PW_DIGITS;
            log.debug(Messages.getString("PwGenerator.DIGITS_ON"));
        } else {
            passwordFlags &= ~PW_DIGITS;
            log.debug(Messages.getString("PwGenerator.DIGITS_OFF"));
        }

        if (isGenerateCapitalLetters()) {
            passwordFlags |= PW_UPPERS;
            log.debug(Messages.getString("PwGenerator.UPPERCASE_ON"));
        } else {
            passwordFlags &= ~PW_UPPERS;
            log.debug(Messages.getString("PwGenerator.UPPERCASE_OFF"));
        }

        if (isIncludeAmbigousChars()) {
            passwordFlags |= PW_AMBIGUOUS;
            log.debug(Messages.getString("PwGenerator.AMBIGOUS_ON"));
        } else {
            passwordFlags &= ~PW_AMBIGUOUS;
            log.debug(Messages.getString("PwGenerator.AMBIGOUS_OFF"));
        }

        if (isIncludeSpecialSymbols()) {
            passwordFlags |= PW_SYMBOLS;
            log.debug(Messages.getString("PwGenerator.SYMBOLS_ON"));
        } else {
            passwordFlags &= ~PW_SYMBOLS;
            log.debug(Messages.getString("PwGenerator.SYMBOLS_OFF"));
        }

        if (isRegexStartsNoSmallLetter())
            passwordFlags |= REGEX_STARTS_NO_SMALL_LETTER_FLAG;

        if (isRegexEndsNoSmallLetter())
            passwordFlags |= REGEX_STARTS_NO_SMALL_LETTER_FLAG;

        if (isRegexStartsNoUpperLetter())
            passwordFlags |= REGEX_STARTS_NO_UPPER_LETTER_FLAG;

        if (isRegexEndsNosUpperLetter())
            passwordFlags |= REGEX_ENDS_NO_UPPER_LETTER_FLAG;

        if (isRegexEndsNoDigit())
            passwordFlags |= REGEX_ENDS_NO_DIGIT_FLAG;

        if (isRegexStartsNoDigit())
            passwordFlags |= REGEX_STARTS_NO_DIGIT_FLAG;

        if (isRegexStartsNoSymbol())
            passwordFlags |= REGEX_STARTS_NO_SYMBOL_FLAG;

        if (isRegexEndsNoSymbol())
            passwordFlags |= REGEX_ENDS_NO_SYMBOL_FLAG;

        if (isRegexOnlyOneCapital())
            passwordFlags |= REGEX_ONLY_1_CAPITAL_FLAG;

        if (isRegexOnlyOneSymbol())
            passwordFlags |= REGEX_ONLY_1_SYMBOL_FLAG;

        if (isRegexAtLeastTwoSymbols())
            passwordFlags |= REGEX_AT_LEAST_2_SYMBOLS_FLAG;

        if (isRegexOnlyOneDigit())
            passwordFlags |= REGEX_ONLY_1_DIGIT_FLAG;

        if (isRegexAtLeastTwoDigits())
            passwordFlags |= REGEX_AT_LEAST_2_DIGITS_FLAG;
        // -------------------------------------------------------------------

        log.debug(Messages.getString("PwGenerator.GENRIC_FLAGS"));

        int res = passwordFlags & PW_DIGITS;
        log.debug(Messages.getString("PwGenerator.DIGITS") + (res != 0));
        res = passwordFlags & PW_AMBIGUOUS;
        log.debug(Messages.getString("PwGenerator.AMBIGOUS") + (res != 0));
        res = passwordFlags & PW_SYMBOLS;
        log.debug(Messages.getString("PwGenerator.SYMBOLS") + (res != 0));
        res = passwordFlags & PW_UPPERS;
        log.debug(Messages.getString("PwGenerator.UPPERS") + (res != 0));
        log.debug(Messages.getString("PwGenerator.SEPARATOR"));

        log.debug(Messages.getString("PwGenerator.GENERATING") + numberOfPasswords + Messages.getString("PwGenerator.PW_LENGTH") + passwordLength);
        log.debug(Messages.getString("PwGenerator.PW"));

        int i;
        for (i = 0; i < numberOfPasswords; i++) {
            String password = generatePassword(passwordLength,
                    passwordFlags);
            if (password != null)
                passwords.add(password);
        }
        return passwords;
    }


    /**
     * Adds a password filter to the registry
     *
     * @param filter the filter instance to be registered
     * @return the registered instance
     */
    public IPasswordFilter addFilter(IPasswordFilter filter) {
        return filters.put(filter.getId(), filter);
    }

    /**
     * Removes a filter from the registry by instance search
     *
     * @param filter the instance of the filter
     * @return the removed instance
     */
    public IPasswordFilter removeFilter(IPasswordFilter filter) {
        return filters.remove(filter.getId());
    }

    /**
     * Removes a filter from the registry by identifier search
     *
     * @param id the identifier of the filter
     * @return the removed instance
     */
    public IPasswordFilter removeFilter(String id) {
        return filters.remove(id);
    }

    // ------------------------------------------------------------< Configuration >

    public Collection<IPasswordFilter> getFilters() {
        return filters.values();
    }

    /**
     * @org.apache.xbean.Property alias="filters" nestedType="org.josso.selfservices.password.generator.IPasswordFilter"
     * @param filters
     */
    public void setFilters(Collection<IPasswordFilter> filters) {
        for (IPasswordFilter filter: filters) {
            this.filters.put(filter.getId(), filter);
        }
    }

    public int getPasswordLength() {
        return passwordLength;
    }

    public void setPasswordLength(int passwordLength) {
        this.passwordLength = passwordLength;
    }

    public boolean isUseSimpleRandom() {
        return useSimpleRandom;
    }

    public void setUseSimpleRandom(boolean useSimpleRandom) {
        this.useSimpleRandom = useSimpleRandom;
    }

    public String getSecureRandomAlgorithm() {
        return secureRandomAlgorithm;
    }

    public void setSecureRandomAlgorithm(String secureRandomAlgorithm) {
        this.secureRandomAlgorithm = secureRandomAlgorithm;
    }

    public String getSecureRandomProvider() {
        return secureRandomProvider;
    }

    public void setSecureRandomProvider(String secureRandomProvider) {
        this.secureRandomProvider = secureRandomProvider;
    }

    public boolean isGenerateNumerals() {
        return generateNumerals;
    }

    public void setGenerateNumerals(boolean generateNumerals) {
        this.generateNumerals = generateNumerals;
    }

    public boolean isGenerateCapitalLetters() {
        return generateCapitalLetters;
    }

    public void setGenerateCapitalLetters(boolean generateCapitalLetters) {
        this.generateCapitalLetters = generateCapitalLetters;
    }

    public boolean isIncludeAmbigousChars() {
        return includeAmbigousChars;
    }

    public void setIncludeAmbigousChars(boolean includeAmbigousChars) {
        this.includeAmbigousChars = includeAmbigousChars;
    }


    public boolean isIncludeSpecialSymbols() {
        return includeSpecialSymbols;
    }

    public void setIncludeSpecialSymbols(boolean includeSpecialSymbols) {
        this.includeSpecialSymbols = includeSpecialSymbols;
    }


    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public boolean isRegexStartsNoSmallLetter() {
        return regexStartsNoSmallLetter;
    }

    public void setRegexStartsNoSmallLetter(boolean regexStartsNoSmallLetter) {
        this.regexStartsNoSmallLetter = regexStartsNoSmallLetter;
    }

    public boolean isRegexEndsNoSmallLetter() {
        return regexEndsNoSmallLetter;
    }

    public void setRegexEndsNoSmallLetter(boolean regexEndsNoSmallLetter) {
        this.regexEndsNoSmallLetter = regexEndsNoSmallLetter;
    }

    public boolean isRegexStartsNoUpperLetter() {
        return regexStartsNoUpperLetter;
    }

    public void setRegexStartsNoUpperLetter(boolean regexStartsNoUpperLetter) {
        this.regexStartsNoUpperLetter = regexStartsNoUpperLetter;
    }

    public boolean isRegexEndsNosUpperLetter() {
        return regexEndsNosUpperLetter;
    }

    public void setRegexEndsNosUpperLetter(boolean regexEndsNosUpperLetter) {
        this.regexEndsNosUpperLetter = regexEndsNosUpperLetter;
    }

    public boolean isRegexEndsNoDigit() {
        return regexEndsNoDigit;
    }

    public void setRegexEndsNoDigit(boolean regexEndsNoDigit) {
        this.regexEndsNoDigit = regexEndsNoDigit;
    }

    public boolean isRegexStartsNoDigit() {
        return regexStartsNoDigit;
    }

    public void setRegexStartsNoDigit(boolean regexStartsNoDigit) {
        this.regexStartsNoDigit = regexStartsNoDigit;
    }

    public boolean isRegexStartsNoSymbol() {
        return regexStartsNoSymbol;
    }

    public void setRegexStartsNoSymbol(boolean regexStartsNoSymbol) {
        this.regexStartsNoSymbol = regexStartsNoSymbol;
    }

    public boolean isRegexEndsNoSymbol() {
        return regexEndsNoSymbol;
    }

    public void setRegexEndsNoSymbol(boolean regexEndsNoSymbol) {
        this.regexEndsNoSymbol = regexEndsNoSymbol;
    }

    public boolean isRegexOnlyOneCapital() {
        return regexOnlyOneCapital;
    }

    public void setRegexOnlyOneCapital(boolean regexOnlyOneCapital) {
        this.regexOnlyOneCapital = regexOnlyOneCapital;
    }

    public boolean isRegexOnlyOneSymbol() {
        return regexOnlyOneSymbol;
    }

    public void setRegexOnlyOneSymbol(boolean regexOnlyOneSymbol) {
        this.regexOnlyOneSymbol = regexOnlyOneSymbol;
    }

    public boolean isRegexAtLeastTwoSymbols() {
        return regexAtLeastTwoSymbols;
    }

    public void setRegexAtLeastTwoSymbols(boolean regexAtLeastTwoSymbols) {
        this.regexAtLeastTwoSymbols = regexAtLeastTwoSymbols;
    }

    public boolean isRegexOnlyOneDigit() {
        return regexOnlyOneDigit;
    }

    public void setRegexOnlyOneDigit(boolean regexOnlyOneDigit) {
        this.regexOnlyOneDigit = regexOnlyOneDigit;
    }

    public boolean isRegexAtLeastTwoDigits() {
        return regexAtLeastTwoDigits;
    }

    public void setRegexAtLeastTwoDigits(boolean regexAtLeastTwoDigits) {
        this.regexAtLeastTwoDigits = regexAtLeastTwoDigits;
    }

    // ---------------------------------------------------------< Utils >

    /**
     * The real password generation is performed in this method
     *
     * @param size     the length of the password
     * @param pw_flags the settings for the password
     * @return the newly created password
     */
    private String phonemes(int size, int pw_flags) {
        int c, i, len, flags, feature_flags;
        int prev, should_be;
        boolean first;
        String str;
        char ch;
        StringBuffer buf = new StringBuffer();

        do {
            buf.delete(0, buf.length());
            feature_flags = pw_flags;
            c = 0;
            prev = 0;
            should_be = 0;
            first = true;
            should_be = random.nextBoolean() ? VOWEL : CONSONANT;

            while (c < size) {

                i = random.nextInt(PW_ELEMENTS.length);
                str = PW_ELEMENTS[i].getValue();
                len = str.length();
                flags = PW_ELEMENTS[i].getType();
                /* Filter on the basic type of the next element */
                if ((flags & should_be) == 0) {
                    continue;
                }
                /* Handle the NOT_FIRST flag */
                if (first && ((flags & NOT_FIRST) != 0))
                    continue;
                /* Don't allow VOWEL followed a Vowel/Dipthong pair */
                if (((prev & VOWEL) != 0) && ((flags & VOWEL) != 0)
                        && ((flags & DIPTHONG) != 0))
                    continue;
                /* Don't allow us to overflow the buffer */
                if (len > size - c)
                    continue;
                /*
                     * OK, we found an element which matches our criteria, let's do
                     * it!
                     */
                buf.append(str);

                /* Handle PW_UPPERS */
                if ((pw_flags & PW_UPPERS) != 0) {
                    if ((first || ((flags & CONSONANT) != 0))
                            && (random.nextInt(10) < 2)) {
                        int lastChar = buf.length() - 1;
                        buf.setCharAt(lastChar, Character.toUpperCase(buf
                                .charAt(lastChar)));
                        feature_flags &= ~PW_UPPERS;
                    }
                }

                c += len;
                /* Handle the AMBIGUOUS flag */
                if ((pw_flags & PW_AMBIGUOUS) != 0) {
                    int k = -1;
                    for (int j = 0; j < PW_AMBIGUOUS_SYMBOLS.length(); j++) {
                        k = buf.indexOf(String.valueOf(PW_AMBIGUOUS_SYMBOLS
                                .charAt(j)));
                        if (k != -1)
                            break;
                    }
                    if (k != -1) {
                        buf.delete(k, buf.length());
                        c = buf.length();
                    }
                }

                /* Time to stop? */
                if (c >= size)
                    break;

                /*
                     * Handle PW_DIGITS
                     */
                if ((pw_flags & PW_DIGITS) != 0) {
                    if (!first && (random.nextInt(10) < 3)) {
                        do {
                            ch = (new Integer(random.nextInt(10))).toString()
                                    .charAt(0);
                        } while (((pw_flags & PW_AMBIGUOUS) != 0)
                                && (PW_AMBIGUOUS_SYMBOLS.indexOf(ch) != -1));
                        c++;
                        buf = buf.append(ch);
                        feature_flags &= ~PW_DIGITS;

                        first = true;
                        prev = 0;
                        should_be = random.nextBoolean() ? VOWEL : CONSONANT;
                        continue;
                    }
                }

                /*
                     * OK, figure out what the next element should be
                     */
                if (should_be == CONSONANT) {
                    should_be = VOWEL;
                } else { /* should_be == VOWEL */
                    if (((prev & VOWEL) != 0) || ((flags & DIPTHONG) != 0)
                            || (random.nextInt(10) > 3))
                        should_be = CONSONANT;
                    else
                        should_be = VOWEL;
                }
                prev = flags;
                first = false;

                /* Handle PW_SYMBOLS */
                if ((pw_flags & PW_SYMBOLS) != 0) {
                    if (!first && (random.nextInt(10) < 2)) {
                        do {
                            ch = PW_SPECIAL_SYMBOLS.charAt(random
                                    .nextInt(PW_SPECIAL_SYMBOLS.length()));
                        } while (((pw_flags & PW_AMBIGUOUS) != 0)
                                && (PW_AMBIGUOUS_SYMBOLS.indexOf(ch) != -1));
                        c++;
                        buf = buf.append(ch);
                        feature_flags &= ~PW_SYMBOLS;
                    }
                }

            }
        } while ((feature_flags & (PW_UPPERS | PW_DIGITS | PW_SYMBOLS)) != 0);

        return buf.toString();
    }

}