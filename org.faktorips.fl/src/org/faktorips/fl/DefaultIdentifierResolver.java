/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.Message;

/**
 * A default implementation of <code>IdentifierResolver</code> that allows to register constant
 * strings as identifiers together with a JavaCodeFragment and Datatype that are used to create a
 * <code>CompilationResult</code> if the resolver is requested to compile the identifier.
 */
public class DefaultIdentifierResolver implements IdentifierResolver {

    // map with (String) identifiers as keys and FragmentDatatypeWrapper instances as values.
    private Map<String, FragmentDatatypeWrapper> identifiers = new HashMap<String, FragmentDatatypeWrapper>();

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

    /**
     * {@inheritDoc}
     */
    public CompilationResult compile(String identifier, ExprCompiler exprCompiler, Locale locale) {
        FragmentDatatypeWrapper wrapper = identifiers.get(identifier);
        if (wrapper != null) {
            CompilationResultImpl compilationResult = new CompilationResultImpl(new JavaCodeFragment(wrapper.fragment),
                    wrapper.datatype);
            addCurrentIdentifer(compilationResult, identifier);
            return compilationResult;
        }
        String text = ExprCompiler.localizedStrings.getString(ExprCompiler.UNDEFINED_IDENTIFIER, locale, identifier);
        CompilationResultImpl compilationResult = new CompilationResultImpl(Message.newError(
                ExprCompiler.UNDEFINED_IDENTIFIER, text));
        addCurrentIdentifer(compilationResult, identifier);
        return compilationResult;
    }

    /*
     * Adds the given identifier candidate to the compilation result
     */
    private void addCurrentIdentifer(CompilationResult result, String identifierCandidate) {
        if (result instanceof CompilationResultImpl) {
            ((CompilationResultImpl)result).addIdentifierUsed(identifierCandidate);
        }
    }

    static class FragmentDatatypeWrapper {

        FragmentDatatypeWrapper(JavaCodeFragment fragment, Datatype datatype) {
            this.fragment = fragment;
            this.datatype = datatype;
        }

        private JavaCodeFragment fragment;
        private Datatype datatype;
    }

}
