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

package org.faktorips.devtools.core.model.enums;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

/**
 * A class that contains validations of the model class <tt>IEnumContent</tt> that are also used in
 * the creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public abstract class EnumContentValidations {

    /**
     * Validates whether the given qualified name of an <tt>IEnumContent</tt> is equal to the
     * enumeration content name that is specified in provided <tt>IEnumType</tt>. Therefore it is
     * assumed that the provided <tt>IEnumType</tt> is the corresponding one for the
     * <tt>IEnumContent</tt> in question.
     * 
     * @param validationMessageList The message list to save validation messages into.
     * @param enumContent The <tt>IEnumContent</tt> object can be specified if available. If so it
     *            is added to the message object in case the check fails.
     * @param enumType The corresponding <tt>IEnumType</tt>.
     * @param enumContentName The qualified name of the <tt>IEnumContent</tt> that is validated by
     *            this method.
     * 
     * @throws NullPointerException If <tt>validationMessageList</tt>, <tt>enumType</tt> or
     *             <tt>enumContentPackageFragmentQualifiedName</tt> is <tt>null</tt>.
     */
    public static void validateEnumContentName(MessageList validationMessageList,
            IEnumContent enumContent,
            IEnumType enumType,
            String enumContentName) {

        ArgumentCheck.notNull(new Object[] { validationMessageList, enumType, enumContentName });

        String enumTypeEnumContentName = enumType.getEnumContentName();
        if (!(enumContentName.equals(enumTypeEnumContentName))) {
            String text = NLS.bind(Messages.EnumContent_EnumContentNameNotCorrect, enumType.getQualifiedName());
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
     * Validates the <tt>enumType</tt> property of the given <tt>IEnumContent</tt>.
     * <p>
     * Appropriate validation messages will be added to the given message list if:
     * <ul>
     * <li>The qualified name of the <tt>IEnumType</tt> equals an empty string (<tt>IEnumType</tt>
     * is missing).
     * <li>The <tt>IEnumType</tt> is specified but does not exist.
     * <li>The <tt>IEnumType</tt> does exist but its values are defined in the model.
     * <li>The <tt>IEnumType</tt> does exist but is abstract.
     * </ul>
     * 
     * @param validationMessageList The message list to save validation messages into.
     * @param enumContent The <tt>IEnumContent</tt> that might be invalid or <tt>null</tt> if that
     *            information cannot be supported.
     * @param enumTypeQualifiedName The qualified name of the <tt>IEnumType</tt> the
     *            <tt>IEnumContent</tt> is based upon.
     * @param ipsProject The IPS object path of this IPS project will be searched.
     * 
     * @throws CoreException If an error occurs while searching for the <tt>IEnumType</tt>.
     * @throws NullPointerException If <tt>validationMessageList</tt>,
     *             <tt>enumTypeQualifiedName</tt> or <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public static void validateEnumType(MessageList validationMessageList,
            IEnumContent enumContent,
            String enumTypeQualifiedName,
            IIpsProject ipsProject) throws CoreException {

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
            text = NLS.bind(Messages.EnumContent_EnumTypeDoesNotExist, enumTypeQualifiedName);
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_DOES_NOT_EXIST, text,
                    Message.ERROR, objectProperties));
            return;
        }

        // Values are part of model?
        IEnumType enumTypeRef = (IEnumType)enumSrcFile.getIpsObject();
        if (enumTypeRef.isContainingValues()) {
            text = NLS.bind(Messages.EnumContent_ValuesArePartOfType, enumTypeQualifiedName);
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_VALUES_ARE_PART_OF_TYPE, text,
                    Message.ERROR, objectProperties));
        }

        // EnumType abstract?
        if (enumTypeRef.isAbstract()) {
            text = NLS.bind(Messages.EnumContent_EnumTypeIsAbstract, enumTypeQualifiedName);
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_IS_ABSTRACT, text,
                    Message.ERROR, objectProperties));
        }
    }

    private EnumContentValidations() {
        // Prohibit initialization.
    }

}
