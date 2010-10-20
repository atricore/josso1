<?php
/**
 * PHP Josso User class definition.
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
02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

/**
 * PHP Josso User implementation.
 * @package  org.josso.agent.php
 *
 * @author Sebastian Gonzalez Oyuela <sgonzalez@josso.org>
 * @version $Id: class.jossouser.php 543 2008-03-18 21:34:58Z sgonzalez $
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 */
class jossouser {

	// Current user name, string
	var $name;
	
	// User properties, string[]
	var $properties;
	
	/**
	* Constructor
	* 
	* @param string n the user name
	* @param array p user custom properties
	* @param strgin s sso session id
	*
	* @access public
	*/
	function jossouser($n, $p) {
		$this->name = $n;
		$this->properties = $p;
	}
	
	/**
	 * Gets the user name.
	 *
	 * @return string the username.
	 *
	 * @access public
	 */
	function getName() {
		return $this->name;
	}
	
	/**
	 * Gets user custom properties. Each element is a two elements array represneting a property with name and value.
	 * Sample properties :
	 * <pre>
     * Array
     *   (
     *       [0] => Array
     *           (
     *               [name] => description
     *               [value] => User1 CN
     *           )
     *
     *       [1] => Array
     *           (
     *               [name] => mail
     *               [value] => user1@joss.org
     *           )
     *
     *   )
     * </pre>
     *
	 * @return array
	 */
	function getProperties() {
		return $this->properties;
	}
	
	/**
	 * Gets a property value based on its name, if any.
	 *
	 * @return string
	 *
	 * @access public
	 */
	function getProperty($name) {
        if (is_array($this->properties)) {
            foreach ($this->properties as $property) {
                if ($property['!name'] == $name) {
                    return $property['!value'];
                }
            }
        }
        return ;
	}
}
?>