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

public enum EqualitySearchOperatorType implements ISearchOperatorType {
    EQUALITY(Messages.EqualitySearchOperatorType_equals, true),
    INEQUALITY(Messages.EqualitySearchOperatorType_notEquals, false);

    private final String label;
    private final boolean equality;

    private EqualitySearchOperatorType(String label, boolean equality) {
        this.label = label;
        this.equality = equality;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int getArgumentCount() {
        return 1;
    }

    public boolean isEquality() {
        return equality;
    }

    @Override
    public ISearchOperator createSearchOperator(IOperandProvider operandProvider,
            ValueDatatype valueDatatype,
            String argument) {
        return new EqualitySearchOperator(valueDatatype, this, operandProvider, argument);
    }
}
