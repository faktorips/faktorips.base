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
import org.faktorips.fl.ExprCompiler;


/**
 * Equals operation for none primitive datatypes that are tested for equality with
 * the equals() Method.
 */
public class NotEqualsObjectDatatype extends EqualsObjectDatatype {

    public final static String ERROR_MESSAGE_CODE = ExprCompiler.PREFIX + "NOTEQUALS-OPERATION"; //$NON-NLS-1$
    
    public NotEqualsObjectDatatype (Datatype type) {
        super("!=", type, type); //$NON-NLS-1$
    }

    public NotEqualsObjectDatatype (Datatype lhsDatatype, Datatype rhsDatatype) {
        super("!=", lhsDatatype, rhsDatatype); //$NON-NLS-1$
    }

    /** 
     * {@inheritDoc}
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs,
            CompilationResultImpl rhs) {
        CompilationResultImpl result = super.generate(lhs, rhs);
        CompilationResultImpl newResult = new CompilationResultImpl();
        newResult.getCodeFragment().append('!');
        newResult.getCodeFragment().append(result.getCodeFragment());
        newResult.setDatatype(result.getDatatype());
        return newResult;
    }

}
