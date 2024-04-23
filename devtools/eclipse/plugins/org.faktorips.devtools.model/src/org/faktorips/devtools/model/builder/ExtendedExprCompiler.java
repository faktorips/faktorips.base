/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.fl.JavaExprCompiler;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.MethodNames;

/**
 * An extension of the formula language compiler that provides additional properties.
 *
 * @author Peter Kuntz
 */
public class ExtendedExprCompiler extends JavaExprCompiler {

    private JavaCodeFragment runtimeRepositoryExpression = new JavaCodeFragment(MethodNames.GET_THIS_REPOSITORY + "()"); //$NON-NLS-1$

    public ExtendedExprCompiler(Locale locale) {
        super(locale);
    }

    /**
     * Returns the expression to access the {@link IRuntimeRepository} which can be used by
     * {@link IdentifierResolver IdentifierResolvers} with which this {@link ExprCompiler} is
     * configured.
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
