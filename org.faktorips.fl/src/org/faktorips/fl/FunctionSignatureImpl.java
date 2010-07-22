/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.fl;

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
    public Datatype getType() {
        return type;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FunctionSignature#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FunctionSignature#getArgTypes()
     */
    public Datatype[] getArgTypes() {
        return argTypes;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FunctionSignature#isSame(org.faktorips.fl.FunctionSignature)
     */
    public boolean match(String name, Datatype[] otherArgTypes) {
        if (!this.name.equals(name)) {
            return false;
        }
        if (hasVarArgs()) {
            for (int i = 0; i < otherArgTypes.length; i++) {
                if (!getArgTypes()[0].equals(otherArgTypes)) {
                    return false;
                }
            }
            return true;
        }
        if (this.argTypes.length != otherArgTypes.length) {
            return false;
        }
        for (int i = 0; i < otherArgTypes.length; i++) {
            if (!argTypes[i].equals(otherArgTypes[i]) && !(argTypes[i] instanceof AnyDatatype)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FunctionSignature#matchUsingConversion(java.lang.String,
     *      org.faktorips.datatype.Datatype[], org.faktorips.datatype.ConversionMatrix)
     */
    public boolean matchUsingConversion(String name, Datatype[] otherArgTypes, ConversionMatrix matrix) {
        if (!this.name.equals(name)) {
            return false;
        }
        if (hasVarArgs()) {
            for (int i = 0; i < otherArgTypes.length; i++) {
                if (!matrix.canConvert(otherArgTypes[i], argTypes[0])) {
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

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FunctionSignature#isSame(org.faktorips.fl.FunctionSignature)
     */
    public boolean isSame(FunctionSignature fctSignature) {
        if (!type.equals(fctSignature.getType())) {
            return false;
        }
        return match(fctSignature.getName(), fctSignature.getArgTypes());
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(type.getName());
        buffer.append(' ');
        buffer.append(name);
        buffer.append('(');
        for (int i = 0; i < argTypes.length; i++) {
            if (i > 0) {
                buffer.append(", "); //$NON-NLS-1$
            }
            buffer.append(argTypes[i].getName());
        }
        buffer.append(')');
        return buffer.toString();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasVarArgs() {
        return hasVarArgs;
    }

}
