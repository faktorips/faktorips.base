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

import java.util.List;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.productcmpt.IMultiValueHolder;
import org.faktorips.devtools.model.productcmpt.IProductPartsContainer;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;

/**
 * The ContainsSearchOperator checks, if the argument is contained in the operand, which is in this
 * case a <em>MultiValueAttribute</em>.
 */
public class ContainsSearchOperator extends AbstractSearchOperator<ContainsSearchOperatorType> {

    public ContainsSearchOperator(ValueDatatype valueDatatype, ContainsSearchOperatorType containsSearchOperatorType,
            IOperandProvider operandProvider, String argument) {
        super(valueDatatype, containsSearchOperatorType, operandProvider, argument);
    }

    @Override
    protected boolean check(Object searchOperand, IProductPartsContainer productPartsContainer) {
        if (searchOperand instanceof IMultiValueHolder valueHolder) {
            List<ISingleValueHolder> values = valueHolder.getValue();
            for (ISingleValueHolder value : values) {
                if (isArgumentEqualTo(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isArgumentEqualTo(ISingleValueHolder value) {
        if (isNullContained(value)) {
            return true;
        }
        return isValueContained(value);
    }

    private boolean isNullContained(ISingleValueHolder value) {
        return getArgument() == null && value.getValue().getContent() == null;
    }

    private boolean isValueContained(ISingleValueHolder value) {
        return getArgument() != null && checkEquality(value);
    }

    private boolean checkEquality(ISingleValueHolder value) {
        try {
            return getValueDatatype().areValuesEqual(value.getValue().getContentAsString(), getArgument());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
