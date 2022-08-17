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

import java.util.ArrayList;

import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.runtime.Message;

/**
 * {@link AbstractBaseVarArgFunction} for {@link JavaCodeFragment Java code} generating functions.
 */
// Should be renamed to AbstractJavaVarArgFunction, but that might break the API
public abstract class AbstractVarArgFunction extends AbstractBaseVarArgFunction<JavaCodeFragment> {

    /**
     * Creates a new AbstractVarArgFunction.
     */
    public AbstractVarArgFunction(String name, String description, FunctionSignatures signature) {
        super(name, description, signature);
    }

    /**
     * Creates a new AbstractVarArgFunction.
     * 
     * @see AbstractBaseFlFunction the super class constructor parameter descripton for more
     *          details.
     */
    public AbstractVarArgFunction(String name, String description, Datatype type, Datatype argType) {
        super(name, description, type, argType);
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {

        ConversionCodeGenerator<JavaCodeFragment> ccg = getCompiler().getConversionCodeGenerator();
        ArrayList<CompilationResultImpl> convertedResults = new ArrayList<>(argResults.length);
        Datatype expectedArgType = getExpectedDatatypeForArgResultConversion(argResults);

        for (int i = 0; i < argResults.length; i++) {

            CompilationResultImpl newResult = (CompilationResultImpl)argResults[i];
            Datatype argDatatype = argResults[i].getDatatype();

            if (!expectedArgType.equals(argDatatype)) {

                if (!ccg.canConvert(argDatatype, expectedArgType)) {
                    String text = Messages.INSTANCE.getString(ERROR_MESSAGE_CODE, expectedArgType, Integer.valueOf(i),
                            argDatatype);
                    Message msg = Message.newError(ERROR_MESSAGE_CODE, text);
                    return new CompilationResultImpl(msg);
                }
                JavaCodeFragment converted = ccg.getConversionCode(argDatatype, expectedArgType,
                        argResults[i].getCodeFragment());
                newResult = new CompilationResultImpl(converted, expectedArgType);
                // TODO pk CompilationResult needs addMessages!?
                newResult.addMessages(argResults[i].getMessages());
            }
            convertedResults.add(newResult);
        }

        JavaCodeFragment fragment = new JavaCodeFragment();
        CompilationResultImpl returnValue = new CompilationResultImpl(fragment, getType());
        @SuppressWarnings("unchecked")
        CompilationResult<JavaCodeFragment>[] compilationResults = new CompilationResult[convertedResults.size()];
        compileInternal(returnValue, convertedResults.toArray(compilationResults), fragment);

        for (CompilationResultImpl compilationResult : convertedResults) {
            returnValue.addMessages(compilationResult.getMessages());
        }

        return returnValue;
    }
}
