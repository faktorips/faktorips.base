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

import org.faktorips.datatype.ValueDatatype;

/**
 * The EqualitySearchOperator checks, if the operand and the argument are equal (or not equal
 * depending on the given {@link EqualitySearchOperatorType}).
 * 
 * @author dicker
 */
public class EqualitySearchOperator extends AbstractStringSearchOperator<EqualitySearchOperatorType> {

    public EqualitySearchOperator(ValueDatatype valueDatatype, EqualitySearchOperatorType searchOperatorType,
            IOperandProvider OperandProvider, String argument) {
        super(valueDatatype, searchOperatorType, OperandProvider, argument);
    }

    @Override
    boolean checkInternal(String operand) {
        return checkEquality(operand) == getSearchOperatorType().isEquality();
    }

    private boolean checkEquality(String operand) {
        if (getArgument() == null) {
            return operand == null;
        }
        return getValueDatatype().areValuesEqual(operand, getArgument());
    }
}
