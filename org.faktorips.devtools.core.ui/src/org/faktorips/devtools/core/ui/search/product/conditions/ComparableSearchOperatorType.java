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

public enum ComparableSearchOperatorType implements ISearchOperatorType {
    LOWER_THAN("<", false, true), //$NON-NLS-1$
    LOWER_THAN_OR_EQUALS("<=", true, true), //$NON-NLS-1$
    GREATER_THAN(">", false, false), //$NON-NLS-1$
    GREATER_THAN_OR_EQUALS(">=", true, false); //$NON-NLS-1$

    private final String label;
    private final int argumentCount;
    private final boolean equalityAllowed;
    private final boolean lowerAllowed;

    private ComparableSearchOperatorType(String label, int argumentCount, boolean equality, boolean lower) {
        this.label = label;
        this.argumentCount = argumentCount;
        this.equalityAllowed = equality;
        this.lowerAllowed = lower;
    }

    private ComparableSearchOperatorType(String label, boolean equality, boolean lower) {
        this(label, 1, equality, lower);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int getArgumentCount() {
        return argumentCount;
    }

    public boolean isEqualityAllowed() {
        return equalityAllowed;
    }

    public boolean isLowerAllowed() {
        return lowerAllowed;
    }

    @Override
    public ISearchOperator createSearchOperator(IOperandProvider iOperandProvider,
            ValueDatatype valueDatatype,
            String argument) {
        return new ComparableSearchOperator(valueDatatype, this, iOperandProvider, argument);
    }
}
