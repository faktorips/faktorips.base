/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields.enumproposal;

import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.core.ui.controller.fields.IValueSource;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;

/**
 * Provides the values of an {@link EnumValueSet}. If the {@link IValueSetOwner} is
 * <code>null</code> or if there is no {@link EnumValueSet} an empty list is returned.
 */
public class EnumValueSetSource implements IValueSource {

    private IValueSetOwner owner;

    public EnumValueSetSource(IValueSetOwner owner) {
        this.owner = owner;
    }

    /**
     * Provides the values of an {@link EnumValueSet}. If the {@link IValueSetOwner} is
     * <code>null</code> or if there is no {@link EnumValueSet} an empty list is returned.
     */
    @Override
    public List<String> getValues() {
        if (isApplicable()) {
            IValueSet valueSet = owner.getValueSet();
            return ((IEnumValueSet)valueSet).getValuesAsList();
        }
        return Collections.emptyList();

    }

    /**
     * Returns <code>true</code> if this value source can provide enum values. That is the case if
     * the value set owner is defined and at the same time the corresponding value set is an enum
     * value set.
     */
    public boolean isApplicable() {
        if (owner != null) {
            IValueSet valueSet = owner.getValueSet();
            return valueSet.canBeUsedAsSupersetForAnotherEnumValueSet();
        }
        return false;
    }

}
