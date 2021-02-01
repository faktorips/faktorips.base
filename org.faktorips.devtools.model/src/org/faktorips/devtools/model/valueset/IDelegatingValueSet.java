package org.faktorips.devtools.model.valueset;

/**
 * An {@link IDelegatingValueSet} is a value set implementation that delegates every reading call to
 * another value set. Depending on the delegate target it acts as {@link IEnumValueSet},
 * {@link IRangeValueSet}, {@link IUnrestrictedValueSet} or {@link IDerivedValueSet}.
 * 
 * Every writing call leads to an {@link IllegalStateException}
 */
public interface IDelegatingValueSet
        extends IEnumValueSet, IRangeValueSet, IUnrestrictedValueSet, IDerivedValueSet {
    // marker interface uniting all value set types

}