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
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductPartsContainer;

/**
 * Abstract implementation of the {@link ISearchOperator}
 * 
 * @author dicker
 */
public abstract class AbstractSearchOperator<S extends ISearchOperatorType> implements ISearchOperator {

    private final S searchOperatorType;
    private final String argument;
    private final ValueDatatype valueDatatype;
    private final IOperandProvider operandProvider;

    public AbstractSearchOperator(ValueDatatype valueDatatype, S searchOperatorType, IOperandProvider operandProvider,
            String argument) {
        this.valueDatatype = valueDatatype;
        this.searchOperatorType = searchOperatorType;
        this.argument = argument;
        this.operandProvider = operandProvider;
    }

    @Override
    public final boolean check(IProductPartsContainer productPartsContainer) {
        Object searchOperand = operandProvider.getSearchOperand(productPartsContainer);
        if (searchOperand != null) {
            return check(searchOperand, productPartsContainer);
        } else {
            return false;
        }
    }

    /**
     * returns true, if the given {@link IProductCmpt} or {@link IProductCmptGeneration} is a hit
     * regarding the given searchOperand
     */
    protected abstract boolean check(Object searchOperand, IProductPartsContainer productPartsContainer);

    public S getSearchOperatorType() {
        return searchOperatorType;
    }

    public String getArgument() {
        return argument;
    }

    public ValueDatatype getValueDatatype() {
        return valueDatatype;
    }

}
