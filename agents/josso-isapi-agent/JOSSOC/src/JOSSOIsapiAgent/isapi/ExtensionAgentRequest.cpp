#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <crtdbg.h>

#include "JOSSOIsapiAgent/isapi/ExtensionAgentRequest.hpp"

#ifdef _DEBUG
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif




ExtensionAgentRequest::ExtensionAgentRequest(LPEXTENSION_CONTROL_BLOCK lpEcb, jk_logger_t * l) {
	this->logger = l;
	this->lpEcb = lpEcb;
	this->secCtx.setLogger(l);
}

ExtensionAgentRequest::~ExtensionAgentRequest() {

}

bool ExtensionAgentRequest::exportSecurityContext(JOSSOSecurityContext & ctx) {
	bool ok = true;

	// TODO !!! Is this reuired for our own app?
	jk_log(logger, JK_LOG_DEBUG, "TODO : IMPLEMENT ME!");

	return ok;
}

string ExtensionAgentRequest::getMethod() {
	return lpEcb->lpszMethod;
}

string ExtensionAgentRequest::getQueryString() {
	if (this->qryString.empty()) {
		this->qryString = lpEcb->lpszQueryString;
	}

	return this->qryString;
}

string ExtensionAgentRequest::getContentType() {
	 return lpEcb->lpszContentType;
}

DWORD ExtensionAgentRequest::getBodySize() {
	if (lpEcb->cbAvailable < lpEcb->cbTotalBytes) {
		jk_log(logger, JK_LOG_ERROR, "Body too long : [%d] bytes, not supported by JOSSO", lpEcb->cbTotalBytes);
		return 0;
	}
	return lpEcb->cbAvailable;

}

LPBYTE ExtensionAgentRequest::getBody() {
	// TODO : read body from lpEcb ...

	// This means that ther's no body 
	if (lpEcb->cbAvailable < 1) {
		jk_log(logger, JK_LOG_TRACE, "No body received for request");
		return NULL;
	}

	// IIS automatically reads up to 48Kb, that should be more than enough for JOSSO Agent ...
	// if cbAvailable is less than total, it means that we get more than 48 Kb on the POST, ignore it!
	if (lpEcb->cbAvailable < lpEcb->cbTotalBytes) {
		// To support bodies larger that 48 Kb, we need to use lpEcb->ReadClient() function ...
		jk_log(logger, JK_LOG_ERROR, "Body too long : [%d] bytes, not supported by JOSSO", lpEcb->cbTotalBytes );
		return NULL;
	} else {
		// IIS automatically reads up to 48Kb, that should be more than enough for JOSSO Agent ...
		// Build a string with this ?!

		// Decode data based on content type ?!
		jk_log(logger, JK_LOG_TRACE, "Received content-type: %s", lpEcb->lpszContentType);
		return lpEcb->lpbData;
	}
}

string ExtensionAgentRequest::getServerVariable(string name, DWORD cbSize) {
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

	if (!lpEcb->GetServerVariable(lpEcb->ConnID, varName, varValue, &cbSize)) {

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