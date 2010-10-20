package org.josso.agent.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.SSOPartnerAppConfig;
import org.josso.auth.util.CipherUtil;
import org.josso.gateway.SSONameValuePair;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @org.apache.xbean.XBean element="http-parameters-builder"
 *
 * @author <a href="mailto:sgonzaelz@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id$
 */
public class HttpParametersBuilder extends AbstractFrontChannelParametersBuilder {

    private static final Log logger = LogFactory.getLog(HttpParametersBuilder .class);

    private boolean encodeValues = true;

    private boolean encodeNames = false;

    private List<String> includeRequestParameters = new ArrayList<String>();

    private List<String> excludeRequestParameters = new ArrayList<String>();

    private List<String> includeSessionAttributes = new ArrayList<String>();

    private List<String> excludeSessionAttributes = new ArrayList<String>();
    
    private List<Pattern> includeRequestParametersPatterns = new ArrayList<Pattern>();

    private List<Pattern> excludeRequestParametersPatterns = new ArrayList<Pattern>();

    private List<Pattern> includeSessionAttributesPatterns = new ArrayList<Pattern>();

    private List<Pattern> excludeSessionAttributesPatterns = new ArrayList<Pattern>();

    private boolean init = false;


    public boolean isEncodeValues() {
        return encodeValues;
    }

    public void setEncodeValues(boolean encodeValues) {
        this.encodeValues = encodeValues;
    }

    public boolean isEncodeNames() {
        return encodeNames;
    }

    public void setEncodeNames(boolean encodeNames) {
        this.encodeNames = encodeNames;
    }

    /**
     * @org.apache.xbean.Property alias="include-params" nestedType="java.lang.String"
     * @return
     */
    public List<String> getIncludeRequestParameters() {
        return includeRequestParameters;
    }

    public void setIncludeRequestParameters(List<String> includeRequestParameters) {
        this.includeRequestParameters = includeRequestParameters;
    }

    /**
     * @org.apache.xbean.Property alias="exclude-params" nestedType="java.lang.String"
     * @return
     */
    public List<String> getExcludeRequestParameters() {
        return excludeRequestParameters;
    }

    public void setExcludeRequestParameters(List<String> excludeRequestParameters) {
        this.excludeRequestParameters = excludeRequestParameters;
    }


    /**
     * @org.apache.xbean.Property alias="include-session-attrs" nestedType="java.lang.String"
     * @return
     */
    public List<String> getIncludeSessionAttributes() {
        return includeSessionAttributes;
    }

    public void setIncludeSessionAttributes(List<String> includeSessionAttributes) {
        this.includeSessionAttributes = includeSessionAttributes;
    }

    /**
     * @org.apache.xbean.Property alias="exclude-session-attrs" nestedType="java.lang.String"
     * @return
     */
    public List<String> getExcludeSessionAttributes() {
        return excludeSessionAttributes;
    }

    public void setExcludeSessionAttributes(List<String> excludeSessionAttributes) {
        this.excludeSessionAttributes = excludeSessionAttributes;
    }

    public SSONameValuePair[] buildParamters(SSOPartnerAppConfig cfg, HttpServletRequest hreq) {
        
        if (!init)
            init();

        List<SSONameValuePair> params = new ArrayList<SSONameValuePair>();

        // Request Parameters
        Enumeration paramNames = hreq.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            if (hreqParamMatches(paramName)) {

                String paramValue = hreq.getParameter(paramName);

                if (encodeNames)
                    paramName = encode(paramName);

                if (encodeValues)
                    paramValue = encode(paramValue);

                params.add(new SSONameValuePair(paramName, paramValue));
            }
        }

        // Session Attributes
        HttpSession session = hreq.getSession();
        Enumeration attrNames = session.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = (String) attrNames.nextElement();

            if (this.sessionAttrMatches(attrName)) {
                Object value = session.getAttribute(attrName);

                if (value instanceof String) {
                    String attrValue = (String) value;

                    if (encodeValues)
                        attrValue = encode(attrValue);

                    if (encodeNames)
                        attrName = encode(attrName);

                    params.add(new SSONameValuePair(attrName, attrValue));
                } else {
                    logger.warn("Non string session attribute cannot be exported " + attrName);
                }
            }


        }


        return params.toArray(new SSONameValuePair[params.size()] );
    }
    
    public void init() {

        // Compile regex patterns

        for (String p : includeRequestParameters) {
            Pattern pattern = Pattern.compile(p);
            includeRequestParametersPatterns.add(pattern);
        }
        
        for (String p : excludeRequestParameters) {
            Pattern pattern = Pattern.compile(p);
            excludeRequestParametersPatterns.add(pattern);
        }

        for (String p : includeSessionAttributes) {
            Pattern pattern = Pattern.compile(p);
            includeSessionAttributesPatterns.add(pattern);
        }

        for (String p : excludeSessionAttributes) {
            Pattern pattern = Pattern.compile(p);
            excludeSessionAttributesPatterns.add(pattern);
        }
        
    }

    protected boolean hreqParamMatches(String paramName) {

        for (Pattern p : includeRequestParametersPatterns) {
            Matcher m = p.matcher(paramName);
            if (m.matches())
                return true;
        }
        // Not found in the include list
        if (includeRequestParametersPatterns.size() > 0)
            return false;

        for (Pattern p : excludeRequestParametersPatterns) {
            Matcher m = p.matcher(paramName);
            if (m.matches())
                return false;
        }
        // Not found in the exclude list
        if (excludeRequestParametersPatterns.size() > 0)
            return true;

        // this means that both lists are empty, all request parameters are sent
        return true;

    }


    protected boolean sessionAttrMatches(String attrName) {

        for (Pattern p : includeSessionAttributesPatterns) {
            Matcher m = p.matcher(attrName);
            if (m.matches())
                return true;
        }
        // Not found in the include list
        if (includeSessionAttributesPatterns.size() > 0)
            return false;

        for (Pattern p : excludeSessionAttributesPatterns) {
            Matcher m = p.matcher(attrName);
            if (m.matches())
                return false;
        }
        // Not found in the exclude list
        if (excludeSessionAttributesPatterns.size() > 0)
            return true;

        // this means that both lists are empty, no session attribute is sent
        return false;


    }

    protected String encode(String value) {
        try {
            return URLEncoder.encode(CipherUtil.encodeBase64(value.getBytes()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Cannot create Base64 encoding " + e.getMessage(), e);
            return value;
        }
    }


}
