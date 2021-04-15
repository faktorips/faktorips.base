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

import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionSignatureImpl;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract default implementation of {@link FlFunction}.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
// Should be renamed to AbstractFlFunction, but that might break the API because old code relies
// on that class handling JavaCodeFragments
public abstract class AbstractBaseFlFunction<T extends CodeFragment> extends FunctionSignatureImpl implements
        FlFunction<T> {

    private ExprCompiler<T> compiler;
    private String description;

    /**
     * Creates a new function with a defined argument list.
     */
    public AbstractBaseFlFunction(String name, String description, FunctionSignatures signature) {
        super(name, signature);
        this.description = description;
    }

    /**
     * Creates a new function with a defined argument list.
     */
    public AbstractBaseFlFunction(String name, String description, Datatype type, Datatype[] argTypes) {
        super(name, type, argTypes);
        this.description = description;
    }

    /**
     * Creates a new function signature with a variable argument list.
     * 
     * @param name the name of this function
     * @param description a description of this function
     * @param type the return type of this function
     * @param argType defines the Datatype of the arguments in the variable argument list
     */
    public AbstractBaseFlFunction(String name, String description, Datatype type, Datatype argType) {
        super(name, type, argType);
        this.description = description;
    }

    @Override
    public void setCompiler(ExprCompiler<T> compiler) {
        ArgumentCheck.notNull(compiler);
        this.compiler = compiler;
    }

    /**
     * Overridden Method.
     * 
     * @see org.faktorips.fl.FlFunction#getCompiler()
     */
    @Override
    public ExprCompiler<T> getCompiler() {
        return compiler;
    }

    /**
     * Overridden Method.
     * 
     * @see org.faktorips.fl.FlFunction#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Overridden Method.
     * 
     * @see org.faktorips.fl.FlFunction#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }
}
