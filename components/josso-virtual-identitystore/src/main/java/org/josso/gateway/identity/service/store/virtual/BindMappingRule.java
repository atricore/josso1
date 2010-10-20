package org.josso.gateway.identity.service.store.virtual;

import java.util.Collection;

/**
 * Service Provider Interface (SPI) for realizing components capable of mapping
 * multiple source bind outcome entries to one virtual bind outcome entry.
 * <p/>
 * Bind Mapping Rules support :
 * <p/>
 * a. selecting a set of bind outcome entries from the entire set of entries provided by the associated sources.
 * b. joining multiple source bind outcome entries into one
 * c. transforming the joint bind outcome entry
 * d. validating the virtual bind outcome entry
 */
public interface BindMappingRule {
    Collection<BindOutcome> select(Collection<BindOutcome> selectedOutcomes);

    BindOutcome join(Collection<BindOutcome> selectedOutcomes);

    BindOutcome transform(BindOutcome jointOutcome);

    void validate(BindOutcome transformedOutcome);
}
