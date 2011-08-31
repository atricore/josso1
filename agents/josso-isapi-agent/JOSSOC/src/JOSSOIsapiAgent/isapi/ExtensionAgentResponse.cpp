#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <crtdbg.h>

#include "JOSSOIsapiAgent/isapi/ExtensionAgentResponse.hpp"

#include "JOSSOIsapiAgent/isapi/HTTPUtil.h"

#ifdef _DEBUG
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif

ExtensionAgentResponse::ExtensionAgentResponse(LPEXTENSION_CONTROL_BLOCK lpEcb, jk_logger_t * l) {
	this->logger = l;
	this->lpEcb = lpEcb;
	committed = false;	
}

ExtensionAgentResponse::~ExtensionAgentResponse() {
	
}

bool ExtensionAgentResponse::flushHeaders() {
	jk_log(this->logger, JK_LOG_ERROR, "Not implemented!");
	return false;
}

bool ExtensionAgentResponse::sendContent(string content) {
	return writeContent(content.c_str(), content.size());
}

bool ExtensionAgentResponse::writeContent(const char * content, size_t length) {
	jk_log(logger, JK_LOG_ERROR, "FilterAgentResponse::writeContent DOS NOT WORK ....");

	jk_log(logger, JK_LOG_TRACE, "CONTENT [%d]\r\n%s\r\n", length, content);

	if (lpEcb->WriteClient(lpEcb, (LPVOID) content, (LPDWORD) length, 0)) {
		jk_log(logger, JK_LOG_TRACE, "WriteClient ... OK");
		return true;
	}

	DWORD dwError;
	dwError = GetLastError();
	jk_log(logger, JK_LOG_ERROR, "WriteClient function failed = %d (%x)", dwError, dwError);
	return false;
}

bool ExtensionAgentResponse::startResponse(int status, string reason, list<pair<string, string>> headers) {

	if (isCommitted()) {
		jk_log(logger, JK_LOG_ERROR, "Attempting to restart a committed response");
		return false;
	}

	if (status < 100 || status > 1000) {
        jk_log(logger, JK_LOG_ERROR,
               "invalid status %d",
               status);
		return false;
	}

	// Create a plain string with headers:
	// <HEADER_NAME_1>: <HEADER_VALUE_1>\r\n<HEADER_NAME_2>: <HEADER_VALUE_2>\r\n...<HEADER_NAME_N>: <HEADER_VALUE_N>\r\n\r\n
	string plainHeaders;
	list<pair<string, string>>::iterator h;
	for (h = headers.begin() ; h != headers.end() ; h ++) {
		pair<string, string> p = *h;
		
		plainHeaders.append(p.first);
		plainHeaders.append(": ");
		plainHeaders.append(p.second);
		plainHeaders.append("\r\n");
	}

	if (plainHeaders.empty()) {
		jk_log(logger, JK_LOG_WARNING, "No HTTP headers in response");
	} else {
		jk_log(logger, JK_LOG_TRACE, "HTTP headers \r\n%s", plainHeaders.c_str());
	}

	plainHeaders.append("\r\n");
	const char * cHeaders = plainHeaders.c_str();

	// Create status string
	char * cStatus;
	cStatus = (char *)malloc((6 + reason.length()));
	StringCbPrintf(cStatus, 6 + reason.length(), "%d %s", status, reason.c_str());
	DWORD szStatus = (DWORD)strlen(cStatus);
	jk_log(logger, JK_LOG_TRACE, "HTTP status %s", cStatus);

	// Check status reason string
	if (reason.empty())
		reason.assign(status_reason(status));

	jk_log(logger, JK_LOG_TRACE, "HTTP status reason %s", reason.c_str());

	// Now start a response:
	jk_log(logger, JK_LOG_DEBUG, "Marking response as committed");
	committed = true;
	
    /* Old style response - forces Connection: close if Tomcat response doesn't
       specify necessary details to allow keep alive */
    BOOL ok = lpEcb->ServerSupportFunction(lpEcb->ConnID,
                                         HSE_REQ_SEND_RESPONSE_HEADER,
										 cStatus,
										 &szStatus,
										 (LPDWORD)plainHeaders.c_str());

	if (cStatus)
		free(cStatus);

	if (!ok) {
		DWORD error = GetLastError();
        jk_log(logger, JK_LOG_ERROR,
               "HSE_REQ_SEND_RESPONSE_HEADER failed with error=%d (0x%08x)", error, error);
		return false;
	} else {
		jk_log(logger, JK_LOG_DEBUG,
               "HSE_REQ_SEND_RESPONSE_HEADER succes");
		return true;
	}
}

string ExtensionAgentResponse::getServerVariable(string name, DWORD cbSize) {
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