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
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.util.ArgumentCheck;

/**
 * A class that implements the square root of a Decimal.
 * 
 * @author hbaagil
 * @since 3.11.0
 */

public class SqrtDecimal extends AbstractFlFunction {

    private static final String MATH_SQRT = "Math.sqrt"; //$NON-NLS-1$
    private final ConversionCodeGenerator<JavaCodeFragment> conversionCodeGenerator;

    /**
     * Constructs a square root function.
     * 
     * @param name The name of the function.
     * @param description The description of the function.
     */

    public SqrtDecimal(String name, String description) {
        super(name, description, FunctionSignatures.SqrtDecimal);
        conversionCodeGenerator = ConversionCodeGenerator.getDefault();
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 1);
        JavaCodeFragment fragment = new JavaCodeFragment();
        JavaCodeFragment fragmentResult = new JavaCodeFragment();
        fragment.append(MATH_SQRT);
        fragment.append('(');
        fragment.append(getConversionCodeDecimalDouble(argResults[0].getCodeFragment()));
        fragment.append(')');
        fragmentResult.append(getConversionCodeDoubleDecimal(fragment));

        CompilationResultImpl result = createCompilationResultImpl(fragmentResult);

        result.addMessages(argResults[0].getMessages());
        return result;
    }

    private CompilationResultImpl createCompilationResultImpl(JavaCodeFragment fragmentResult) {
        return new CompilationResultImpl(fragmentResult, Datatype.DECIMAL);
    }

    private JavaCodeFragment getConversionCodeDecimalDouble(JavaCodeFragment fragment) {
        return conversionCodeGenerator.getConversionCode(Datatype.DECIMAL, Datatype.DOUBLE, fragment);
    }

    private JavaCodeFragment getConversionCodeDoubleDecimal(JavaCodeFragment fragment) {
        return conversionCodeGenerator.getConversionCode(Datatype.DOUBLE, Datatype.DECIMAL, fragment);
    }
}
