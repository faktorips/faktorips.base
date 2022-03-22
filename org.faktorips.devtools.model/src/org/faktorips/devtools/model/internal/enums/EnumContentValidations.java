/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.enums;

import java.text.MessageFormat;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.Messages;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.util.ArgumentCheck;

/**
 * A class that contains validations of the model class <code>IEnumContent</code> that are also used
 * in the creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @see org.faktorips.devtools.model.enums.IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public abstract class EnumContentValidations {

    private EnumContentValidations() {
        // Prohibit initialization.
    }

    /**
     * Validates whether the given qualified name of an <code>IEnumContent</code> is equal to the
     * enumeration content name that is specified in provided <code>IEnumType</code>. Therefore it
     * is assumed that the provided <code>IEnumType</code> is the corresponding one for the
     * <code>IEnumContent</code> in question.
     * 
     * @param validationMessageList The message list to save validation messages into.
     * @param enumContent The <code>IEnumContent</code> object can be specified if available. If so
     *            it is added to the message object in case the check fails.
     * @param enumType The corresponding <code>IEnumType</code>.
     * @param enumContentName The qualified name of the <code>IEnumContent</code> that is validated
     *            by this method.
     * 
     * @throws NullPointerException If <code>validationMessageList</code>, <code>enumType</code> or
     *             <code>enumContentPackageFragmentQualifiedName</code> is <code>null</code>.
     */
    public static void validateEnumContentName(MessageList validationMessageList,
            IEnumContent enumContent,
            IEnumType enumType,
            String enumContentName) {

        ArgumentCheck.notNull(new Object[] { validationMessageList, enumType, enumContentName });

        String enumTypeEnumContentName = enumType.getEnumContentName();
        if (!(enumContentName.equals(enumTypeEnumContentName))) {
            String text = MessageFormat.format(Messages.EnumContent_EnumContentNameNotCorrect,
                    enumType.getQualifiedName());
            Message message;
            if (enumContent == null) {
                message = new Message(IEnumContent.MSGCODE_ENUM_CONTENT_NAME_NOT_CORRECT, text, Message.ERROR);
            } else {
                message = new Message(IEnumContent.MSGCODE_ENUM_CONTENT_NAME_NOT_CORRECT, text, Message.ERROR,
                        enumContent);
            }
            validationMessageList.add(message);
        }
    }

    /**
     * Validates the <code>enumType</code> property of the given <code>IEnumContent</code>.
     * <p>
     * Appropriate validation messages will be added to the given message list if:
     * <ul>
     * <li>The qualified name of the <code>IEnumType</code> equals an empty string
     * (<code>IEnumType</code> is missing).
     * <li>The <code>IEnumType</code> is specified but does not exist.
     * <li>The <code>IEnumType</code> does exist but its values are defined in the model.
     * <li>The <code>IEnumType</code> does exist but is abstract.
     * </ul>
     * 
     * @param validationMessageList The message list to save validation messages into.
     * @param enumContent The <code>IEnumContent</code> that might be invalid or <code>null</code>
     *            if that information cannot be supported.
     * @param enumTypeQualifiedName The qualified name of the <code>IEnumType</code> the
     *            <code>IEnumContent</code> is based upon.
     * @param ipsProject The IPS object path of this IPS project will be searched.
     * 
     * @throws IpsException If an error occurs while searching for the <code>IEnumType</code>.
     * @throws NullPointerException If <code>validationMessageList</code>,
     *             <code>enumTypeQualifiedName</code> or <code>ipsProject</code> is
     *             <code>null</code>.
     */
    public static void validateEnumType(MessageList validationMessageList,
            IEnumContent enumContent,
            String enumTypeQualifiedName,
            IIpsProject ipsProject) {

        ArgumentCheck.notNull(new Object[] { validationMessageList, enumTypeQualifiedName, ipsProject });

        String text;
        ObjectProperty[] objectProperties = (enumContent != null) ? new ObjectProperty[] { new ObjectProperty(
                enumContent, IEnumContent.PROPERTY_ENUM_TYPE) } : new ObjectProperty[0];

        // EnumType missing?
        if (enumTypeQualifiedName.length() == 0) {
            text = Messages.EnumContent_EnumTypeMissing;
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_MISSING, text,
                    Message.ERROR, objectProperties));
            return;
        }

        // EnumType exists?
        IIpsSrcFile enumSrcFile = ipsProject.findIpsSrcFile(IpsObjectType.ENUM_TYPE, enumTypeQualifiedName);
        if (enumSrcFile == null) {
            text = MessageFormat.format(Messages.EnumContent_EnumTypeDoesNotExist, enumTypeQualifiedName);
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_DOES_NOT_EXIST, text,
                    Message.ERROR, objectProperties));
            return;
        }

        // Values are part of model?
        IEnumType enumTypeRef = (IEnumType)enumSrcFile.getIpsObject();
        if (enumTypeRef.isInextensibleEnum()) {
            text = MessageFormat.format(Messages.EnumContent_ValuesArePartOfType, enumTypeQualifiedName);
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_VALUES_ARE_PART_OF_TYPE, text,
                    Message.ERROR, objectProperties));
        }

        // EnumType abstract?
        if (enumTypeRef.isAbstract()) {
            text = MessageFormat.format(Messages.EnumContent_EnumTypeIsAbstract, enumTypeQualifiedName);
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_IS_ABSTRACT, text,
                    Message.ERROR, objectProperties));
        }
    }
}
