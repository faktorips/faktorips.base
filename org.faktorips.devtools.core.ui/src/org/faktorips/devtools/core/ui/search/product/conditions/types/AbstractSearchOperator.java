/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;

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
    public final boolean check(IProductCmptGeneration productCmptGeneration) {
        return check(operandProvider.getSearchOperand(productCmptGeneration), productCmptGeneration);
    }

    /**
     * returns true, if the given {@link IProductCmptGeneration} is a hit regarding the given
     * searchOperand
     */
    protected abstract boolean check(Object searchOperand, IProductCmptGeneration productCmptGeneration);

    S getSearchOperatorType() {
        return searchOperatorType;
    }

    String getArgument() {
        return argument;
    }

    ValueDatatype getValueDatatype() {
        return valueDatatype;
    }

}