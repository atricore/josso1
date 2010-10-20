#ifndef HTTP_UTIL
#define HTTP_UTIL

static struct error_reasons {
    int status;
    const char *reason;
    const char *title;
    const char *description;
} error_reasons[] = {
    { 100,
      "Continue",
      NULL,
      NULL
    },
    { 101,
      "Switching Protocols",
      NULL,
      NULL
    },
    { 200,
      "OK",
      NULL,
      NULL
    },
    { 201,
      "Created",
      NULL,
      NULL
    },
    { 202,
      "Accepted",
      NULL,
      NULL
    },
    { 203,
      "Non-Authoritative Information",
      NULL,
      NULL
    },
    { 204,
      "No Content",
      NULL,
      NULL
    },
    { 205,
      "Reset Content",
      NULL,
      NULL
    },
    { 206,
      "Partial Content",
      NULL,
      NULL
    },
    { 300,
      "Multiple Choices",
      NULL,
      NULL
    },
    { 301,
      "Moved Permanently",
      NULL,
      NULL
    },
    { 302,
      "Moved Temporarily",
      NULL,
      NULL
    },
    { 303,
      "See Other",
      NULL,
      NULL
    },
    { 304,
      "Not Modified",
      NULL,
      NULL
    },
    { 305,
      "Use Proxy",
      NULL,
      NULL
    },
    { 400,
      "Bad Request",
      "Bad Request",
      "Your browser (or proxy) sent a request that "
      "this server could not understand."
    },
    { 401,
      "Unauthorized",
      "Access is denied due to invalid credentials",
      "You do not have permission to view this directory or "
      "page using the credentials that you supplied."
    },
    { 402,
      "Payment Required",
      NULL,
      NULL
    },
    { 403,
      "Forbidden",
      "Access is denied",
      "You do not have permission to view this directory or page "
      "using the credentials that you supplied."
    },
    { 404,
      "Not Found",
      "The requested URL was not found on this server",
      "If you entered the URL manually please check your"
      "spelling and try again."
    },
    { 405,
      "Method Not Allowed",
      "HTTP method used to access this page is not allowed",
      "The page you are looking for cannot be displayed because an "
      "invalid method (HTTP method) was used to attempt access."
    },
    { 406,
      "Not Acceptable",
      "Client browser does not accept the MIME type of the requested page",
      "The page you are looking for cannot be opened by your browser "
      "because it has a file name extension that your browser "
      "does not accept."
    },
    { 407,
      "Proxy Authentication Required",
      NULL,
      "The client must first authenticate itself with the proxy"
    },
    { 408,
      "Request Timeout",
      NULL,
      "The client did not produce a request within the time "
      "that the server was prepared to wait."
    },
    { 409,
      "Conflict",
      NULL,
      "The request could not be completed due to a conflict with "
      "the current state of the resource."
    },
    { 410,
      "Gone",
      NULL,
      "The requested resource is no longer available at the "
      "server and no forwarding address is known."
    },
    { 411,
      "Length Required",
      NULL,
      "The server refuses to accept the request without a "
      "defined Content-Length."
    },
    { 412,
      "Precondition Failed",
      NULL,
      "The precondition given in one or more of the request "
      "header fields evaluated to false when it was tested on the server."
    },
    { 413,
      "Request Entity Too Large",
      NULL,
      "The HTTP method does not allow the data transmitted, "
      "or the data volume exceeds the capacity limit."
    },
    { 414,
      "Request-URI Too Long",
      "Submitted URI too large",
      "The length of the requested URL exceeds the capacity limit "
      "for this server. The request cannot be processed."
    },
    { 415,
      "Unsupported Media Type",
      NULL,
      "The server is refusing to service the request because the "
      "entity of the request is in a format not supported by the "
      "requested resource for the requested method."
    },
    { 500,
      "Internal Server Error",
      NULL,
      "The server encountered an internal error and was "
      "unable to complete your request."
    },
    { 501,
      "Not Implemented",
      NULL,
      "The server does not support the functionality required "
      "to fulfill the request."
    },
    { 502,
      "Bad Gateway",
      NULL,
      "There is a problem with the page you are looking for, "
      "and it cannot be displayed. When the Web server (while "
      "acting as a gateway or proxy) contacted the upstream content "
      "server, it received an invalid response from the content server."
    },
    { 503,
      "Service Unavailable",
      "Service Temporary Unavailable",
      "The server is temporarily unable to service your "
      "request due to maintenance downtime or capacity problems. "
      "Please try again later."
    },
    { 504,
      "Gateway Timeout",
      NULL,
      "The server, while acting as a gateway or proxy, "
      "did not receive a timely response from the upstream server"
    },
    { 505,
      "HTTP Version Not Supported",
      NULL,
      "The server does not support, or refuses to support, the "
      "HTTP protocol version that was used in the request message."
    },
    { 0,
      NULL,
      NULL,
      NULL
    }
};


static const char *status_reason(int status)
{
    struct error_reasons *r;

    r = error_reasons;
    while (r->status <= status) {
        if (r->status == status)
            return r->reason;
        else
            r++;
    }
    return "No Reason";
}

static const char *status_title(int status)
{
    struct error_reasons *r;

    r = error_reasons;
    while (r->status <= status) {
        if (r->status == status) {
            if (r->title)
                return r->title;
            else
                return r->reason;
        }
        else
            r++;
    }
    return "Unknown Error";
}

static const char *status_description(int status)
{
    struct error_reasons *r;

    r = error_reasons;
    while (r->status <= status) {
        if (r->status == status) {
            if (r->description)
                return r->description;
            else
                return r->reason;
        }
        else
            r++;
    }
    return "Unknown Error";
}

#endif