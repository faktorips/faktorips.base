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
 * Boolean function NOT for the wrapper type.
 * 
 * @author Jan Ortmann
 */
public class NotBoolean extends AbstractFlFunction {

    public NotBoolean(String name, String description) {
        super(name, description, FunctionSignatures.NotBoolean);
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        CompilationResultImpl result = (CompilationResultImpl)argResults[0];
        JavaCodeFragment code = result.getCodeFragment();
        JavaCodeFragment newCode = new JavaCodeFragment();
        newCode.append("((");
        newCode.append(code);
        newCode.append(")==null ? (Boolean)null : ");
        newCode.append("Boolean.valueOf(!(" + code + ").booleanValue())");
        newCode.append(')');
        result.setCodeFragment(newCode);
        return result;
    }

}
