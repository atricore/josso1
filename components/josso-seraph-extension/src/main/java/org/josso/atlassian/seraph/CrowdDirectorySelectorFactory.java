package org.josso.atlassian.seraph;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @author <a href=mailto:sgonzalez@atricore.org>Sebastian Gonzalez Oyuela</a>
 */
public abstract class CrowdDirectorySelectorFactory {

    private static final Logger logger = Logger.getLogger(CrowdDirectorySelectorFactory.class);

    private static CrowdDirectorySelectorFactory factory;

    public static CrowdDirectorySelectorFactory getInstance(String factoryType) {

        // TODO : Can be improved!

        if (factory == null) {

            try {
                factory = (CrowdDirectorySelectorFactory) Class.forName(factoryType).newInstance();
            } catch (Exception e) {
                logger.error("Cannot create factory instance : " + e.getMessage(), e);
            }
        }

        return factory;

    }

    public abstract CrowdDirectorySelectorStrategy getStrategy(Map<String, String> initParams, CrowdDirectoryService directoryService);
}
