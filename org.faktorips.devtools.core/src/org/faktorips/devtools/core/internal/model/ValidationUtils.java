/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

/**
 * A collection of helper methods for validating model objects.
 * 
 * @author Jan Ortmann
 */
public class ValidationUtils {

    /**
     * Tests if the given qualified name identifies a policy component type. If not, it adds an
     * error message to the given message list.
     * <p>
     * Returns <tt>true</tt> if the reference is valid, otherwise <tt>false</tt>.
     * 
     * @param objectName the qualified type name to check.
     * @param type The type the object to check is of.
     * @param propertyDisplayName The name used to display the value to the user.
     * @param part The part the checked reference belongs to (used if a message has to be created).
     * @param propertyName The (technical) name of the property used if a message has to be created.
     * @param msgCode The message code to use if a message has to be created.
     * @param list The list of messages to add a new one.
     */
    public final static boolean checkIpsObjectReference(String objectName,
            IpsObjectType type,
            String propertyDisplayName,
            IIpsObjectPartContainer part,
            String propertyName,
            String msgCode,
            MessageList list) throws CoreException {

        if (!checkStringPropertyNotEmpty(objectName, propertyDisplayName, part, propertyName, msgCode, list)) {
            return false;
        }

        /*
         * Due to better performance findIpsSrcFile is used instead of findIpsObject, because only
         * the existence of the IPS object needs to be checked here, not the initialization (which
         * is implicitly done by calling findIpsObject).
         */
        if (part.getIpsProject().findIpsSrcFile(type, objectName) == null) {
            String text = NLS.bind(Messages.ValidationUtils_msgObjectDoesNotExist, StringUtils
                    .capitalize(propertyDisplayName), objectName);
            list.add(new Message(msgCode, text, Message.ERROR, part, propertyName));
            return false;
        }
        return true;
    }

    /**
     * Tests if the given qualified name identifies a policy component type. If not, it adds an
     * error message to the given message list.
     * <p>
     * Returns the checked and found {@link IpsObject} or <tt>null</tt> if it is not valid.
     * <p>
     * Use this method instead of
     * {@link #checkIpsObjectReference(String, IpsObjectType, String, IIpsObjectPartContainer, String, String, MessageList)}
     * to get the found object for better performance.
     * 
     * @param objectName the qualified type name to check.
     * @param type The type the object to check is of.
     * @param propertyDisplayName The name used to display the value to the user.
     * @param part The part the checked reference belongs to (used if a message has to be created).
     * @param propertyName The (technical) name of the property used if a message has to be created.
     * @param msgCode The message code to use if a message has to be created.
     * @param list The list of messages to add a new one.
     */
    public final static IIpsObject checkAndGetIpsObjectReference(String objectName,
            IpsObjectType type,
            String propertyDisplayName,
            IIpsObjectPartContainer part,
            String propertyName,
            String msgCode,
            MessageList list,
            IIpsProject ipsProject) throws CoreException {

        if (!checkStringPropertyNotEmpty(objectName, propertyDisplayName, part, propertyName, msgCode, list)) {
            return null;
        }
        IIpsSrcFile srcFile = ipsProject.findIpsSrcFile(type, objectName);
        if (srcFile == null) {
            String text = NLS.bind(Messages.ValidationUtils_msgObjectDoesNotExist, StringUtils
                    .capitalize(propertyDisplayName), objectName);
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
     * Returns the data type if no error was detected, otherwise <tt>null</tt>.
     * 
     * @param datatypeName The data type name to check.
     * @param voidAllowed <code>true</code> to allow void as data type, <code>false</code> to
     *            prohibit void.
     * @param part The part the checked reference belongs to (used if a message has to be created).
     * @param propertyName The (technical) name of the property used if a message has to be created.
     * @param msgcode The message code to use if a message has to be created.
     * @param list The list of messages to add a new one.
     * @param ipsProject The ips project which ips object path is used to search the data type.
     */
    public final static Datatype checkDatatypeReference(String datatypeName,
            boolean voidAllowed,
            IIpsObjectPart part,
            String propertyName,
            String msgcode,
            MessageList list,
            IIpsProject ipsProject) throws CoreException {

        if (!checkStringPropertyNotEmpty(datatypeName, "Datatype", part, propertyName, msgcode, list)) { //$NON-NLS-1$
            return null;
        }

        Datatype datatype = ipsProject.findDatatype(datatypeName);
        if (datatype == null) {
            String text = NLS.bind(Messages.ValidationUtils_msgDatatypeDoesNotExist, datatypeName);
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
     * Returns the data type if no error was detected, otherwise <tt>null</tt>.
     * 
     * @param datatypeName The data type name to check.
     * @param voidAllowed <code>true</code> to allow void as data type, <code>false</code> to
     *            prohibit void.
     * @param part The part the checked reference belongs to (used if a message has to be created).
     * @param propertyName The (technical) name of the property used if a message has to be created.
     * @param msgcode The message code to use if a message has to be created.
     * @param list The list of messages to add a new one.
     */
    public final static ValueDatatype checkValueDatatypeReference(String datatypeName,
            boolean voidAllowed,
            IIpsObjectPart part,
            String propertyName,
            String msgcode,
            MessageList list) throws CoreException {

        if (!checkStringPropertyNotEmpty(datatypeName, "Datatype", part, propertyName, msgcode, list)) { //$NON-NLS-1$
            return null;
        }
        ValueDatatype datatype = part.getIpsProject().findValueDatatype(datatypeName);
        if (datatype == null) {
            String text = NLS.bind(Messages.ValidationUtils_msgDatatypeDoesNotExist, datatypeName);
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
     * @param valueDatatype The qualified value data type name which will be used to validate the
     *            given value.
     * @param value The value which will be validated with the given data type.
     * @param part The part the checked reference belongs to (used if a message has to be created).
     * @param propertyName The (technical) name of the property used if a message has to be created.
     * @param list The list of messages to add a new one.
     */
    public final static boolean checkValue(String valueDatatype,
            String value,
            IIpsObjectPart part,
            String propertyName,
            MessageList list) throws CoreException {

        return checkValue(part.getIpsProject().findValueDatatype(valueDatatype), value, part, propertyName, list);
    }

    /**
     * Checks if a given value is an "instance" of the indicated value data type. Adds a warning to
     * the given list, if the data type either can't be found or is invalid. Adds an error message
     * if the value data type is OK, but the value is not an instance of it.
     * <p>
     * Returns <code>true</code> if the value is valid otherwise <code>false</code>.
     * 
     * @param datatype The data type which will be used to validate the given value.
     * @param value The value which will be validated with the given data type.
     * @param part The part the checked reference belongs to (used if a message has to be created).
     * @param propertyName The (technical) name of the property used if a message has to be created.
     * @param list The list of messages to add a new one.
     */
    public final static boolean checkValue(ValueDatatype datatype,
            String value,
            IIpsObjectPart part,
            String propertyName,
            MessageList list) throws CoreException {

        if (datatype == null) {
            String text = Messages.ValidationUtils_VALUE_VALUEDATATYPE_NOT_FOUND;
            Message msg = new Message(
                    IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_CANT_BE_FOUND,
                    text, Message.WARNING, part, propertyName);
            list.add(msg);
            return false;
        }
        try {
            if (datatype.checkReadyToUse().containsErrorMsg()) {
                String text = NLS.bind(Messages.ValidationUtils_VALUEDATATYPE_INVALID, datatype.getName());
                Message msg = new Message(
                        IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_IS_INVALID,
                        text, Message.WARNING, part, propertyName);
                list.add(msg);
                return false;
            }
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(e));
        }

        if (!datatype.isParsable(value)) {
            String text = NLS.bind(Messages.ValidationUtils_NO_INSTANCE_OF_VALUEDATATYPE, value, datatype);
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
     * Returns <tt>true</tt> if the string is not empty, <tt>false</tt> if the string is empty.
     * 
     * @param propertyValue The value to check.
     * @param propertyDisplayName The name used to display the value to the user.
     * @param object The part the checked reference belongs to (used if a message has to be
     *            created).
     * @param propertyName The (technical) name of the property used if a message has to be created.
     * @param msgCode The message code to use if a message has to be created.
     * @param list The list of messages to add a new one.
     */
    public final static boolean checkStringPropertyNotEmpty(String propertyValue,
            String propertyDisplayName,
            Object object,
            String propertyName,
            String msgCode,
            MessageList list) {

        if (StringUtils.isEmpty(propertyValue)) {
            String text = NLS.bind(Messages.ValidationUtils_msgPropertyMissing, StringUtils
                    .capitalize(propertyDisplayName));
            list.add(new Message(msgCode, text, Message.ERROR, object, propertyName));
            return false;
        }
        return true;
    }

    /**
     * Validate the given field name using the source and compliance levels used by the given
     * IpsProject/JavaProject.
     * 
     * @param name The name of a field.
     * @param ipsProject The project which source and compliance level should be used.
     * 
     * @see JavaConventions#validateFieldName(String, String, String)
     */
    public static IStatus validateFieldName(String name, IIpsProject ipsProject) {
        String complianceLevel = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
        String sourceLevel = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_SOURCE, true);
        return JavaConventions.validateFieldName(name, sourceLevel, complianceLevel);
    }

    /**
     * Validate the given Java type name, either simple or qualified. For example,
     * <code>"java.lang.Object"</code>, or <code>"Object"</code> using the source and compliance
     * levels used by the given IpsProject/JavaProject.
     * <p>
     * Returns a status object with code <code>IStatus.OK</code> if the given name is valid as a
     * Java type name, a status with code <code>IStatus.WARNING</code> indicating why the given name
     * is discouraged, otherwise a status object indicating what is wrong with the name.
     * 
     * @param name The name of a type.
     * @param ipsProject The project which source and compliance level should be used.
     * 
     * @see JavaConventions#validateJavaTypeName(String, String, String)
     */
    public static IStatus validateJavaTypeName(String name, IIpsProject ipsProject) {
        String complianceLevel = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
        String sourceLevel = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_SOURCE, true);
        return JavaConventions.validateJavaTypeName(name, sourceLevel, complianceLevel);
    }

    /**
     * Validate the given Java identifier using the source and compliance levels used by the given
     * IpsProject/JavaProject. The identifier must not have the same spelling as a Java keyword,
     * boolean literal (<code>"true"</code>, <code>"false"</code>), or null literal (
     * <code>"null"</code>). See section 3.8 of the
     * <em>Java Language Specification, Second Edition</em> (JLS2). A valid identifier can act as a
     * simple type name, method name or field name.
     * <p>
     * Returns a status object with code <code>IStatus.OK</code> if the given identifier is a valid
     * Java identifier, otherwise a status object indicating what is wrong with the identifier.
     * 
     * @param ipsProject the project which source and compliance level should be used.
     * 
     * @see JavaConventions#validateIdentifier(String, String, String)
     */
    public static IStatus validateJavaIdentifier(String name, IIpsProject ipsProject) {
        String complianceLevel = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
        String sourceLevel = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_SOURCE, true);
        return JavaConventions.validateIdentifier(name, sourceLevel, complianceLevel);
    }

    private ValidationUtils() {
        // Utility class not to be instantiated.
    }

}
