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
 * This Enumeration contains {@link ISearchOperatorType ISearchOperatorTypes} to check
 * {@link Comparable Comparables} within the Product Search.
 * 
 * @author dicker
 */
public enum ComparableSearchOperatorType implements ISearchOperatorType {
    LESS(Messages.ComparableSearchOperatorType_labelLess, false, true),
    LESS_OR_EQUALS(Messages.ComparableSearchOperatorType_labelLessOrEqual, true, true),
    GREATER(Messages.ComparableSearchOperatorType_labelGreater, false, false),
    GREATER_OR_EQUALS(Messages.ComparableSearchOperatorType_labelGreaterOrEqual, true, false);

    private final String label;
    private final boolean equalityAllowed;
    private final boolean ascendingOrder;

    private ComparableSearchOperatorType(String label, boolean equality, boolean ascendingOrder) {
        this.label = label;
        this.equalityAllowed = equality;
        this.ascendingOrder = ascendingOrder;
    }

    @Override
    public String getLabel() {
        return label;
    }

    boolean isEqualityAllowed() {
        return equalityAllowed;
    }

    boolean isAscendingOrder() {
        return ascendingOrder;
    }

    @Override
    public ISearchOperator createSearchOperator(IOperandProvider operandProvider,
            ValueDatatype valueDatatype,
            String argument) {
        return new ComparableSearchOperator(valueDatatype, this, operandProvider, argument);
    }
}
