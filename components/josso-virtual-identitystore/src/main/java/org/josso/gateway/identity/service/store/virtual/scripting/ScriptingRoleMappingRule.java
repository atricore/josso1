package org.josso.gateway.identity.service.store.virtual.scripting;

import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.store.virtual.RoleMappingRule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @org.apache.xbean.XBean element="role-mapping-script"
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: ScriptingRoleMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class ScriptingRoleMappingRule extends BaseScriptingMappingRule implements RoleMappingRule {
    private static final Log logger = LogFactory.getLog(ScriptingRoleMappingRule.class);

    public Collection<BaseRole> select(Collection<BaseRole> sourceRoles) {

        if (selectRule != null) {
            Collection<ScriptingRuleParameter> selectParams = new ArrayList<ScriptingRuleParameter>();

            selectParams.add(new ScriptingRuleParameterImpl( "sourceRoles", sourceRoles, Collection.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(selectRule, selectParams);
                return (Collection<BaseRole>)outcome.getObject();
            } catch (Exception e) {
                logger.error("Role Mapping Rule Failed...ignoring", e);
            }
        }

        return sourceRoles;
    }

    public Collection<BaseRole> join(Collection<BaseRole> selectedRoles) {

        if (joinRule != null) {
            Collection<ScriptingRuleParameter> joinParams = new ArrayList<ScriptingRuleParameter>();

            joinParams.add(new ScriptingRuleParameterImpl( "selectedRoles", selectedRoles, Collection.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(joinRule, joinParams);
                return (Collection<BaseRole>)outcome.getObject();
            } catch (Exception e) {
                logger.error("Role Mapping Rule Failed...ignoring", e);
            }

        }

        return null;
    }

    public Collection<BaseRole> transform(Collection<BaseRole> jointRole) {

        if (transformRule != null) {

            Collection<ScriptingRuleParameter> transformParams = new ArrayList<ScriptingRuleParameter>();

            transformParams.add(new ScriptingRuleParameterImpl( "jointRole", jointRole, BaseRole.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(transformRule, transformParams);
                return (Collection<BaseRole>)outcome.getObject();
            } catch (Exception e) {
                logger.error("Role Mapping Rule Failed...ignoring", e);
            }
        }


        return null;
    }

    public void validate(Collection<BaseRole> transformedRoles) {

        if (validateRule != null) {
            Collection<ScriptingRuleParameter> validateParams = new ArrayList<ScriptingRuleParameter>();

            validateParams.add(new ScriptingRuleParameterImpl( "transformedRoles", transformedRoles, BaseRole.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(validateRule, validateParams);
                Boolean error = (Boolean)outcome.getObject();

                if (error) {
                    throw new IllegalArgumentException("Error validating mapped role entry");
                }

            } catch (Exception e) {
                logger.error("Role Mapping Rule Failed...ignoring", e);
            }
        }
    }

}
