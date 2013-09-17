#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <crtdbg.h>

#include <JOSSOIsapiAgent/isapi/FilterAgentRequest.hpp>

#include <httpfilt.h>

#ifdef _DEBUG
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif


FilterAgentRequest::FilterAgentRequest(HTTP_FILTER_CONTEXT * pfc, DWORD NotificationType, VOID* pvNotification, jk_logger_t * l) {
	this->logger = l;

	this->pfc = pfc;
	this->NotificationType = NotificationType;
	this->pvNotification = pvNotification;

	this->secCtx.setLogger(l);

	// Get current URI

	// Initialize maps ...
}

FilterAgentRequest::~FilterAgentRequest() {
}


bool FilterAgentRequest::exportSecurityContext(JOSSOSecurityContext & ctx) {

	bool ok = true;

	if (!ctx.isAuthenticated()) {
		return ok;
	}

	// Export protected resource URL
	ok = ok && this->setHeader("josso-original-resource-url", ctx.getOriginalResource());

	// Export user
	ok = ok && this->setHeader("josso-user", ctx.getPrincipal());

	map<string, string>::iterator ip;

	map<string, string> p = ctx.getUserProperties();
	for (ip = p.begin() ; ip != p.end() ; ip ++) {
		string name = (*ip).first;
		string value = (*ip).second;

		name = normalizeServerVariableName(name);

		ok = ok && setHeader("josso-user-property-" + name, value);
	}

	// Export roles
	vector<string> roles = ctx.getRoles();
	vector<string>::const_iterator ir;
	for (ir = roles.begin() ; ir < roles.end() ; ir ++) {
		string role = "josso-role-" + (*ir);

		role = normalizeServerVariableName(role);

		ok = ok && this->setHeader(role, (*ir));
	}

	return ok;


}

string FilterAgentRequest::getMethod() {
	return getHeader("METHOD");
}


string FilterAgentRequest::getQueryString() {
	// TODO : Implement me!
	jk_log(logger, JK_LOG_DEBUG, "IMPLEMENT ME!");
	return "";
}

string FilterAgentRequest::getContentType() {
	// TODO : Implement me!
	jk_log(logger, JK_LOG_DEBUG, "IMPLEMENT ME!");
	return "";
}



DWORD FilterAgentRequest::getBodySize() {
	// TODO : Implement me!
	jk_log(logger, JK_LOG_DEBUG, "IMPLEMENT ME!");
	return 0;
}


LPBYTE FilterAgentRequest::getBody() {
	// TODO : Implement me!
	jk_log(logger, JK_LOG_DEBUG, "IMPLEMENT ME!");
	return NULL;
}

bool FilterAgentRequest::setHeader(string name, string value) {

	if (this->NotificationType != SF_NOTIFY_PREPROC_HEADERS) {
		jk_log(logger, JK_LOG_ERROR, "Unsupported Notification Type %d", this->NotificationType);
		return false;
	}

	HTTP_FILTER_PREPROC_HEADERS * pPPH = (HTTP_FILTER_PREPROC_HEADERS *) this->pvNotification;

    string hdr = name + ":";
	string hdrv = value + "";
    int rc = pPPH->SetHeader(this->pfc, const_cast<char*>(hdr.c_str()), const_cast<char*>(hdrv.c_str()));
	if (!rc) {
		DWORD dwError = GetLastError();

		if (rc == ERROR_INVALID_PARAMETER ) {
			jk_log(logger, JK_LOG_ERROR, "SetHeader %s=[%s] failed = %d (%x), Invalid Parameter", 
				name.c_str(), value.c_str(), dwError, dwError);
		} else if (rc == ERROR_NOT_SUPPORTED) {
			jk_log(logger, JK_LOG_ERROR, "SetHeader %s=[%s] failed = %d (%x), Not supported", 
				name.c_str(), value.c_str(), dwError, dwError);
		} else {
			jk_log(logger, JK_LOG_ERROR, "SetHeader %s=[%s] failed = %d (%x), Unknown", 
				name.c_str(), value.c_str(), dwError, dwError);

		}
		return false;

	}

	return true;

}

/**
 * This will return a server variable value no longer that cbSize.
 */
string FilterAgentRequest::getServerVariable(string name, DWORD cbSize) {

	string value;

	if (name.empty() || cbSize < 1) {
		jk_log(logger, JK_LOG_ERROR, "Server variable name or value size are invalid %s %d", name.c_str(), cbSize);
		return false;
	}

	jk_log(logger, JK_LOG_TRACE, "getServerVariable(%s (size=%d), %d, ...)", name.c_str(), name.size(), cbSize);

	DWORD dwError;

	char *varValue = new char[cbSize];
	char *varName = new char[name.size() + 1];

	varValue[0] = '\0';
	varName[0] = '\0';

	StringCbCopy(varName, name.size() + 1, name.c_str());

	if (!pfc->GetServerVariable(pfc, varName, varValue, &cbSize)) {

		dwError = GetLastError();

		if (dwError == ERROR_INVALID_INDEX) { // Server variable does not exists
			jk_log(logger, JK_LOG_DEBUG, " GetServerVariable[%s] failed = %d (%x) (likely reason: server var not set yet), buffer size= %d\n", 
				varName, dwError, dwError, cbSize);
		} else if ( dwError == ERROR_INSUFFICIENT_BUFFER) {  // Should quit if too much header
			jk_log(logger, JK_LOG_ERROR, " GetServerVariable[%s] failed = %d (%x) (likely reason: variable data is too large for buffer), buffer size= %d\n", 
				varName, dwError, dwError, cbSize);
		} else {
			jk_log(logger, JK_LOG_ERROR, " GetServerVariable[%s] failed = %d (%x), buffer size= %d\n", 
				varName, dwError, dwError, cbSize);
		}
	
	}  else {

		jk_log(logger, JK_LOG_TRACE, "getServerVariable(%s (size=%d), %d, ...) : %s", name.c_str(), name.size(), cbSize, varValue);

		value.assign(varValue);
	}

	delete varName;
	delete varValue;

	return value;

}

string FilterAgentRequest::normalizeServerVariableName(string svarName) {

	int start = svarName.find("_");

	if (start != string::npos) {
		svarName.replace(start, 1, "-");
	}

	return svarName;
}