/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.controller.fields;

import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;

/**
 * This implementation of {@link IValueSource} considers all {@link IValueSet}s of
 * <code>IValueSetType.ENUM</code>. The datatype of the {@link IValueSet} is non specific.
 */
public class EnumValueSetValueSource implements IValueSource {

    private IValueSetOwner owner;

    public EnumValueSetValueSource(IValueSetOwner owner) {
        this.owner = owner;
    }

    @Override
    public List<String> getValues() {
        if (isApplicable()) {
            IValueSet valueSet = owner.getValueSet();
            return ((IEnumValueSet)valueSet).getValuesAsList();
        }
        return Collections.emptyList();

    }

    @Override
    public boolean isApplicable() {
        if (owner != null) {
            IValueSet valueSet = owner.getValueSet();
            return valueSet.isEnum();
        }
        return false;
    }

}
