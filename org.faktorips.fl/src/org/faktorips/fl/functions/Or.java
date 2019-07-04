/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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
import org.faktorips.fl.FunctionSignatures;

/**
 * A function that provides a boolean or-operation and has the following signature <i>boolean
 * OR(boolean...)</i>.
 */
public class Or extends AbstractVarArgFunction {

    public Or(String name, String description) {
        super(name, description, FunctionSignatures.Or);
    }

    @Override
    protected void compileInternal(CompilationResult<JavaCodeFragment> returnValue,
            CompilationResult<JavaCodeFragment>[] convertedArgs,
            JavaCodeFragment fragment) {
        fragment.append('(');
        for (int i = 0; i < convertedArgs.length; i++) {
            fragment.append(convertedArgs[i].getCodeFragment());

            if (i < convertedArgs.length - 1) {
                fragment.append("||"); //$NON-NLS-1$
            }
        }
        fragment.append(')');
    }

}
