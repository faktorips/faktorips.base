/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util.message;

import java.util.Map;

import org.apache.commons.lang.SystemUtils;

/**
 * A human readable text message with an optional code that identifies the type of the message and a
 * severity that indicates if this is an error, warning or information.
 * <p>
 * Example: If the formula compiler can't resolve a symbol xyz it generates a message
 * "Can't resolve symbol xyz" (if the local is English). The message's code is UnresolvedSymbol
 * independent of the concrete symbol and locale.
 * <p>
 * In addition a message can provide access to the invalid objects the message relates to and its
 * properties. E.g. if a message reads that "the association's minimum cardinality can't be greater
 * than the maximum cardinality" than the association's minimum and maximum cardinality are invalid.
 * This information can be used for example to mark controls in the UI that show these properties.
 * <p>
 * Message is an immutable value object. Two message objects are considered equal if they have the
 * same severity, code and text.
 * 
 * @author Jan Ortmann
 */
public class Message {

    /** The XML tag name for messages. */
    public static final String TAG_NAME = "Message";

    /** Severity code for errors. */
    public static final int ERROR = 30;

    /** Severity code for warnings. */
    public static final int WARNING = 20;

    /** Severity code for informations. */
    public static final int INFO = 10;

    /** Severity code for no severity. */
    public static final int NONE = 0;

    private static final ObjectProperty[] EMPTY_OBJECT_PROPERTIES = new ObjectProperty[0];

    /** One of the constants ERROR, WARNING, INFO. */
    private final int severity;

    /** The human readable message text. */
    private final String text;

    /** Code to identify the type of message. */
    private final String code;

    /**
     * The object and its properties that are addressed in the message as having an error or that a
     * warning or information relates to.
     */
    private final ObjectProperty[] invalidOp;

    /**
     * Copy constructor.
     * 
     * @param msg The message to copy from.
     */
    public Message(Message msg) {
        this(msg.code, msg.text, msg.severity, msg.invalidOp);
    }

    /**
     * Creates a new message with the indicated code, text and severity.
     * 
     * @param code The code that identifies the message.
     * @param text The human readable text of the message.
     * @param severity The severity of the message.
     */
    public Message(String code, String text, int severity) {
        this(code, text, severity, EMPTY_OBJECT_PROPERTIES);
    }

    /**
     * Creates a new message with the indicated code, text and severity. The message refers to the
     * indicated object and the indicated property of that object.
     * 
     * @param code The code that identifies the message.
     * @param text The human readable text of the message.
     * @param severity The severity of the message.
     * @param invalidObject The invalid object to refer to.
     * @param invalidProperty The name of the invalid property that is part of the invalidObject to
     *            refer to.
     */
    public Message(String code, String text, int severity, Object invalidObject, String invalidProperty) {
        this(code, text, severity, invalidObject, new String[] { invalidProperty });
    }

    /**
     * Creates a new message with the indicated code, text and severity. The message refers to the
     * indicated object and it's indicated property encapsulated in an <code>ObjectProperty</code>
     * object.
     * 
     * @param code The code that identifies the message.
     * @param text The human readable text of the message.
     * @param severity The severity of the message.
     * @param invalidObjectProperty The <code>ObjectProperty</code> which encapsulated the invalid
     *            object and its indicated invalid property to refer to.
     */
    public Message(String code, String text, int severity, ObjectProperty invalidObjectProperty) {
        this(code, text, severity, new ObjectProperty[] { invalidObjectProperty });
    }

    /**
     * Creates a new message with the indicated code, text and severity. The message refers to the
     * indicated object.
     * 
     * @param code The code that identifies the message.
     * @param text The human readable text of the message.
     * @param severity The severity of the message.
     * @param invalidObject The invalid object to refer to.
     */
    public Message(String code, String text, int severity, Object invalidObject) {
        this(code, text, severity, new ObjectProperty[] { new ObjectProperty(invalidObject, null) });
    }

    /**
     * Creates a new message with the indicated code, text and severity. The message refers to the
     * indicated object and it's indicated properties.
     * 
     * @param code The code that identifies the message.
     * @param text The human readable text of the message.
     * @param severity The severity of the message.
     * @param invalidObject The invalid object to refer to.
     * @param invalidProperties A <code>String</code> array containing all invalid properties of the
     *            invalidObject to refer to.
     */
    public Message(String code, String text, int severity, Object invalidObject, String... invalidProperties) {
        this.code = code;
        this.text = text;
        this.severity = severity;
        if (invalidProperties == null || invalidProperties.length == 0) {
            this.invalidOp = new ObjectProperty[] { new ObjectProperty(invalidObject, null) };
        } else {
            this.invalidOp = new ObjectProperty[invalidProperties.length];
            for (int i = 0; i < invalidProperties.length; i++) {
                invalidOp[i] = new ObjectProperty(invalidObject, invalidProperties[i]);
            }
        }
    }

    /**
     * Creates a new message with the indicated code, text and severity. The message refers to the
     * indicated invalid objects and their invalid properties encapsulated in an
     * <code>ObjectProperty</code> array.
     * 
     * @param code The code that identifies the message.
     * @param text The human readable text of the message.
     * @param severity The severity of the message.
     * @param invalidObjectProperties A <code>ObjectProperty</code> array encapsulating all invalid
     *            properties of all the invalid objects to refer to.
     */
    public Message(String code, String text, int severity, ObjectProperty... invalidObjectProperties) {
        this.code = code;
        this.text = text;
        this.severity = severity;
        invalidOp = new ObjectProperty[invalidObjectProperties.length];
        System.arraycopy(invalidObjectProperties, 0, invalidOp, 0, invalidOp.length);
    }

    /**
     * Constructs and returns a new information message.
     * 
     * @param code The code that identifies the message.
     * @param text The human readable text of the message.
     */
    public static final Message newInfo(String code, String text) {
        return new Message(code, text, INFO);
    }

    /**
     * Constructs and returns a new information message.
     * 
     * @param code The code that identifies the message.
     * @param text The human readable text of the message.
     * @param invalidObject The invalid object
     * @param invalidProperty The name of the invalid property (which is a property of the
     *            invalidObject)
     */
    public static final Message newInfo(String code, String text, Object invalidObject, String invalidProperty) {
        return new Message(code, text, INFO, invalidObject, invalidProperty);
    }

    /**
     * Constructs and returns a new warning message.
     * 
     * @param code The code that identifies the message.
     * @param text The human readable text of the message.
     */
    public static final Message newWarning(String code, String text) {
        return new Message(code, text, WARNING);
    }

    /**
     * Constructs and returns a new warning message.
     * 
     * @param code The code that identifies the message.
     * @param text The human readable text of the message.
     * @param invalidObject The invalid object
     */
    public static final Message newWarning(String code, String text, Object invalidObject) {
        return new Message(code, text, WARNING, new ObjectProperty[] { new ObjectProperty(invalidObject, null) });
    }

    /**
     * Constructs and returns a new warning message.
     * 
     * @param code The code that identifies the message.
     * @param text The human readable text of the message.
     * @param invalidObject The invalid object
     * @param invalidProperties The name of the invalid properties (which are properties of the
     *            invalidObject)
     */
    public static final Message newWarning(String code, String text, Object invalidObject, String... invalidProperties) {
        return new Message(code, text, WARNING, invalidObject, invalidProperties);
    }

    /**
     * Constructs and returns a new error message.
     * 
     * @param code The code that identifies the message.
     * @param text The human readable text of the message.
     */
    public static final Message newError(String code, String text) {
        return new Message(code, text, ERROR);
    }

    /**
     * Constructs and returns a new error message.
     * 
     * @param code The code that identifies the message.
     * @param text The human readable text of the message.
     * @param invalidObject The invalid object to refer to.
     * @param invalidProperties The name of the invalid properties (which are properties of the
     *            invalidObject)
     */
    public static final Message newError(String code, String text, Object invalidObject, String... invalidProperties) {
        return new Message(code, text, ERROR, invalidObject, invalidProperties);
    }

    /**
     * Constructs and returns a new error message.
     * 
     * @param code The code that identifies the message.
     * @param text The human readable text of the message.
     * @param invalidObjectProperty The invalid object properties to refer to.
     */
    public static final Message newError(String code, String text, ObjectProperty... invalidObjectProperty) {
        return new Message(code, text, ERROR, invalidObjectProperty);
    }

    /**
     * Creates and returns a copy of the given message and replaces all references to the old object
     * with the new object.
     * 
     * @param objectPropertyMap The <code>Map</code> between old and new <code>ObjectProperty</code>
     * @return Message
     */
    public Message createCopy(Map<ObjectProperty, ObjectProperty> objectPropertyMap) {
        ObjectProperty[] op = getInvalidObjectProperties();

        ObjectProperty[] newInvalidObjectProperties = new ObjectProperty[op.length];
        System.arraycopy(op, 0, newInvalidObjectProperties, 0, op.length);

        int i = 0;
        for (ObjectProperty objectProperty : newInvalidObjectProperties) {
            ObjectProperty newObjectProperty = objectPropertyMap.get(objectProperty);
            if (newObjectProperty != null) {
                newInvalidObjectProperties[i] = newObjectProperty;
            }
            i++;
        }
        return new Message(code, text, severity, newInvalidObjectProperties);
    }

    /**
     * Returns the message's severity as one of the constants ERROR, WARNING, INFO or NONE.
     */
    public int getSeverity() {
        return severity;
    }

    /**
     * Returns the human readable message text.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns a string representing the identification code of this message.
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns an array containing the object properties the message refers to.
     * <p>
     * E.g. if a message reads "The driver's age must be greater than 18.", this method would
     * probably return the driver object and the property name age.
     * <p>
     * Returns an empty array if the list does not refer to any objects / properties.
     */
    public ObjectProperty[] getInvalidObjectProperties() {
        return invalidOp;
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
        for (int i = 0; i < invalidOp.length; i++) {
            if (i > 0) {
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

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Message)) {
            return false;
        }
        Message other = (Message)o;

        boolean equalSeverity = severity == other.severity;
        boolean equalCode = code.equals(other.code);
        boolean equalText = text.equals(other.text);
        boolean equalInvalidOpLength = invalidOp.length == other.invalidOp.length;
        if (!(equalSeverity && equalCode && equalText && equalInvalidOpLength)) {
            return false;
        }

        if (!equalsProperties(other)) {
            return false;
        }

        return true;
    }

    private boolean equalsProperties(Message other) {
        for (int i = 0; i < invalidOp.length; i++) {
            Object invalidObject = invalidOp[i].getObject();
            Object otherInvalidObject = other.invalidOp[i].getObject();
            if (!((invalidObject == null) ? otherInvalidObject == null : invalidObject.equals(otherInvalidObject))) {
                return false;
            }

            String invalidProperty = invalidOp[i].getProperty();
            String otherInvalidProperty = other.invalidOp[i].getProperty();
            if (!((invalidProperty == null) ? otherInvalidProperty == null : invalidProperty
                    .equals(otherInvalidProperty))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = 31 * result + severity;

        int codeHashCode = (code == null) ? 0 : code.hashCode();
        result = 31 * result + codeHashCode;

        int textHashCode = (text == null) ? 0 : text.hashCode();
        result = 31 * result + textHashCode;

        result = 31 * result + invalidOp.length;

        for (ObjectProperty invalidObjectProperty : invalidOp) {
            Object invalidObject = invalidObjectProperty.getObject();
            int invalidObjectHashCode = (invalidObject == null) ? 0 : invalidObject.hashCode();
            result = 31 * result + invalidObjectHashCode;

            String invalidProperty = invalidObjectProperty.getProperty();
            int invalidPropertyHashCode = (invalidProperty == null) ? 0 : invalidProperty.hashCode();
            result = 31 * result + invalidPropertyHashCode;
        }

        return result;
    }

}
