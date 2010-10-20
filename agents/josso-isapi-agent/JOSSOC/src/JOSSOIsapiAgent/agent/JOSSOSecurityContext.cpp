#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <crtdbg.h>

#include <JOSSOIsapiAgent/agent/JOSSOSecurityContext.hpp>

#ifdef _DEBUG
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif


JOSSOSecurityContext::JOSSOSecurityContext() {
	this->authn= false;
}

void JOSSOSecurityContext::setAuthN(string originalResource, string ssoSessionId, string principal, map<string, string> p, vector<string> r) {
	this->authn = true;
	this->principal = principal;
	this->originalResource = originalResource;
	this->ssoSessionId = ssoSessionId;
	this->roles.assign(r.begin(), r.end());
	this->usrProperties = p; // TODO ?
}

bool JOSSOSecurityContext::isAuthenticated() {
	return authn;
}

bool JOSSOSecurityContext::hasRole(string name) {

	jk_log(logger, JK_LOG_TRACE, "Received %s", name.c_str());

	vector<string>::const_iterator itRoles;
	for (itRoles = this->roles.begin() ; itRoles < this->roles.end() ; itRoles ++) {

		if ( (*itRoles).compare(name) == 0) {
			jk_log(logger, JK_LOG_TRACE, "User Role %s MATCHES", (*itRoles).c_str());
			return true;
		}

		jk_log(logger, JK_LOG_TRACE, "User Role %s DOES NOT MATCH", (*itRoles).c_str());


	}
	return false;
}

string JOSSOSecurityContext::getOriginalResource() {
	return this->originalResource;
}


string JOSSOSecurityContext::getPrincipal() {
	return this->principal;
}
