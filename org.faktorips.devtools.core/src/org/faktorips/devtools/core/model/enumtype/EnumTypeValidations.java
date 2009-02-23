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

package org.faktorips.devtools.core.model.enumtype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.enumtype.Messages;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.ObjectProperty;

/**
 * A class that contains validations of the model class <code>IEnumType</code> which are also used
 * in the creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @see org.faktorips.devtools.core.model.enumtype.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public abstract class EnumTypeValidations {

    /**
     * <p>
     * Validates whether the given super enum type exists in the ips object path of the given ips
     * project.
     * </p>
     * <p>
     * If a super enum type with the given qualified name exists in the given ips project
     * <code>null</code> will be returned.
     * </p>
     * <p>
     * If no super enum type with the given qualified name exists in the given ips project an
     * appropriate validation message will be returned.
     * </p>
     * 
     * @param enumType The enum type that might be invalid or <code>null</code> if that information
     *            cannot be supported.
     * @param superEnumTypeQualifiedName The qualified name of the super enum type.
     * @param ipsProject The ips object path of this ips project will be searched.
     * 
     * @return An appropriate validation message or <code>null</code> if the validation was
     *         successful.
     * 
     * @throws CoreException If an error occurs while searching for the super enum type.
     * @throws IllegalArgumentException If superEnumTypeQualifiedName is an empty string.
     * @throws NullPointerException If superEnumTypeQualifiedName or ipsProject is <code>null</code>.
     */
    public static Message validateSuperEnumType(IEnumType enumType,
            String superEnumTypeQualifiedName,
            IIpsProject ipsProject) throws CoreException {

        ArgumentCheck.notNull(new Object[] { superEnumTypeQualifiedName, ipsProject });
        ArgumentCheck.isTrue(!(superEnumTypeQualifiedName.equals("")));

        if (ipsProject.findEnumType(superEnumTypeQualifiedName) == null) {
            String text = NLS.bind(Messages.EnumType_SupertypeDoesNotExist, superEnumTypeQualifiedName);
            return new Message(
                    IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST,
                    text,
                    Message.ERROR,
                    enumType != null ? new ObjectProperty[] { new ObjectProperty(enumType, IEnumType.PROPERTY_SUPERTYPE) }
                            : new ObjectProperty[0]);
        }

        return null;
    }

    /** Prohibits initialization. */
    private EnumTypeValidations() {

    }

}
