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
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A singleton that provides various
 */
public class RandomFactory implements IRandomFactory
{
	// The class instance
	private static RandomFactory instance;

	// The class logger
	private static final Log logger = LogFactory.getLog(RandomFactory.class);

	/**
	 * Accessor to the instance
	 * 
	 * @return the singleton instance
	 */
	public static RandomFactory getInstance()
	{
		if (instance == null)
			instance = new RandomFactory();

		return instance;
	}

	/**
	 * Constructor
	 */
	private RandomFactory()
	{
	}

	/**
	 * Create a two pseudo random generator by utilizing the
	 * <em>SecureRandom</em> class provided by SUN. Uses a two step procedure
	 * for feeding the generator seed with two separate SecureRandom instances.
	 * 
	 * @see http://java.sun.com/j2se/1.4.2/docs/api/java/security/SecureRandom.html
	 * 
	 * @param algorithm
	 *            The algorithm used for creating the pseudo random generator
	 * @param provider
	 *            the provider identifier
	 * @return a seeded <em>SecureRandom</em>
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	private SecureRandom initSecureRandom(String algorithm, String provider)
			throws NoSuchAlgorithmException, NoSuchProviderException
	{
		logger.debug("Initializing random with: " + algorithm + " : "
				+ provider);
		if (provider == null)
			provider = PROVIDER_DEFAULT;

		// Create a secure random number generator
		SecureRandom sr = SecureRandom.getInstance(algorithm, provider);

		// Get 1024 random bits
		byte[] bytes = new byte[1024 / 8];
		sr.nextBytes(bytes);

		// Create two secure number generators with the same seed
		int seedByteCount = 10;
		byte[] seed = sr.generateSeed(seedByteCount);

		sr = SecureRandom.getInstance(algorithm, provider);
		sr.setSeed(seed);

		SecureRandom sr2 = SecureRandom.getInstance(algorithm, provider);
		sr2.setSeed(seed);
		return sr2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IRandomFactory#getAlgorithms()
	 */
	public Set<String> getAlgorithms()
	{
		Set<String> result = new HashSet<String>();

		// All providers
		Provider[] providers = Security.getProviders();
		for (int i = 0; i < providers.length; i++)
		{
			// Get services provided by each provider
			Set keys = providers[i].keySet();
			for (Iterator<String> it = keys.iterator(); it.hasNext();)
			{
				String key = (String) it.next();
				String value = (String) providers[i].get(key);
				result.add(value);
			}
		}
		return result;
	}

	/**
	 * Returns a cleaned up version of the service providers.
	 * 
	 * @return a set of service providers that can be used for SecureRandom
	 *         feed.
	 */
	public Set<String> getServiceProviders()
	{
		Set<String> result = new HashSet<String>();

		// All providers
		Provider[] providers = Security.getProviders();
		for (int i = 0; i < providers.length; i++)
		{
			// Get services provided by each provider
			Set keys = providers[i].keySet();
			for (Iterator<String> it = keys.iterator(); it.hasNext();)
			{
				String key = (String) it.next();
				key = key.split(" ")[0]; //$NON-NLS-1$

				if (key.startsWith(ALG_PARSE_STRING))
				{
					// Strip the alias
					key = key.substring(10);
				}
				int ix = key.indexOf('.');
				result.add(key.substring(0, ix));
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IRandomFactory#getServiceProviderFor(java.lang.String)
	 */
	public Set<String> getServiceProviderFor(String type)
	{
		Set<String> result = new HashSet<String>();

		Provider[] providers = Security.getProviders();
		for (int i = 0; i < providers.length; i++)
		{
			// Get services provided by each provider
			Set keys = providers[i].keySet();
			for (Iterator it = keys.iterator(); it.hasNext();)
			{
				String key = (String) it.next();
				key = key.split(" ")[0]; //$NON-NLS-1$

				if (key.startsWith(type + ".")) //$NON-NLS-1$
				{
					result.add(key.substring(type.length() + 1));
				} else if (key.startsWith(ALG_PARSE_STRING + type + ".")) //$NON-NLS-1$
				{
					// This is an alias
					result.add(key.substring(type.length() + 11));
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IRandomFactory#getProviders()
	 */
	public Provider[] getProviders()
	{
		return Security.getProviders();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IRandomFactory#getRandom()
	 */
	public Random getRandom()
	{
		return new Random(System.currentTimeMillis());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IRandomFactory#getRandom(long)
	 */
	public Random getRandom(long seed)
	{
		return new Random(seed);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IRandomFactory#getSecureRandom()
	 */
	public Random getSecureRandom() throws NoSuchAlgorithmException,
			NoSuchProviderException
	{
		return initSecureRandom(ALG_SHA1PRNG, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IRandomFactory#getSecureRandom(java.lang.String)
	 */
	public Random getSecureRandom(String algorithm)
			throws NoSuchAlgorithmException, NoSuchProviderException
	{
		return initSecureRandom(algorithm, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IRandomFactory#getSecureRandom(java.lang.String,
	 *      java.lang.String)
	 */
	public Random getSecureRandom(String algorithm, String provider)
			throws NoSuchAlgorithmException, NoSuchProviderException
	{
		return initSecureRandom(algorithm, provider);
	}

}
