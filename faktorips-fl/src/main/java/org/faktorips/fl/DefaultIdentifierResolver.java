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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.runtime.Message;

/**
 * A default implementation of {@link IdentifierResolver} that allows to register constant strings
 * as identifiers together with a {@link JavaCodeFragment} and {@link Datatype} that are used to
 * create a {@link CompilationResult} if the resolver is requested to compile the identifier.
 * 
 * This implementation generates {@link JavaCodeFragment Java code}.
 */
public class DefaultIdentifierResolver implements IdentifierResolver<JavaCodeFragment> {

    // map with (String) identifiers as keys and FragmentDatatypeWrapper instances as values.
    private Map<String, FragmentDatatypeWrapper> identifiers = new HashMap<>();

    /**
     * Creates a new resolver.
     */
    public DefaultIdentifierResolver() {
        // nothing to do
    }

    /**
     * Register the identifier.
     */
    public void register(String identifier, JavaCodeFragment fragment, Datatype datatype) {
        JavaCodeFragment defensiveCopy = new JavaCodeFragment(fragment);
        identifiers.put(identifier, new FragmentDatatypeWrapper(defensiveCopy, datatype));
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(String identifier,
            ExprCompiler<JavaCodeFragment> exprCompiler,
            Locale locale) {
        FragmentDatatypeWrapper wrapper = identifiers.get(identifier);
        if (wrapper != null) {
            CompilationResultImpl compilationResult = new CompilationResultImpl(new JavaCodeFragment(wrapper.fragment),
                    wrapper.datatype);
            return compilationResult;
        }
        String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.UNDEFINED_IDENTIFIER, locale,
                identifier);
        CompilationResultImpl compilationResult = new CompilationResultImpl(Message.newError(
                ExprCompiler.UNDEFINED_IDENTIFIER, text));
        return compilationResult;
    }

    static class FragmentDatatypeWrapper {

        private JavaCodeFragment fragment;
        private Datatype datatype;

        FragmentDatatypeWrapper(JavaCodeFragment fragment, Datatype datatype) {
            this.fragment = fragment;
            this.datatype = datatype;
        }
    }

}
