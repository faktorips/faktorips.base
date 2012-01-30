/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.MethodNames;

/**
 * An extension of the formula language compiler that provides additional properties.
 * 
 * @author Peter Kuntz
 */
public class ExtendedExprCompiler extends ExprCompiler {

    private JavaCodeFragment runtimeRepositoryExpression = new JavaCodeFragment(MethodNames.GET_THIS_REPOSITORY + "()"); //$NON-NLS-1$

    /**
     * Returns the expression to access the {@link IRuntimeRepository} which can be used by
     * {@link IdentifierResolver}s with which this {@link ExprCompiler} is configured.
     */
    public JavaCodeFragment getRuntimeRepositoryExpression() {
        return runtimeRepositoryExpression;
    }

    /**
     * Sets the expression for the {@link IRuntimeRepository}.
     * 
     * @see #getRuntimeRepositoryExpression()
     */
    public void setRuntimeRepositoryExpression(JavaCodeFragment runtimeRepositoryExpression) {
        this.runtimeRepositoryExpression = runtimeRepositoryExpression;
    }

}
