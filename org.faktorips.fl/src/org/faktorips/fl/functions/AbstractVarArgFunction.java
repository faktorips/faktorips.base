/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;

/**
 * An extension of the AbstractFlFunction that provides base functionality for variable argument
 * functions.
 */
public abstract class AbstractVarArgFunction extends AbstractFlFunction {

    public final static String ERROR_MESSAGE_CODE = ExprCompiler.PREFIX + "VARARG"; //$NON-NLS-1$

    /**
     * Creates a new AbstractVarArgFunction. See the super class constructor parameter descripton
     * for more details.
     */
    public AbstractVarArgFunction(String name, String description, Datatype type, Datatype argType) {
        super(name, description, type, argType);
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult compile(CompilationResult[] argResults) {

        ConversionCodeGenerator ccg = compiler.getConversionCodeGenerator();
        ArrayList<CompilationResultImpl> convertedResults = new ArrayList<CompilationResultImpl>(argResults.length);

        for (int i = 0; i < argResults.length; i++) {

            CompilationResultImpl newResult = (CompilationResultImpl)argResults[i];
            Datatype argDatatype = argResults[i].getDatatype();
            Datatype expectedArgType = getArgTypes()[0];

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
        compileInternal(returnValue, convertedResults.toArray(new CompilationResult[convertedResults.size()]), fragment);

        for (int i = 0; i < convertedResults.size(); i++) {
            CompilationResultImpl compilationResult = convertedResults.get(i);
            returnValue.addMessages(compilationResult.getMessages());
        }

        return returnValue;
    }

    /**
     * The actual compile logic for this function has to be implemented within this method. The
     * called provides the CompilationResult that will be returned by this function, an array of
     * already converted arguments and a JavaCodeFragment where the code that is to generate needs
     * to be written to. The compilation result is provided to this method to write error messages
     * to that may occure during the code generation or to get status information. Implementations
     * don't need to care about shoveling messages from the argument CompilationResult object to the
     * returned CompilationResult object. This is already be handled by the caller.
     */
    protected abstract void compileInternal(CompilationResult returnValue,
            CompilationResult[] convertedArgs,
            JavaCodeFragment fragment);
}
