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

public enum AllowanceSearchOperatorType implements ISearchOperatorType {
    ALLOWED(Messages.AllowanceSearchOperatorType_allowed, false),
    NOT_ALLOWED(Messages.AllowanceSearchOperatorType_notAllowed, true);

    private final String label;
    private final boolean negation;

    private AllowanceSearchOperatorType(String label, boolean negation) {
        this.label = label;
        this.negation = negation;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int getArgumentCount() {
        return 1;
    }

    @Override
    public ISearchOperator createSearchOperator(IOperandProvider operandProvider,
            ValueDatatype valueDatatype,
            String argument) {
        return new AllowanceSearchOperator(valueDatatype, this, operandProvider, argument);
    }

    public boolean isNegation() {
        return negation;
    }

}
