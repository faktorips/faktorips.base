/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;

/**
 *
 */
public class If extends AbstractFlFunction {

    public final static String ERROR_MESSAGE_CODE = ExprCompiler.PREFIX + "IF"; //$NON-NLS-1$

    public If(String name, String description) {
        super(name, description, AnyDatatype.INSTANCE, new Datatype[] { Datatype.PRIMITIVE_BOOLEAN,
                AnyDatatype.INSTANCE, AnyDatatype.INSTANCE });
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        ArgumentCheck.length(argResults, 3);

        ConversionCodeGenerator ccg = compiler.getConversionCodeGenerator();
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
                String text = Messages.INSTANCE.getString(ERROR_MESSAGE_CODE, new Object[] { datatype1, datatype2 });
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
        addIdentifier(argResults[0].getResolvedIdentifiers(), result);
        addIdentifier(argResults[1].getResolvedIdentifiers(), result);
        addIdentifier(argResults[2].getResolvedIdentifiers(), result);
        return result;
    }

    private void addIdentifier(String[] identifiers, CompilationResultImpl compilationResult) {
        for (String identifier : identifiers) {
            compilationResult.addIdentifierUsed(identifier);
        }
    }
}
