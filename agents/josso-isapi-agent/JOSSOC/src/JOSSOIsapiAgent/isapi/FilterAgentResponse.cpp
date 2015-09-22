#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <crtdbg.h>

#include <JOSSOIsapiAgent/isapi/FilterAgentResponse.hpp>

#ifdef _DEBUG
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif


FilterAgentResponse::FilterAgentResponse(HTTP_FILTER_CONTEXT * pfc, DWORD NotificationType, VOID* pvNotification, jk_logger_t * l) {
	this->logger = l;

	this->pfc = pfc;
	this->NotificationType = NotificationType;
	this->pvNotification = pvNotification;

	committed = false;
}

FilterAgentResponse::~FilterAgentResponse() {
	
}

bool sendContent(string content) {
	// TODO !!!!
	return true;
}

bool FilterAgentResponse::flushHeaders() {

	if (committed) {
		jk_log(this->logger, JK_LOG_ERROR, "Response already committed!");
		return false;
	}
	committed = true;

	string plainHeaders = "";
	list<pair<string, string>>::iterator h;
	for (h = headers.begin() ; h != headers.end() ; h ++) {
		pair<string, string> p = *h;
		
		plainHeaders.append(p.first);
		plainHeaders.append(": ");
		plainHeaders.append(p.second);
		plainHeaders.append("\r\n");
	}
	if (plainHeaders.empty()) {
		jk_log(logger, JK_LOG_DEBUG, "No HTTP headers in response");
	} else {
		jk_log(logger, JK_LOG_TRACE, "HTTP headers \r\n%s", plainHeaders.c_str());
	}

	// plainHeaders.append("\r\n");
	size_t length = plainHeaders.length() + 1;
	char * cHeaders = (char*)malloc(length);

	StringCbCopy(cHeaders, length, plainHeaders.c_str());

	if (!this->pfc->AddResponseHeaders(this->pfc, cHeaders, 0)) {
		DWORD dwError;
		dwError = GetLastError();
		jk_log(logger, JK_LOG_ERROR, "AddResponseHeaders[%s] failed = %d (%x)", plainHeaders.c_str(), dwError, dwError);
		return false;
	}

	free (cHeaders);

	return true;
	
}



// TODO : This will not only start the response but also send all headers, without content!
bool FilterAgentResponse::startResponse(int status, string reason, list<pair<string, string>> headers) {

	if (committed) {
		jk_log(this->logger, JK_LOG_ERROR, "Response already committed!");
		return false;
	}
	committed = true;

	char statusLine[MAX_HEADER_SIZE];
	statusLine[0] = '\0';

	StringCbPrintf(statusLine, MAX_HEADER_SIZE, "%d %s", status, reason.c_str());

	string plainHeaders = "";
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

	if(!pfc->ServerSupportFunction (pfc, SF_REQ_SEND_RESPONSE_HEADER, (PVOID) statusLine, (ULONG_PTR) cHeaders, 0)) {
		DWORD dwError;
		dwError = GetLastError();
		jk_log(logger, JK_LOG_ERROR, "ServerSupportFunction[SF_REQ_SEND_RESPONSE_HEADER] failed = %d (%x)", dwError, dwError);
		return false;
	}
		
	return true;
}

bool FilterAgentResponse::sendContent(string content) {
	jk_log(logger, JK_LOG_ERROR, "IMPLEMENT ME!");
	return false;
}

// TODO : Does not work
bool FilterAgentResponse::writeContent(const char * content, size_t length) {

	jk_log(logger, JK_LOG_TRACE, "CONTENT [%d]\r\n%s\r\n", length, content);

	DWORD size = length;
	if (pfc->WriteClient(pfc, (LPVOID) content, &size, 0)) {
		jk_log(logger, JK_LOG_TRACE, "WriteClient ... OK");
		return true;
	}

	DWORD dwError;
	dwError = GetLastError();
	jk_log(logger, JK_LOG_ERROR, "WriteClient function failed = %d (%x)", dwError, dwError);
	return false;
}


/**
 * This will return a server variable value no longer that cbSize.
 */
string FilterAgentResponse::getServerVariable(string name, DWORD cbSize) {

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

		// TODO : Should this be deleted by the client ?
		value.assign(varValue);
	}

	delete varName;
	delete varValue;

	return value;
}