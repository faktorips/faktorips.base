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
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.runtime.Message;
import org.faktorips.util.ArgumentCheck;

/**
 * A function that counts the instances of the path
 * 
 * @author frank
 * @since 3.11.0
 */
public class Count extends AbstractFlFunction {

    public static final String ERROR_MESSAGE_CODE = ExprCompiler.PREFIX + "COUNT"; //$NON-NLS-1$

    /**
     * @param name the function name
     */
    public Count(String name, String description) {
        super(name, description, FunctionSignatures.Count);
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 1);
        Datatype argType = argResults[0].getDatatype();
        JavaCodeFragment fragment = new JavaCodeFragment();
        if (argType instanceof ListOfTypeDatatype) {
            fragment.append(argResults[0].getCodeFragment());
            fragment.append(".size()"); //$NON-NLS-1$
        } else {
            String text = Messages.INSTANCE.getString(ERROR_MESSAGE_CODE, argType);
            Message msg = Message.newError(ERROR_MESSAGE_CODE, text);
            return new CompilationResultImpl(msg);
        }
        return new CompilationResultImpl(fragment, getType());
    }
}
