<?php
/**
 * PHP Robot class definition.
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
 * PHP Robot class.
 * @package  org.josso.agent.php
 */
class robot {
	
	/**
	 * @var string
	 * @access private
	 */
	var $id;

	/**
	 * @var string
	 * @access private
	 */
	var $name;

	/**
	 * @var string
	 * @access private
	 */
	var $coverUrl;

	/**
	 * @var string
	 * @access private
	 */
	var $detailsUrl;

	/**
	 * @var string
	 * @access private
	 */
	var $ownerName;

	/**
	 * @var string
	 * @access private
	 */
	var $ownerUrl;

	/**
	 * @var string
	 * @access private
	 */
	var $ownerEmail;

	/**
	 * @var string
	 * @access private
	 */
	var $status;

	/**
	 * @var string
	 * @access private
	 */
	var $purpose;

	/**
	 * @var string
	 * @access private
	 */
	var $type;

	/**
	 * @var string
	 * @access private
	 */
	var $platform;

	/**
	 * @var string
	 * @access private
	 */
	var $availability;

	/**
	 * @var string
	 * @access private
	 */
	var $exclusion;

	/**
	 * @var string
	 * @access private
	 */
	var $exclusionUserAgent;

	/**
	 * @var string
	 * @access private
	 */
	var $noindex;

	/**
	 * @var string
	 * @access private
	 */
	var $host;

	/**
	 * @var string
	 * @access private
	 */
	var $from;

	/**
	 * @var string
	 * @access private
	 */
	var $userAgent;

	/**
	 * @var string
	 * @access private
	 */
	var $language;

	/**
	 * @var string
	 * @access private
	 */
	var $description;

	/**
	 * @var string
	 * @access private
	 */
	var $history;

	/**
	 * @var string
	 * @access private
	 */
	var $environment;

	/**
	 * @var string
	 * @access private
	 */
	var $modifiedDate;

	/**
	 * @var string
	 * @access private
	 */
	var $modifiedBy;

	/**
	* Constructor
	* 
	* @access public
	*/
	function robot() {
	}
	
	/**
	 * @return string the id
	 *
	 * @access public
	 */
	function getId() {
		return $this->$this->id;
	}

	/**
	 * @param string $id the id to set
	 *
	 * @access public
	 */
	function setId($id) {
		$this->id = $id;
	}

	/**
	 * @return string the name
	 *
	 * @access public
	 */
	function getName() {
		return $this->name;
	}

	/**
	 * @param string $name the name to set
	 *
	 * @access public
	 */
	function setName($name) {
		$this->name = $name;
	}

	/**
	 * @return string the coverUrl
	 *
	 * @access public
	 */
	function getCoverUrl() {
		return $this->coverUrl;
	}

	/**
	 * @param coverUrl the coverUrl to set
	 * 
	 * @access public
	 */
	function setCoverUrl($coverUrl) {
		$this->coverUrl = $coverUrl;
	}

	/**
	 * @return string the detailsUrl
	 *
	 * @access public
	 */
	function getDetailsUrl() {
		return $this->detailsUrl;
	}

	/**
	 * @param detailsUrl the detailsUrl to set
	 * 
	 * @access public
	 */
	function setDetailsUrl($detailsUrl) {
		$this->detailsUrl = $detailsUrl;
	}

	/**
	 * @return string the ownerName
	 *
	 * @access public
	 */
	function getOwnerName() {
		return $this->ownerName;
	}

	/**
	 * @param ownerName the ownerName to set
	 * 
	 * @access public
	 */
	function setOwnerName($ownerName) {
		$this->ownerName = $ownerName;
	}

	/**
	 * @return string the ownerUrl
	 *
	 * @access public
	 */
	function getOwnerUrl() {
		return $this->ownerUrl;
	}

	/**
	 * @param ownerUrl the ownerUrl to set
	 * 
	 * @access public
	 */
	function setOwnerUrl($ownerUrl) {
		$this->ownerUrl = $ownerUrl;
	}

	/**
	 * @return string the ownerEmail
	 *
	 * @access public
	 */
	function getOwnerEmail() {
		return $this->ownerEmail;
	}

	/**
	 * @param ownerEmail the ownerEmail to set
	 * 
	 * @access public
	 */
	function setOwnerEmail($ownerEmail) {
		$this->ownerEmail = $ownerEmail;
	}

	/**
	 * @return string the status
	 *
	 * @access public
	 */
	function getStatus() {
		return $this->status;
	}

	/**
	 * @param status the status to set
	 * 
	 * @access public
	 */
	function setStatus($status) {
		$this->status = $status;
	}

	/**
	 * @return string the purpose
	 *
	 * @access public
	 */
	function getPurpose() {
		return $this->purpose;
	}

	/**
	 * @param purpose the purpose to set
	 * 
	 * @access public
	 */
	function setPurpose($purpose) {
		$this->purpose = $purpose;
	}

	/**
	 * @return string the type
	 *
	 * @access public
	 */
	function getType() {
		return $this->type;
	}

	/**
	 * @param type the type to set
	 * 
	 * @access public
	 */
	function setType($type) {
		$this->type = $type;
	}

	/**
	 * @return string the platform
	 *
	 * @access public
	 */
	function getPlatform() {
		return $this->platform;
	}

	/**
	 * @param platform the platform to set
	 * 
	 * @access public
	 */
	function setPlatform($platform) {
		$this->platform = $platform;
	}

	/**
	 * @return string the availability
	 *
	 * @access public
	 */
	function getAvailability() {
		return $this->availability;
	}

	/**
	 * @param availability the availability to set
	 * 
	 * @access public
	 */
	function setAvailability($availability) {
		$this->availability = $availability;
	}

	/**
	 * @return string the exclusion
	 *
	 * @access public
	 */
	function getExclusion() {
		return $this->exclusion;
	}

	/**
	 * @param exclusion the exclusion to set
	 * 
	 * @access public
	 */
	function setExclusion($exclusion) {
		$this->exclusion = $exclusion;
	}

	/**
	 * @return string the userAgent
	 *
	 * @access public
	 */
	function getUserAgent() {
		return $this->userAgent;
	}

	/**
	 * @param userAgent the userAgent to set
	 * 
	 * @access public
	 */
	function setUserAgent($userAgent) {
		$this->userAgent = $userAgent;
	}

	/**
	 * @return string the noindex
	 *
	 * @access public
	 */
	function getNoindex() {
		return $this->noindex;
	}

	/**
	 * @param noindex the noindex to set
	 * 
	 * @access public
	 */
	function setNoindex($noindex) {
		$this->noindex = $noindex;
	}

	/**
	 * @return string the host
	 *
	 * @access public
	 */
	function getHost() {
		return $this->host;
	}

	/**
	 * @param host the host to set
	 * 
	 * @access public
	 */
	function setHost($host) {
		$this->host = $host;
	}

	/**
	 * @return string the from
	 *
	 * @access public
	 */
	function getFrom() {
		return $this->from;
	}

	/**
	 * @param from the from to set
	 * 
	 * @access public
	 */
	function setFrom($from) {
		$this->from = $from;
	}

	/**
	 * @return string the exclusionUserAgent
	 *
	 * @access public
	 */
	function getExclusionUserAgent() {
		return $this->exclusionUserAgent;
	}

	/**
	 * @param exclusionUserAgent the exclusionUserAgent to set
	 * 
	 * @access public
	 */
	function setExclusionUserAgent($exclusionUserAgent) {
		$this->exclusionUserAgent = $exclusionUserAgent;
	}

	/**
	 * @return string the language
	 *
	 * @access public
	 */
	function getLanguage() {
		return $this->language;
	}

	/**
	 * @param language the language to set
	 * 
	 * @access public
	 */
	function setLanguage($language) {
		$this->language = $language;
	}

	/**
	 * @return string the description
	 *
	 * @access public
	 */
	function getDescription() {
		return $this->description;
	}

	/**
	 * @param description the description to set
	 * 
	 * @access public
	 */
	function setDescription($description) {
		$this->description = $description;
	}

	/**
	 * @return string the history
	 *
	 * @access public
	 */
	function getHistory() {
		return $this->history;
	}

	/**
	 * @param history the history to set
	 * 
	 * @access public
	 */
	function setHistory($history) {
		$this->history = $history;
	}

	/**
	 * @return string the environment
	 *
	 * @access public
	 */
	function getEnvironment() {
		return $this->environment;
	}

	/**
	 * @param environment the environment to set
	 * 
	 * @access public
	 */
	function setEnvironment($environment) {
		$this->environment = $environment;
	}

	/**
	 * @return string the modifiedDate
	 *
	 * @access public
	 */
	function getModifiedDate() {
		return $this->modifiedDate;
	}

	/**
	 * @param modifiedDate the modifiedDate to set
	 * 
	 * @access public
	 */
	function setModifiedDate($modifiedDate) {
		$this->modifiedDate = $modifiedDate;
	}

	/**
	 * @return string the modifiedBy
	 *
	 * @access public
	 */
	function getModifiedBy() {
		return $this->modifiedBy;
	}

	/**
	 * @param modifiedBy the modifiedBy to set
	 * 
	 * @access public
	 */
	function setModifiedBy($modifiedBy) {
		$this->modifiedBy = $modifiedBy;
	}
}
?>
