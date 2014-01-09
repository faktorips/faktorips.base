/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.valueset.IValueSet;

/**
 * Abstract implementation of {@link IConditionType} for searching attribute values.
 * 
 * @author dicker
 */
public abstract class AbstractAttributeConditionType extends AbstractConditionType {

    @Override
    public ValueDatatype getValueDatatype(IIpsElement elementPart) {
        IAttribute attribute = (IAttribute)elementPart;
        try {
            return attribute.findDatatype(attribute.getIpsProject());
        } catch (CoreException e) {
            // TODO Exception Handling
            throw new RuntimeException(e);
        }
    }

    @Override
    public IValueSet getValueSet(IIpsElement elementPart) {
        IAttribute attribute = (IAttribute)elementPart;

        try {
            return attribute.getValueSet();
        } catch (CoreException e) {
            // TODO Exception Handling
            throw new RuntimeException(e);
        }
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