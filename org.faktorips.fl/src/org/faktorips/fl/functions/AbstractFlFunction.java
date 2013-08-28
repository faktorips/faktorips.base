/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.fl.functions;

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
}
