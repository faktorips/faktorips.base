/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
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
public class MultiplyDecimalMoney extends AbstractBinaryOperation {

    public MultiplyDecimalMoney() {
        super("*", Datatype.DECIMAL, Datatype.MONEY); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl,
     *      org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        JavaCodeFragment fragment = rhs.getCodeFragment();
        fragment.append(".multiply("); //$NON-NLS-1$
        rhs.add(lhs);
        fragment.append(", "); //$NON-NLS-1$
        fragment.appendClassName(BigDecimal.class);
        fragment.append(".ROUND_HALF_UP)"); //$NON-NLS-1$
        rhs.setDatatype(Datatype.MONEY);
        return rhs;
    }

}
