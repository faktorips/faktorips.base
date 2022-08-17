/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

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
