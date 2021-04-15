/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import java.util.Objects;

import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.ConversionMatrix;
import org.faktorips.datatype.Datatype;

/**
 * Default implementation of FunctionSignature.
 */
public class FunctionSignatureImpl implements FunctionSignature {

    private String name;
    private Datatype type;
    private Datatype[] argTypes;
    private boolean hasVarArgs;

    /**
     * Creates a new function signature with a defined argument list.
     */
    public FunctionSignatureImpl(String name, FunctionSignatures signature) {
        this.name = name;
        this.type = signature.getType();
        this.argTypes = signature.getArgTypes();
        this.hasVarArgs = signature.hasVarArgs();
    }

    /**
     * Creates a new function signature with a defined argument list.
     */
    public FunctionSignatureImpl(String name, Datatype type, Datatype[] argTypes) {
        this.name = name;
        this.type = type;
        this.argTypes = argTypes;
        this.hasVarArgs = false;
    }

    /**
     * Creates a new function signature with a variable argument list.
     * 
     * @param name the name of this function signature
     * @param type the return type of this function signature
     * @param argType defines the Datatype of the arguments in the variable argument list
     */
    public FunctionSignatureImpl(String name, Datatype type, Datatype argType) {
        this.name = name;
        this.type = type;
        this.argTypes = new Datatype[] { argType };
        this.hasVarArgs = true;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FunctionSignature#getType()
     */
    @Override
    public Datatype getType() {
        return type;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FunctionSignature#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FunctionSignature#getArgTypes()
     */
    @Override
    public Datatype[] getArgTypes() {
        return argTypes;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FunctionSignature#isSame(org.faktorips.fl.FunctionSignature)
     */
    @Override
    public boolean match(String name, Datatype[] otherArgTypes) {
        if (!this.name.equals(name)) {
            return false;
        }
        if (hasVarArgs()) {
            if (!getArgTypes()[0].equals(otherArgTypes[0])) {
                return false;
            }
            return true;
        }
        if (this.argTypes.length != otherArgTypes.length) {
            return false;
        }
        for (int i = 0; i < otherArgTypes.length; i++) {
            if (!matchDatatype(argTypes[i], otherArgTypes[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether the two data types matches or not. The functionDataType parameter is the data
     * type provided by the function signature. It may be of type {@link AnyDatatype}. The
     * expressionDataType is the type parsed from the formula expression. It must be a concrete data
     * type.
     */
    private boolean matchDatatype(Datatype functionDataType, Datatype expressionDataType) {
        if (Objects.equals(functionDataType, expressionDataType)) {
            return true;
        }
        if (expressionDataType instanceof AbstractDatatype) {
            AbstractDatatype abstractDatatype = (AbstractDatatype)expressionDataType;
            return abstractDatatype.matchDatatype(functionDataType);
        }
        return false;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FunctionSignature#matchUsingConversion(java.lang.String,
     *      org.faktorips.datatype.Datatype[], org.faktorips.datatype.ConversionMatrix)
     */
    @Override
    public boolean matchUsingConversion(String name, Datatype[] otherArgTypes, ConversionMatrix matrix) {
        if (!this.name.equals(name)) {
            return false;
        }
        if (hasVarArgs()) {
            for (Datatype otherArgType : otherArgTypes) {
                if (!matrix.canConvert(otherArgType, argTypes[0])) {
                    return false;
                }
            }
            return true;
        }
        if (this.argTypes.length != otherArgTypes.length) {
            return false;
        }
        for (int i = 0; i < otherArgTypes.length; i++) {
            if (!matrix.canConvert(otherArgTypes[i], argTypes[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSame(FunctionSignature fctSignature) {
        // this check is also done in #match(name, datatypes) but for performance issue we do it
        // before getting the argTypes of fctSignature
        if (!this.name.equals(fctSignature.getName())) {
            return false;
        } else if (!match(fctSignature.getName(), fctSignature.getArgTypes())) {
            return false;
        } else {
            return type.equals(fctSignature.getType());
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type.getName());
        builder.append(' ');
        builder.append(name);
        builder.append('(');
        for (int i = 0; i < argTypes.length; i++) {
            if (i > 0) {
                builder.append(", "); //$NON-NLS-1$
            }
            builder.append(argTypes[i].getName());
        }
        builder.append(')');
        return builder.toString();
    }

    @Override
    public boolean hasVarArgs() {
        return hasVarArgs;
    }

}
