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

public class ComparableSearchOperator extends AbstractSearchOperator<ComparableSearchOperatorType> {

    protected ComparableSearchOperator(ValueDatatype valueDatatype, ComparableSearchOperatorType searchOperatorType,
            OperandProvider operandProvider, String argument) {
        super(valueDatatype, searchOperatorType, operandProvider, argument);
    }

    @Override
    public boolean checkInternal(String operand) {
        if (operand == null || getArgument() == null) {
            return false;
        }

        int compare = getValueDatatype().compare(operand, getArgument());

        if (getSearchOperatorType().isEqualityAllowed() && compare == 0) {
            return true;
        }

        boolean lowerAllowed = getSearchOperatorType().isLowerAllowed();
        if (lowerAllowed && compare < 0) {
            return true;
        }
        if (!lowerAllowed && compare > 0) {
            return true;
        }
        return false;
    }
}