/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;

/**
 * Boolean function NOT.
 * 
 * @author Jan Ortmann
 */
public class Not extends AbstractFlFunction {

    public Not(String name, String description) {
        super(name, description, FunctionSignatures.Not);
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        JavaCodeFragment code = new JavaCodeFragment("!(");
        code.append(argResults[0].getCodeFragment());
        code.append(')');
        ((CompilationResultImpl)argResults[0]).setCodeFragment(code);
        return argResults[0];
    }

}
