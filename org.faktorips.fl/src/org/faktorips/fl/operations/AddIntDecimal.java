/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;

/**
 * Operation for the addition of two decimals.
 */
public class AddIntDecimal extends AbstractBinaryOperation {

    public AddIntDecimal() {
        super("+", Datatype.PRIMITIVE_INT, Datatype.DECIMAL); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl,
     *      org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        rhs.getCodeFragment().append(".add("); //$NON-NLS-1$
        rhs.add(lhs);
        rhs.getCodeFragment().append(')');
        return rhs;
    }

}
