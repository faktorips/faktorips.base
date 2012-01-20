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

/**
 * The ComparableSearchOperator checks, if the operand and the argument compare to each other
 * accourding to the given {@link ComparableSearchOperatorType}.
 * 
 * @author dicker
 */
public class ComparableSearchOperator extends AbstractStringSearchOperator<ComparableSearchOperatorType> {

    protected ComparableSearchOperator(ValueDatatype valueDatatype, ComparableSearchOperatorType searchOperatorType,
            IOperandProvider iOperandProvider, String argument) {
        super(valueDatatype, searchOperatorType, iOperandProvider, argument);
    }

    @Override
    boolean checkInternal(String operand) {
        if (operand == null || getArgument() == null) {
            return false;
        }

        int compare = getValueDatatype().compare(operand, getArgument());

        if (getSearchOperatorType().isEqualityAllowed() && compare == 0) {
            return true;
        }

        boolean ascendingOrder = getSearchOperatorType().isAscendingOrder();

        return ascendingOrder == compare < 0;
    }
}