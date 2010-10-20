package org.josso.gateway.identity.service.store.virtual.scripting;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: BaseScriptingMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class BaseScriptingMappingRule {

    protected String selectRule;
    protected String joinRule;
    protected String transformRule;
    protected String validateRule;

    public String getSelectRule() {
        return selectRule;
    }

    public void setSelectRule(String selectRule) {
        this.selectRule = selectRule;
    }

    public String getJoinRule() {
        return joinRule;
    }

    public void setJoinRule(String joinRule) {
        this.joinRule = joinRule;
    }

    public String getTransformRule() {
        return transformRule;
    }

    public void setTransformRule(String transformRule) {
        this.transformRule = transformRule;
    }

    public String getValidateRule() {
        return validateRule;
    }

    public void setValidateRule(String validateRule) {
        this.validateRule = validateRule;
    }
}
