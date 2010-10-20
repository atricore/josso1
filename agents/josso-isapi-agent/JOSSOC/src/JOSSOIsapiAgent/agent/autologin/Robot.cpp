#include <JOSSOIsapiAgent/agent/autologin/Robot.hpp>
#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <crtdbg.h>

#ifdef _DEBUG
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif

const char *Robot::getId() {
	return id.c_str();
}

void Robot::setId(const string &id) {
	this->id.assign(id);
}

const char *Robot::getName() {
	return name.c_str();
}

void Robot::setName(const string &name) {
	this->name.assign(name);
}

const char *Robot::getCoverUrl() {
	return coverUrl.c_str();
}

void Robot::setCoverUrl(const string &coverUrl) {
	this->coverUrl.assign(coverUrl);
}

const char *Robot::getDetailsUrl() {
	return detailsUrl.c_str();
}

void Robot::setDetailsUrl(const string &detailsUrl) {
	this->detailsUrl.assign(detailsUrl);
}

const char *Robot::getOwnerName() {
	return ownerName.c_str();
}

void Robot::setOwnerName(const string &ownerName) {
	this->ownerName.assign(ownerName);
}

const char *Robot::getOwnerUrl() {
	return ownerUrl.c_str();
}

void Robot::setOwnerUrl(const string &ownerUrl) {
	this->ownerUrl.assign(ownerUrl);
}

const char *Robot::getOwnerEmail() {
	return ownerEmail.c_str();
}

void Robot::setOwnerEmail(const string &ownerEmail) {
	this->ownerEmail.assign(ownerEmail);
}

const char *Robot::getStatus() {
	return status.c_str();
}

void Robot::setStatus(const string &status) {
	this->status.assign(status);
}

const char *Robot::getPurpose() {
	return purpose.c_str();
}

void Robot::setPurpose(const string &purpose) {
	this->purpose.assign(purpose);
}

const char *Robot::getType() {
	return type.c_str();
}

void Robot::setType(const string &type) {
	this->type.assign(type);
}

const char *Robot::getPlatform() {
	return platform.c_str();
}

void Robot::setPlatform(const string &platform) {
	this->platform.assign(platform);
}

const char *Robot::getAvailability() {
	return availability.c_str();
}

void Robot::setAvailability(const string &availability) {
	this->availability.assign(availability);
}

const char *Robot::getExclusion() {
	return exclusion.c_str();
}

void Robot::setExclusion(const string &exclusion) {
	this->exclusion.assign(exclusion);
}

const char *Robot::getExclusionUserAgent() {
	return exclusionUserAgent.c_str();
}

void Robot::setExclusionUserAgent(const string &exclusionUserAgent) {
	this->exclusionUserAgent.assign(exclusionUserAgent);
}

const char *Robot::getNoindex() {
	return noindex.c_str();
}

void Robot::setNoindex(const string &noindex) {
	this->noindex.assign(noindex);
}

const char *Robot::getHost() {
	return host.c_str();
}

void Robot::setHost(const string &host) {
	this->host.assign(host);
}

const char *Robot::getFrom() {
	return from.c_str();
}

void Robot::setFrom(const string &from) {
	this->from.assign(from);
}

const char *Robot::getUserAgent() {
	return userAgent.c_str();
}

void Robot::setUserAgent(const string &userAgent) {
	this->userAgent.assign(userAgent);
}

const char *Robot::getLanguage() {
	return language.c_str();
}

void Robot::setLanguage(const string &language) {
	this->language.assign(language);
}

const char *Robot::getDescription() {
	return description.c_str();
}

void Robot::setDescription(const string &description) {
	this->description.assign(description);
}

const char *Robot::getHistory() {
	return history.c_str();
}

void Robot::setHistory(const string &history) {
	this->history.assign(history);
}

const char *Robot::getEnvironment() {
	return environment.c_str();
}

void Robot::setEnvironment(const string &environment) {
	this->environment.assign(environment);
}

const char *Robot::getModifiedDate() {
	return modifiedDate.c_str();
}

void Robot::setModifiedDate(const string &modifiedDate) {
	this->modifiedDate.assign(modifiedDate);
}

const char *Robot::getModifiedBy() {
	return modifiedBy.c_str();
}

void Robot::setModifiedBy(const string &modifiedBy) {
	this->modifiedBy.assign(modifiedBy);
}
