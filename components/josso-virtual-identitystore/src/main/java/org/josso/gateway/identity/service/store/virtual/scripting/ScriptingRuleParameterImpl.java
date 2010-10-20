package org.josso.gateway.identity.service.store.virtual.scripting;

/**
 * Default implementation of contract for mapping rule engine parameters.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: ScriptingRuleParameterImpl.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class ScriptingRuleParameterImpl implements ScriptingRuleParameter {
    private String name;
    private Object value;
    private Class type;


    public ScriptingRuleParameterImpl(String name, Object value, Class type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }
}
