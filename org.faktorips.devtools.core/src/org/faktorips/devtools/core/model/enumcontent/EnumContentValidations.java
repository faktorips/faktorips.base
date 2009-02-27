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

package org.faktorips.devtools.core.model.enumcontent;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.enumcontent.Messages;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

/**
 * A class that contains validations of the model class <code>IEnumContent</code> which are also
 * used in the creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @see org.faktorips.devtools.core.model.enumcontent.IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public abstract class EnumContentValidations {

    /**
     * Validates the enum type property of the given enum content.
     * <p>
     * An appropriate validation message list will be returned if:
     * <ul>
     * <li>The qualified name of the enum type equals an empty string (enum type is missing)</li>
     * <li>The enum type is specified but does not exist</li>
     * <li>The enum type does exist but its values are defined in the model</li>
     * <li>The enum type does exist but is abstract</li>
     * </ul
     * <p>
     * If the enum type property is valid an empty validation message list will be returned.
     * </p>
     * 
     * @param enumContent The enum content that might be invalid or <code>null</code> if that
     *            information cannot be supported.
     * @param enumTypeQualifiedName The qualified name of the enum type this enum content is based
     *            upon.
     * @param ipsProject The ips object path of this ips project will be searched.
     * 
     * @return A proper validation message list or an empty validation message list if the
     *         validation was successful.
     * 
     * @throws CoreException If an error occurs while searching for the enum type.
     * @throws NullPointerException If enumTypeQualifiedName or ipsProject is <code>null</code>.
     */
    public static MessageList validateEnumType(IEnumContent enumContent,
            String enumTypeQualifiedName,
            IIpsProject ipsProject) throws CoreException {

        ArgumentCheck.notNull(new Object[] { enumTypeQualifiedName, ipsProject });

        String text;
        MessageList validationMessageList = new MessageList();
        ObjectProperty[] objectProperties = (enumContent != null) ? new ObjectProperty[] { new ObjectProperty(
                enumContent, IEnumContent.PROPERTY_ENUM_TYPE) } : new ObjectProperty[0];

        // Enum type missing?
        if (enumTypeQualifiedName.equals("")) {
            text = Messages.EnumContent_EnumTypeMissing;
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_MISSING, text,
                    Message.ERROR, objectProperties));
            return validationMessageList;
        }

        // Enum type exists?
        IEnumType enumTypeRef = ipsProject.findEnumType(enumTypeQualifiedName);
        if (enumTypeRef == null) {
            text = NLS.bind(Messages.EnumContent_EnumTypeDoesNotExist, enumTypeQualifiedName);
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_DOES_NOT_EXIST, text,
                    Message.ERROR, objectProperties));
            return validationMessageList;
        }

        // Values are part of model?
        if (enumTypeRef.getValuesArePartOfModel()) {
            text = NLS.bind(Messages.EnumContent_ValuesArePartOfModel, enumTypeQualifiedName);
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_VALUES_ARE_PART_OF_MODEL, text,
                    Message.ERROR, objectProperties));
        }

        // Enum type abstract?
        if (enumTypeRef.isAbstract()) {
            text = NLS.bind(Messages.EnumContent_EnumTypeIsAbstract, enumTypeQualifiedName);
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_IS_ABSTRACT, text,
                    Message.ERROR, objectProperties));
        }

        return validationMessageList;
    }

    /** Prohibits initialization. */
    private EnumContentValidations() {

    }

}
