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

package org.faktorips.fl;

import java.util.Locale;
import java.util.Set;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Implementation of the CompilationResult interface for {@link JavaCodeFragment Java code}.
 */
// Should be renamed to JavaCompilationResult(Impl), but that might break the API
public class CompilationResultImpl extends AbstractCompilationResult<JavaCodeFragment> {

    /**
     * Creates a {@link CompilationResult} with the given parameters.
     */
    public CompilationResultImpl(JavaCodeFragment sourcecode, Datatype datatype, MessageList messages,
            Set<String> identifiers) {
        super(sourcecode, datatype, messages, identifiers);
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
        String text = ExprCompiler.LOCALIZED_STRINGS.getString(ExprCompiler.UNDEFINED_IDENTIFIER, locale, identifier);
        return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
    }

}
