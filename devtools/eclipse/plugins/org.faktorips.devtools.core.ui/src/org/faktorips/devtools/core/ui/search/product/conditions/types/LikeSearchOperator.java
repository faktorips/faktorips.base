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
import org.faktorips.devtools.core.ui.search.matcher.WildcardMatcher;

/**
 * The LikeSearchOperator checks, if the operand matches or matches not the argument as a wildcard
 * expression.
 * <p>
 * This class uses the {@link WildcardMatcher}.
 * 
 * @author dicker
 */
public class LikeSearchOperator extends AbstractStringSearchOperator<LikeSearchOperatorType> {

    private final WildcardMatcher matcher;

    public LikeSearchOperator(ValueDatatype valueDatatype, LikeSearchOperatorType searchOperatorType,
            IOperandProvider operandProvider, String argument) {
        super(valueDatatype, searchOperatorType, operandProvider, argument);
        matcher = new WildcardMatcher(argument);
    }

    @Override
    boolean checkInternal(String searchOperand) {
        return matcher.isMatching(searchOperand) == getSearchOperatorType().isNegation();
    }

}
