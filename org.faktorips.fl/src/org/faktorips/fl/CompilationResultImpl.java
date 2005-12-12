package org.faktorips.fl;

import java.util.Locale;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;


/**
 * Implementation of the CompilationResult interface.
 */
public class CompilationResultImpl implements CompilationResult {

    /**
     * Creates a new result, that contains a message that the given identifier
     * is undefined. This method is intended to be used by implementations of
     * <code>IdentifierResolver</code>
     */
    public final static CompilationResult newResultUndefinedIdentifier(Locale locale, String identifier) {
        String text = ExprCompiler.localizedStrings.getString(ExprCompiler.UNDEFINED_IDENTIFIER, locale, identifier);
        return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
    }
    
    /**
     * Extracts the datatypes from an array of compilation results.
     */
    public final static Datatype[] getDatatypes(CompilationResult[] results) {
        Datatype[] types = new Datatype[results.length];
        for (int i=0; i<types.length; i++) {
            types[i] = results[i].getDatatype();
        }
        return types;
    }
    
    private JavaCodeFragment codeFragment;
    private MessageList messages;
    private Datatype datatype;
    
    /**
     * Creates a CompilationResult that contains the given sourcecode fragment
     * and datatype.
     * @throws IllegalArgumentException if either sourcecode or datatype is null.
     */
    public CompilationResultImpl(JavaCodeFragment sourcecode, Datatype datatype) {
        this.codeFragment = sourcecode;
        this.datatype = datatype;
        messages = new MessageList();
    }
    
    /**
     * Creates a CompilationResult that contains the given sourcecode and datatype.
     * 
     * @throws IllegalArgumentException if either sourcecode or datatype is null.
     */
    public CompilationResultImpl(String sourcecode, Datatype datatype) {
        this(new JavaCodeFragment(sourcecode), datatype);
    }
    
    /**
     * Creates a CompilationResult that contains the given message.
     * 
     * @throws IllegalArgumentException if msg is null.
     */
    public CompilationResultImpl(Message message) {
        messages = new MessageList(message);
        codeFragment = new JavaCodeFragment();
    }
    
    /**
     * Creates a new CompilationResult.
     */
    public CompilationResultImpl() {
        this.codeFragment = new JavaCodeFragment();
        messages = new MessageList();
    }
    
    /**
     * Appends the given compilation result's sourcecode fragment and messages to this 
     * result's sourcecode fragment. This result's datatype remains unchanged.
     */
    public void add(CompilationResult result) {
        codeFragment.append(result.getCodeFragment());
        messages.add(result.getMessages());
    }
    
    /**
     * Returns the generated Java sourcecode.
     */
    public JavaCodeFragment getCodeFragment() {
       return codeFragment; 
    }
    
    /**
     * Adds the code fragment to the result ones.
     */
    public void addCodeFragment(JavaCodeFragment code) {
       codeFragment.append(code); 
    }

    /**
     * Sets the result's datatype.
     */
    public void setDatatype(Datatype newType) {
        datatype = newType;
    }

    /**
     * Returns the compiled expression's datatype.
     */
    public Datatype getDatatype() {
        return datatype;
    }
    
    /**
     * Returns the messages generated during compilation. 
     */
    public MessageList getMessages() {
        return messages;
    }
    
    /**
     * Adds the message to the result.
     * 
     * @throws IllegalArgumentException if msg is null.
     */
    public void addMessage(Message msg) {
        messages.add(msg);
    }

    /**
     * Adds the message list to the result.
     * 
     * @throws IllegalArgumentException if list is null.
     */
    public void addMessages(MessageList list) {
        messages.add(list);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.CompilationResult#successfull()
     */
    public boolean successfull() {
        return !messages.containsErrorMsg();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.CompilationResult#failed()
     */
    public boolean failed() {
        return messages.containsErrorMsg();
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof CompilationResult)) {
            return false;
        }
        CompilationResult other = (CompilationResult)o;
        return datatype.equals(other.getDatatype())
        	&& this.codeFragment.equals(other.getCodeFragment())
        	&& this.messages.equals(other.getMessages());
    }
    
    public String toString() {
        return "Datatype: " + (datatype==null?"null":datatype.toString())
                + SystemUtils.LINE_SEPARATOR + messages.toString() + codeFragment.toString();  
    }
    
}
