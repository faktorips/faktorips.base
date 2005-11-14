package org.faktorips.fl;

import java.util.*;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.Message;


/**
 * A default implementation of <code>IdentifierResolver</code> that allows to register
 * constant strings as identifiers together with a JavaCodeFragment and Datatype
 * that are used to create a <code>CompilationResult</code> if the resolver is
 * requested to compile the identifier.
 */
public class DefaultIdentifierResolver implements IdentifierResolver {
    
    // map with (String) identifiers as keys and FragmentDatatypeWrapper instances as values. 
    private Map identifiers = new HashMap();

    /**
     * Creates a new resolver. 
     */
    public DefaultIdentifierResolver() {
    }
    
    /**
     * Register the identifier.
     */
    public void register(String identifier, JavaCodeFragment fragment, Datatype datatype) {
        JavaCodeFragment defensiveCopy = new JavaCodeFragment(fragment); 
        identifiers.put(identifier, new FragmentDatatypeWrapper(defensiveCopy, datatype));
    }

    /**
     * Overridden method.
     * @see org.faktorips.fl.IdentifierResolver#compile(java.lang.String, java.util.Locale)
     */ 
    public CompilationResult compile(String identifier, Locale locale) {
        FragmentDatatypeWrapper wrapper = (FragmentDatatypeWrapper)identifiers.get(identifier);
        if (wrapper!=null) {
            return new CompilationResultImpl(wrapper.fragment, wrapper.datatype);
        } 
        String text = ExprCompiler.localizedStrings.getString(ExprCompiler.UNDEFINED_IDENTIFIER, locale, identifier);
        return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
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
