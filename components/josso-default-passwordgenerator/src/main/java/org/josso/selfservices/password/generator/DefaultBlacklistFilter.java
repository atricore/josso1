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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is used for filtering passwords from a blacklist. If the proposed
 * password is contained within the blacklist, <em>null</em> is returned to
 * indicate the password is not suitable. Otherwise the password itself is
 * returned.
 *
 * @org.apache.xbean.XBean element="blacklist-passwordfilter"
 * 
 * @author unrz205
 */
public class DefaultBlacklistFilter implements IPasswordFilter
{
	// Keeps a reference to a logger
	private static final Log logger = LogFactory.getLog(DefaultBlacklistFilter.class);

	// A list that stores the forbidden words
	private List<String> blacklist = new ArrayList<String>();

	/**
	 * Default constructor.
	 */
	public DefaultBlacklistFilter()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IPassowrdFilter#filter(int,
	 *      java.lang.String)
	 */
	public String filter(int passwordFlags, String password)
	{
		// Iterate over the list and check whether it contains the word
        for (String blackword : blacklist) {
            logger.debug(Messages.getString("DefaultBlacklistFilter.CHECK_PASSWD") + password + Messages.getString("DefaultBlacklistFilter.BLACKLIST_ENTRY") + blackword + "\"");
            if (password.equals(blackword))
                return null;
        }

		return password;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IPassowrdFilter#filter(int,
	 *      java.util.List)
	 */
	public List<String> filter(int passwordFlags, List<String> password)
	{
		List<String> suiatble = new ArrayList<String>();
        for (String element : password) {
            if (filter(passwordFlags, password) != null)
                suiatble.add(element);
        }
		return suiatble;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IPassowrdFilter#getDescription()
	 */
	public String getDescription()
	{
		return Messages.getString("DefaultBlacklistFilter.DESCR"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IPassowrdFilter#getId()
	 */
	public String getId()
	{
		return this.getClass().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IPassowrdFilter#setDescription(java.lang.String)
	 */
	public void setDescription(String description)
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IPassowrdFilter#setId(java.lang.String)
	 */
	public void setId(String id)
	{
		logger.debug(Messages.getString("DefaultBlacklistFilter.ID_CHANGE")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IPassowrdFilter#getBlacklist()
	 */
	public List<String> getBlacklist()
	{
		return blacklist;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IPassowrdFilter#setBlacklist(java.util.List)
	 * @org.apache.xbean.Property alias="blacklist" nestedType="java.lang.String"
	 */
	public void setBlacklist(List<String> blacklist) {

        if (logger.isDebugEnabled()) {
            for (String blacklisted : blacklist) {
                logger.debug("Blacklisted password ["+blacklisted+"] ");
            }
        }
        
		this.blacklist = blacklist;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IPassowrdFilter#addToBlacklist(java.lang.String)
	 */
	public boolean addToBlacklist(String blackWord)
	{
		return blacklist.add(blackWord);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.josso.selfservices.password.generator.IPassowrdFilter#removeFromBlacklist(java.lang.String)
	 */
	public boolean removeFromBlacklist(String blackWord)
	{
		return blacklist.remove(blackWord);
	}

}
