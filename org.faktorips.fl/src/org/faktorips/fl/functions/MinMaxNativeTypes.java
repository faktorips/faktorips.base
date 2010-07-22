/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;

/**
 *
 */
public abstract class MinMaxNativeTypes extends AbstractFlFunction {

    protected String functionName = null;
    private String errorCodeSuffix = null;
    private Datatype functionDatatype = null;

    public MinMaxNativeTypes(String name, String description, Datatype datatype, boolean isMax) {
        super(name, description, datatype, new Datatype[] { datatype, datatype });
        ArgumentCheck.notNull(datatype);
        functionDatatype = datatype;
        functionName = isMax ? "max" : "min";
        errorCodeSuffix = isMax ? "MAX" : "MIN";
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        ArgumentCheck.length(argResults, 2);

        ConversionCodeGenerator ccg = compiler.getConversionCodeGenerator();
        Datatype datatype1 = argResults[0].getDatatype();
        Datatype datatype2 = argResults[1].getDatatype();

        CompilationResult first = convertIfNecessay(datatype1, ccg, argResults[0]);
        if (first == null) {
            return createErrorCompilationResult(datatype1);
        }
        CompilationResult second = convertIfNecessay(datatype2, ccg, argResults[1]);
        if (second == null) {
            return createErrorCompilationResult(datatype2);
        }

        // Math.max(value1, value2)
        JavaCodeFragment fragment = new JavaCodeFragment();
        writeBody(fragment, first, second);

        CompilationResultImpl result = new CompilationResultImpl(fragment, functionDatatype);
        result.addMessages(argResults[0].getMessages());
        result.addMessages(argResults[1].getMessages());
        addIdentifier(argResults[0].getResolvedIdentifiers(), result);
        addIdentifier(argResults[1].getResolvedIdentifiers(), result);
        return result;
    }

    protected abstract void writeBody(JavaCodeFragment fragment, CompilationResult first, CompilationResult second);

    private CompilationResult createErrorCompilationResult(Datatype datatype) {
        String code = ExprCompiler.PREFIX + errorCodeSuffix;
        String text = Messages.INSTANCE.getString(code, new Object[] { datatype });
        Message msg = Message.newError(code, text);
        return new CompilationResultImpl(msg);

    }

    private CompilationResult convertIfNecessay(Datatype datatype,
            ConversionCodeGenerator ccg,
            CompilationResult argResult) {
        if (!functionDatatype.equals(datatype)) {
            if (ccg.canConvert(datatype, functionDatatype)) {
                JavaCodeFragment converted = ccg.getConversionCode(datatype, functionDatatype, argResult
                        .getCodeFragment());
                CompilationResultImpl newResult = new CompilationResultImpl(converted, functionDatatype);
                newResult.addMessages(argResult.getMessages());
                return newResult;
            }
            return null;
        }
        return argResult;
    }

    private void addIdentifier(String[] identifiers, CompilationResultImpl compilationResult) {
        for (String identifier : identifiers) {
            compilationResult.addIdentifierUsed(identifier);
        }
    }
}
