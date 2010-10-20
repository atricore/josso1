<?php
/**
 * PHP Abstract automatic login strategy class definition.
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

/**
 * PHP Abstract automatic login strategy class.
 * @package  org.josso.agent.php
 */
abstract class abstract_automatic_login_strategy {

	/**
	 * This tells the agent how to handle success or failure for this component.
	 *
	 * @var string
	 * @access private
	 */
	var $mode;

	/**
	* Constructor
	* 
	* @access public
	*/
	function abstract_automatic_login_strategy() {
	}

	/**
	 * @return string the mode
	 *
	 * @access public
	 */
	function getMode() {
	    return $this->mode;
	}

	/**
	 * @param string $mode the mode to set
	 *
	 * @access public
	 */
	function setMode($mode) {
	    $this->mode = $mode;
	}

	/**
	* Componenets must evaluate if automatic login is required for the received request.
	*
	* @return true if automatic login is required, false otherwise
	*/
	function isAutomaticLoginRequired() {
	    return FALSE;
	}
}
?>
