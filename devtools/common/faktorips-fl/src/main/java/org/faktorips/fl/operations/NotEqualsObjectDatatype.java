/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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

    public NotEqualsObjectDatatype(Datatype type) {
        super("!=", type, type);
    }

    public NotEqualsObjectDatatype(Datatype lhsDatatype, Datatype rhsDatatype) {
        super("!=", lhsDatatype, rhsDatatype);
    }

    @Override
    protected String getErrorMessageCode() {
        return ExprCompiler.PREFIX + "NOTEQUALS-OPERATION"; //$NON-NLS-1$
    }

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
