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

package org.faktorips.devtools.stdbuilder.xpand.model;

import org.eclipse.jdt.core.Signature;

/**
 * This class represents one parameter in a method's definition.
 * 
 * @see MethodDefinition
 * 
 * @author dirmeier
 */
public class MethodParameter {

    private final String type;

    private final String paramName;

    /**
     * Create the parameter with the type and the parameter name. It does not matter if the type is
     * qualified or not as far the import statement is already present.
     * 
     * @param type The parameter type, e.g. <em>String</em> or <em>ProductComponent</em>
     * @param paramName The name of the parameter
     */
    public MethodParameter(String type, String paramName) {
        this.type = type;
        this.paramName = paramName;
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
     * parameter type <em>Foo</em> and the name <em>bar</em> this method would return
     * <em>Foo bar</em>
     * 
     * @return The definition of the parameter for use in method definitions.
     */
    public String getDefinition() {
        return type + " " + paramName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((paramName == null) ? 0 : paramName.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MethodParameter other = (MethodParameter)obj;
        if (paramName == null) {
            if (other.paramName != null) {
                return false;
            }
        } else if (!paramName.equals(other.paramName)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MethodParameter " + getDefinition();
    }

}