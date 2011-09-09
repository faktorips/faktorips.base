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

package org.faktorips.devtools.core.ui.search.product.conditions;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;

public abstract class AbstractSearchOperator<S extends ISearchOperatorType> implements ISearchOperator {

    protected final S searchOperatorType;
    protected final String argument;
    protected final ValueDatatype valueDatatype;
    protected final IOperandProvider operandProvider;

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

    protected abstract boolean check(Object searchOperand, IProductCmptGeneration productCmptGeneration);

    protected S getSearchOperatorType() {
        return searchOperatorType;
    }

    @Override
    public String getLabel() {
        return getSearchOperatorType().getLabel();
    }

    protected String getArgument() {
        return argument;
    }

    public ValueDatatype getValueDatatype() {
        return valueDatatype;
    }

}