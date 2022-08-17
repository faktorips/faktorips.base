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
 * This Enumeration contains {@link ISearchOperatorType ISearchOperatorTypes} to check with
 * wildcards within the Product Search.
 * 
 * @author dicker
 */
public enum LikeSearchOperatorType implements ISearchOperatorType {
    LIKE(Messages.LikeSearchOperatorType_labelLike, true),
    NOT_LIKE(Messages.LikeSearchOperatorType_labelDoesNotLike, false);

    private final String label;
    private final boolean negation;

    LikeSearchOperatorType(String label, boolean negation) {
        this.label = label;
        this.negation = negation;
    }

    @Override
    public String getLabel() {
        return label;
    }

    boolean isNegation() {
        return negation;
    }

    @Override
    public ISearchOperator createSearchOperator(IOperandProvider operandProvider,
            ValueDatatype valueDatatype,
            String argument) {

        // if argument is null, then just null as an allowed hit, which is checked by the
        // EqualitySearchOperator.
        if (argument == null) {
            return new EqualitySearchOperator(valueDatatype, EqualitySearchOperatorType.EQUALITY, operandProvider,
                    null);
        }

        return new LikeSearchOperator(valueDatatype, this, operandProvider, argument);
    }
}
