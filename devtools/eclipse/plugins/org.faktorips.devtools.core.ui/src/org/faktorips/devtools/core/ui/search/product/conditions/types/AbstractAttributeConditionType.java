/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import java.util.Collection;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * Abstract implementation of {@link IConditionType} for searching attribute values.
 * 
 * @author dicker
 */
public abstract class AbstractAttributeConditionType extends AbstractConditionType {

    @Override
    public ValueDatatype getValueDatatype(IIpsElement elementPart) {
        IAttribute attribute = (IAttribute)elementPart;
        return attribute.findDatatype(attribute.getIpsProject());
    }

    @Override
    public IValueSet getValueSet(IIpsElement elementPart) {
        IAttribute attribute = (IAttribute)elementPart;
        return attribute.getValueSet();
    }

    @Override
    public Collection<?> getAllowedValues(IIpsElement elementPart) {
        throw new IllegalStateException("This Condition doesn't allow calling getAllowedValues"); //$NON-NLS-1$
    }

    @Override
    public boolean hasValueSet() {
        return true;
    }

    @Override
    public boolean isArgumentIpsObject() {
        return false;
    }

}
