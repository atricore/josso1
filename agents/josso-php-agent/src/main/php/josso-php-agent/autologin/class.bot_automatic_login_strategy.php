<?php
/**
 * PHP Bot automatic login class definition.
 */

/**
JOSSO: Java Open Single Sign-On

Copyright 2004-2009, Atricore, Inc.

This is free software; you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation; either version 2.1 of
the License, or (at your option) any later version.

This software is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this software; if not, write to the Free
Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

require_once('class.abstract_automatic_login_strategy.php');
require_once('class.robot.php');

/**
 * PHP Bot automatic login strategy implementation.
 * This will not require an automatic login when a bot is crawling the site.
 * @package  org.josso.agent.php
 */
class bot_automatic_login_strategy extends abstract_automatic_login_strategy {

	/**
	 * Robots file path.
	 *
	 * @var string
	 * @access private
	 */
	var $botsFile;

	/**
	 * Robots.
	 *
	 * @var array
	 * @access private
	 */
	var $bots;

	/**
	* Constructor
	* 
	* @access public
	*/
	function __construct() {
	}

	/**
	* Componenets must evaluate if automatic login is required for the received request.
	*
	* @return true if automatic login is required, false otherwise
	*/
	function isAutomaticLoginRequired() {
	    if (!isset($this->bots)) {
    		$this->loadRobots();
	    }
	    $userAgent = $_SERVER['HTTP_USER_AGENT'];
	    if (isset($this->bots[$userAgent])) {
			return FALSE;
	    }
	    return TRUE;
	}

	/**
	 * Load robots from the file.
	 */
	function loadRobots() {
	    $this->bots = array();
	    $robot = new robot();
        if (file_exists($this->botsFile) && is_readable($this->botsFile)) {
			$file_handle = fopen($this->botsFile, 'r');
			while (!feof($file_handle)) {
			    $line = fgets($file_handle);
			    if (isset($line) && trim($line) != '') {
					if ($this->startsWith($line, 'robot-') || $this->startsWith($line, 'modified-')) {
					    $separatorIndex = strpos($line, ':');
					    $name = substr($line, 0, $separatorIndex);
					    $value = trim(substr($line, $separatorIndex + 1));
					    $this->setRobotProperty($robot, $name, $value, FALSE);
					} else {
					    $this->setRobotProperty($robot, $name, $line, TRUE);
					}
				} else {
					$this->bots[$robot->getUserAgent()] = $robot;
				  	$robot = new robot();
				  	$name = null;
				  	$value = null;
			    }
			}
			fclose($file_handle);
	    }
	}

	/**
	 * Sets robot property value.
	 * 
	 * @param robot $robot robot
	 * @param string $name property name
	 * @param string $value property value
	 * @param boolean $append true if value should be appended to existing value, false otherwise
	 */
	function setRobotProperty($robot, $name, $value, $append) {
	    if (!isset($robot) || !isset($name) || !isset($value)) {
			return;
	    }
	    
	    $value = trim($value);
	    
	    if ($this->startsWith($name, 'robot-id')) {
			$robot->setId($value);
	    } else if ($this->startsWith($name, 'robot-name')) {
			$robot->setName($value);
	    } else if ($this->startsWith($name, 'robot-cover-url')) {
			$robot->setCoverUrl($value);
	    } else if ($this->startsWith($name, 'robot-details-url')) {
			$robot->setDetailsUrl($value);
	    } else if ($this->startsWith($name, 'robot-owner-name')) {
			$robot->setOwnerName($value);
	    } else if ($this->startsWith($name, 'robot-owner-url')) {
			$robot->setOwnerUrl($value);
	    } else if ($this->startsWith($name, 'robot-owner-email')) {
			$robot->setOwnerEmail($value);
	    } else if ($this->startsWith($name, 'robot-status')) {
			$robot->setStatus($value);
	    } else if ($this->startsWith($name, 'robot-purpose')) {
			$robot->setPurpose($value);
	    } else if ($this->startsWith($name, 'robot-type')) {
			$robot->setType($value);
	    } else if ($this->startsWith($name, 'robot-platform')) {
			$robot->setPlatform($value);
	    } else if ($this->startsWith($name, 'robot-availability')) {
			$robot->setAvailability($value);
	    } else if ($this->startsWith($name, 'robot-exclusion-useragent')) {
			$robot->setExclusionUserAgent($value);
	    } else if ($this->startsWith($name, 'robot-exclusion')) {
			$robot->setExclusion($value);
	    } else if ($this->startsWith($name, 'robot-noindex')) {
			$robot->setNoindex($value);
	    } else if ($this->startsWith($name, 'robot-host')) {
			$robot->setHost($value);
	    } else if ($this->startsWith($name, 'robot-from')) {
			$robot->setFrom($value);
	    } else if ($this->startsWith($name, 'robot-useragent')) {
			$robot->setUserAgent($value);
	    } else if ($this->startsWith($name, 'robot-language')) {
			$robot->setLanguage($value);
	    } else if ($this->startsWith($name, 'robot-description')) {
			$description = $robot->getDescription();
			if (append && isset($description)) {
			    $robot->setDescription($description . ' ' . $value);
			} else {
			    $robot->setDescription($value);
			}
	    } else if ($this->startsWith($name, 'robot-history')) {
			$history = $robot->getHistory();
			if (append && isset($history)) {
			    $robot->setHistory($history . ' ' . $value);
			} else {
			    $robot->setHistory($value);
			}
	    } else if ($this->startsWith($name, 'robot-environment')) {
			$robot->setEnvironment($value);
	    } else if ($this->startsWith($name, 'modified-date')) {
			$robot->setModifiedDate($value);
	    } else if ($this->startsWith($name, 'modified-by')) {
			$robot->setModifiedBy($value);
	    }
	}

	function startsWith($haystack,$needle,$case=true) {
	    if ($case) {
			return (strcmp(substr($haystack, 0, strlen($needle)),$needle)===0);
	    }
	    return (strcasecmp(substr($haystack, 0, strlen($needle)),$needle)===0);
	}

	/**
	 * @return string bots file
	 *
	 * @access public
	 */
	function getBotsFile() {
	    return $this->botsFile;
	}

	/**
	 * @param string $botsFile the bots file to set
	 *
	 * @access public
	 */
	function setBotsFile($botsFile) {
	    $this->botsFile = $botsFile;
	}
}
?>
