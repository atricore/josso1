package org.josso.gateway.identity.service.store.virtual.scripting;

import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.store.virtual.UserMappingRule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @org.apache.xbean.XBean element="user-mapping-script"

 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: ScriptingUserMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class ScriptingUserMappingRule extends BaseScriptingMappingRule implements UserMappingRule {
    private static final Log logger = LogFactory.getLog(ScriptingUserMappingRule.class);

    public Collection<BaseUser> select(Collection<BaseUser> sourceUsers) {

        if (selectRule != null) {

            Collection<ScriptingRuleParameter> selectParams = new ArrayList<ScriptingRuleParameter>();

            selectParams.add(new ScriptingRuleParameterImpl( "sourceUsers", sourceUsers, Collection.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(selectRule, selectParams);
                return (Collection<BaseUser>)outcome.getObject();
            } catch (Exception e) {
                logger.error("User Mapping Rule Failed...ignoring", e);
            }

        }

        return sourceUsers;
    }

    public BaseUser join(Collection<BaseUser> selectedUsers) {

        if (joinRule != null) {
            Collection<ScriptingRuleParameter> joinParams = new ArrayList<ScriptingRuleParameter>();

            joinParams.add(new ScriptingRuleParameterImpl( "selectedUsers", selectedUsers, Collection.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(joinRule, joinParams);
                return (BaseUser)outcome.getObject();
            } catch (Exception e) {
                logger.error("User Mapping Rule Failed...ignoring", e);
            }

        }

        return null;
    }

    public BaseUser transform(BaseUser jointUser) {

        if (transformRule != null) {
            Collection<ScriptingRuleParameter> transformParams = new ArrayList<ScriptingRuleParameter>();
            transformParams.add(new ScriptingRuleParameterImpl( "jointUser", jointUser, BaseUser.class ));

              ScriptingRuleEngine sre = new ScriptingRuleEngine();
              ScriptingRuleExecutionOutcome outcome;

              try {
                  outcome = sre.execute(transformRule, transformParams);
                  return (BaseUser)outcome.getObject();
              } catch (Exception e) {
                  logger.error("User Mapping Rule Failed...ignoring", e);
              }
        }

        return null;
    }

    public void validate(BaseUser transformedUser) {

        if (validateRule != null) {
            Collection<ScriptingRuleParameter> validateParams = new ArrayList<ScriptingRuleParameter>();
            validateParams.add(new ScriptingRuleParameterImpl( "transformedUser", transformedUser, BaseUser.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(validateRule, validateParams);
                Boolean error = (Boolean)outcome.getObject();

                if (error) {
                    throw new IllegalArgumentException("Error validating mapped user entry");
                }

            } catch (Exception e) {
                logger.error("User Mapping Rule Failed...ignoring", e);
            }
        }
    }

}
