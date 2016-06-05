package org.josso.agent.config;

import org.apache.aries.blueprint.container.BlueprintContainerImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.SSOAgent;
import org.josso.agent.reverseproxy.ReverseProxyConfiguration;


import java.net.URL;
import java.util.Arrays;

/**
 * Created by sgonzalez.
 */
public class BlueprintComponentKeeperImpl implements org.josso.agent.config.ComponentKeeper {

    private static final Log logger = LogFactory.getLog(BlueprintComponentKeeperImpl.class);

    private BlueprintContainerImpl container;

    /**
     * Creates a new Spring Component Keeper.
     *
     * @param resource The xml file holding JOSSO Spring/xbean configuration.
     */
    public BlueprintComponentKeeperImpl(String resource) {

        try {

            logger.info("Initializing Blueprint Component Keeper with configuration " + resource);
            // Try class classloader
            URL url = getClass().getClassLoader().getResource(resource);

            // If not found, try thread classloader
            if (url == null)
                url = Thread.currentThread().getContextClassLoader().getResource(resource);

            if (url == null)
                throw new RuntimeException("Cannot find agent config " + resource);

            container = new BlueprintContainerImpl(getClass().getClassLoader(), Arrays.asList(url));
            logger.info("Initialized Blueprint Component Keeper");

        } catch (Exception e) {
            logger.error("Initializing container: " + e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    public SSOAgent fetchSSOAgent() throws Exception {
        return (SSOAgent) container.getComponentInstance("josso-agent");
    }

    public ReverseProxyConfiguration fetchReverseProxyConfiguration() throws Exception {
        throw new UnsupportedOperationException();
    }
}
