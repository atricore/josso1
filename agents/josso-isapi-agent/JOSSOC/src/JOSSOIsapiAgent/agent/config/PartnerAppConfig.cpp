#include "JOSSOIsapiAgent/agent/config/PartnerAppConfig.hpp"

const char * PartnerAppConfig::getId() {
	return id.c_str();
}

const char * PartnerAppConfig::getBaseUri() {
	return baseUri.c_str();
}

const char * PartnerAppConfig::getSplashResource() {
	return splashResource.c_str();
}

const char * PartnerAppConfig::getPartnerAppId() {
	return partnerAppId.c_str();
}

void PartnerAppConfig::setSplashResource(const string &splashResource) {
	this->splashResource.assign(splashResource);
}

void PartnerAppConfig::setPartnerAppId(const string &partnerAppId) {
	this->partnerAppId.assign(partnerAppId);
}
