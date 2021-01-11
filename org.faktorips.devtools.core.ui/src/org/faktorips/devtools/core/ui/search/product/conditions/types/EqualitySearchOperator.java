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

/**
 * The EqualitySearchOperator checks, if the operand and the argument are equal (or not equal
 * depending on the given {@link EqualitySearchOperatorType}).
 * 
 * @author dicker
 */
public class EqualitySearchOperator extends AbstractStringSearchOperator<EqualitySearchOperatorType> {

    public EqualitySearchOperator(ValueDatatype valueDatatype, EqualitySearchOperatorType searchOperatorType,
            IOperandProvider operandProvider, String argument) {
        super(valueDatatype, searchOperatorType, operandProvider, argument);
    }

    @Override
    boolean checkInternal(String operand) {
        return checkEquality(operand) == getSearchOperatorType().isEquality();
    }

    private boolean checkEquality(String operand) {
        if (getArgument() == null) {
            return operand == null;
        } else {
            try {
                return getValueDatatype().areValuesEqual(operand, getArgument());
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }
}
