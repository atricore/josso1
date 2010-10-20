<?php
/**
 * PHP Josso Role class definition.
 */
 
/**
JOSSO: Java Open Single Sign-On

Copyright 2004-2008, Atricore, Inc.

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
02110-1301 USA, or see the FSF site: http://www.fsf.org.*/

/**
 * PHP Josso Role implementation
 * @package  org.josso.agent.php
 *
 * @author Sebastian Gonzalez Oyuela <sgonzalez@josso.org>
 * @version $Id: class.jossorole.php 543 2008-03-18 21:34:58Z sgonzalez $
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 */
class jossorole {
	// Current user name, String
	var $name;
	
	/**
	 * Constructor
	 *
	 * @param string role name
	 *
	 * @access public
	 */
	function jossorole($n) {
		$this->name = $n;
	}
	
	/**
	 * Get the role name
	 *
	 * @return string the role name.
	 * @access public
	 */
	function getName() {
		return $this->name;
	}
	
}
?>