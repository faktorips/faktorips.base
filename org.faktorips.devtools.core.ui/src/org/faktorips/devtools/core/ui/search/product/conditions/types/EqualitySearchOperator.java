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
 * The EqualitySearchOperator checks, if the operand and the argument are equal (or not equal
 * depending on the given {@link EqualitySearchOperatorType}).
 * 
 * @author dicker
 */
public class EqualitySearchOperator extends AbstractStringSearchOperator<EqualitySearchOperatorType> {

    public EqualitySearchOperator(ValueDatatype valueDatatype, EqualitySearchOperatorType searchOperatorType,
            IOperandProvider OperandProvider, String argument) {
        super(valueDatatype, searchOperatorType, OperandProvider, argument);
    }

    @Override
    boolean checkInternal(String operand) {
        return checkEquality(operand) == getSearchOperatorType().isEquality();
    }

    private boolean checkEquality(String operand) {
        if (getArgument() == null) {
            return operand == null;
        }
        return getValueDatatype().areValuesEqual(operand, getArgument());
    }
}
