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
 * Operation for the addition of two ints.
 */
public class SubtractIntInt extends AbstractBinaryOperation {

    public SubtractIntInt() {
        super("-", Datatype.PRIMITIVE_INT, Datatype.PRIMITIVE_INT); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        lhs.getCodeFragment().append(" - "); //$NON-NLS-1$
        lhs.add(rhs);
        return lhs;
    }

}
