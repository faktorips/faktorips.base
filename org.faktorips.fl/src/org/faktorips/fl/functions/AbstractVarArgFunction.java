/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl.functions;

import java.util.ArrayList;

import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.util.message.Message;

/**
 * {@link AbstractBaseVarArgFunction} for {@link JavaCodeFragment Java code} generating functions.
 */
// Should be renamed to AbstractJavaVarArgFunction, but that might break the API
public abstract class AbstractVarArgFunction extends AbstractBaseVarArgFunction<JavaCodeFragment> {

    /**
     * Creates a new AbstractJavaVarArgFunction.
     * 
     * @see AbstractFlFunction the super class constructor parameter descripton for more details.
     */
    public AbstractVarArgFunction(String name, String description, Datatype type, Datatype argType) {
        super(name, description, type, argType);
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {

        ConversionCodeGenerator<JavaCodeFragment> ccg = compiler.getConversionCodeGenerator();
        ArrayList<CompilationResultImpl> convertedResults = new ArrayList<CompilationResultImpl>(argResults.length);
        Datatype expectedArgType = getExpectedDatatypeForArgResultConversion(argResults);

        for (int i = 0; i < argResults.length; i++) {

            CompilationResultImpl newResult = (CompilationResultImpl)argResults[i];
            Datatype argDatatype = argResults[i].getDatatype();

            if (!expectedArgType.equals(argDatatype)) {

                if (!ccg.canConvert(argDatatype, expectedArgType)) {
                    String text = Messages.INSTANCE.getString(ERROR_MESSAGE_CODE, new Object[] { expectedArgType,
                            new Integer(i), argDatatype });
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

        for (int i = 0; i < convertedResults.size(); i++) {
            CompilationResultImpl compilationResult = convertedResults.get(i);
            returnValue.addMessages(compilationResult.getMessages());
        }

        return returnValue;
    }
}
