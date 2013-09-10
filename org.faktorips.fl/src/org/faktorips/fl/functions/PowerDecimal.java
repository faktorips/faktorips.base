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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.util.ArgumentCheck;

/**
 * A class that implements the power of a Decimal.
 * 
 * @author hbaagil
 */
public class PowerDecimal extends AbstractFlFunction {

    /**
     * Constructs the to the power of function.
     * 
     * @param name The name of the function.
     * @param description The description of the function.
     */
    public PowerDecimal(String name, String description) {
        super(name, description, FunctionSignatures.PowerDecimal);
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 2);
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("Decimal.valueOf(");
        fragment.append("Math.pow");
        fragment.append('(');
        fragment.append(argResults[0].getCodeFragment());
        fragment.append('.');
        fragment.append("doubleValue()");
        fragment.append(',');
        fragment.append(argResults[1].getCodeFragment());
        fragment.append('.');
        fragment.append("doubleValue()");
        fragment.append(')');
        fragment.append(')');

        CompilationResultImpl result = new CompilationResultImpl(fragment, Datatype.DECIMAL);
        result.addMessages(argResults[0].getMessages());
        result.addMessages(argResults[1].getMessages());
        addIdentifier(argResults[0].getResolvedIdentifiers(), result);
        addIdentifier(argResults[1].getResolvedIdentifiers(), result);
        return result;
    }

    private void addIdentifier(String[] identifiers, CompilationResultImpl compilationResult) {
        for (String identifier : identifiers) {
            compilationResult.addIdentifierUsed(identifier);
        }
    }

}
