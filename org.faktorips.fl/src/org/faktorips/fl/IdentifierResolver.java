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

import java.util.Locale;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.Datatype;

/**
 * Resolves identifiers used in an expression.
 * <p>
 * Example: <blockquote>
 * 
 * <pre>
 * 2 * a + 3
 * </pre>
 * 
 * </blockquote> In the above example {@code a} is an identifier. The compiler needs to know
 * {@code a's} {@link Datatype} and generate appropriate {@link CompilationResult source code} that
 * returns the value of {@code a} at runtime. The compiler delegates this task to an
 * {@link IdentifierResolver identifier resolver}.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
@FunctionalInterface
public interface IdentifierResolver<T extends CodeFragment> {

    /**
     * Returns the compilation result for the indicated identifier.
     * <P>
     * If the given string is a valid identifier the compilation result must contain the
     * {@link CodeFragment source code} that can be inserted in the expression's code along with the
     * identifier's {@link Datatype}.
     * <P>
     * If the string is not a valid identifier the returned compilation result must contain an
     * <strong>error</strong> message with the code {@link ExprCompiler#UNDEFINED_IDENTIFIER
     * UNDEFINED_IDENTIFIER}.
     * 
     * @param identifier The identifier to resolve.
     * @param locale The locale used for locale dependent messages.
     * @param exprCompiler specialized {@link ExprCompiler} can down casted and used to ask for
     *            properties or services that are necessary to be able to resolve an identifier
     *            properly
     */
    public CompilationResult<T> compile(String identifier, ExprCompiler<T> exprCompiler, Locale locale);

}
