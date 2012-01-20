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
 * This Enumeration contains {@link ISearchOperatorType ISearchOperatorTypes} to check with
 * wildcards within the Product Search.
 * 
 * @author dicker
 */
public enum LikeSearchOperatorType implements ISearchOperatorType {
    LIKE(Messages.LikeSearchOperatorType_labelLike, true),
    NOT_LIKE(Messages.LikeSearchOperatorType_labelDoesNotLike, false);

    private final String label;
    private final boolean negation;

    private LikeSearchOperatorType(String label, boolean negation) {
        this.label = label;
        this.negation = negation;
    }

    @Override
    public String getLabel() {
        return label;
    }

    boolean isNegation() {
        return negation;
    }

    @Override
    public ISearchOperator createSearchOperator(IOperandProvider operandProvider,
            ValueDatatype valueDatatype,
            String argument) {

        // if argument is null, then just null as an allowed hit, which is checked by the
        // EqualitySearchOperator.
        if (argument == null) {
            return new EqualitySearchOperator(valueDatatype, EqualitySearchOperatorType.EQUALITY, operandProvider, null);
        }

        return new LikeSearchOperator(valueDatatype, this, operandProvider, argument);
    }
}
