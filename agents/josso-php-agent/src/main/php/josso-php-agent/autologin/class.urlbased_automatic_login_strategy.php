<?php
/**
 * PHP Url-based automatic login class definition.
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

/**
 * PHP Url-based automatic login strategy implementation.
 * This strategy returns false if the accessed URL matches any of the configured URL patterns.
 * The patterns are regular expressions.
 * @package  org.josso.agent.php
 */
class urlbased_automatic_login_strategy extends abstract_automatic_login_strategy {

	/**
	 * Url patterns that should be ignored for automatic login.
	 *
	 * @var array
	 * @access private
	 */
	var $urlPatterns;

	/**
	* Constructor
	* 
	* @access public
	*/
	function urlbased_automatic_login_strategy() {
	}

	/**
	* Componenets must evaluate if automatic login is required for the received request.
	*
	* @return true if automatic login is required, false otherwise
	*/
	function isAutomaticLoginRequired() {
	    $autoLoginRequired = TRUE;
	    if (isset($this->urlPatterns)) {
			$requestURI = $_SERVER['REQUEST_URI'];
			foreach ($this->urlPatterns as $urlPattern) {
			    if (preg_match($urlPattern, $requestURI)) {
					$autoLoginRequired = FALSE;
					break;
			    }
			}
	    }
	    return $autoLoginRequired;
	}

	/**
	 * @return array url patterns
	 *
	 * @access public
	 */
	function getUrlPatterns() {
	    return $this->urlPatterns;
	}

	/**
	 * @param array $urlPatterns the url patterns to set
	 *
	 * @access public
	 */
	function setUrlPatterns($urlPatterns) {
	    $this->urlPatterns = $urlPatterns;
	}
}
?>
