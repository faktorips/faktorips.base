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

import java.util.List;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.util.ArgumentCheck;

public class ReferenceSearchOperator extends AbstractSearchOperator<ReferenceSearchOperatorType> {

    public ReferenceSearchOperator(ValueDatatype valueDatatype, ReferenceSearchOperatorType searchOperatorType,
            IOperandProvider operandProvider, String argument) {
        super(valueDatatype, searchOperatorType, operandProvider, argument);
    }

    @Override
    protected boolean check(Object operand, IProductCmptGeneration productCmptGeneration) {
        ArgumentCheck.notNull(operand);
        List<?> operandList = (List<?>)operand;
        if (operandList.isEmpty()) {
            return false;
        }

        // System.out.println(operand.getClass().getName() + " - " + operand.toString() + " - " +
        // getArgument());
        return operandList.contains(argument);
    }

}
