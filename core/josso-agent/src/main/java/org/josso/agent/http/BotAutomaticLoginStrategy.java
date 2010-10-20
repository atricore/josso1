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

package org.josso.agent.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This will not require an automatic login when a bot is crawling the site.
 *
 * @org.apache.xbean.XBean element="bot-automaticlogin-strategy"
 * 
 * @author <a href="mailto:sgonzaelz@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id$
 */
public class BotAutomaticLoginStrategy extends AbstractAutomaticLoginStrategy {

	private static final Log log = LogFactory.getLog(BotAutomaticLoginStrategy.class);

	private String file = "/bots.properties";
	
	private Map<String, Robot> bots;
	
    public BotAutomaticLoginStrategy() {
        super();
    }

    public BotAutomaticLoginStrategy(String mode) {
        super(mode);
    }
    
    /**
     * Components must evaluate if automatic login is required for the received request.
     *
     * @return
     */
    @Override
    public boolean isAutomaticLoginRequired(HttpServletRequest hreq, HttpServletResponse hres) {
    	if (bots == null) {
    		loadRobots();
    	}
    	String userAgent = hreq.getHeader("User-Agent");
    	if (bots.get(userAgent) != null) {
    		log.debug("Autologin not required for bot: " + userAgent);
    		return false;
    	}
    	return true;
    }
    
    /**
     * Loads bots from the file.
     */
    private void loadRobots() {
    	bots = new HashMap<String, Robot>();
        InputStream is = null;
        try {
            is = this.getClass().getResourceAsStream(file);
            if (is == null)
                throw new IOException("Cannot find resource: " + file + ". Make sure this file is installed with JOSSO Agent!");

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            
            String line = null;
            Robot robot = new Robot();
            String name = null;
            String value = null;
            while ((line = br.readLine()) != null) {
            	if (!line.trim().equals("")) {
            		if (line.startsWith("robot-") || line.startsWith("modified-")) {
            			int separatorIndex = line.indexOf(":");
            			name = line.substring(0, separatorIndex);
	            		value = line.substring(separatorIndex + 1).trim();
	            		setRobotProperty(robot, name, value, false);
            		} else {
            			setRobotProperty(robot, name, line, true);
            		}
            	} else {
            		bots.put(robot.getUserAgent(), robot);
            		robot = new Robot();
            		name = null;
            		value = null;
            	}
            }
            
            log.info("Loaded bots file: " + file);
        } catch (IOException e) {
            log.error("Cannot load bot properties from " + file + " : " + e.getMessage(), e);
            bots = null;
        } finally  {
            if (is != null) try { is.close(); } catch (IOException e) { /**/}
        }
    }
    
    /**
     * Sets robot property value.
     * 
     * @param robot robot
     * @param name property name
     * @param value property value
     * @param append true if value should be appended to existing value, false otherwise
     */
    private void setRobotProperty(Robot robot, String name, String value, boolean append) {
    	if (robot == null || name == null || value == null) {
    		return;
    	}
    	
    	value = value.trim();
    	
    	if (name.startsWith("robot-id")) {
    		robot.setId(value);
		} else if (name.startsWith("robot-name")) {
			robot.setName(value);
		} else if (name.startsWith("robot-cover-url")) {
			robot.setCoverUrl(value);
		} else if (name.startsWith("robot-details-url")) {
			robot.setDetailsUrl(value);
		} else if (name.startsWith("robot-owner-name")) {
			robot.setOwnerName(value);
		} else if (name.startsWith("robot-owner-url")) {
			robot.setOwnerUrl(value);
		} else if (name.startsWith("robot-owner-email")) {
			robot.setOwnerEmail(value);
		} else if (name.startsWith("robot-status")) {
			robot.setStatus(value);
		} else if (name.startsWith("robot-purpose")) {
			robot.setPurpose(value);
		} else if (name.startsWith("robot-type")) {
			robot.setType(value);
		} else if (name.startsWith("robot-platform")) {
			robot.setPlatform(value);
		} else if (name.startsWith("robot-availability")) {
			robot.setAvailability(value);
		} else if (name.startsWith("robot-exclusion-useragent")) {
			robot.setExclusionUserAgent(value);
		} else if (name.startsWith("robot-exclusion")) {
			robot.setExclusion(value);
		} else if (name.startsWith("robot-noindex")) {
			robot.setNoindex(value);
		} else if (name.startsWith("robot-host")) {
			robot.setHost(value);
		} else if (name.startsWith("robot-from")) {
			robot.setFrom(value);
		} else if (name.startsWith("robot-useragent")) {
			robot.setUserAgent(value);
		} else if (name.startsWith("robot-language")) {
			robot.setLanguage(value);
		} else if (name.startsWith("robot-description")) {
			if (append && robot.getDescription() != null) {
				robot.setDescription(robot.getDescription() + " " + value);
			} else {
				robot.setDescription(value);
			}
		} else if (name.startsWith("robot-history")) {
			if (append && robot.getHistory() != null) {
				robot.setHistory(robot.getHistory() + " " + value);
			} else {
				robot.setHistory(value);
			}
		} else if (name.startsWith("robot-environment")) {
			robot.setEnvironment(value);
		} else if (name.startsWith("modified-date")) {
			robot.setModifiedDate(value);
		} else if (name.startsWith("modified-by")) {
			robot.setModifiedBy(value);
		}
    }
    
	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(String file) {
		this.file = file;
	}
}
