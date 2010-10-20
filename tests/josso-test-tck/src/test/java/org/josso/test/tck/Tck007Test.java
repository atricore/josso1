package org.josso.test.tck;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.test.support.TckTestSupport;
import org.junit.Test;

public class Tck007Test extends TckTestSupport {

	public static final Log log = LogFactory.getLog(Tck007Test.class);
	
	@Test
    public void runTck() throws Exception {
		doGetProtectedDelegated();
        doPostCustomAuthentication();
        doPostAutoSubmitForm();
    }
	
    protected void doGetProtectedDelegated() throws Exception {
        // This will be redirected until the gateway login form is presented
        HttpMethod getMethod = doGet("http://localhost:"+getProperty("josso.tck.serverPort")+"/webapp1/protected-delegated.jsp");
        String body = getMethod.getResponseBodyAsString();

        assert body.indexOf("josso_username") > 0 : "No 'josso_username' field received in response";
        assert body.indexOf("josso_password") > 0 : "No 'josso_password' field received in response";
    }	
	
	private void doPostCustomAuthentication() throws Exception {
        NameValuePair username= new NameValuePair("josso_username", "user1");
        NameValuePair password = new NameValuePair("josso_password", "user1pwd");
        NameValuePair cmd = new NameValuePair(PARAM_JOSSO_CMD, "login");
        String referer = "http://localhost:"+getProperty("josso.tck.serverPort")+"/webapp1/protected-delegated-splash-3.jsp";

        PostMethod postMethod = doPost("http://localhost:"+getProperty("josso.tck.serverPort")+"/webapp1/josso_authentication/", 
        		referer, username, password, cmd);
        int status = postMethod.getStatusCode();
        assert status == HttpStatus.SC_OK : "Unexpected HTTP status " + status;
        
        String body = postMethod.getResponseBodyAsString();
        assert body.indexOf("josso_username") > 0 : "josso_username not sent in generated form";
        assert body.indexOf("josso_password") > 0 : "josso_password not sent in generated form";
        assert body.indexOf("josso_cmd") > 0 : "josso_cmd not sent in generated form";
        assert body.indexOf("josso_back_to") > 0 : "josso_back_to not sent in generated form";
	}

	private void doPostAutoSubmitForm() throws Exception {
        NameValuePair username= new NameValuePair("josso_username", "user1");
        NameValuePair password = new NameValuePair("josso_password", "user1pwd");
        NameValuePair cmd = new NameValuePair(PARAM_JOSSO_CMD, "login");
        

        PostMethod postMethod = doPost("http://localhost:"+getProperty("josso.tck.serverPort")+"/josso/signon/login.do?josso_back_to=/webapp1/josso_security_check", 
        		username, password, cmd);
        int status = postMethod.getStatusCode();
        assert status == HttpStatus.SC_MOVED_TEMPORARILY : "Unexpected HTTP status " + status;

        Header location = postMethod.getResponseHeader("Location");
        HttpMethod getMethod = doGet(location.getValue());
        status = getMethod.getStatusCode();

        assert status == HttpStatus.SC_OK : "Unexpected HTTP status " + status;

        String body = getMethod.getResponseBodyAsString();
        assert body.indexOf("JOSSO_SESSIONID=") > 0 : "No JOSSO_SESSIONID= recived in response";
        assert body.indexOf("referer splash resource") > 0 : "Not a referer splash resource page";
	}

	@Override
	protected String getTckId() {
		return "007";
	}

}
