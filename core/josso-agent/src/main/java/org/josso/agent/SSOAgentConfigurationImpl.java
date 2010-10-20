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
package org.josso.agent;

import java.util.ArrayList;
import java.util.List;

/**
 * @org.apache.xbean.XBean element="agent-configuration"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOAgentConfigurationImpl.java 598 2008-08-16 05:41:50Z gbrigand $
 */

public class SSOAgentConfigurationImpl implements SSOAgentConfiguration {

    private List<SSOPartnerAppConfig> _cfgs;

    public SSOAgentConfigurationImpl() {
        _cfgs = new ArrayList<SSOPartnerAppConfig>();
    }

    public void addSSOPartnerApp(String id, String context, String[] ignoredWebResources) {
        SSOPartnerAppConfig cfg = new SSOPartnerAppConfig();
        cfg.setId(id);
        cfg.setContext(context);
        cfg.setIgnoredWebResources(ignoredWebResources);
        _cfgs.add(cfg);
    }

    public void addSSOPartnerApp(String id, String vhost, String context, String[] ignoredWebResources, SecurityContextPropagationConfig secCtxPropCfg) {

        SSOPartnerAppConfig cfg = new SSOPartnerAppConfig();
        cfg.setId(id);
        cfg.setVhost(vhost);
        cfg.setContext(context);
        cfg.setIgnoredWebResources(ignoredWebResources);
        cfg.setSecurityContextPropagationConfig(secCtxPropCfg);

        _cfgs.add(cfg);
    }

    public void addSSOPartnerApp(SSOPartnerAppConfig cfg) {
        _cfgs.add(cfg);
    }

    public void removeSSOPartnerApp(String c) {
        for (int i = 0; i < _cfgs.size(); i++) {
            SSOPartnerAppConfig cfg = (SSOPartnerAppConfig) _cfgs.get(i);
            if (cfg.getContext().equals(c)) {
                _cfgs.remove(cfg);
                return;
            }
        }
    }

    /**
     * @org.apache.xbean.Property alias="partner-apps" nestedType="org.josso.agent.SSOPartnerAppConfig"
     * @return
     */
    public List<SSOPartnerAppConfig> getSsoPartnerApps() {
        return _cfgs;
    }

    public void setSsoPartnerApps(List<SSOPartnerAppConfig> apps) {
        this._cfgs = apps;
    }

}
