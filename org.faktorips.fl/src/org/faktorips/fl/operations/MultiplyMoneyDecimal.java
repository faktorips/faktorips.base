/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.math.BigDecimal;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;

/**
 * Operation for the multiplication of two decimals.
 */
public class MultiplyMoneyDecimal extends AbstractBinaryOperation {

    public MultiplyMoneyDecimal() {
        super("*", Datatype.MONEY, Datatype.DECIMAL); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl,
     *      org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        JavaCodeFragment fragment = lhs.getCodeFragment();
        fragment.append(".multiply("); //$NON-NLS-1$
        lhs.add(rhs);
        fragment.append(", "); //$NON-NLS-1$
        fragment.appendClassName(BigDecimal.class);
        fragment.append(".ROUND_HALF_UP)"); //$NON-NLS-1$
        lhs.setDatatype(Datatype.MONEY);
        return lhs;
    }

}
