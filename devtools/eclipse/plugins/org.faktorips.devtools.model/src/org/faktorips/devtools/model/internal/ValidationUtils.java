/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.mapping.StatusMessageListMapping;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaConventions;
import org.faktorips.devtools.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.model.Validatable;
import org.faktorips.devtools.model.internal.ipsobject.IpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.Severity;

/**
 * A collection of helper methods for validating model objects.
 * 
 * @author Jan Ortmann
 */
public class ValidationUtils {

    private ValidationUtils() {
        // Utility class not to be instantiated.
    }

    /**
     * Tests if the given qualified name identifies a policy component type. If not, it adds an
     * error message to the given message list.
     * <p>
     * Returns <code>true</code> if the reference is valid, otherwise <code>false</code>.
     * 
     * @param objectName the qualified type name to check
     * @param type the type the object to check is of
     * @param propertyDisplayName the name used to display the value to the user
     * @param part the part the checked reference belongs to (used if a message has to be created)
     * @param propertyName the (technical) name of the property used if a message has to be created
     * @param msgCode the message code to use if a message has to be created
     * @param list the list of messages to add a new one
     */
    public static final boolean checkIpsObjectReference(String objectName,
            IpsObjectType type,
            String propertyDisplayName,
            IIpsObjectPartContainer part,
            String propertyName,
            String msgCode,
            MessageList list) {

        if (!checkStringPropertyNotEmpty(objectName, propertyDisplayName, part, propertyName, msgCode, list)) {
            return false;
        }

        /*
         * Due to better performance findIpsSrcFile is used instead of findIpsObject, because only
         * the existence of the IPS object needs to be checked here, not the initialization (which
         * is implicitly done by calling findIpsObject).
         */
        if (part.getIpsProject().findIpsSrcFile(type, objectName) == null) {
            String text = MessageFormat.format(Messages.ValidationUtils_msgObjectDoesNotExist,
                    StringUtils.capitalize(propertyDisplayName), objectName);
            list.add(new Message(msgCode, text, Message.ERROR, part, propertyName));
            return false;
        }
        return true;
    }

    /**
     * Tests if the given qualified name identifies a policy component type. If not, it adds an
     * error message to the given message list.
     * <p>
     * Returns the checked and found {@link IpsObject} or <code>null</code> if it is not valid.
     * <p>
     * Use this method instead of
     * {@link #checkIpsObjectReference(String, IpsObjectType, String, IIpsObjectPartContainer, String, String, MessageList)}
     * to get the found object for better performance.
     * 
     * @param objectName the qualified type name to check
     * @param type the type the object to check is of
     * @param propertyDisplayName the name used to display the value to the user
     * @param part the part the checked reference belongs to (used if a message has to be created)
     * @param propertyName the (technical) name of the property used if a message has to be created
     * @param msgCode the message code to use if a message has to be created
     * @param list the list of messages to add a new one
     */
    public static final IIpsObject checkAndGetIpsObjectReference(String objectName,
            IpsObjectType type,
            String propertyDisplayName,
            IIpsObjectPartContainer part,
            String propertyName,
            String msgCode,
            MessageList list,
            IIpsProject ipsProject) {

        if (!checkStringPropertyNotEmpty(objectName, propertyDisplayName, part, propertyName, msgCode, list)) {
            return null;
        }
        IIpsSrcFile srcFile = ipsProject.findIpsSrcFile(type, objectName);
        if (srcFile == null) {
            String text = MessageFormat.format(Messages.ValidationUtils_msgObjectDoesNotExist,
                    StringUtils.capitalize(propertyDisplayName), objectName);
            list.add(new Message(msgCode, text, Message.ERROR, part, propertyName));
            return null;
        }
        return srcFile.getIpsObject();
    }

    /**
     * Checks if the given name identifies a data type. If not, it adds an error message to the
     * given message list. If the data type is found, it is validated and any messages generated by
     * the data type validation are added to the given message list.
     * <p>
     * Returns the data type if no error was detected, otherwise <code>null</code>.
     * 
     * @param datatypeName the data type name to check
     * @param voidAllowed <code>true</code> to allow void as data type, <code>false</code> to
     *            prohibit void
     * @param part the part the checked reference belongs to (used if a message has to be created)
     * @param propertyName the (technical) name of the property used if a message has to be created
     * @param msgcode the message code to use if a message has to be created
     * @param list the list of messages to add a new one
     * @param ipsProject the IPS project which IPS object path is used to search the data type
     */
    public static final Datatype checkDatatypeReference(String datatypeName,
            boolean voidAllowed,
            IIpsObjectPart part,
            String propertyName,
            String msgcode,
            MessageList list,
            IIpsProject ipsProject) {

        if (!checkStringPropertyNotEmpty(datatypeName, "Datatype", part, propertyName, msgcode, list)) { //$NON-NLS-1$
            return null;
        }

        Datatype datatype = ipsProject.findDatatype(datatypeName);
        if (datatype == null) {
            String text = MessageFormat.format(Messages.ValidationUtils_msgDatatypeDoesNotExist, datatypeName,
                    part.getName());
            list.add(new Message(msgcode, text, Message.ERROR, part, propertyName));
            return null;
        }
        if (datatype instanceof ValueDatatype) {
            list.add(((ValueDatatype)datatype).checkReadyToUse(), new ObjectProperty(part, propertyName), true);
        }
        if (datatype.isVoid() && !voidAllowed) {
            String text = Messages.ValidationUtils_msgVoidNotAllowed;
            list.add(new Message("", text, Message.ERROR, part, propertyName)); //$NON-NLS-1$
        }

        return datatype;
    }

    /**
     * Checks if the given name identifies a data type. If not, it adds an error message to the
     * given message list. If the data type is found, it is validated and any messages generated by
     * the data type validation are added to the given message list.
     * <p>
     * Returns the data type if no error was detected, otherwise <code>null</code>.
     * 
     * @param datatypeName the data type name to check
     * @param voidAllowed <code>true</code> to allow void as data type, <code>false</code> to
     *            prohibit void
     * @param part the part the checked reference belongs to (used if a message has to be created)
     * @param propertyName the (technical) name of the property used if a message has to be created
     * @param msgcode the message code to use if a message has to be created
     * @param list the list of messages to add a new one
     */
    public static final ValueDatatype checkValueDatatypeReference(String datatypeName,
            boolean voidAllowed,
            IIpsObjectPart part,
            String propertyName,
            String msgcode,
            MessageList list) {

        if (!checkStringPropertyNotEmpty(datatypeName, "Datatype", part, propertyName, msgcode, list)) { //$NON-NLS-1$
            return null;
        }
        ValueDatatype datatype = part.getIpsProject().findValueDatatype(datatypeName);
        if (datatype == null) {
            String text = MessageFormat.format(Messages.ValidationUtils_msgDatatypeDoesNotExist, datatypeName,
                    part.getName());
            list.add(new Message(msgcode, text, Message.ERROR, part, propertyName));
            return null;
        }
        list.add(datatype.checkReadyToUse(), new ObjectProperty(part, propertyName), true);
        if (datatype.isVoid() && !voidAllowed) {
            String text = Messages.ValidationUtils_msgVoidNotAllowed;
            list.add(new Message(msgcode, text, Message.ERROR, part, propertyName));
        }
        return datatype;
    }

    /**
     * Checks if a given value is an "instance" of the indicated value data type. Adds a warning to
     * the given list, if the data type either can't be found or is invalid. Adds an error message
     * if the value data type is OK, but the value is not an instance of it.
     * <p>
     * Returns <code>true</code> if the value is valid otherwise <code>false</code>.
     * 
     * @param valueDatatype the qualified value data type name which will be used to validate the
     *            given value
     * @param value the value which will be validated with the given data type
     * @param part the part the checked reference belongs to (used if a message has to be created)
     * @param propertyName the (technical) name of the property used if a message has to be created
     * @param list the list of messages to add a new one
     */
    public static final boolean checkValue(String valueDatatype,
            String value,
            IIpsObjectPart part,
            String propertyName,
            MessageList list) {

        return checkValue(part.getIpsProject().findValueDatatype(valueDatatype), valueDatatype, value, part,
                propertyName, list);
    }

    /**
     * Checks if a given value is an "instance" of the indicated value data type. Adds a warning to
     * the given list, if the data type either can't be found or is invalid. Adds an error message
     * if the value data type is OK, but the value is not an instance of it.
     * <p>
     * Returns <code>true</code> if the value is valid otherwise <code>false</code>.
     * 
     * @param datatype the data type which will be used to validate the given value
     * @param datatypeName the name of the datatype, used in exception messages if the datatype is
     *            {@code null}
     * @param value the value which will be validated with the given data type
     * @param part the part the checked reference belongs to (used if a message has to be created)
     * @param propertyName the (technical) name of the property used if a message has to be created
     * @param list the list of messages to add a new one
     */
    public static final boolean checkValue(ValueDatatype datatype,
            String datatypeName,
            String value,
            Object part,
            String propertyName,
            MessageList list) {

        if (datatype == null) {
            String text = MessageFormat.format(Messages.ValidationUtils_VALUE_VALUEDATATYPE_NOT_FOUND,
                    propertyName, value, datatypeName);
            Message msg = new Message(
                    IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_CANT_BE_FOUND,
                    text, Message.ERROR, part, propertyName);
            list.add(msg);
            return false;
        }

        if (datatype.checkReadyToUse().containsErrorMsg()) {
            String text = MessageFormat.format(Messages.ValidationUtils_VALUEDATATYPE_INVALID, propertyName,
                    datatype.getName());
            Message msg = new Message(
                    IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_IS_INVALID, text,
                    Message.WARNING, part, propertyName);
            list.add(msg);
            return false;
        }

        return checkParsable(datatype, value, part, propertyName, list);
    }

    /**
     * Tests if the given value can be parsed to the given datatype. It adds an error message to the
     * given message list if the value can't be parsed.
     * 
     * @param datatype the type the value has to be parsed to
     * @param value the value that has to be parsed
     * @param part the associated object of the value
     * @param propertyName the associated property name of the value
     * @param list the error message list
     * @return whether the given value is parsable
     */
    public static boolean checkParsable(ValueDatatype datatype,
            String value,
            Object part,
            String propertyName,
            MessageList list) {
        if (!datatype.isParsable(value)) {
            String text;

            if (Datatype.MONEY.equals(datatype)) {
                text = MessageFormat.format(Messages.ValidationUtils_NO_INSTANCE_OF_VALUEDATATYPE_MONEY,
                        propertyName, value, datatype.getName());
            } else {
                text = MessageFormat.format(Messages.ValidationUtils_NO_INSTANCE_OF_VALUEDATATYPE,
                        value, propertyName, datatype.getName());
            }
            Message msg = new Message(
                    IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE, text,
                    Message.ERROR, part, propertyName);
            list.add(msg);
            return false;
        }
        return true;
    }

    /**
     * Tests if the given property value is not empty. If it is empty, it adds an error message to
     * the given message list.
     * <p>
     * Returns <code>true</code> if the string is not empty, <code>false</code> if the string is
     * empty.
     * 
     * @param propertyValue the value to check
     * @param propertyDisplayName the name used to display the value to the user
     * @param object the part the checked reference belongs to (used if a message has to be created)
     * @param propertyName the (technical) name of the property used if a message has to be created
     * @param msgCode the message code to use if a message has to be created
     * @param list the list of messages to add a new one
     */
    public static final boolean checkStringPropertyNotEmpty(String propertyValue,
            String propertyDisplayName,
            Validatable object,
            String propertyName,
            String msgCode,
            MessageList list) {

        if (StringUtils.isEmpty(propertyValue)) {
            String text = MessageFormat.format(Messages.ValidationUtils_msgPropertyMissing,
                    StringUtils.capitalize(propertyDisplayName));
            list.add(new Message(msgCode, text, Message.ERROR, object, propertyName));
            return false;
        }
        return true;
    }

    /**
     * Validate the given field name using the source and compliance levels used by the given
     * IpsProject/JavaProject.
     * 
     * @param name the name of a field
     * @param ipsProject the project which source and compliance level should be used
     * 
     * @see JavaConventions#validateFieldName(String, String, String)
     */
    public static IStatus validateFieldName(String name, IIpsProject ipsProject) {
        Runtime.Version sourceVersion = ipsProject.getJavaProject().getSourceVersion();
        IStatus validateFieldName = JavaConventions.validateFieldName(StringUtils.capitalize(name),
                sourceVersion.toString(),
                sourceVersion.toString());
        if (validateFieldName.isOK()) {
            return JavaConventions.validateFieldName(StringUtils.uncapitalize(name),
                    sourceVersion.toString(),
                    sourceVersion.toString());
        } else {
            return validateFieldName;
        }
    }

    /**
     * Validate the given Java type name, either simple or qualified. For example,
     * <code>"java.lang.Object"</code>, or <code>"Object"</code> using the source and compliance
     * levels used by the given IpsProject/JavaProject.
     * <p>
     * Returns an empty {@link MessageList} if the given name is valid as a Java type name, one
     * containing a {@link Message} with {@link Severity#WARNING} if the given name is discouraged,
     * otherwise a {@link Message} with {@link Severity#ERROR} indicating what is wrong with the
     * name.
     * 
     * @param name the name of a type
     * @param ipsProject the project which source and compliance level should be used
     * 
     * @see JavaConventions#validateJavaTypeName(String, String, String)
     */
    public static MessageList validateJavaTypeName(String name, IIpsProject ipsProject) {
        Runtime.Version sourceVersion = ipsProject.getJavaProject().getSourceVersion();
        if (Abstractions.isEclipseRunning()) {
            return StatusMessageListMapping.toMessageList(
                    JavaConventions.validateJavaTypeName(name, sourceVersion.toString(), sourceVersion.toString()));
        } else {
            return PlainJavaConventions.validateTypeName(name);
        }
    }

    /**
     * Validate the given Java identifier using the source and compliance levels used by the given
     * IpsProject/JavaProject. The identifier must not have the same spelling as a Java keyword,
     * boolean literal (<code>"true"</code>, <code>"false"</code>), or null literal (
     * <code>"null"</code>). See section 3.8 of the <em>Java Language Specification, Second
     * Edition</em> (JLS2). A valid identifier can act as a simple type name, method name or field
     * name.
     * <p>
     * Returns a status object with code <code>IStatus.OK</code> if the given identifier is a valid
     * Java identifier, otherwise a status object indicating what is wrong with the identifier.
     * 
     * @param ipsProject the project which source and compliance level should be used
     * 
     * @see JavaConventions#validateIdentifier(String, String, String)
     */
    public static IStatus validateJavaIdentifier(String name, IIpsProject ipsProject) {
        Runtime.Version sourceVersion = ipsProject.getJavaProject().getSourceVersion();
        return JavaConventions.validateIdentifier(name, sourceVersion.toString(), sourceVersion.toString());
    }

}
