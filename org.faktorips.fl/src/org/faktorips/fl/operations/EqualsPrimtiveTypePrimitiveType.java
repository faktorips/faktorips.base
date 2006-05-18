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

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.util.ArgumentCheck;


/**
 * Operation for the equality check for two decimals. 
 */
public class EqualsPrimtiveTypePrimitiveType extends AbstractBinaryOperation {

    public EqualsPrimtiveTypePrimitiveType(Datatype primitiveType) {
        super("=", primitiveType, primitiveType); //$NON-NLS-1$
        ArgumentCheck.isTrue(primitiveType.isPrimitive());
    }

    /** 
     * {@inheritDoc}
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs,
            CompilationResultImpl rhs) {
        lhs.getCodeFragment().append("=="); //$NON-NLS-1$
        lhs.add(rhs);
        lhs.setDatatype(Datatype.PRIMITIVE_BOOLEAN);
        return lhs;
    }

}
