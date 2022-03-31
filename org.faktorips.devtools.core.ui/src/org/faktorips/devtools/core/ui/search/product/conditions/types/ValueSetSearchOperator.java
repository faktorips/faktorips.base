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

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.productcmpt.IProductPartsContainer;
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * The ValueSetSearchOperator checks, if the argument is or is not contained in the operand, which
 * is in this case an {@link IValueSet}.
 * <p>
 * This Operator is used for searching the IValueSets for policy attributes.
 * 
 * @author dicker
 */
public class ValueSetSearchOperator extends AbstractSearchOperator<ValueSetSearchOperatorType> {

    protected ValueSetSearchOperator(ValueDatatype valueDatatype, ValueSetSearchOperatorType searchOperatorType,
            IOperandProvider operandProvider, String argument) {
        super(valueDatatype, searchOperatorType, operandProvider, argument);
    }

    @Override
    protected boolean check(Object searchOperand, IProductPartsContainer productPartsContainer) {
        IValueSet valueSet = (IValueSet)searchOperand;
        boolean isContained = valueSet.containsValue(getArgument(), productPartsContainer.getIpsProject());

        return isContained != getSearchOperatorType().isNegation();
    }
}
