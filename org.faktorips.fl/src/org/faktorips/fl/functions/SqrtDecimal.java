/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.util.ArgumentCheck;

/**
 * A class that implements the square root of a Decimal.
 * 
 * @author hbaagil
 */

public class SqrtDecimal extends AbstractFlFunction {

    /**
     * Constructs a square root function.
     * 
     * @param name The name of the function.
     * @param description The description of the function.
     */

    public SqrtDecimal(String name, String description) {
        super(name, description, FunctionSignatures.SqrtDecimal);
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 1);
        ConversionCodeGenerator<JavaCodeFragment> conversionCodeGenerator = ConversionCodeGenerator.getDefault();
        JavaCodeFragment fragment = new JavaCodeFragment();
        JavaCodeFragment fragmentResult = new JavaCodeFragment();
        fragment.append("Math.sqrt");
        fragment.append('(');
        fragment.append(conversionCodeGenerator.getConversionCode(Datatype.DECIMAL, Datatype.DOUBLE,
                argResults[0].getCodeFragment()));
        fragment.append(')');
        fragmentResult.append(conversionCodeGenerator.getConversionCode(Datatype.DOUBLE, Datatype.DECIMAL, fragment));

        CompilationResultImpl result = new CompilationResultImpl(fragmentResult, Datatype.DECIMAL);
        result.addMessages(argResults[0].getMessages());
        addIdentifier(argResults[0].getResolvedIdentifiers(), result);
        return result;
    }

    private void addIdentifier(String[] identifiers, CompilationResultImpl compilationResult) {
        for (String identifier : identifiers) {
            compilationResult.addIdentifierUsed(identifier);
        }
    }

}
