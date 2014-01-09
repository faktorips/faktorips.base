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

    private EqualitySearchOperatorType(String label, boolean equality) {
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
