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
 * <em>MultiValueAttributes</em> within the Product Search.
 */
public enum ContainsSearchOperatorType implements ISearchOperatorType {
    CONTAINS(Messages.ContainsSearchOperatorType__labelContains);

    private final String label;

    private ContainsSearchOperatorType(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public ISearchOperator createSearchOperator(IOperandProvider operandProvider,
            ValueDatatype valueDatatype,
            String argument) {
        return new ContainsSearchOperator(valueDatatype, this, operandProvider, argument);
    }

}
