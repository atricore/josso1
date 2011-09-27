#include <JOSSOIsapiAgent/agent/autologin/AbstractAutomaticLoginStrategy.hpp>
#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <crtdbg.h>

#include <JOSSOIsapiAgent/agent/AbstractSSOAgent.hpp>

#ifdef _DEBUG
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif

const char * AbstractAutomaticLoginStrategy::getMode() {
	return mode.c_str();
}

void AbstractAutomaticLoginStrategy::setMode(const string &mode) {
	this->mode.assign(mode);
}

AbstractSSOAgent * AbstractAutomaticLoginStrategy::getSSOAgent() {
	return ssoAgent;
}

void AbstractAutomaticLoginStrategy::setSSOAgent(AbstractSSOAgent *ssoAgent) {
	this->ssoAgent = ssoAgent;
}
