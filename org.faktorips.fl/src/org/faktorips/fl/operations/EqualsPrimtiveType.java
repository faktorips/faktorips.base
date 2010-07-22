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

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.util.ArgumentCheck;

/**
 * Operation for the equality check for two decimals.
 */
public class EqualsPrimtiveType extends AbstractBinaryOperation {

    public EqualsPrimtiveType(Datatype primitiveType) {
        super("=", primitiveType, primitiveType); //$NON-NLS-1$
        ArgumentCheck.isTrue(primitiveType.isPrimitive());
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        lhs.getCodeFragment().append("=="); //$NON-NLS-1$
        lhs.add(rhs);
        lhs.setDatatype(Datatype.PRIMITIVE_BOOLEAN);
        return lhs;
    }

}
