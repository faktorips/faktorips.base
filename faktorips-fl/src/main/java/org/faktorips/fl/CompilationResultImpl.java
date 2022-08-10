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
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * Implementation of the CompilationResult interface for {@link JavaCodeFragment Java code}.
 */
// Should be renamed to JavaCompilationResult(Impl), but that might break the API
public class CompilationResultImpl extends AbstractCompilationResult<JavaCodeFragment> {

    /**
     * Creates a {@link CompilationResult} with the given parameters.
     */
    public CompilationResultImpl(JavaCodeFragment sourcecode, Datatype datatype, MessageList messages) {
        super(sourcecode, datatype, messages);
    }

    /**
     * Creates a {@link CompilationResult} that contains the given {@link CodeFragment source code
     * fragment} and {@link Datatype}.
     */
    public CompilationResultImpl(JavaCodeFragment sourcecode, Datatype datatype) {
        super(sourcecode, datatype);
    }

    /**
     * Creates a {@link CompilationResult} that contains the given source code and {@link Datatype}.
     * 
     * @throws IllegalArgumentException if either {@code sourcecode} or {@code datatype} is
     *             {@code null}.
     */
    public CompilationResultImpl(String sourcecode, Datatype datatype) {
        this(new JavaCodeFragment(sourcecode), datatype);
    }

    /**
     * Creates a {@link CompilationResult} that contains the given {@link Message}.
     * 
     * @throws IllegalArgumentException if {@code message} is {@code null}.
     */
    public CompilationResultImpl(Message message) {
        super(message, new JavaCodeFragment());
    }

    /**
     * Creates a new {@link CompilationResult}.
     */
    public CompilationResultImpl() {
        super(new JavaCodeFragment());
    }

    /**
     * Creates a new {@link CompilationResult}, that contains a {@link Message} that the given
     * identifier is undefined. This method is intended to be used by implementations of
     * {@link IdentifierResolver}.
     */
    public static final CompilationResult<JavaCodeFragment> newResultUndefinedIdentifier(Locale locale,
            String identifier) {
        String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.UNDEFINED_IDENTIFIER, locale,
                identifier);
        return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
    }

    /**
     * Sets the code fragment.
     * 
     * @see AbstractCompilationResult#setCodeFragment(CodeFragment)
     */
    public void setJavaCodeFragment(JavaCodeFragment javaCodeFragment) {
        setCodeFragment(javaCodeFragment);
    }

}
