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
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;

public class SumList extends AbstractListFunction {

    public SumList(String name, String description) {
        super(name, description, FunctionSignatures.SumList);
    }

    @Override
    protected JavaCodeFragment generateReturnFallBackValueCall() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("return ");
        fragment.append(getCompiler().getDatatypeHelper(getDatatype()).newInstance("0"));
        return fragment;
    }

    @Override
    protected JavaCodeFragment generateFunctionCall(CompilationResultImpl argument1, CompilationResultImpl argument2) {
        return getCompiler().getBinaryOperation("+", argument1, argument2).getCodeFragment();
    }
}
