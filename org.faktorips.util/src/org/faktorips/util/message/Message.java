package org.faktorips.util.message;

import org.apache.commons.lang.SystemUtils;


/**
 * A human readable text message with an optional code that identifies the
 * type of the message and a severity that indicates if this is an error, warning
 * or information.
 * <p>
 * Example: If the formula compiler can't resolve a symbol xyz it generates
 * a message "Can't resolve symbol xyz" (if the local is english). The message's
 * code is UnresolvedSymbol independent of the concrete symbol and locale.
 * <p>
 * In addition a message can provides access to the objects and their properties
 * that message relates to. E.g. if a message reads that "the relation's minimum
 * cardinality can't be greater than the maximum cardinality" than the relation's minimum and
 * maximum cardinality are invalid. This information can be used for example
 * to mark controls in the UI that show these properties.  
 * <p>
 * Message is an immutable value object. Two message objects are considered equal
 * if they have the same severity, code and text. 
 * 
 * @author Jan Ortmann
 */
public class Message {
    
    public final static String TAG_NAME = "Message";
    
    public final static int ERROR = 30;
    public final static int WARNING = 20;
    public final static int INFO = 10;
    public final static int NONE = 0;

    //  one of the constants ERROR, WARNING, INFO
    private int severity = ERROR;
    
    // the human readable text
    private String text = "";
    
    // code to identifiy the type of message
    private String code = "";
    
    // the object and their properties that are adresses in the message 
    // as having an error or that a warning or information relates to.
    private ObjectProperty[] invalidOp = new ObjectProperty[0];

    /**
     * Constructs a new information message.
     */
    public final static Message newInfo(String code, String text) {
        return new Message(code, text, INFO);
    }
    
    /**
     * Constructs a new warning message.
     */
    public final static Message newWarning(String code, String text) {
        return new Message(code, text, WARNING);
    }
    
    /**
     * Constructs a new error message.
     */
    public final static Message newError(String code, String text) {
        return new Message(code, text, ERROR);
    }
    
    /**
     * Copy constructor.
     */
    public Message(Message msg) {
        this.code = msg.code;
        this.severity = msg.severity;
        this.text = msg.text;
        if (msg.invalidOp!=null) {
            invalidOp = new ObjectProperty[msg.invalidOp.length];
            System.arraycopy(msg.invalidOp, 0, invalidOp, 0, invalidOp.length);
        }
    }
    
    /**
     * Creates a new message with the indicated code, text and severity.
     */
    public Message(String code, String text, int severity) {
        this.code = code;
        this.text = text;
        this.severity = severity;
    }
    
    /**
     * Creates a new message with the indicated code, text and severity.
     * The message refers to the indicated object and it's property.
     */
    public Message(
            String code, 
            String text, 
            int severity,
            Object referencedObject,
            String referencedProperty) {
        this(code, text, severity, referencedObject, new String[]{referencedProperty});
    }
    
    /**
     * Creates a new message with the indicated code, text and severity.
     * The message refers to the indicated object and it's properties.
     */
    public Message(
            String code, 
            String text, 
            int severity,
            Object invalidObject) {
        
        this(code, text, severity, new ObjectProperty[] {new ObjectProperty(invalidObject, null)} );
    }
            
    /**
     * Creates a new message with the indicated code, text and severity.
     * The message refers to the indicated object and it's properties.
     */
    public Message(
            String code, 
            String text, 
            int severity,
            Object invalidObject,
            String invalidProperties[]) {
        this(code, text, severity);
        invalidOp = new ObjectProperty[invalidProperties.length];
        for (int i=0; i<invalidProperties.length; i++) {
            invalidOp[i] = new ObjectProperty(invalidObject, invalidProperties[i]);
        }
    }
    
    /**
     * Creates a new message with the indicated code, text and severity.
     * The message refers to the indicated objects and their properties.
     */
    public Message(
            String code, 
            String text, 
            int severity,
            ObjectProperty[] refersTo) {
        this(code, text, severity);
        invalidOp = new ObjectProperty[refersTo.length];
        System.arraycopy(refersTo, 0, invalidOp, 0, invalidOp.length);
    }
    
    /**
     * Returns the message's severity as one of the constants ERROR, WARNING,
     * INFO or NONE.
     */
    public int getSeverity() {
        return severity;
    }
    
    /**
     * Returns the humand readable message text.
     */
    public String getText() {
        return text;
    }
    
    /**
     * Returns the message code.
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Returns the list of object properties the message refers to. 
     * E.g. if a message reads "The driver's age must be greater than 18.", this
     * method would probably return the driver object and the property name age.  
     */
    public ObjectProperty[] getInvalidObjectProperties() {
        return invalidOp;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        switch (severity) {
	    	case ERROR:
	    	    buffer.append("ERROR");
	    	    break;
	    	case WARNING:
	    	    buffer.append("WARNING ");
	    	    break;
	    	case INFO:
	    	    buffer.append("INFO");
	    	    break;
	    	default:
	    	    buffer.append("Severity ");
	        	buffer.append(severity);
        }
    	buffer.append(' ');
        buffer.append(code);
        buffer.append('[');
        for (int i=0; i<invalidOp.length; i++) {
            if (i>0) {
                buffer.append(", ");
            }
            buffer.append(invalidOp[i].getObject().toString());
            buffer.append('.');
            buffer.append(invalidOp[i].getProperty());
        }
        buffer.append(']');
        buffer.append(SystemUtils.LINE_SEPARATOR);
        buffer.append(text);
        return buffer.toString();
    }

    /**
     * Returns true if o is a Message and severity, code and text are equal.
     *   
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (!(o instanceof Message)) {
            return false;
        }
        Message other = (Message)o;
        boolean equal = 
            severity==other.severity 
        	&& code.equals(other.code)
        	&& text.equals(other.text)
        	&& invalidOp.length == other.invalidOp.length;
        if (!equal) {
            return false;
        }
        for (int i=0; i<invalidOp.length; i++) {
            if (!invalidOp[i].getObject().equals(other.invalidOp[i].getObject())) {
                return false;
            }
                
            if (!invalidOp[i].getProperty().equals(other.invalidOp[i].getProperty())) {
                return false;
            }
        }
        return true;
    }
    
    public int hashCode() {
        return text.hashCode();
    }

}
