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

import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.Operation;

/**
 * Operation for the addition of two ints.
 */
public class AddIntInt extends AbstractBinaryJavaOperation {

    public AddIntInt() {
        super(Operation.AddIntInt);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.operations.AbstractBinaryJavaOperation#generate(org.faktorips.fl.CompilationResultImpl,
     *      org.faktorips.fl.CompilationResultImpl)
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        lhs.getCodeFragment().append(" + "); //$NON-NLS-1$
        lhs.add(rhs);
        return lhs;
    }

}
