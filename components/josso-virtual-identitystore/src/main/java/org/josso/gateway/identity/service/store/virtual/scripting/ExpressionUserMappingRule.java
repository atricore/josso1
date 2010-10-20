package org.josso.gateway.identity.service.store.virtual.scripting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.store.virtual.UserMappingRule;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: ExpressionUserMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="user-mapping-expression"
 */
public class ExpressionUserMappingRule extends BaseExpressionMappingRule implements UserMappingRule {
    private static final Log logger = LogFactory.getLog(ExpressionUserMappingRule.class);

    public Collection<BaseUser> select(Collection<BaseUser> sourceUsers) {

        if (selectExpression != null) {

            Collection<ScriptingRuleParameter> selectParams = new ArrayList<ScriptingRuleParameter>();

            selectParams.add(new ScriptingRuleParameterImpl("sourceUsers", sourceUsers, Collection.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(selectExpression, language, selectParams);
                return (Collection<BaseUser>) outcome.getObject();
            } catch (Exception e) {
                logger.error("User Mapping Expression Failed...ignoring", e);
            }

        }

        return sourceUsers;
    }

    public BaseUser join(Collection<BaseUser> selectedUsers) {

        if (joinExpression != null) {
            Collection<ScriptingRuleParameter> joinParams = new ArrayList<ScriptingRuleParameter>();

            joinParams.add(new ScriptingRuleParameterImpl("selectedUsers", selectedUsers, Collection.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(joinExpression, language, joinParams);
                return (BaseUser) outcome.getObject();
            } catch (Exception e) {
                logger.error("User Mapping Expression Failed...ignoring", e);
            }

        }

        return null;
    }

    public BaseUser transform(BaseUser jointUser) {

        if (transformExpression != null) {
            Collection<ScriptingRuleParameter> transformParams = new ArrayList<ScriptingRuleParameter>();
            transformParams.add(new ScriptingRuleParameterImpl("jointUser", jointUser, BaseUser.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(transformExpression, language, transformParams);
                return (BaseUser) outcome.getObject();
            } catch (Exception e) {
                logger.error("User Mapping Expression Failed...ignoring", e);
            }
        }

        return null;
    }

    public void validate(BaseUser transformedUser) {

        if (validateExpression != null) {
            Collection<ScriptingRuleParameter> validateParams = new ArrayList<ScriptingRuleParameter>();
            validateParams.add(new ScriptingRuleParameterImpl("transformedUser", transformedUser, BaseUser.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(validateExpression, language, validateParams);
                Boolean error = (Boolean) outcome.getObject();

                if (error) {
                    throw new IllegalArgumentException("Error validating mapped user entry");
                }

            } catch (Exception e) {
                logger.error("User Mapping Expression Failed...ignoring", e);
            }
        }
    }

}