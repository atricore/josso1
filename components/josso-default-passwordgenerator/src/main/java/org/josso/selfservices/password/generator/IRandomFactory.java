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

import org.apache.commons.logging.Log;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.Random;
import java.util.Set;

/**
 * Interface defining an advanced type of factory for random number generation.
 * It utilizes secure random numbers, different algorithms and security
 * providers.
 * 
 * @author unrz205
 */
public interface IRandomFactory
{
	/**
	 * Identifier of the SecureRandom type
	 */
	public static final String TYPE_SECURE_RANDOM = "SecureRandom";

	/**
	 * Identifier of the ordinary Random type
	 */
	public static final String TYPE_RANDOM = "Random";

	/**
	 * Identifier of the default SUN security provider
	 */
	public static final String PROVIDER_SUN = "SUN";

	/**
	 * Identifier of the default SUN security provider
	 */
	public static final String PROVIDER_SOFTWARE = "Software";

	/**
	 * Default security provider
	 */
	public static final String PROVIDER_DEFAULT = PROVIDER_SUN;

	/**
	 * The SHA1PRNG algorithm
	 */
	public static final String ALG_SHA1PRNG = "SHA1PRNG";

	/**
	 * Default algorithm to be used
	 */
	public static final String ALG_DEFAULT = ALG_SHA1PRNG;

	/**
	 * Helper string for parsing and filtering available algorithms
	 */
	public static final String ALG_PARSE_STRING = "Alg.Alias.";

	/**
	 * Returns a normal Random number
	 * 
	 * @return the random number
	 */
	public Random getRandom();

	/**
	 * Returns a seeded normal Random number
	 * 
	 * @return the seeded random number
	 */
	public Random getRandom(long seed);

	/**
	 * Returns a default instance of a SecureRandom number
	 * 
	 * @return the SecureRandom number
	 */
	public Random getSecureRandom() throws NoSuchAlgorithmException,
			NoSuchProviderException;

	/**
	 * Returns a SecureNumber initialized with a predefined algorithm
	 * 
	 * @param algorithm
	 *            the algorithm that should be used for the random number
	 *            initialization
	 * @return the SecureNumber
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	public Random getSecureRandom(String algorithm)
			throws NoSuchAlgorithmException, NoSuchProviderException;

	/**
	 * Returns a SecureNumber initialized with a predefined algorithm
	 * 
	 * @param algorithm
	 *            the algorithm that should be used for the random number
	 *            initialization
	 * @param provider
	 *            the security provider that should be used for the random
	 *            number initialization
	 * @return the SecureNumber
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	public Random getSecureRandom(String algorithm, String provider)
			throws NoSuchAlgorithmException, NoSuchProviderException;;

	/**
	 * Reads all available security providers and extracts all the registered
	 * algorithms.
	 * 
	 * @return a list of the available algorithms
	 */
	public Set<String> getAlgorithms();

	/**
	 * Reads all registered security providers
	 * 
	 * @return a list of all registered security providers
	 */
	public Provider[] getProviders();

	/**
	 * Returns all the algorithm implementations for the provided type.
	 * 
	 * @param type
	 *            the algorithm type
	 * @return the algorithm implementations
	 */
	public Set<String> getServiceProviderFor(String type);
}
