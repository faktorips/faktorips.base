/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.Signature;

/**
 * This class represents one parameter in a method's definition.
 * 
 * @see MethodDefinition
 * 
 * @author dirmeier
 */
public class MethodParameter {

    private static final String KEYWORD_FINAL = "final";

    private final String type;

    private final String paramName;

    private final boolean isFinalFlag;

    /**
     * Create the parameter with the type and the parameter name. It does not matter if the type is
     * qualified or not as far the import statement is already present.
     * <p>
     * The parameter has not the keyword 'final'. Use constructor #MethodParameter(String, String,
     * boolean) with true if the parameter needs the keyword 'final'.
     * 
     * @param type The parameter type, e.g. <em>String</em> or <em>ProductComponent</em>
     * @param paramName The name of the parameter
     */
    public MethodParameter(String type, String paramName) {
        this(type, paramName, false);
    }

    /**
     * Create the parameter with the type and the parameter name. It does not matter if the type is
     * qualified or not as far the import statement is already present.
     * <p>
     * If isFinalFlag is <strong>true</strong> then the parameter gets the preceding keyword
     * 'final'.
     * 
     * @param type The parameter type, e.g. <em>String</em> or <em>ProductComponent</em>
     * @param paramName The name of the parameter
     * @param isFinalFlag indicates if the parameter is final
     */
    public MethodParameter(String type, String paramName, boolean isFinalFlag) {
        this.type = type;
        this.paramName = paramName;
        this.isFinalFlag = isFinalFlag;
    }

    /**
     * Returns the type of the parameter as it is was set in constructor.
     * 
     * @return Returns the name of the parameter's type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the parameter's name
     * 
     * @return The name of the parameter.
     */
    public String getName() {
        return paramName;
    }

    /**
     * Returns the type signature according to JDT type signature definition.
     * 
     * @see Signature
     * 
     * @return The type signature as used by JDT.
     */
    public String getTypeSignature() {
        return Signature.createTypeSignature(type, false);
    }

    /**
     * Returns the parameter definition as it is used in a method definition. For example for the
     * parameter type <em>Foo</em> and the name <em>bar</em> this method would return <em>Foo
     * bar</em>
     * 
     * @return The definition of the parameter for use in method definitions.
     */
    public String getDefinition() {
        if (isFinalFlag) {
            return KEYWORD_FINAL + " " + type + " " + paramName;
        } else {
            return type + " " + paramName;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramName, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        MethodParameter other = (MethodParameter)obj;
        return Objects.equals(paramName, other.paramName)
                && Objects.equals(type, other.type);
    }

    @Override
    public String toString() {
        return "MethodParameter " + getDefinition();
    }

    public static final MethodParameter[] arrayOf(String... parameterTypesAndNames) {
        if (parameterTypesAndNames.length % 2 == 0) {
            List<MethodParameter> methodParameters = new ArrayList<>();
            for (int i = 0; i < parameterTypesAndNames.length; i = i + 2) {
                methodParameters.add(new MethodParameter(parameterTypesAndNames[i], parameterTypesAndNames[i + 1]));
            }
            return methodParameters.toArray(new MethodParameter[methodParameters.size()]);
        } else {
            throw new IllegalArgumentException(
                    "Invalid number of parameters. The number of parameters has to be even.");
        }
    }

}
