/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.values.Decimal;

/**
 * 
 * @author Jan Ortmann
 */
public class DecimalNullFct extends AbstractFlFunction {

    public DecimalNullFct() {
        super("DECIMALNULL", "", Datatype.DECIMAL, new Datatype[0]);
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        JavaCodeFragment code = new JavaCodeFragment();
        code.appendClassName(Decimal.class);
        code.append(".NULL");
        return new CompilationResultImpl(code, Datatype.DECIMAL);
    }

}
