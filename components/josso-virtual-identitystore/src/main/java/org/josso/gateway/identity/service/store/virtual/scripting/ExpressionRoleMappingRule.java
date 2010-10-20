package org.josso.gateway.identity.service.store.virtual.scripting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.store.virtual.RoleMappingRule;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: ExpressionRoleMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="role-mapping-expression"
 */
public class ExpressionRoleMappingRule extends BaseExpressionMappingRule implements RoleMappingRule {
    private static final Log logger = LogFactory.getLog(ExpressionRoleMappingRule.class);

    public Collection<BaseRole> select(Collection<BaseRole> sourceRoles) {

        if (selectExpression != null) {
            Collection<ScriptingRuleParameter> selectParams = new ArrayList<ScriptingRuleParameter>();

            selectParams.add(new ScriptingRuleParameterImpl("sourceRoles", sourceRoles, Collection.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(selectExpression, language, selectParams);
                return (Collection<BaseRole>) outcome.getObject();
            } catch (Exception e) {
                logger.error("Role Mapping Expression Failed...ignoring", e);
            }
        }

        return sourceRoles;
    }

    public Collection<BaseRole> join(Collection<BaseRole> selectedRoles) {

        if (joinExpression != null) {
            Collection<ScriptingRuleParameter> joinParams = new ArrayList<ScriptingRuleParameter>();

            joinParams.add(new ScriptingRuleParameterImpl("selectedRoles", selectedRoles, Collection.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(joinExpression, language, joinParams);
                return (Collection<BaseRole>) outcome.getObject();
            } catch (Exception e) {
                logger.error("Role Mapping Expression Failed...ignoring", e);
            }

        }

        return null;
    }

    public Collection<BaseRole> transform(Collection<BaseRole> jointRole) {

        if (transformExpression != null) {

            Collection<ScriptingRuleParameter> transformParams = new ArrayList<ScriptingRuleParameter>();

            transformParams.add(new ScriptingRuleParameterImpl("jointRole", jointRole, BaseRole.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(transformExpression, language, transformParams);
                return (Collection<BaseRole>) outcome.getObject();
            } catch (Exception e) {
                logger.error("Role Mapping Rule Failed...ignoring", e);
            }
        }


        return null;
    }

    public void validate(Collection<BaseRole> transformedRoles) {

        if (validateExpression != null) {
            Collection<ScriptingRuleParameter> validateParams = new ArrayList<ScriptingRuleParameter>();

            validateParams.add(new ScriptingRuleParameterImpl("transformedRoles", transformedRoles, BaseRole.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(validateExpression, language, validateParams);
                Boolean error = (Boolean) outcome.getObject();

                if (error) {
                    throw new IllegalArgumentException("Error validating mapped role entry");
                }

            } catch (Exception e) {
                logger.error("Role Mapping Rule Failed...ignoring", e);
            }
        }
    }
}
