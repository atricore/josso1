package org.josso.spring.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class JOSSOAuthenticationProvider implements AuthenticationProvider, InitializingBean, Ordered {
    private static final Log logger = LogFactory.getLog(JOSSOAuthenticationProvider.class);

    private int order = -1; // default: same as non-ordered

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return null;
    }

    public void afterPropertiesSet() throws Exception {

    }

    public boolean supports(Class<?> authentication) {
        return JOSSOAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public int getOrder() {
        return order;
    }
}
