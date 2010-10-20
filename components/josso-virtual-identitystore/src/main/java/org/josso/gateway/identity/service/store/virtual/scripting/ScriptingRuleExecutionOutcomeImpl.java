package org.josso.gateway.identity.service.store.virtual.scripting;

/**
 * Default implementation for the script outcome.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: ScriptingRuleExecutionOutcomeImpl.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class ScriptingRuleExecutionOutcomeImpl implements ScriptingRuleExecutionOutcome {
    private Object object;

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
