/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A human readable text message with an optional code that identifies the type of the message and a
 * severity that indicates if this is an error, warning or information.
 * <p>
 * In addition a message provides access to the objects and their properties the message relates to.
 * E.g. if a message reads that "insured person's age must be at least 18" than the person's age is
 * invalid. This information can be used for example to mark controls in the UI that display this
 * property.
 * <p>
 * Message is an immutable value object. Two message objects are considered equal if they have the
 * same severity, code, text, "invalid properties" and replacement parameters.
 * 
 * @author Jan Ortmann
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 7155891629458031466L;

    public enum Severity {

        /**
         * Severity none.
         */
        NONE,

        /**
         * Severity info.
         */
        INFO,

        /**
         * Severity warning.
         */
        WARNING,

        /**
         * Severity error.
         */
        ERROR;
    }

    /**
     * Severity none.
     */
    public static final Severity NONE = Severity.NONE;

    /**
     * Severity info.
     */
    public static final Severity INFO = Severity.INFO;

    /**
     * Severity warning.
     */
    public static final Severity WARNING = Severity.WARNING;

    /**
     * Severity error.
     */
    public static final Severity ERROR = Severity.ERROR;

    /** One of the constants ERROR, WARNING or INFO. */
    private Severity severity = Severity.ERROR;

    /** The human readable text. */
    private String text = "";

    /** Code to identify the type of message. */
    private String code = "";

    /**
     * The object and their properties that are addressed in the message as having an error or that
     * a warning or information relates to.
     */
    private List<ObjectProperty> invalidOp = null;

    private List<MsgReplacementParameter> replacementParameters = null;

    /**
     * Creates a copy from the message and replaces all references to the old object with the new
     * object.
     */
    public final static Message createCopy(Message msg, Object oldObject, Object newObject) {
        List<ObjectProperty> op = msg.getInvalidObjectProperties();
        List<ObjectProperty> newOp = new ArrayList<ObjectProperty>(op.size());
        for (ObjectProperty objectProperty : op) {
            if (objectProperty.getObject() == oldObject) {
                newOp.add(new ObjectProperty(newObject, objectProperty.getProperty()));
            } else {
                newOp.add(objectProperty);
            }
        }
        return new Message(msg.code, msg.text, msg.severity, newOp);
    }

    /**
     * Constructs a new information message.
     */
    public final static Message newInfo(String code, String text) {
        return new Message(code, text, Severity.INFO);
    }

    /**
     * Constructs a new warning message.
     */
    public final static Message newWarning(String code, String text) {
        return new Message(code, text, Severity.WARNING);
    }

    /**
     * Constructs a new error message.
     */
    public final static Message newError(String code, String text) {
        return new Message(code, text, Severity.ERROR);
    }

    /**
     * Copy constructor.
     */
    public Message(Message msg) {
        this.code = msg.code;
        this.severity = msg.severity;
        this.text = msg.text;
        if (msg.invalidOp != null) {
            invalidOp = new ArrayList<ObjectProperty>(msg.invalidOp.size());
            Collections.copy(invalidOp, msg.invalidOp);
        }
        if (msg.replacementParameters != null) {
            replacementParameters = new ArrayList<MsgReplacementParameter>();
            replacementParameters.addAll(msg.replacementParameters);
        }

    }

    /**
     * Creates a new message with the indicated code, text and severity.
     */
    public Message(String code, String text, Severity severity) {
        this.code = code;
        this.text = text;
        this.severity = severity;
    }

    /**
     * Creates a new message with the indicated code, text and severity. The message refers to the
     * indicated object and it's property.
     */
    public Message(String code, String text, Severity severity, ObjectProperty invalidObjectProperty) {
        this(code, text, severity, new ObjectProperty[] { invalidObjectProperty });
    }

    /**
     * Creates a new message with the indicated code, text and severity. The message refers to the
     * indicated object and it's properties.
     */
    public Message(String code, String text, Severity severity, Object invalidObject) {

        this(code, text, severity, new ObjectProperty(invalidObject, null));
    }

    /**
     * Creates a new message with the indicated code, text and severity. The message refers to the
     * indicated object and it's properties.
     */
    public Message(String code, String text, Severity severity, Object invalidObject, String... invalidProperties) {
        this(code, text, severity);
        invalidOp = new ArrayList<ObjectProperty>(invalidProperties.length);
        for (String invalidProperty : invalidProperties) {
            invalidOp.add(new ObjectProperty(invalidObject, invalidProperty));
        }
    }

    /**
     * Creates a new message with the indicated code, text and severity. The message refers to the
     * indicated objects and their properties.
     */
    public Message(String code, String text, Severity severity, ObjectProperty[] refersTo) {
        this(code, text, severity);
        if (refersTo != null) {
            invalidOp = new ArrayList<ObjectProperty>();
            invalidOp.addAll(Arrays.asList(refersTo));
        }
    }

    /**
     * Creates a new message with the indicated code, text and severity. The message refers to the
     * indicated objects and their properties.
     */
    public Message(String code, String text, Severity severity, List<ObjectProperty> refersTo) {
        this(code, text, severity);
        if (refersTo != null) {
            invalidOp = new ArrayList<ObjectProperty>(refersTo);
        }
    }

    /**
     * Creates a new message with the indicated code, text and severity. The message refers to the
     * indicated objects and their properties. The message's contains the given replacement
     * parameters.
     */
    public Message(String code, String text, Severity severity, ObjectProperty refersTo,
            MsgReplacementParameter... parameters) {

        this(code, text, severity);
        if (refersTo != null) {
            invalidOp = new ArrayList<ObjectProperty>(1);
            invalidOp.add(refersTo);
        }
        if (parameters != null) {
            replacementParameters = Arrays.asList(parameters);
        }
    }

    /**
     * Creates a new message with the indicated code, text and severity. The message refers to the
     * indicated objects and their properties. The message's contains the given replacement
     * parameters.
     */
    public Message(String code, String text, Severity severity, ObjectProperty[] refersTo,
            MsgReplacementParameter[] parameters) {
        this(code, text, severity);
        if (refersTo != null) {
            invalidOp = new ArrayList<ObjectProperty>(1);
            invalidOp.addAll(Arrays.asList(refersTo));
        }
        if (parameters != null) {
            replacementParameters = Arrays.asList(parameters);
        }
    }

    public Message(String code, String text, Severity severity, ObjectProperty refersTo,
            List<MsgReplacementParameter> parameters) {
        this(code, text, severity);
        if (refersTo != null) {
            invalidOp = new ArrayList<ObjectProperty>(1);
            invalidOp.add(refersTo);
        }
        if (parameters != null) {
            replacementParameters = new ArrayList<MsgReplacementParameter>(parameters);
        }
    }

    /**
     * Returns the message's severity as one of the constants ERROR, WARNING, INFO or NONE.
     */
    public Severity getSeverity() {
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
     * Returns the number of referenced invalid object properties.
     */
    public int getNumOfInvalidObjectProperties() {
        if (invalidOp == null) {
            return 0;
        }
        return invalidOp.size();
    }

    /**
     * Returns the list of object properties the message refers to. E.g. if a message reads "The
     * driver's age must be greater than 18.", this method would probably return the driver object
     * and the property name age. Returns an empty array if this message does not refer to any
     * objects / properties.
     */
    public List<ObjectProperty> getInvalidObjectProperties() {
        if (invalidOp == null) {
            return new ArrayList<ObjectProperty>(0);
        }
        return Collections.unmodifiableList(invalidOp);
    }

    /**
     * Returns the number of replacement parameters..
     */
    public int getNumOfReplacementParameters() {
        if (replacementParameters == null) {
            return 0;
        }
        return replacementParameters.size();
    }

    /**
     * Returns the list of replacement parameters. Returns an empty list if this message hasn't got
     * any replacements.
     */
    public List<MsgReplacementParameter> getReplacementParameters() {
        if (replacementParameters == null) {
            return new ArrayList<MsgReplacementParameter>(0);
        }
        return replacementParameters;
    }

    /**
     * Returns <code>true</code> if the message has a replacement parameter with the given name,
     * otherwise <code>false</code>. Returns <code>false</code> if paramName is <code>null</code>.
     */
    public boolean hasReplacementParameter(String paramName) {
        if (replacementParameters == null) {
            return false;
        }
        for (MsgReplacementParameter replacementParameter : replacementParameters) {
            if (replacementParameter.getName().equals(paramName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value for the given replacement parameter. Returns <code>null</code> if the
     * message hasn't got a parameter with the indicated name.
     * 
     * @see #hasReplacementParameter(String)
     */
    public Object getReplacementValue(String paramName) {
        if (replacementParameters == null) {
            return null;
        }
        for (MsgReplacementParameter replacementParameter : replacementParameters) {
            if (replacementParameter.getName().equals(paramName)) {
                return replacementParameter.getValue();
            }
        }
        return null;
    }

    @Override
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
        String lineSeparator = System.getProperty("line.separator");
        int max = invalidOp == null ? 0 : invalidOp.size();
        for (int i = 0; i < max; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append(invalidOp.get(i).getObject().toString());
            buffer.append('.');
            buffer.append(invalidOp.get(i).getProperty());
        }
        buffer.append(']');
        buffer.append(lineSeparator);
        buffer.append(text);
        return buffer.toString();
    }

    /**
     * Returns true if o is a Message and severity, code and text are equal.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Message)) {
            return false;
        }
        Message other = (Message)o;
        if (!code.equals(other.code) || severity != other.severity || !text.equals(other.text)) {
            return false;
        }
        int numOfInvalidObjectProperties = getNumOfInvalidObjectProperties();
        if (numOfInvalidObjectProperties != other.getNumOfInvalidObjectProperties()) {
            return false;
        }
        if (invalidOp == null) {
            return other.invalidOp == null;
        } else if (!invalidOp.equals(other.invalidOp)) {
            return false;
        }
        int numOfReplacementParams = getNumOfReplacementParameters();
        if (numOfReplacementParams != other.getNumOfReplacementParameters()) {
            return false;
        }
        if (replacementParameters == null) {
            return other.replacementParameters == null;
        } else if (!replacementParameters.equals(other.replacementParameters)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

}
