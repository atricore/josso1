package org.josso.agent.config;

/**
 *
 */
public class BlueprintComponentKeeperFactoryImpl extends ComponentKeeperFactory {
    public ComponentKeeper newComponentKeeper() {
        return new BlueprintComponentKeeperImpl(super.getResourceFileName());
    }
}
