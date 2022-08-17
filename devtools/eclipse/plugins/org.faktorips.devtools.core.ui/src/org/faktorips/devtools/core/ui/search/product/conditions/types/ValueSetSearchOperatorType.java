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
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * This Enumeration contains {@link ISearchOperatorType ISearchOperatorTypes} to check with
 * {@link IValueSet IValueSet} within the Product Search.
 * 
 * @author dicker
 */
public enum ValueSetSearchOperatorType implements ISearchOperatorType {
    ALLOWED(Messages.AllowanceSearchOperatorType_allowed, false),
    NOT_ALLOWED(Messages.AllowanceSearchOperatorType_notAllowed, true);

    private final String label;
    private final boolean negation;

    ValueSetSearchOperatorType(String label, boolean negation) {
        this.label = label;
        this.negation = negation;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public ISearchOperator createSearchOperator(IOperandProvider operandProvider,
            ValueDatatype valueDatatype,
            String argument) {
        return new ValueSetSearchOperator(valueDatatype, this, operandProvider, argument);
    }

    boolean isNegation() {
        return negation;
    }

}
