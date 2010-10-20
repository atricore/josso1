/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2009, Atricore, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.josso.tc50.gateway.reverseproxy;

import org.apache.catalina.*;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.josso.agent.Lookup;
import org.josso.gateway.Constants;
import org.josso.agent.reverseproxy.ProxyContextConfig;
import org.josso.agent.reverseproxy.ReverseProxyConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * Reverse Proxy implementation using Tomcat Valves.
 *
 * @deprecated This component is no longer needed for N-Tier configurations.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: ReverseProxyValve.java 974 2009-01-14 00:39:45Z sgonzalez $
 */

public class ReverseProxyValve extends ValveBase implements Lifecycle {

    // ----------------------------------------------------- Constants

    static final String METHOD_GET  = "GET";
    static final String METHOD_POST = "POST";
    static final String METHOD_PUT  = "PUT";
    private final String METHOD_HEAD = "HEAD";

    // ----------------------------------------------------- Instance Variables
    private String _configurationFileName;
    private ReverseProxyConfiguration _rpc;
    private boolean started;
    private String _reverseProxyHost; // Reverse proxy host value.
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
            "org.josso.tc50.gateway.reverseproxy.ReverseProxyValve/1.0";



    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }

    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @throws LifecycleException if this component detects a fatal error
     *                            that prevents this component from being used
     */
    public void start() throws LifecycleException {

        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                    ("ReverseProxy already started");
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        try {
            _rpc = Lookup.getInstance().lookupReverseProxyConfiguration();
        } catch (Exception e) {
            throw new LifecycleException(e.getMessage(), e);
        }

        if (debug >= 1)
            log("Started");
    }

    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @throws LifecycleException if this component detects a fatal error
     *                            that needs to be reported
     */
    public void stop() throws LifecycleException {

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                    ("ReverseProxy not started");
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        if (debug >= 1)
            log("Stopped");

    }


    // ------------------------------------------------------------- Properties

    /**
     * Sets reverse proxy configuration file name.
     *
     * @param configurationFileName configuration file name property value
     */
    public void setConfiguration(String configurationFileName) {
        _configurationFileName = configurationFileName;
    }

    /**
     * Returns reverse proxy configuration file name.
     *
     * @return configuration property value
     */
    public String getConfiguration() {
        return _configurationFileName;
    }


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {
        return (info);
    }



    /**
     * Intercepts Http request and redirects it to the configured SSO partner application.
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     * @param valveContext The valve _context used to invoke the next valve
     *  in the current processing pipeline
     * @exception IOException if an input/output error occurs
     * @exception javax.servlet.ServletException if a servlet error occurs
     */
    public void invoke(Request request, Response response, ValveContext valveContext) throws IOException, javax.servlet.ServletException {

        if (debug >= 1)
            log("ReverseProxyValve Acting.");

        ProxyContextConfig[] contexts =
                _rpc.getProxyContexts();


        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        HttpServletRequest hsr = (HttpServletRequest)request.getRequest();
        String uri = hsr.getRequestURI();

        String uriContext = null;

        StringTokenizer st = new StringTokenizer (uri.substring(1), "/");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            uriContext = "/" + token;
            break;
        }

        if (uriContext == null)
            uriContext = uri;

        // Obtain the target host from the
        String proxyForwardHost = null;
        String proxyForwardUri = null;

        for (int i=0; i < contexts.length; i++) {
            if (contexts[i].getContext().equals(uriContext)) {
                log("Proxy context mapped to host/uri: " + contexts[i].getForwardHost() +
                        contexts[i].getForwardUri() );
                proxyForwardHost = contexts[i].getForwardHost();
                proxyForwardUri = contexts[i].getForwardUri();
                break;
            }
        }


        if (proxyForwardHost == null)
        {
            log("URI '" + uri + "' can't be mapped to host");
            valveContext.invokeNext(request, response);
            return;
        }

        if (proxyForwardUri == null) {
            // trim the uri context before submitting the http request
            int uriTrailStartPos = uri.substring(1).indexOf("/") + 1;
            proxyForwardUri = uri.substring(uriTrailStartPos);
        } else {
            int uriTrailStartPos = uri.substring(1).indexOf("/") + 1;
            proxyForwardUri = proxyForwardUri + uri.substring(uriTrailStartPos);
        }

        // log ("Proxy request mapped to " + "http://" + proxyForwardHost + proxyForwardUri);

        HttpMethod method;

        // to be moved to a builder which instantiates and build concrete methods.
        if ( hsr.getMethod().equals(METHOD_GET)) {
            // Create a method instance.
            HttpMethod getMethod = new GetMethod(proxyForwardHost +
                    proxyForwardUri + (hsr.getQueryString() != null ? ("?" + hsr.getQueryString()) : "")
            );
            method = getMethod;
        } else
        if ( hsr.getMethod().equals(METHOD_POST)) {
            // Create a method instance.
            PostMethod postMethod = new PostMethod(proxyForwardHost +
                    proxyForwardUri + (hsr.getQueryString() != null ? ("?" + hsr.getQueryString()) : "")
            );
            postMethod.setRequestBody(hsr.getInputStream());
            method = postMethod;
        } else
        if ( hsr.getMethod().equals(METHOD_HEAD)) {
            // Create a method instance.
            HeadMethod headMethod = new HeadMethod(proxyForwardHost +
                    proxyForwardUri + (hsr.getQueryString() != null ? ("?" + hsr.getQueryString()) : "")
            );
            method = headMethod;
        } else
        if ( hsr.getMethod().equals(METHOD_PUT)) {
            method = new PutMethod(proxyForwardHost +
                    proxyForwardUri + (hsr.getQueryString() != null ? ("?" + hsr.getQueryString()) : "")
            );
        } else
            throw new java.lang.UnsupportedOperationException("Unknown method : " + hsr.getMethod());

        // copy incoming http headers to reverse proxy request
        Enumeration hne = hsr.getHeaderNames();
        while (hne.hasMoreElements()) {
            String hn = (String)hne.nextElement();

            // Map the received host header to the target host name
            // so that the configured virtual domain can
            // do the proper handling.
            if (hn.equalsIgnoreCase("host")) {
                method.addRequestHeader("Host", proxyForwardHost);
                continue;
            }

            Enumeration hvals = hsr.getHeaders(hn);
            while (hvals.hasMoreElements()) {
                String hv = (String)hvals.nextElement();
                method.addRequestHeader(hn, hv);
            }
        }

        // Add Reverse-Proxy-Host header
        String reverseProxyHost = getReverseProxyHost(request);
        method.addRequestHeader(Constants.JOSSO_REVERSE_PROXY_HEADER, reverseProxyHost);

        if (debug >= 1)
            log("Sending " + Constants.JOSSO_REVERSE_PROXY_HEADER + " " + reverseProxyHost);

        // DO NOT follow redirects !
        method.setFollowRedirects(false);

        // By default the httpclient uses HTTP v1.1. We are downgrading it
        // to v1.0 so that the target server doesn't set a reply using chunked
        // transfer encoding which doesn't seem to be handled properly.
        // Check how to make chunked transfer encoding work.
        client.getParams().setVersion(new HttpVersion(1, 0));

        // Execute the method.
        int statusCode = -1;
        try {
            // execute the method.
            statusCode = client.executeMethod(method);
        } catch (HttpRecoverableException e) {
            log(
                    "A recoverable exception occurred " +
                    e.getMessage());
        } catch (IOException e) {
            log("Failed to connect.");
            e.printStackTrace();
        }

        // Check that we didn't run out of retries.
        if (statusCode == -1) {
            log("Failed to recover from exception.");
        }

        // Read the response body.
        byte[] responseBody = method.getResponseBody();

        // Release the connection.
        method.releaseConnection();

        HttpServletResponse sres = (HttpServletResponse)response.getResponse();

        // First thing to do is to copy status code to response, otherwise
        // catalina will do it as soon as we set a header or some other part of the response.
        sres.setStatus(method.getStatusCode());

        // copy proxy response headers to client response
        Header[] responseHeaders = method.getResponseHeaders();
        for (int i=0; i < responseHeaders.length; i++) {
            Header responseHeader = responseHeaders[i];
            String name = responseHeader.getName();
            String value = responseHeader.getValue();

            // Adjust the URL in the Location, Content-Location and URI headers on HTTP redirect responses
            // This is essential to avoid by-passing the reverse proxy because of HTTP redirects on the
            // backend servers which stay behind the reverse proxy
            switch (method.getStatusCode()) {
                case HttpStatus.SC_MOVED_TEMPORARILY:
                case HttpStatus.SC_MOVED_PERMANENTLY:
                case HttpStatus.SC_SEE_OTHER:
                case HttpStatus.SC_TEMPORARY_REDIRECT:

                    if ("Location".equalsIgnoreCase(name) ||
                        "Content-Location".equalsIgnoreCase(name) || "URI".equalsIgnoreCase(name)) {

                        // Check that this redirect must be adjusted.
                        if (value.indexOf(proxyForwardHost) >= 0) {
                            String trail = value.substring(proxyForwardHost.length());
                            value = getReverseProxyHost(request) + trail;
                            if (debug >= 1)
                                log("Adjusting redirect header to " + value);
                        }
                    }
                    break;

            } //end of switch
            sres.addHeader(name, value);

        }

        // Sometimes this is null, when no body is returned ...
        if (responseBody != null && responseBody.length > 0)
            sres.getOutputStream().write(responseBody);

        sres.getOutputStream().flush();

        if (debug >= 1)
            log("ReverseProxyValve finished.");

        return;
    }


    /**
     * Return a String rendering of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("ReverseProxyValve[");
        if (container != null)
            sb.append(container.getName());
        sb.append("]");
        return (sb.toString());
    }


    // ------------------------------------------------------ Protected Methods

    /**
     * This method calculates the reverse-proxy-host header value.
     */
    protected String getReverseProxyHost(Request request) {
        HttpServletRequest hsr = (HttpServletRequest)request.getRequest();
        if (_reverseProxyHost == null) {
            synchronized(this) {
                String h = hsr.getProtocol().substring(0,  hsr.getProtocol().indexOf("/")).toLowerCase() +
                         "://" + hsr.getServerName() +
                         (hsr.getServerPort() != 80 ? (":" + hsr.getServerPort()) : "");
                _reverseProxyHost = h;
            }
        }

        return _reverseProxyHost;

    }

    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {

        Logger logger = container.getLogger();
        if (logger != null)
            logger.log(this.toString() + ": " + message);
        else
            System.out.println(this.toString() + ": " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    protected void log(String message, Throwable throwable) {

        Logger logger = container.getLogger();
        if (logger != null)
            logger.log(this.toString() + ": " + message, throwable);
        else {
            System.out.println(this.toString() + ": " + message);
            throwable.printStackTrace(System.out);
        }

    }


}
