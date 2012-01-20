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
 * This Enumeration contains {@link ISearchOperatorType ISearchOperatorTypes} to check references
 * within the Product Search.
 * 
 * @author dicker
 */
public enum ReferenceSearchOperatorType implements ISearchOperatorType {
    REFERENCE(Messages.ReferenceSearchOperatorType_labelReferences, false),
    NO_REFERENCE(Messages.ReferenceSearchOperatorType_labelDoesNotReference, true);

    private final String label;
    private final boolean negation;

    private ReferenceSearchOperatorType(String label, boolean negation) {
        this.label = label;
        this.negation = negation;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public ISearchOperator createSearchOperator(IOperandProvider operandProvider,
            ValueDatatype valueDatatype,
            String argument) {
        return new ReferenceSearchOperator(valueDatatype, this, operandProvider, argument);
    }

    boolean isNegation() {
        return negation;
    }
}
