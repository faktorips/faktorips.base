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
 * This Enumeration contains {@link ISearchOperatorType ISearchOperatorTypes} to check references
 * within the Product Search.
 * 
 * @author dicker
 */
public enum ReferenceSearchOperatorType implements ISearchOperatorType {
    REFERENCE(Messages.ReferenceSearchOperatorType_labelReferences, false),
    NO_REFERENCE(Messages.ReferenceSearchOperatorType_labelDoesNotReference, true);

    private final String label;
    private final boolean negation;

    ReferenceSearchOperatorType(String label, boolean negation) {
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
        return new ReferenceSearchOperator(valueDatatype, this, operandProvider, argument);
    }

    boolean isNegation() {
        return negation;
    }
}
