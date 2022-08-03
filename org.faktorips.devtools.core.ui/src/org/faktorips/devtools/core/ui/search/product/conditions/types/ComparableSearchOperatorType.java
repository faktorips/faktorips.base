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
 * This Enumeration contains {@link ISearchOperatorType ISearchOperatorTypes} to check
 * {@link Comparable Comparables} within the Product Search.
 * 
 * @author dicker
 */
public enum ComparableSearchOperatorType implements ISearchOperatorType {
    LESS(Messages.ComparableSearchOperatorType_labelLess, false, true),
    LESS_OR_EQUALS(Messages.ComparableSearchOperatorType_labelLessOrEqual, true, true),
    GREATER(Messages.ComparableSearchOperatorType_labelGreater, false, false),
    GREATER_OR_EQUALS(Messages.ComparableSearchOperatorType_labelGreaterOrEqual, true, false);

    private final String label;
    private final boolean equalityAllowed;
    private final boolean ascendingOrder;

    ComparableSearchOperatorType(String label, boolean equality, boolean ascendingOrder) {
        this.label = label;
        equalityAllowed = equality;
        this.ascendingOrder = ascendingOrder;
    }

    @Override
    public String getLabel() {
        return label;
    }

    boolean isEqualityAllowed() {
        return equalityAllowed;
    }

    boolean isAscendingOrder() {
        return ascendingOrder;
    }

    @Override
    public ISearchOperator createSearchOperator(IOperandProvider operandProvider,
            ValueDatatype valueDatatype,
            String argument) {
        return new ComparableSearchOperator(valueDatatype, this, operandProvider, argument);
    }
}
