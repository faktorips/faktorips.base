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

public abstract class AbstractStringSearchOperator<S extends ISearchOperatorType> extends AbstractSearchOperator<S> {

    protected AbstractStringSearchOperator(ValueDatatype valueDatatype, S searchOperatorType,
            IOperandProvider operandProvider, String argument) {
        super(valueDatatype, searchOperatorType, operandProvider, argument);
    }

    @Override
    protected final boolean check(Object operand, IProductCmptGeneration productCmptGeneration) {
        if (operand instanceof String && getValueDatatype().isParsable((String)operand)) {
            return checkInternal((String)operand);
        }
        return false;
    }

    protected abstract boolean checkInternal(String operand);
}
