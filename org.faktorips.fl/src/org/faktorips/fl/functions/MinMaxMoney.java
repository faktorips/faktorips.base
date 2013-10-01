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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.util.ArgumentCheck;

/**
 *
 */
public class MinMaxMoney extends AbstractFlFunction {

    private String functionName = null;

    public MinMaxMoney(String name, String description, boolean isMax) {
        super(name, description, isMax ? FunctionSignatures.MaxMoney : FunctionSignatures.MinMoney);
        functionName = isMax ? "max" : "min";
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 2);
        // value1.max(value2)
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append(argResults[0].getCodeFragment());
        fragment.append('.');
        fragment.append(functionName);
        fragment.append('(');
        fragment.append(argResults[1].getCodeFragment());
        fragment.append(')');

        CompilationResultImpl result = new CompilationResultImpl(fragment, Datatype.MONEY);
        result.addMessages(argResults[0].getMessages());
        result.addMessages(argResults[1].getMessages());
        return result;
    }

}
