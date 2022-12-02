/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.faktorips.runtime.util.StringBuilderJoiner;

/**
 * A human readable text message with an optional code that identifies the type of the message and a
 * severity that indicates if this is an error, warning or information.
 * <p>
 * In addition a message provides access to the objects and their properties the message relates to.
 * E.g. if a message reads that "insured person's age must be at least 18" than the person's age is
 * invalid. This information can be used for example to mark controls in the UI that display this
 * property.
 * <p>
 * If the provided message has replacement parameters that cannot be evaluated while creating the
 * message text, it is possible to provide there parameters to the message object. Have a look at
 * {@link MsgReplacementParameter} for further information.
 * <p>
 * If you need any further information stored with the message, it is possible to implement the
 * IMarker object and provide some additional markers to the message. The exact use of the markers
 * depends on the custom implementation.
 * <p>
 * Message is an immutable value object. Two message objects are considered equal if they have the
 * same severity, code, text, "invalid properties" and replacement parameters.
 * 
 * @see MsgReplacementParameter
 */
public class Message implements Serializable {

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

    private static final long serialVersionUID = 6538319330010542283L;

    /** One of the constants ERROR, WARNING or INFO. */
    private final Severity severity;

    /** The human readable text. */
    private final String text;

    /** Code to identify the type of message. */
    private final String code;

    /**
     * The object and their properties that are addressed in the message as having an error or that
     * a warning or information relates to.
     */
    private final List<ObjectProperty> invalidOp;

    private final List<MsgReplacementParameter> replacementParameters;

    /**
     * A set of {@link IMarker} containing additional information.
     */
    private final Set<? extends IMarker> markers;

    /**
     * Creates a new message by using the fields of a {@link Builder}.
     * 
     * @param builder the {@link Builder}
     */
    public Message(Builder builder) {
        this(builder.code, builder.text, builder.severity, builder.invalidObjectProperties, builder.replacementParams,
                builder.markers);
    }

    /**
     * Creates a new message by copying everything of the given {@link Message}.
     * 
     * @param msg the {@link Message} to copy from
     */
    public Message(Message msg) {
        this(msg.code, msg.text, msg.severity, msg.invalidOp, msg.replacementParameters, msg.markers);
    }

    /**
     * Creates a new message by defining the following parameters.
     * 
     * @param text The human readable text of this message
     * @param severity The message's severity: {@link #ERROR}, {@link #WARNING} or {@link #INFO}
     */
    public Message(String text, Severity severity) {
        this(new Builder(text, severity));
    }

    /**
     * Creates a new message by defining the following parameters.
     * 
     * @param code A message code that identifies the kind of the message
     * @param text The human readable text of this message
     * @param severity The message's severity: {@link #ERROR}, {@link #WARNING} or {@link #INFO}
     */
    public Message(String code, String text, Severity severity) {
        this(new Builder(text, severity).code(code));
    }

    /**
     * Creates a new message by defining the following parameters.
     * 
     * @param code A message code that identifies the kind of the message
     * @param text The human readable text of this message
     * @param severity The message's severity: {@link #ERROR}, {@link #WARNING} or {@link #INFO}
     * @param invalidObject An object properties the message refers to
     */
    public Message(String code, String text, Severity severity, Object invalidObject) {
        this(new Builder(text, severity).code(code).invalidObjectWithProperties(invalidObject, (String)null));
    }

    /**
     * Creates a new message by defining the following parameters.
     * 
     * @param code A message code that identifies the kind of the message
     * @param text The human readable text of this message
     * @param severity The message's severity: {@link #ERROR}, {@link #WARNING} or {@link #INFO}
     * @param invalidObjectProperty An object property the message refers to
     */
    public Message(String code, String text, Severity severity, ObjectProperty invalidObjectProperty) {
        this(new Builder(text, severity).code(code).invalidObjects(invalidObjectProperty));
    }

    /**
     * Creates a new message by defining the following parameters.
     * 
     * @param code A message code that identifies the kind of the message
     * @param text The human readable text of this message
     * @param severity The message's severity: {@link #ERROR}, {@link #WARNING} or {@link #INFO}:
     *            {@link #ERROR}, {@link #WARNING} or {@link #INFO}
     * @param invalidObject the Object of the ObjectProperty
     * @param invalidObjectProperties An array of propertie's names the message refers to
     */
    public Message(String code, String text, Severity severity, Object invalidObject,
            String... invalidObjectProperties) {
        this(new Builder(text, severity).code(code).invalidObjectWithProperties(invalidObject,
                invalidObjectProperties));
    }

    /**
     * Creates a new message by defining the following parameters.
     * 
     * @param code A message code that identifies the kind of the message
     * @param text The human readable text of this message
     * @param severity The message's severity: {@link #ERROR}, {@link #WARNING} or {@link #INFO}
     * @param invalidObjectProperties An array of object properties the message refers to
     */
    public Message(String code, String text, Severity severity, ObjectProperty... invalidObjectProperties) {
        this(new Builder(text, severity).code(code).invalidObjects(Arrays.asList(invalidObjectProperties)));
    }

    /**
     * Creates a new message by defining the following parameters.
     * 
     * @param code A message code that identifies the kind of the message
     * @param text The human readable text of this message
     * @param severity The message's severity: {@link #ERROR}, {@link #WARNING} or {@link #INFO}
     * @param invalidObjectProperties A list of object properties the message refers to
     */
    public Message(String code, String text, Severity severity, List<ObjectProperty> invalidObjectProperties) {
        this(new Builder(text, severity).code(code).invalidObjects(invalidObjectProperties));
    }

    /**
     * Creates a new message by defining the following parameters.
     * 
     * @param code A message code that identifies the kind of the message
     * @param text The human readable text of this message
     * @param severity The message's severity: {@link #ERROR}, {@link #WARNING} or {@link #INFO}
     * @param invalidObjectProperties A list of object properties the message refers to
     */
    public Message(String code, String text, Severity severity, List<ObjectProperty> invalidObjectProperties,
            List<MsgReplacementParameter> replacementParameters) {
        this(new Builder(text, severity).code(code).invalidObjects(invalidObjectProperties)
                .replacements(replacementParameters).markers(Collections.<IMarker> emptySet()));
    }

    /**
     * Creates a new message by defining the following parameters.
     * 
     * @param code A message code that identifies the kind of the message
     * @param text The human readable text of this message
     * @param severity The message's severity: {@link #ERROR}, {@link #WARNING} or {@link #INFO}
     * @param invalidObjectProperty A list of object properties the message refers to
     * @param parameters an array of replacement parameters
     */
    public Message(String code, String text, Severity severity, ObjectProperty invalidObjectProperty,
            MsgReplacementParameter... parameters) {
        this(new Builder(text, severity).code(code).invalidObjects(invalidObjectProperty).replacements(parameters)
                .markers(Collections.<IMarker> emptySet()));
    }

    /**
     * Creates a new message by defining the following parameters.
     * 
     * @param code A message code that identifies the kind of the message
     * @param text The human readable text of this message
     * @param severity The message's severity: {@link #ERROR}, {@link #WARNING} or {@link #INFO}
     * @param invalidObjectProperty An object properties the message refers to
     * @param parameters a list of replacement parameters
     */
    public Message(String code, String text, Severity severity, ObjectProperty invalidObjectProperty,
            List<MsgReplacementParameter> parameters) {
        this(new Builder(text, severity).code(code).invalidObjects(invalidObjectProperty).replacements(parameters)
                .markers(Collections.<IMarker> emptySet()));
    }

    /**
     * Creates a new message by defining the following parameters.
     * 
     * @param code A message code that identifies the kind of the message
     * @param text The human readable text of this message
     * @param severity The message's severity: {@link #ERROR}, {@link #WARNING} or {@link #INFO}
     * @param invalidObjectProperties An array of object properties the message refers to
     * @param parameters an array of replacement parameters
     */
    public Message(String code, String text, Severity severity, ObjectProperty[] invalidObjectProperties,
            MsgReplacementParameter[] parameters) {
        this(new Builder(text, severity).code(code).invalidObjects(Arrays.asList(invalidObjectProperties))
                .replacements(parameters).markers(Collections.<IMarker> emptySet()));
    }

    /**
     * Creates a new message by defining the following parameters.
     * 
     * @param code A message code that identifies the kind of the message
     * @param text The human readable text of this message
     * @param severity The message's severity: {@link #ERROR}, {@link #WARNING} or {@link #INFO}
     * @param invalidObjectProperties A list of object properties the message refers to
     * @param parameters a list of replacement parameters
     * @param markers a list of markers. If this parameter is null an empty list is set as markers.
     *            The List of markers is
     */
    public Message(String code, String text, Severity severity, List<ObjectProperty> invalidObjectProperties,
            List<MsgReplacementParameter> parameters, Set<? extends IMarker> markers) {
        this.code = code;
        this.text = text;
        this.severity = severity;
        if (markers != null) {
            this.markers = Collections.unmodifiableSet(new LinkedHashSet<IMarker>(markers));
        } else {
            this.markers = Collections.emptySet();
        }
        if (invalidObjectProperties != null) {
            invalidOp = Collections.unmodifiableList(new ArrayList<>(invalidObjectProperties));
        } else {
            invalidOp = Collections.emptyList();
        }
        if (parameters != null) {
            replacementParameters = Collections.unmodifiableList(new ArrayList<>(parameters));
        } else {
            replacementParameters = Collections.emptyList();
        }
    }

    /**
     * Creates a new {@link Builder} with {@link #ERROR} and the given message.
     * <p>
     * To create a new {@link Message} you can use for example:<br>
     * <code>
     * Message.error("MessageText").code("1").invalidObjects("object",
     * "property").create();
     * </code>
     * 
     * @param text The human readable text of this message
     */
    public static final Builder error(String text) {
        return new Builder(text, ERROR);
    }

    /**
     * Creates a new {@link Builder} with {@link #WARNING} and the given message.
     * <p>
     * To create a new {@link Message} you can use for example:<br>
     * <code>
     * Message.warning("MessageText").code("1").invalidObjects("object",
     * "property").create();
     * </code>
     * 
     * @param text The human readable text of this message
     */
    public static final Builder warning(String text) {
        return new Builder(text, WARNING);
    }

    /**
     * Creates a new {@link Builder} with {@link #INFO} and the given message.
     * <p>
     * To create a new {@link Message} you can use for example:<br>
     * <code>
     * Message.info("MessageText").code("1").invalidObjects("object",
     * "property").create();
     * </code>
     * 
     * @param text The human readable text of this message
     */
    public static final Builder info(String text) {
        return new Builder(text, INFO);
    }

    /**
     * Creates a copy from the message and replaces all references to the old object with the new
     * object.
     */
    public static final Message createCopy(Message msg, Object oldObject, Object newObject) {
        List<ObjectProperty> op = msg.getInvalidObjectProperties();
        List<ObjectProperty> newOp = new ArrayList<>(op.size());
        for (ObjectProperty objectProperty : op) {
            if (objectProperty.getObject() == oldObject) {
                newOp.add(new ObjectProperty(newObject, objectProperty.getProperty()));
            } else {
                newOp.add(objectProperty);
            }
        }
        return new Builder(msg.text, msg.severity).code(msg.code).invalidObjects(newOp)
                .replacements(msg.getReplacementParameters()).markers(msg.getMarkers()).create();
    }

    /**
     * Creates and returns a copy of the given message and replaces all references to the old object
     * with the new object.
     * 
     * @param objectPropertyMap The <code>Map</code> between old and new <code>ObjectProperty</code>
     * @return Message
     */
    public static final Message createCopy(Message msg, Map<ObjectProperty, ObjectProperty> objectPropertyMap) {
        List<ObjectProperty> op = msg.getInvalidObjectProperties();
        List<ObjectProperty> newOp = new ArrayList<>(op.size());
        for (ObjectProperty objectProperty : op) {
            ObjectProperty newObjectProperty = objectPropertyMap.get(objectProperty);
            if (newObjectProperty != null) {
                newOp.add(newObjectProperty);
            } else {
                newOp.add(objectProperty);
            }
        }
        return new Builder(msg.text, msg.severity).code(msg.code).invalidObjects(newOp)
                .replacements(msg.getReplacementParameters()).markers(msg.getMarkers()).create();
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
    public static final Message newWarning(String code,
            String text,
            Object invalidObject,
            String... invalidProperties) {
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
     * Returns the message's severity as one of the constants ERROR, WARNING, INFO or NONE.
     */
    public Severity getSeverity() {
        return severity;
    }

    public String getText() {
        return text;
    }

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
     * and the property name age. Returns an empty list if this message does not refer to any
     * objects / properties.
     */
    public List<ObjectProperty> getInvalidObjectProperties() {
        if (invalidOp == null) {
            return new ArrayList<>(0);
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
            return new ArrayList<>(0);
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

    /**
     * Returns a set of {@link IMarker}s associated with this class. Returns an empty set if no
     * markers are set.
     */
    public Set<? extends IMarker> getMarkers() {
        return markers;
    }

    /**
     * Returns <code>true</code> if the message contains the specified {@link IMarker} marker
     * otherwise <code>false</code>.
     */
    public boolean hasMarker(IMarker marker) {
        return markers.contains(marker);
    }

    /**
     * Returns <code>true</code> if the message has markers otherwise <code>false</code>.
     */
    public boolean hasMarkers() {
        return !markers.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (severity) {
            case ERROR:
                sb.append("ERROR");
                break;
            case WARNING:
                sb.append("WARNING ");
                break;
            case INFO:
                sb.append("INFO");
                break;
            default:
                sb.append("Severity ");
                sb.append(severity);
        }
        sb.append(' ');
        sb.append(code);
        appendInvalidObjectProperties(sb);
        sb.append(System.lineSeparator());
        sb.append(text);
        return sb.toString();
    }

    public void appendInvalidObjectProperties(StringBuilder sb) {
        sb.append('[');
        StringBuilderJoiner.join(sb, invalidOp, op -> {
            sb.append(op.getObject().toString());
            sb.append('.');
            sb.append(op.getProperty());
        });
        sb.append(']');
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
        if (!Objects.equals(code, other.code) || !Objects.equals(text, other.text) || (severity != other.severity)
                || !Objects.equals(invalidOp, other.invalidOp)) {
            return false;
        }
        if (!Objects.equals(replacementParameters, other.replacementParameters)
                || !Objects.equals(markers, other.markers)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    /**
     * A builder for the {@link Message} class. This builder has been designed due to heavy
     * constructor overloading with many parameters. It helps instantiating global variables of
     * {@link Message}.
     * <p>
     * To use the builder simply create an instance by calling the
     * {@link Message#Message(String, Severity)} or by calling one of the static creation methods
     * like {@link Message#error(String)}, {@link Message#warning(String)} or
     * {@link Message#info(String)}. Afterwards add needed information to the builder for example
     * call {@link #invalidObjectWithProperties(Object object, String... properties)} to provide
     * some invalid object properties. When the builder has every information that is needed to
     * create a proper message call {@link #create()}.
     * 
     * @see Message#error(String)
     * @see Message#warning(String)
     * @see Message#info(String)
     */
    public static class Builder {

        private String text;

        private Severity severity;

        private String code;

        private final List<ObjectProperty> invalidObjectProperties = new ArrayList<>();

        private final List<MsgReplacementParameter> replacementParams = new ArrayList<>();

        private final Set<IMarker> markers = new LinkedHashSet<>();

        /**
         * Creates a new builder that is able to create a proper {@link Message} with all needed
         * information.
         * 
         * @param text The human readable text of this message
         * @param severity The message's severity: {@link #ERROR}, {@link #WARNING} or {@link #INFO}
         */
        public Builder(String text, Severity severity) {
            this.text = text;
            this.severity = severity;
        }

        /**
         * Creates a new builder from a given {@link Message} to create a copy of the
         * {@link Message} with all information.
         * 
         * @param message the {@link Message} to copy
         */
        public Builder(Message message) {
            this(message.text, message.severity);
            code = message.code;
            invalidObjectProperties.addAll(message.getInvalidObjectProperties());
            replacementParams.addAll(message.getReplacementParameters());
            markers.addAll(message.getMarkers());
        }

        /**
         * Set the message's code that identifies the kind of the message.
         * 
         * @param code A message code that identifies the kind of the message
         * @return This builder instance to directly add further properties
         */
        public Builder code(String code) {
            this.code = code;
            return this;
        }

        /**
         * Set the message's text which is human readable.
         * 
         * @param text The human readable text of this message
         * @return This builder instance to directly add further properties
         */
        public Builder text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Set the message's severity, in exact {@link #ERROR}, {@link #WARNING} or {@link #INFO}.
         * 
         * @param severity Severity of the message: {@link #ERROR}, {@link #WARNING} or
         *            {@link #INFO}
         * @return This builder instance to directly add further properties
         */
        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        /**
         * Add a list of object properties that the message refers to.
         * 
         * @param invalidObjectProperties A list of object properties that the message refers to
         * @return This builder instance to directly add further properties
         */
        public Builder invalidObjects(List<ObjectProperty> invalidObjectProperties) {
            this.invalidObjectProperties.addAll(invalidObjectProperties);
            return this;
        }

        /**
         * Add an object property that the message refers to.
         * 
         * @param invalidObjectProperty An object property that the message refers to
         * @return This builder instance to directly add further properties
         * 
         */
        public Builder invalidObject(ObjectProperty invalidObjectProperty) {
            return invalidObjects(invalidObjectProperty);
        }

        /**
         * Add object properties that the message refers to.
         * 
         * @param invalidObjectProperties Object properties that the message refers to
         * @return This builder instance to directly add further properties
         */
        public Builder invalidObjects(ObjectProperty... invalidObjectProperties) {
            return invalidObjects(Arrays.asList(invalidObjectProperties));
        }

        /**
         * Add some object properties the message refers to by creating instances of
         * {@link ObjectProperty} for every given property and the given object.
         * 
         * @param object The object the message refers to
         * @param properties Some properties the message refers to
         * @return This builder instance to directly add further properties
         * 
         */
        public Builder invalidObjectWithProperties(Object object, String... properties) {
            if (properties.length == 0) {
                invalidObjectProperties.add(new ObjectProperty(object));
            } else {
                for (String property : properties) {
                    invalidObjectProperties.add(new ObjectProperty(object, property));
                }
            }
            return this;
        }

        /**
         * A list of replacement parameters the message should reference.
         * 
         * @param replacementParams a list of replacement parameters
         * @return This builder instance to directly add further properties
         */
        public Builder replacements(List<MsgReplacementParameter> replacementParams) {
            this.replacementParams.addAll(replacementParams);
            return this;
        }

        /**
         * Some replacement parameters the message should reference.
         * 
         * @param replacementParams Some replacement parameters
         * @return This builder instance to directly add further properties
         */
        public Builder replacements(MsgReplacementParameter... replacementParams) {
            this.replacementParams.addAll(Arrays.asList(replacementParams));
            return this;
        }

        /**
         * Creates a new {@link MsgReplacementParameter} the message should reference
         * 
         * @param name The name of the {@link MsgReplacementParameter}
         * @param value The value of the {@link MsgReplacementParameter}
         * @return This builder instance to directly add further properties
         */
        public Builder replacements(String name, Object value) {
            replacementParams.add(new MsgReplacementParameter(name, value));
            return this;
        }

        /**
         * Set a collection of markers that should be provided to the new message.
         * 
         * @param markers a set of markers
         * @return This builder instance to directly add further properties
         */
        public Builder markers(Collection<? extends IMarker> markers) {
            this.markers.addAll(markers);
            return this;
        }

        /**
         * Set some markers that should be provided to the new message.
         * 
         * @param markers Some markers
         * @return This builder instance to directly add further properties
         */
        public Builder markers(IMarker... markers) {
            this.markers.addAll(Arrays.asList(markers));
            return this;
        }

        /**
         * Creates a new {@link Message} with all previously given properties.
         * 
         * @return A new message that has the parameters of this builder.
         */
        public Message create() {
            return new Message(this);
        }

    }

}
