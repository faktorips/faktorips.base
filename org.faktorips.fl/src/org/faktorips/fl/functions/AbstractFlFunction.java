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
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.fl.JavaExprCompiler;

/**
 * Abstract {@link JavaCodeFragment Java} implementation of {@link FlFunction}.
 */
// Should be renamed to AbstractJavaFlFunction, but that might break the API
public abstract class AbstractFlFunction extends AbstractBaseFlFunction<JavaCodeFragment> {

    /**
     * Creates a new function with a defined argument list.
     */
    public AbstractFlFunction(String name, String description, FunctionSignatures signature) {
        super(name, description, signature);
    }

    /**
     * Creates a new function with a defined argument list.
     */
    public AbstractFlFunction(String name, String description, Datatype type, Datatype[] argTypes) {
        super(name, description, type, argTypes);
    }

    /**
     * Creates a new function signature with a variable argument list.
     * 
     * @param name the name of this function
     * @param description a description of this function
     * @param type the return type of this function
     * @param argType defines the Datatype of the arguments in the variable argument list
     */
    public AbstractFlFunction(String name, String description, Datatype type, Datatype argType) {
        super(name, description, type, argType);
    }

    public void setCompiler(JavaExprCompiler compiler) {
        super.setCompiler(compiler);
    }

    /**
     * Overridden Method.
     * 
     * @see org.faktorips.fl.FlFunction#getCompiler()
     */
    @Override
    public JavaExprCompiler getCompiler() {
        return (JavaExprCompiler)super.getCompiler();
    }

    public DatatypeHelper getDatatypeHelper(Datatype datatype) {
        return getCompiler().getDatatypeHelper(datatype);
    }

    public String getJavaClassName(Datatype datatype) {
        return getDatatypeHelper(datatype).getJavaClassName();
    }

}
