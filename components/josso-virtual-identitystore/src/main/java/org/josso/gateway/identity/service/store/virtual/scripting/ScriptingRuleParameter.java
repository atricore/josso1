package org.josso.gateway.identity.service.store.virtual.scripting;

/**
 * Contract for mapping rule engine parameters.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: ScriptingRuleParameter.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public interface ScriptingRuleParameter {

    String getName();

    Object getValue();

    Class getType();
}
