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
 * The ComparableSearchOperator checks, if the operand and the argument compare to each other
 * accourding to the given {@link ComparableSearchOperatorType}.
 * 
 * @author dicker
 */
public class ComparableSearchOperator extends AbstractStringSearchOperator<ComparableSearchOperatorType> {

    protected ComparableSearchOperator(ValueDatatype valueDatatype, ComparableSearchOperatorType searchOperatorType,
            IOperandProvider iOperandProvider, String argument) {
        super(valueDatatype, searchOperatorType, iOperandProvider, argument);
    }

    @Override
    boolean checkInternal(String operand) {
        if (operand == null || getArgument() == null) {
            return false;
        }

        int compare = getValueDatatype().compare(operand, getArgument());

        if (getSearchOperatorType().isEqualityAllowed() && compare == 0) {
            return true;
        }

        boolean ascendingOrder = getSearchOperatorType().isAscendingOrder();

        return ascendingOrder == compare < 0;
    }
}
