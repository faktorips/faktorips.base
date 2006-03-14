/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.fl.operations;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Operation for the addition of two decimals. 
 */
public class AddIntegerDecimal extends AbstractBinaryOperation {

    public AddIntegerDecimal() {
        super("+", Datatype.INTEGER, Datatype.DECIMAL); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl, org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs,
            CompilationResultImpl rhs) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append('(');
        fragment.append(lhs.getCodeFragment());
        fragment.append("==null?null:"); //$NON-NLS-1$
        fragment.append(rhs.getCodeFragment());
        fragment.append(".add("); //$NON-NLS-1$
        fragment.append(lhs.getCodeFragment());
        fragment.append("))"); //$NON-NLS-1$
        return new CompilationResultImpl(fragment, Datatype.DECIMAL);
    }

}
