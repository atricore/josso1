package org.josso.atlassian.seraph;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;

import java.util.Map;

/**
 * @author <a href=mailto:sgonzalez@atricore.org>Sebastian Gonzalez Oyuela</a>
 */
public abstract class AbstractCrowdDirectorySelectorStrategy implements CrowdDirectorySelectorStrategy {

    private Map<String, String> initParams;

    private CrowdDirectoryService directoryService;

    protected AbstractCrowdDirectorySelectorStrategy(Map<String, String> initParams, CrowdDirectoryService directoryService) {
        this.initParams = initParams;
        this.directoryService = directoryService;
    }



    public void setInitParams(Map<String, String> initParams) {
        this.initParams = initParams;
    }

    public void setDirectoryService(CrowdDirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    public String getInitParam(String key) {
        return initParams.get(key);
    }

    public CrowdDirectoryService getDirectoryService() {
        return directoryService;
    }
}
