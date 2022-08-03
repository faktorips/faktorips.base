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
 * This Enumeration contains {@link ISearchOperatorType ISearchOperatorTypes} to check equality
 * within the Product Search.
 * 
 * @author dicker
 */
public enum EqualitySearchOperatorType implements ISearchOperatorType {
    EQUALITY(Messages.EqualitySearchOperatorType_equals, true),
    INEQUALITY(Messages.EqualitySearchOperatorType_notEquals, false);

    private final String label;
    private final boolean equality;

    EqualitySearchOperatorType(String label, boolean equality) {
        this.label = label;
        this.equality = equality;
    }

    @Override
    public String getLabel() {
        return label;
    }

    boolean isEquality() {
        return equality;
    }

    @Override
    public ISearchOperator createSearchOperator(IOperandProvider operandProvider,
            ValueDatatype valueDatatype,
            String argument) {
        return new EqualitySearchOperator(valueDatatype, this, operandProvider, argument);
    }
}
