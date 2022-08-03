/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.faktorips.runtime.util.StringBuilderJoiner;
import org.faktorips.util.ArgumentCheck;

/**
 * Class to store the method definition as it is generated. A method consists of the method's name
 * and optional an array of parameters.
 * <p>
 * This class could store a method definition and is able to give the methods definition string for
 * example <em>getFooFor(String bar)</em> as well as the list of parameter type signatures as it is
 * needed by a JDT model.
 * 
 * @see #getDefinition()
 * @see #getTypeSignatures()
 * 
 * @author dirmeier
 */
public class MethodDefinition implements IGeneratedJavaElement {

    private final String name;

    private final MethodParameter[] parameters;

    /**
     * Create the method definition with the methods name and any optional parameters
     * 
     * @param name The methods name
     * @param parameters The parameters if you need any
     */
    public MethodDefinition(String name, MethodParameter... parameters) {
        ArgumentCheck.notNull(name);
        ArgumentCheck.notNull(parameters);
        this.name = name;
        this.parameters = parameters;
    }

    @Override
    public IJavaElement getJavaElement(IType javaType) {
        return javaType.getMethod(getName(), getTypeSignatures());
    }

    /**
     * Returns the name of the method.
     * 
     * @return The method's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the list of parameters, may be empty but never null.
     * 
     * @return The list of the method's parameters
     */
    public List<MethodParameter> getParameters() {
        return Arrays.asList(parameters);
    }

    /**
     * Returns an array of type signatures for the parameters. The type signature is created by
     * using {@link Signature}.
     * 
     * @see MethodParameter#getTypeSignature()
     * @see Signature#createTypeSignature(String, boolean)
     * 
     * @return The array of type signatures, one entry for every parameter
     */
    public String[] getTypeSignatures() {
        String[] result = new String[parameters.length];
        int i = 0;
        for (MethodParameter parameter : parameters) {
            result[i] = parameter.getTypeSignature();
            i++;
        }
        return result;
    }

    /**
     * Returns the method definition as it is used in generated java code. This method definition
     * does not include the return type as far it is not part of the signature.
     * 
     * @return The methods definition, for example <em>getFooFor(String bar)</em>
     */
    public String getDefinition() {
        StringBuilder result = new StringBuilder(name);
        result.append("(");
        StringBuilderJoiner.join(result, parameters, p -> result.append(p.getDefinition()));
        result.append(")");
        return result.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Arrays.hashCode(parameters));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        MethodDefinition other = (MethodDefinition)obj;
        return Objects.equals(name, other.name)
                && Arrays.equals(parameters, other.parameters);
    }

    @Override
    public String toString() {
        return "MethodDefinition " + getDefinition();
    }

}
