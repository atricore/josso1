package org.josso.gateway.identity.service.store.virtual.scripting;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: BaseExpressionMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class BaseExpressionMappingRule {

    protected String language;
    protected String selectExpression;
    protected String joinExpression;
    protected String transformExpression;
    protected String validateExpression;

    public String getSelectExpression() {
        return selectExpression;
    }

    public void setSelectExpression(String selectExpression) {
        this.selectExpression = selectExpression;
    }

    public String getJoinExpression() {
        return joinExpression;
    }

    public void setJoinExpression(String joinExpression) {
        this.joinExpression = joinExpression;
    }

    public String getTransformExpression() {
        return transformExpression;
    }

    public void setTransformExpression(String transformExpression) {
        this.transformExpression = transformExpression;
    }

    public String getValidateExpression() {
        return validateExpression;
    }

    public void setValidateExpression(String validateExpression) {
        this.validateExpression = validateExpression;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}