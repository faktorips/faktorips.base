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

import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.runtime.Message;
import org.faktorips.util.ArgumentCheck;

/**
 * This class implements the if statement.
 */
public class AbstractIf extends AbstractFlFunction {

    public static final String ERROR_MESSAGE_CODE = ExprCompiler.PREFIX + "IF"; //$NON-NLS-1$

    public AbstractIf(String name, String description, FunctionSignatures signature) {
        super(name, description, signature);
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 3);

        ConversionCodeGenerator<JavaCodeFragment> ccg = getCompiler().getConversionCodeGenerator();
        Datatype datatype1 = argResults[1].getDatatype();
        Datatype datatype2 = argResults[2].getDatatype();

        // check if the 2. and 3. argument have either the same datatype or can be converted
        if (!datatype1.equals(datatype2)) {
            if (ccg.canConvert(datatype1, datatype2)) {
                JavaCodeFragment converted = ccg.getConversionCode(datatype1, datatype2,
                        argResults[1].getCodeFragment());
                CompilationResultImpl newResult = new CompilationResultImpl(converted, datatype2);
                newResult.addMessages(argResults[1].getMessages());
                argResults[1] = newResult;
            } else if (ccg.canConvert(datatype2, datatype1)) {
                JavaCodeFragment converted = ccg.getConversionCode(datatype2, datatype1,
                        argResults[2].getCodeFragment());
                CompilationResultImpl newResult = new CompilationResultImpl(converted, datatype1);
                newResult.addMessages(argResults[2].getMessages());
                argResults[2] = newResult;
            } else {
                String text = Messages.INSTANCE.getString(ERROR_MESSAGE_CODE, datatype1, datatype2);
                Message msg = Message.newError(ERROR_MESSAGE_CODE, text);
                return new CompilationResultImpl(msg);
            }
        }

        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("(");
        fragment.append(argResults[0].getCodeFragment());
        fragment.append('?');
        fragment.append(argResults[1].getCodeFragment());
        fragment.append(':');
        fragment.append(argResults[2].getCodeFragment());
        fragment.append(")");

        CompilationResultImpl result = new CompilationResultImpl(fragment, argResults[1].getDatatype());
        result.addMessages(argResults[0].getMessages());
        result.addMessages(argResults[1].getMessages());
        result.addMessages(argResults[2].getMessages());
        return result;
    }

}
