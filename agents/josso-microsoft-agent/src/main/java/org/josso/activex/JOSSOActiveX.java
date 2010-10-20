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

package org.josso.activex;


import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.GatewayServiceLocator;
import org.josso.gateway.assertion.exceptions.AssertionNotValidException;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.IdentityProvisioningException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.SSOIdentityManagerService;
import org.josso.gateway.identity.service.SSOIdentityProviderService;
import org.josso.gateway.session.exceptions.NoSuchSessionException;
import org.josso.gateway.session.service.SSOSessionManagerService;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * This component is based on JavaBeans components architecture.
 * It is packaged by the J2SDK ActiveX bridge as an ActiveX control,
 * thereby allowing it to be used as a functional component in an ActiveX container.
 *
 * To use this ActiveX control, you have to follow this steps :
 *
 * <ul>
 *   <li>1. Instantiate the control.</li>
 *   <li>2. Configure control properties using setProperty method. The default implementation uses SOAP,
 *          so you must configure the SOAP end point i.e. setProperty("endpoint", "myhost.com:8080");</li>
 *   <li>3. Initialize the control : invoke the init() method befor using the control.</li>
 *   <li>4. Invoke operations, i.e. accessSession("2F122BEE8684C0BEE186C0BE91083171");</li>
 * </ul>
 *
 * You could specify a differente GatewayServiceLocator class and configure specific properties for it.
 * If no GatewayServiceLocator FQCN is specified, the WebserviceGatewayServiceLocator is used as default.
 *
 * The control configuration can be specified through the "setProperty" method, all properties starting with
 * the "gwy." prefix will be used to configure the GatewayServiceLocator this control uses.
 *
 * If you use the WebserviceGatweayServiceLocator, you can use the following properties :
 * <ul>
 *  <li>gwy.endpoint : the SOAP endpoint</li>
 *  <li>gwy.transportSecurity : "none" or "confidential", default to "none"./li>
 *  <li>gwy.username : the username credential used for the "confidential" transport security.</li>
 *  <li>gwy.password : the passwrord credential used for "confidential" transport security.</li>
 * </ul>
 *
 * Check the Java Console for log messages.
 *
 * @see org.josso.gateway.GatewayServiceLocator
 * @see org.josso.gateway.WebserviceGatewayServiceLocator
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: JOSSOActiveX.java 1607 2010-05-11 13:39:08Z sgonzalez $
 */

public class JOSSOActiveX  {

    private static final Log logger = LogFactory.getLog(JOSSOActiveX.class);


    private String _id;  // TODO : read from configuration
    private String _version;
    private String log4jProperties;
    private SSOIdentityProviderService _ip;
    private SSOIdentityManagerService _im;
    private SSOSessionManagerService _sm;
    private Properties _props;

    private String _gwyServiceLocatorClass = "org.josso.gateway.WebserviceGatewayServiceLocator";

    public JOSSOActiveX() {

		logger.debug("JOSSOActiveX:Creating new instance ... ");

        _props = new Properties();

        Properties p = new Properties();
        InputStream is = getClass().getResourceAsStream("/org/josso/josso.properties");
        try {
            p.load(is);
            _version = p.get("Name") + "-" + p.get("version");
        } catch (Exception e) {
            _version = "n/a";
        }
    }

    public void init() {

        try {

            if (log4jProperties != null) {
                resetLog4j();
            }

            GatewayServiceLocator sl = doMakeGatewayServiceLocator();

            logger.debug("JOSSOActiveX:Getting new SSOIdentityProvider instance");
            _ip = sl.getSSOIdentityProvider();
            assert _ip != null : "No Identity provider found !";

            logger.debug("JOSSOActiveX:Getting new SSOIdentityManager instance");
            _im = sl.getSSOIdentityManager();
            assert _im != null : "No Identity manager found";

            logger.debug("JOSSOActiveX:Getting new SSOSessionManager instance");
            _sm = sl.getSSOSessionManager();
            assert _sm != null : "No Session manager found";

            logger.debug("JOSSOActiveX:" + getVersion()+" initialized OK");

        } catch (Exception e) {
            logger.error("JOSSOActiveX:" + e.getMessage(), e);
            logger.debug("JOSSOActiveX:" + getVersion()+ " initialized with ERRORS");
            
            throw new RuntimeException("JOSSOActiveX:Error during initialization : " +
                                       (e.getMessage() != null ? e.getMessage() : e.toString()), e);
        }

    }

    /**
     * This operation allows external log4j configuration while using JRE/ActiveX bridge ...
     * @throws IOException
     */
    private void resetLog4j() throws IOException {
        FileInputStream fis = null;

        try {
            org.apache.log4j.LogManager.resetConfiguration();

            fis = new FileInputStream(log4jProperties);
            Properties log4jProperties = new Properties();
            log4jProperties.load(fis);

            new org.apache.log4j.PropertyConfigurator().configure(log4jProperties);

        }finally {
            if (fis != null)
                fis.close();
        }
    }

    /**
     * The version associated with this control.
     */
    public String getVersion() {
        return _version;
    }

    public String getLog4jProperties() {
        return log4jProperties;
    }

    public void setLog4jProperties(String log4jProperties) {
        this.log4jProperties = log4jProperties;
    }

    /**
     * Obtains the SSO Session token associated to the authentication assertion token.
     * @param assertionId
     * @return
     */
    public String resolveAuthenticationAssertion(String assertionId) {
        try {
            return getIdentityProvider().resolveAuthenticationAssertion(_id, assertionId);
        } catch (AssertionNotValidException e) {
            return null;
        } catch (IdentityProvisioningException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.toString(), e);
        }
    }

    /**
     * Finds the user associated to a sso session
     *
     * @param sessionId the sso session identifier
     */
    public SSOUser findUserInSession(String sessionId) {
        try {
            return getIdentityManager().findUserInSession(_id, sessionId);
        } catch (SSOIdentityException e) {
            return null; // Session has expired ...
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.toString(), e);
        }
    }

     /**
     * Finds the username associated to a sso session
     *
     * @param sessionId the sso session identifier
     */
    public String getUserName(String sessionId) {
        try {
            return findUserInSession( sessionId ).getName();
        } catch ( Exception e ) {
            logger.error( e.getMessage(), e );
            throw new RuntimeException( e.getMessage() != null ? e.getMessage() : e.toString(), e );
        }
    }

    /**
     * 
     * @param sessionId
     * @return
     */
     public SSOProperties getUserProperties(String sessionId) {
        try {
            return new SSOProperties( findUserInSession( sessionId ).getProperties() );
        } catch ( Exception e ) {
            logger.error( e.getMessage(), e );
            throw new RuntimeException( e.getMessage() != null ? e.getMessage() : e.toString(), e );
        }
    }
    
    /**
     * Returns all roles associated to a given user.
     */
    public SSORoles getUserRoles(String ssoSessionId)  {
        try {
            return new SSORoles (getIdentityManager().findRolesBySSOSessionId(_id, ssoSessionId));
        } catch (Exception e) {
			logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.toString(), e);
        }
    }

    /**
     * Returns true if the user belongs to the given rolename.
     */
    public boolean isUserInRole(String sessionId, String rolename)  {
        try {
            SSOUser user = this.findUserInSession(sessionId);
            if (user == null)
                return false;

            /*
            SSORole[] roles = getIdentityManager().findRolesBySSOSessionId(user.getName());

            for ( SSORole role : roles ) {
                if ( role.getName().equals( rolename ) ) {
                    return true;
                }
            }
            */
            SSORoles roles = new SSORoles( getIdentityManager().findRolesBySSOSessionId(_id,  sessionId ) );
            for ( int i = 0; i < roles.count(); i++ ) {
                if ( roles.getRole( i ).getName().equals( rolename ) )
                    return true;
            }
            return false;
        } catch (Exception e) {
			logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.toString(), e);
        }

    }

    // -----------------------------------------------------------------------------

    /**
     * This method accesss the session associated to the received id.
     * This resets the session last access time and updates the access count.
     *
     * @param sessionId the session id previously returned by initiateSession.
     *
     * @return true if the session is valid, flase otherwise.
     */
    public boolean accessSession(String sessionId) {
        try {
            getSessionManager().accessSession(_id, sessionId);
            return true;
        } catch (NoSuchSessionException e) {
            return false;
        } catch (Exception e) {
			logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.toString(), e);
        }

    }

    // -----------------------------------------------------------------------------

    /**
     * This method is used to configure the control.
     * Available properties
     *
     * @param name the property name (i.e. .endpoint)
     * @param value
     */
    public void setProperty(String name, String value) {
        _props.setProperty(name, value);
    }

    /**
     * Returns the value of the specified property.
     */
    public String getProperty(String name) {
        return _props.getProperty(name);
    }

    /**
     * Getter for the configuration property to define the concrete GatewayServiceLocator class.
     *
     * @return the FQCN used to create the GatewayServiceLocatorInstance
     */
    public String getGwyServiceLocatorClass() {
        return _gwyServiceLocatorClass;
    }

    /**
     * Configuration property to define the concrete GatewayServiceLocator class.
     *
     * @param gwyServiceLocatorClass the FQCN used to create the GatewayServiceLocatorInstance
     */
    public void setGwyServiceLocatorClass(String gwyServiceLocatorClass) {
        _gwyServiceLocatorClass = gwyServiceLocatorClass;
    }

    /**
     * Getter for the Identity Manager this control is using.
     */
    protected SSOIdentityProviderService getIdentityProvider() {
        return _ip;
    }

    /**
     * Getter for the Identity Manager this control is using.
     */
    protected SSOIdentityManagerService getIdentityManager() {
        return _im;
    }

    /**
     * Getter for the Session Manager this control is using.
     */
    protected SSOSessionManagerService getSessionManager() {
        return _sm;
    }

    /**
     * This method creates a new GatewayServiceLocatorInstance using the
     * configured GatewayServiceLocator class.
     *
     * It also sets all configured properties with the prefix "gwy." to the new service locator instance.
     * For example : the "gwy.endpoint" property will be used to set the endpoint property (setEndpoint(""))
     * in the new gateway service locator instance.
     *
     *
     */
    protected GatewayServiceLocator doMakeGatewayServiceLocator() {
        GatewayServiceLocator serviceLocator = null;

        try {
            serviceLocator = (GatewayServiceLocator) Class.forName(_gwyServiceLocatorClass).newInstance();
        } catch (Exception e) {
			logger.error(e.getMessage(), e);
            throw new RuntimeException("JOSSOActiveX:Can't instantiate gwy service locator : \n" +
                                       (e.getMessage() != null ? e.getMessage() : e.toString()), e);
        }

        Enumeration en = _props.keys();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            Object value = _props.get(key);

            if (key.startsWith("gwy.")) {
                String name = key.substring(4);

                try {
                    if (value != null)
                        BeanUtils.setProperty(serviceLocator, name, value);

                    logger.debug("JOSSOActiveX:setting property to GatewayServiceLocator : " + name+ "=" + value);

                } catch (IllegalAccessException e) {
                    logger.error("JOSSOActiveX:Can't set property to GatewayServiceLocator : " + name + "=" + value + "\n" + e.getMessage());

                } catch (InvocationTargetException e) {
                    logger.error("JOSSOActiveX:Can't set property to GatewayServiceLocator : " + name + "=" + value + "\n" + e.getMessage());
                }
            }
        }

        return serviceLocator;
    }

	public static void main(String[] args) {
		logger.debug("Hello, World!");

		JOSSOActiveX x = new JOSSOActiveX();
		x.init();
	}
}
