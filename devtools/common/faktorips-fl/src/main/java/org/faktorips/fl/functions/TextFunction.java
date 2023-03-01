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

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;

/**
 * A function that converts the input to a String.
 */
public class TextFunction extends AbstractFlFunction {

    public TextFunction(String name, String description) {
        super(name, description, FunctionSignatures.TextFunction);
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 1);
        JavaCodeFragment argumentFragment = argResults[0].getCodeFragment();
        DatatypeHelper datatypeHelper = getCompiler().getDatatypeHelper(argResults[0].getDatatype());
        JavaCodeFragment fragment;
        if (datatypeHelper != null) {
            fragment = getToStringFragment(argumentFragment, datatypeHelper);
        } else {
            fragment = getFallbackToStringFragment(argumentFragment);
        }
        return new CompilationResultImpl(fragment, Datatype.STRING);
    }

    private JavaCodeFragment getToStringFragment(JavaCodeFragment argumentFragment, DatatypeHelper datatypeHelper) {
        JavaCodeFragment fragment = new JavaCodeFragment(IpsStringUtils.EMPTY, argumentFragment.getImportDeclaration());
        fragment.append("(").append(datatypeHelper.getToStringExpression(argumentFragment.getSourcecode())).append(")");
        return fragment;
    }

    private JavaCodeFragment getFallbackToStringFragment(JavaCodeFragment argumentFragment) {
        JavaCodeFragment fragment;
        fragment = new JavaCodeFragment();
        fragment.appendClassName(String.class).append(".valueOf(").append(argumentFragment).append(")");
        return fragment;
    }

}
