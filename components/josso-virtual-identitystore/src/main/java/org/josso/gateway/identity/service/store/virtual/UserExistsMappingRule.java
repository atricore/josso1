package org.josso.gateway.identity.service.store.virtual;

import java.util.Collection;

/**
 * Service Provider Interface (SPI) for realizing components capable of mapping
 * multiple source user exists outcome entries to one virtual user exists outcome entry.
 * <p/>
 * User Exists Mapping Rules support :
 * <p/>
 * a. selecting a set of user exists outcome entries from the entire set of entries provided by the associated sources.
 * b. joining multiple source user exists outcome entries into one
 * c. transforming the joint user exists outcome entry
 * d. validating the virtual user exists outcome entry
 */
public interface UserExistsMappingRule {
    Collection<UserExistsOutcome> select(Collection<UserExistsOutcome> selectedOutcomes);

    UserExistsOutcome join(Collection<UserExistsOutcome> selectedOutcomes);

    UserExistsOutcome transform(UserExistsOutcome jointOutcome);

    void validate(UserExistsOutcome transformedOutcome);
}
