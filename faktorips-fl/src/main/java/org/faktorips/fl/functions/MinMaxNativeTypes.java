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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.runtime.Message;
import org.faktorips.util.ArgumentCheck;

/**
 *
 */
public abstract class MinMaxNativeTypes extends AbstractFlFunction {

    private String functionName = null;
    private String errorCodeSuffix = null;
    private Datatype functionDatatype = null;

    public MinMaxNativeTypes(String name, String description, Datatype datatype, boolean isMax) {
        super(name, description, datatype, new Datatype[] { datatype, datatype });
        ArgumentCheck.notNull(datatype);
        functionDatatype = datatype;
        functionName = isMax ? "max" : "min";
        errorCodeSuffix = isMax ? "MAX" : "MIN";
    }

    protected String getFunctionName() {
        return functionName;
    }

    @Override
    public boolean match(String name, Datatype[] otherArgTypes) {
        if (!this.getName().equals(name)) {
            return false;
        }
        Datatype[] args = getArgTypes();
        if (args.length != otherArgTypes.length) {
            return false;
        }
        for (int i = 0; i < otherArgTypes.length; i++) {
            if (args[i].equals(otherArgTypes[i])) {
                continue;
            }
            ValueDatatype argType = (ValueDatatype)args[i];
            if (argType.isPrimitive()) {
                if (argType.getWrapperType().equals(otherArgTypes[i])) {
                    continue;
                }
            } else if (otherArgTypes[i] instanceof ValueDatatype) {
                ValueDatatype other = (ValueDatatype)otherArgTypes[i];
                if (argType.equals(other.getWrapperType())) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 2);

        ConversionCodeGenerator<JavaCodeFragment> ccg = getCompiler().getConversionCodeGenerator();
        Datatype datatype1 = argResults[0].getDatatype();
        Datatype datatype2 = argResults[1].getDatatype();

        CompilationResult<JavaCodeFragment> first = convertIfNecessay(datatype1, ccg, argResults[0]);
        if (first == null) {
            return createErrorCompilationResult(datatype1);
        }
        CompilationResult<JavaCodeFragment> second = convertIfNecessay(datatype2, ccg, argResults[1]);
        if (second == null) {
            return createErrorCompilationResult(datatype2);
        }

        // Math.max(value1, value2)
        JavaCodeFragment fragment = new JavaCodeFragment();
        writeBody(fragment, first, second);

        CompilationResultImpl result = new CompilationResultImpl(fragment, functionDatatype);
        result.addMessages(argResults[0].getMessages());
        result.addMessages(argResults[1].getMessages());
        return result;
    }

    protected abstract void writeBody(JavaCodeFragment fragment,
            CompilationResult<JavaCodeFragment> first,
            CompilationResult<JavaCodeFragment> second);

    private CompilationResult<JavaCodeFragment> createErrorCompilationResult(Datatype datatype) {
        String code = ExprCompiler.PREFIX + errorCodeSuffix;
        String text = Messages.INSTANCE.getString(code, datatype);
        Message msg = Message.newError(code, text);
        return new CompilationResultImpl(msg);

    }

    private CompilationResult<JavaCodeFragment> convertIfNecessay(Datatype datatype,
            ConversionCodeGenerator<JavaCodeFragment> ccg,
            CompilationResult<JavaCodeFragment> argResult) {
        if (!functionDatatype.equals(datatype)) {
            if (ccg.canConvert(datatype, functionDatatype)) {
                JavaCodeFragment converted = ccg.getConversionCode(datatype, functionDatatype,
                        argResult.getCodeFragment());
                CompilationResultImpl newResult = new CompilationResultImpl(converted, functionDatatype);
                newResult.addMessages(argResult.getMessages());
                return newResult;
            }
            return null;
        }
        return argResult;
    }

}
