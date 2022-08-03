/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.fl;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.ConversionMatrix;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionSignature;
import org.faktorips.fl.FunctionSignatureImpl;

/**
 * Implementation of {@link FlFunction} to support adaptions to the FlFunction interface. In
 * subclasses must be defined, how this adaption will be done.
 * 
 * @author dicker
 */
public abstract class AbstractFlFunctionAdapter<T extends CodeFragment> implements FlFunction<T> {

    private ExprCompiler<T> compiler;
    private final IIpsProject ipsProject;

    public AbstractFlFunctionAdapter(IIpsProject ipsProject) {
        super();
        this.ipsProject = ipsProject;
    }

    @Override
    public void setCompiler(ExprCompiler<T> compiler) {
        this.compiler = compiler;
    }

    @Override
    public ExprCompiler<T> getCompiler() {
        return compiler;
    }

    @Override
    public void setDescription(String description) {
        throw new RuntimeException("The adpater does not support setDescription()!"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: This method is called very often. If you have a chance to implement it with better
     * performance, for example by avoiding finding datatypes in {@link #getType()} or
     * {@link #getArgTypes()}, overwrite this method and implement it as fast as possible!
     */
    @Override
    public boolean isSame(FunctionSignature fctSignature) {
        if (!getName().equals(fctSignature.getName())) {
            return false;
        } else if (equals(fctSignature)) {
            return true;
        } else if (!getClass().equals(fctSignature.getClass())) {
            return isSameSignatureInDifferentClass(fctSignature);
        } else {
            return false;
        }
    }

    /**
     * if the classes are different there may be a signature map with other types same signature in
     * the same class should be found by equals-Method.
     */
    private boolean isSameSignatureInDifferentClass(FunctionSignature fctSignature) {
        FunctionSignature thisFct = new FunctionSignatureImpl(getName(), getType(), getArgTypes());
        return thisFct.isSame(fctSignature);
    }

    @Override
    public boolean match(String name, Datatype[] argTypes) {
        FunctionSignature thisFct = new FunctionSignatureImpl(getName(), getType(), getArgTypes());
        return thisFct.match(name, argTypes);
    }

    @Override
    public boolean matchUsingConversion(String name, Datatype[] argTypes, ConversionMatrix matrix) {
        FunctionSignature thisFct = new FunctionSignatureImpl(getName(), getType(), getArgTypes());
        return thisFct.matchUsingConversion(name, argTypes, matrix);
    }

    /**
     * Returns false;
     */
    @Override
    public boolean hasVarArgs() {
        return false;
    }

    protected IIpsProject getIpsProject() {
        return ipsProject;
    }
}
