package org.josso.atlassian.seraph;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;

import java.util.Map;

/**
 * @author <a href=mailto:sgonzalez@atricore.org>Sebastian Gonzalez Oyuela</a>
 */
public class DnBasedCrowdDirectorySelectorFactory extends CrowdDirectorySelectorFactory {

    public DnBasedCrowdDirectorySelectorFactory() {
    }

    @Override
    public CrowdDirectorySelectorStrategy getStrategy(Map<String, String> initParams, CrowdDirectoryService directoryService) {
        return new DnBasedCrowdDirectorySelector(initParams, directoryService);
    }
}
