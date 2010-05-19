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
import org.faktorips.fl.ExprCompiler;

/**
 * Equals operation for none primitive datatypes that are tested for equality with the equals()
 * Method.
 */
public class NotEqualsObjectDatatype extends EqualsObjectDatatype {

    protected static String getErrorMessageCode() {
        return ExprCompiler.PREFIX + "NOTEQUALS-OPERATION"; //$NON-NLS-1$
    }

    public NotEqualsObjectDatatype(Datatype type) {
        super("!=", type, type);
    }

    public NotEqualsObjectDatatype(Datatype lhsDatatype, Datatype rhsDatatype) {
        super("!=", lhsDatatype, rhsDatatype);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        CompilationResultImpl result = super.generate(lhs, rhs);
        CompilationResultImpl newResult = new CompilationResultImpl();
        newResult.getCodeFragment().append('!');
        newResult.getCodeFragment().append(result.getCodeFragment());
        newResult.setDatatype(result.getDatatype());
        return newResult;
    }

}
