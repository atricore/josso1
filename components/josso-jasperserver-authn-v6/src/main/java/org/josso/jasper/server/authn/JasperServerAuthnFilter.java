package org.josso.jasper.server.authn;

import com.jaspersoft.jasperserver.api.security.externalAuth.preauth.BasePreAuthenticatedProcessingFilter;
import org.josso.gateway.GatewayServiceLocator;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.SSOIdentityManagerService;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class JasperServerAuthnFilter extends BasePreAuthenticatedProcessingFilter {

    private GatewayServiceLocator gatewayServiceLocator;

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {

        String authnType = request.getAuthType();

        Map<String, String> jossoCtx = (Map<String, String>) request.getAttribute("org.josso.agent.http.securitycontext.content");
        String ssoSessionId = (String) request.getAttribute("org.josso.agent.ssoSessionid");
        String requester = (String) request.getAttribute("org.josso.agent.requester");

        if (jossoCtx == null) {
            return null;
        }

        // Build a user token:
        StringBuilder token = new StringBuilder();

        // This includes JOSSO_USER and ALL JOSSO_PROPERTY_<name> values
        String prefix = "";
        for(String attr : jossoCtx.keySet()) {
            String value = jossoCtx.get(attr);
            token.append(prefix).append(attr).append("=").append(value);
            prefix = "|";
        }

        // If available, this adds JOSSO_ROLES=<csv>
        if (getIdentityManager() != null) {
            // Load roles
            try {
                SSORole[] roles = getIdentityManager().findRolesBySSOSessionId(ssoSessionId, requester);
                token.append(prefix).append("JOSSO_ROLES=");
                String rolePrefix = "";
                for (SSORole role : roles) {
                    token.append(rolePrefix).append(role.getName());
                    rolePrefix = ",";
                }
            } catch (SSOIdentityException e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (logger.isDebugEnabled())
            logger.debug("Token ["+token.toString()+"]");

        return token.toString();
    }

    public SSOIdentityManagerService getIdentityManager() {
        try {
            return gatewayServiceLocator!= null ? gatewayServiceLocator.getSSOIdentityManager() : null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


        public GatewayServiceLocator getGatewayServiceLocator() {
        return gatewayServiceLocator;
    }

    public void setGatewayServiceLocator(GatewayServiceLocator gatewayServiceLocator) {
        this.gatewayServiceLocator = gatewayServiceLocator;
    }
}
